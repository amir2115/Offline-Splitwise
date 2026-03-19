package com.encer.offlinesplitwise.data.repository

import com.encer.offlinesplitwise.data.local.dao.SettlementDao
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.data.repository.mapper.toDomain
import com.encer.offlinesplitwise.data.repository.mapper.toEntity
import com.encer.offlinesplitwise.data.sync.SyncCoordinator
import com.encer.offlinesplitwise.domain.model.Settlement
import com.encer.offlinesplitwise.domain.repository.SettlementRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DefaultSettlementRepository @Inject constructor(
    private val settlementDao: SettlementDao,
    private val syncCoordinator: SyncCoordinator
) : SettlementRepository {
    override fun observeSettlements(groupId: String): Flow<List<Settlement>> =
        settlementDao.observeSettlements(groupId).map { list -> list.map { it.toDomain() } }

    override suspend fun getSettlement(settlementId: String): Settlement? =
        settlementDao.getById(settlementId)?.takeIf { it.deletedAt == null }?.toDomain()

    override suspend fun upsertSettlement(settlement: Settlement): String {
        val now = System.currentTimeMillis()
        val id = settlement.id.ifBlank { UUID.randomUUID().toString() }
        val existing = settlementDao.getById(id)
        settlementDao.upsert(
            settlement.toEntity(
                id = id,
                createdAt = existing?.createdAt ?: settlement.createdAt.takeIf { it > 0 } ?: now,
                updatedAt = now,
                syncState = SyncState.PENDING_UPSERT
            )
        )
        syncCoordinator.requestSync()
        return id
    }

    override suspend fun deleteSettlement(settlementId: String) {
        val current = settlementDao.getById(settlementId) ?: return
        settlementDao.upsert(current.copy(deletedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), syncState = SyncState.PENDING_DELETE))
        syncCoordinator.requestSync()
    }
}
