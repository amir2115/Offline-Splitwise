package com.encer.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.encer.splitwise.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    @Query("SELECT * FROM settlements WHERE groupId = :groupId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeSettlements(groupId: String): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements ORDER BY createdAt DESC")
    suspend fun getAll(): List<SettlementEntity>

    @Query("SELECT * FROM settlements WHERE syncState = :syncState")
    suspend fun getBySyncState(syncState: SyncState): List<SettlementEntity>

    @Query("SELECT * FROM settlements WHERE id = :settlementId LIMIT 1")
    suspend fun getById(settlementId: String): SettlementEntity?

    @Upsert
    suspend fun upsert(settlement: SettlementEntity)

    @Upsert
    suspend fun upsertAll(settlements: List<SettlementEntity>)

    @Query("DELETE FROM settlements WHERE id = :settlementId")
    suspend fun hardDeleteById(settlementId: String)

    @Query("DELETE FROM settlements WHERE id IN (:settlementIds)")
    suspend fun hardDeleteByIds(settlementIds: List<String>)
}
