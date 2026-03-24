package com.encer.offlinesplitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.encer.offlinesplitwise.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    suspend fun getAll(): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE syncState = :syncState")
    suspend fun getBySyncState(syncState: SyncState): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getById(groupId: String): GroupEntity?

    @Upsert
    suspend fun upsert(group: GroupEntity)

    @Upsert
    suspend fun upsertAll(groups: List<GroupEntity>)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun hardDeleteById(groupId: String)

    @Query("DELETE FROM groups WHERE id IN (:groupIds)")
    suspend fun hardDeleteByIds(groupIds: List<String>)

    @Query("SELECT COUNT(*) FROM groups")
    suspend fun countAll(): Int
}
