package com.encer.offlinesplitwise.features.settlement_editor

import com.encer.offlinesplitwise.core.common.MessageKey
import com.encer.offlinesplitwise.domain.model.Member

data class SettlementEditorUiState(
    val isEdit: Boolean = false,
    val members: List<Member> = emptyList(),
    val fromMemberId: String? = null,
    val toMemberId: String? = null,
    val suggestedAmount: Int? = null,
    val amountInput: String = "",
    val note: String = "",
    val message: MessageKey? = null,
    val saved: Boolean = false,
    val canCreateTransaction: Boolean = true,
)
