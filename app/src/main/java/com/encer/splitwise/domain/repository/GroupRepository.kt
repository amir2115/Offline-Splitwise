package com.encer.splitwise.domain.repository

import com.encer.splitwise.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeGroups(): Flow<List<Group>>
    suspend fun createGroup(name: String): String
    suspend fun updateGroup(group: Group)
    suspend fun deleteGroup(groupId: String)
    suspend fun leaveGroup(groupId: String)
    suspend fun getGroup(groupId: String): Group?
}
