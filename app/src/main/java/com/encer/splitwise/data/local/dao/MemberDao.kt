package com.encer.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.encer.splitwise.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE groupId = :groupId AND isArchived = 0 AND deletedAt IS NULL ORDER BY createdAt ASC")
    fun observeMembers(groupId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE groupId = :groupId AND isArchived = 0 AND deletedAt IS NULL ORDER BY createdAt ASC")
    suspend fun getActiveMembers(groupId: String): List<MemberEntity>

    @Query("SELECT * FROM members ORDER BY createdAt ASC")
    suspend fun getAll(): List<MemberEntity>

    @Query("SELECT * FROM members WHERE syncState = :syncState")
    suspend fun getBySyncState(syncState: SyncState): List<MemberEntity>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    suspend fun getById(memberId: String): MemberEntity?

    @Query("SELECT * FROM members WHERE groupId = :groupId AND userId = :userId AND deletedAt IS NULL LIMIT 1")
    suspend fun getByGroupAndUserId(groupId: String, userId: String): MemberEntity?

    @Query("SELECT DISTINCT groupId FROM members WHERE userId = :userId AND isArchived = 0 AND deletedAt IS NULL")
    fun observeActiveGroupIdsForUser(userId: String): Flow<List<String>>

    @Upsert
    suspend fun upsert(member: MemberEntity)

    @Upsert
    suspend fun upsertAll(members: List<MemberEntity>)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun hardDeleteById(memberId: String)

    @Query("DELETE FROM members WHERE id IN (:memberIds)")
    suspend fun hardDeleteByIds(memberIds: List<String>)

    @Query(
        """
        UPDATE members
        SET deletedAt = :deletedAt,
            updatedAt = :updatedAt,
            syncState = :syncState
        WHERE id IN (:memberIds)
        """
    )
    suspend fun markDeletedByIds(
        memberIds: List<String>,
        deletedAt: Long,
        updatedAt: Long,
        syncState: SyncState = SyncState.SYNCED,
    )

    @Query("SELECT id FROM members WHERE groupId = :groupId AND userId = :userId AND id != :keepId")
    suspend fun getDuplicateIdsByGroupAndUserId(groupId: String, userId: String, keepId: String): List<String>

    @Query("DELETE FROM members WHERE groupId = :groupId AND userId = :userId AND id != :keepId")
    suspend fun hardDeleteByGroupAndUserIdExceptId(groupId: String, userId: String, keepId: String)
}
