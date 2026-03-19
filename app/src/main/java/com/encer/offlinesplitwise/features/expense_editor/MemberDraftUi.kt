package com.encer.offlinesplitwise.features.expense_editor

data class MemberDraftUi(
    val memberId: String,
    val name: String,
    val includedInSplit: Boolean = true,
    val payerAmountInput: String = "",
    val exactShareInput: String = "",
)
