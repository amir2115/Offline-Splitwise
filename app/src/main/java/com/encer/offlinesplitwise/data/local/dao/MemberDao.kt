package com.encer.offlinesplitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.encer.offlinesplitwise.data.local.entity.*
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(members: List<MemberEntity>)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun hardDeleteById(memberId: String)

    @Query("DELETE FROM members WHERE id IN (:memberIds)")
    suspend fun hardDeleteByIds(memberIds: List<String>)
}
