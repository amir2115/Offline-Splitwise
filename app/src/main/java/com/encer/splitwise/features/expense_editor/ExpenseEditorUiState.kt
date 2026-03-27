package com.encer.splitwise.features.expense_editor

import com.encer.splitwise.core.common.MessageKey
import com.encer.splitwise.domain.model.SplitType

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
