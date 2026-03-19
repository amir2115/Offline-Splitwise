package com.encer.offlinesplitwise.domain.repository

import com.encer.offlinesplitwise.domain.model.Settlement
import kotlinx.coroutines.flow.Flow

interface SettlementRepository {
    fun observeSettlements(groupId: String): Flow<List<Settlement>>
    suspend fun getSettlement(settlementId: String): Settlement?
    suspend fun upsertSettlement(settlement: Settlement): String
    suspend fun deleteSettlement(settlementId: String)
}
