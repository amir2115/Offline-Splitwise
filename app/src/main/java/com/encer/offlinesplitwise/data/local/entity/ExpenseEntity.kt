package com.encer.offlinesplitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.encer.offlinesplitwise.domain.model.SplitType

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("groupId")]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val title: String,
    val note: String?,
    val totalAmount: Int,
    val splitType: SplitType,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val userId: String? = null,
    val syncState: SyncState = SyncState.SYNCED,
)
