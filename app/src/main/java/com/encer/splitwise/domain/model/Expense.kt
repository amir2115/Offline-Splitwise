package com.encer.splitwise.domain.model

data class Expense(
    val id: String,
    val groupId: String,
    val title: String,
    val note: String?,
    val totalAmount: Int,
    val splitType: SplitType,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val userId: String? = null,
    val payers: List<ExpenseShare>,
    val shares: List<ExpenseShare>,
)
