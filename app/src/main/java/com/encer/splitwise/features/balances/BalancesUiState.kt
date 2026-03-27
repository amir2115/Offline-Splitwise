package com.encer.splitwise.features.balances

import com.encer.splitwise.domain.model.Group
import com.encer.splitwise.domain.model.MemberBalance
import com.encer.splitwise.domain.model.SimplifiedTransfer

data class BalancesUiState(
    val group: Group? = null,
    val balances: List<MemberBalance> = emptyList(),
    val simplifiedTransfers: List<SimplifiedTransfer> = emptyList(),
)
