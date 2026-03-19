package com.encer.offlinesplitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
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
data class MemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    val userId: String? = null,
    val syncState: SyncState = SyncState.SYNCED,
)
