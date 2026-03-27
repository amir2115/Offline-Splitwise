package com.encer.splitwise.data.repository.mapper

import com.encer.splitwise.data.local.entity.ExpenseEntity
import com.encer.splitwise.data.local.entity.ExpensePayerEntity
import com.encer.splitwise.data.local.entity.ExpenseShareEntity
import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.ExpenseShare

fun ExpenseEntity.toDomain(
    payers: List<ExpenseShare>,
    shares: List<ExpenseShare>
) = Expense(
    id = id,
    groupId = groupId,
    title = title,
    note = note,
    totalAmount = totalAmount,
    splitType = splitType,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    userId = userId,
    payers = payers,
    shares = shares
)

fun ExpensePayerEntity.toDomainPayer() = ExpenseShare(memberId = memberId, amount = amountPaid)
fun ExpenseShareEntity.toDomainShare() = ExpenseShare(memberId = memberId, amount = amountOwed)
