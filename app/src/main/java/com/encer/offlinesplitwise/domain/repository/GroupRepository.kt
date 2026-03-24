package com.encer.offlinesplitwise.domain.repository

import com.encer.offlinesplitwise.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeGroups(): Flow<List<Group>>
    suspend fun createGroup(name: String): String
    suspend fun updateGroup(group: Group)
    suspend fun deleteGroup(groupId: String)
    suspend fun leaveGroup(groupId: String)
    suspend fun getGroup(groupId: String): Group?
}
