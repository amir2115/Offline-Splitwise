package com.encer.offlinesplitwise.data.repository

import com.encer.offlinesplitwise.data.local.dao.GroupDao
import com.encer.offlinesplitwise.data.local.dao.MemberDao
import com.encer.offlinesplitwise.data.local.entity.MemberEntity
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.data.preferences.SessionRepository
import com.encer.offlinesplitwise.data.repository.mapper.normalizeMemberIdentity
import com.encer.offlinesplitwise.data.repository.mapper.toDomain
import com.encer.offlinesplitwise.data.repository.mapper.toEntity
import com.encer.offlinesplitwise.data.sync.SyncCoordinator
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.domain.repository.MemberRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DefaultMemberRepository @Inject constructor(
    private val memberDao: MemberDao,
    private val groupDao: GroupDao,
    private val sessionRepository: SessionRepository,
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
        val normalizedCandidates = listOf(session.name, session.username).map(::normalizeMemberIdentity).filter { it.isNotBlank() }.toSet()
        val matchedMember = memberDao.getActiveMembers(groupId).firstOrNull { member -> normalizeMemberIdentity(member.name) in normalizedCandidates }
        if (matchedMember != null) {
            memberDao.upsert(matchedMember.copy(userId = session.userId, updatedAt = now, syncState = SyncState.PENDING_UPSERT))
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
                name = session.name,
                createdAt = now,
                updatedAt = now,
                userId = session.userId,
                syncState = SyncState.PENDING_UPSERT
            )
        )
        syncCoordinator.requestSync()
    }

    override suspend fun addMember(groupId: String, name: String): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        memberDao.upsert(MemberEntity(id = id, groupId = groupId, name = name.trim(), createdAt = now, updatedAt = now, syncState = SyncState.PENDING_UPSERT))
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
