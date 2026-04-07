package com.encer.splitwise.data.repository

import com.encer.splitwise.data.local.dao.GroupDao
import com.encer.splitwise.data.local.dao.MemberDao
import com.encer.splitwise.data.local.entity.MemberEntity
import com.encer.splitwise.data.local.entity.SyncState
import com.encer.splitwise.data.preferences.SessionRepository
import com.encer.splitwise.data.remote.mapper.toEntity as remoteToEntity
import com.encer.splitwise.data.remote.network.ApiClient
import com.encer.splitwise.data.repository.mapper.normalizeMemberIdentity
import com.encer.splitwise.data.repository.mapper.toDomain
import com.encer.splitwise.data.repository.mapper.toEntity
import com.encer.splitwise.data.sync.SyncCoordinator
import com.encer.splitwise.domain.model.MembershipStatus
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.domain.repository.MemberRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DefaultMemberRepository @Inject constructor(
    private val memberDao: MemberDao,
    private val groupDao: GroupDao,
    private val sessionRepository: SessionRepository,
    private val apiClient: ApiClient,
    private val syncCoordinator: SyncCoordinator
) : MemberRepository {
    override fun observeMembers(groupId: String): Flow<List<Member>> = memberDao.observeMembers(groupId).map { list -> list.map { it.toDomain() } }

    override suspend fun ensureSelfMember(groupId: String) {
        val session = sessionRepository.currentSession() ?: return
        val group = groupDao.getById(groupId)?.takeIf { it.deletedAt == null } ?: return
        val now = System.currentTimeMillis()
        val existing = memberDao.getByGroupAndUserId(groupId, session.userId)
        if (existing != null) {
            if (group.userId == null) {
                groupDao.upsert(group.copy(userId = session.userId, updatedAt = now, syncState = SyncState.PENDING_UPSERT))
                syncCoordinator.requestSync()
            }
            return
        }
        val normalizedCandidates = listOf(session.username).map(::normalizeMemberIdentity).filter { it.isNotBlank() }.toSet()
        val matchedMember = memberDao.getActiveMembers(groupId).firstOrNull { member -> normalizeMemberIdentity(member.username) in normalizedCandidates }
        if (matchedMember != null) {
            memberDao.upsert(
                matchedMember.copy(
                    userId = session.userId,
                    username = session.username,
                    membershipStatus = MembershipStatus.ACTIVE,
                    updatedAt = now,
                    syncState = SyncState.PENDING_UPSERT
                )
            )
            if (group.userId == null) {
                groupDao.upsert(group.copy(userId = session.userId, updatedAt = now, syncState = SyncState.PENDING_UPSERT))
            }
            syncCoordinator.requestSync()
            return
        }
        if (group.userId != null && group.userId != session.userId) return
        if (group.userId == null) {
            groupDao.upsert(group.copy(userId = session.userId, updatedAt = now, syncState = SyncState.PENDING_UPSERT))
        }
        memberDao.upsert(
            MemberEntity(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                username = session.username,
                createdAt = now,
                updatedAt = now,
                userId = session.userId,
                membershipStatus = MembershipStatus.ACTIVE,
                syncState = SyncState.PENDING_UPSERT
            )
        )
        syncCoordinator.requestSync()
    }

    override suspend fun addMember(groupId: String, username: String): String {
        val normalizedUsername = normalizeMemberIdentity(username)
        if (sessionRepository.currentSession() != null) {
            val response = apiClient.createMember(groupId = groupId, username = normalizedUsername)
            memberDao.upsert(response.member.remoteToEntity())
            return response.member.id
        }
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        memberDao.upsert(
            MemberEntity(
                id = id,
                groupId = groupId,
                username = normalizedUsername,
                createdAt = now,
                updatedAt = now,
                membershipStatus = MembershipStatus.PENDING_INVITE,
                syncState = SyncState.PENDING_UPSERT
            )
        )
        syncCoordinator.requestSync()
        return id
    }

    override suspend fun updateMember(member: Member) {
        memberDao.upsert(member.toEntity(syncState = SyncState.PENDING_UPSERT, updatedAt = System.currentTimeMillis()))
        syncCoordinator.requestSync()
    }

    override suspend fun deleteMember(memberId: String) {
        val current = memberDao.getById(memberId) ?: return
        val now = System.currentTimeMillis()
        memberDao.upsert(current.copy(deletedAt = now, updatedAt = now, syncState = SyncState.PENDING_DELETE))
        syncCoordinator.requestSync()
    }

    override suspend fun getMember(memberId: String): Member? = memberDao.getById(memberId)?.takeIf { it.deletedAt == null }?.toDomain()
}
