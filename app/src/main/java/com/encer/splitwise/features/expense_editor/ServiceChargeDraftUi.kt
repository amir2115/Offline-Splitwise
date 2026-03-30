package com.encer.splitwise.features.expense_editor

data class ServiceChargeDraftUi(
    val id: String,
    val title: String = "",
    val amountInput: String = "",
    val selectedMemberIds: Set<String> = emptySet(),
)
