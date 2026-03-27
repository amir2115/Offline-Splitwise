package com.encer.splitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.encer.splitwise.domain.model.MembershipStatus

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
    val username: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    val userId: String? = null,
    val membershipStatus: MembershipStatus = MembershipStatus.ACTIVE,
    val syncState: SyncState = SyncState.SYNCED,
)
