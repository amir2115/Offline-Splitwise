package com.encer.offlinesplitwise.features.expense_editor

import com.encer.offlinesplitwise.core.common.MessageKey
import com.encer.offlinesplitwise.domain.model.SplitType

data class ExpenseEditorUiState(
    val isEdit: Boolean = false,
    val title: String = "",
    val note: String = "",
    val totalAmountInput: String = "",
    val splitType: SplitType = SplitType.EQUAL,
    val members: List<MemberDraftUi> = emptyList(),
    val message: MessageKey? = null,
    val savedExpenseId: String? = null,
    val loaded: Boolean = false,
    val canCreateTransaction: Boolean = true,
)
