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
    val payerTotal: Int = 0,
    val shareTotal: Int = 0,
    val remainingPayerAmount: Int = 0,
    val remainingShareAmount: Int = 0,
    val isPayerOverflow: Boolean = false,
    val isShareOverflow: Boolean = false,
    val taxEnabled: Boolean = false,
    val taxPercentInput: String = "",
    val serviceCharges: List<ServiceChargeDraftUi> = emptyList(),
    val baseAmountPreview: Int = 0,
    val taxAmountPreview: Int = 0,
    val serviceChargeTotalPreview: Int = 0,
    val finalShareTotal: Int = 0,
    val hasInvalidServiceCharges: Boolean = false,
    val hasInvalidTaxPercent: Boolean = false,
    val isAmountsReady: Boolean = false,
)
