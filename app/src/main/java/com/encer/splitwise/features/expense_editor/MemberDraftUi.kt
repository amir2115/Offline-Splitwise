package com.encer.splitwise.features.expense_editor

data class MemberDraftUi(
    val memberId: String,
    val username: String,
    val includedInSplit: Boolean = true,
    val payerAmountInput: String = "",
    val exactShareInput: String = "",
    val suggestedRemainingPayer: Int? = null,
    val suggestedRemainingShare: Int? = null,
    val equalRemainingShare: Int? = null,
    val baseSharePreview: Int = 0,
    val taxSharePreview: Int = 0,
    val serviceChargeSharePreview: Int = 0,
    val finalSharePreview: Int = 0,
)
