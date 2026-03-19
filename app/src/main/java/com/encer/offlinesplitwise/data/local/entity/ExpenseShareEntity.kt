package com.encer.offlinesplitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "expense_shares",
    primaryKeys = ["expenseId", "memberId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("memberId")]
)
data class ExpenseShareEntity(
    val expenseId: String,
    val memberId: String,
    val amountOwed: Int,
)
