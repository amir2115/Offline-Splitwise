package com.encer.splitwise.domain.repository

import com.encer.splitwise.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun observeMembers(groupId: String): Flow<List<Member>>
    suspend fun ensureSelfMember(groupId: String)
    suspend fun addMember(groupId: String, username: String): String
    suspend fun updateMember(member: Member)
    suspend fun deleteMember(memberId: String)
    suspend fun getMember(memberId: String): Member?
}
