package com.encer.offlinesplitwise.features.balances

import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.model.MemberBalance
import com.encer.offlinesplitwise.domain.model.SimplifiedTransfer

data class BalancesUiState(
    val group: Group? = null,
    val balances: List<MemberBalance> = emptyList(),
    val simplifiedTransfers: List<SimplifiedTransfer> = emptyList(),
)
