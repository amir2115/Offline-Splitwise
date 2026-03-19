package com.encer.offlinesplitwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromMemberId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["toMemberId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("groupId"), Index("fromMemberId"), Index("toMemberId")]
)
data class SettlementEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Int,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val userId: String? = null,
    val syncState: SyncState = SyncState.SYNCED,
)
