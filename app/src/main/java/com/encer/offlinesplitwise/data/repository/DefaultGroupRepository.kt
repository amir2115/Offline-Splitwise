package com.encer.offlinesplitwise.data.repository

import com.encer.offlinesplitwise.data.local.dao.GroupDao
import com.encer.offlinesplitwise.data.local.entity.GroupEntity
import com.encer.offlinesplitwise.data.local.dao.MemberDao
import com.encer.offlinesplitwise.data.local.entity.MemberEntity
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.data.preferences.SessionRepository
import com.encer.offlinesplitwise.data.repository.mapper.toDomain
import com.encer.offlinesplitwise.data.repository.mapper.toEntity
import com.encer.offlinesplitwise.data.sync.SyncCoordinator
import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.model.MembershipStatus
import com.encer.offlinesplitwise.domain.repository.GroupRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.UUID

class DefaultGroupRepository @Inject constructor(
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val sessionRepository: SessionRepository,
    private val syncCoordinator: SyncCoordinator
) : GroupRepository {
    override fun observeGroups(): Flow<List<Group>> = sessionRepository.observeSession().flatMapLatest { session ->
        val groupsFlow = groupDao.observeGroups()
        if (session == null) {
            groupsFlow.map { groups -> groups.map { it.toDomain() } }
        } else {
            combine(
                groupsFlow,
                memberDao.observeActiveGroupIdsForUser(session.userId)
            ) { groups, memberGroupIds ->
                val visibleGroupIds = memberGroupIds.toSet()
                groups
                    .filter { it.userId == session.userId || it.id in visibleGroupIds }
                    .map { it.toDomain() }
            }
        }
    }

    override suspend fun createGroup(name: String): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        groupDao.upsert(
            GroupEntity(
                id = id,
                name = name.trim(),
                createdAt = now,
                updatedAt = now,
                userId = sessionRepository.currentSession()?.userId,
                syncState = SyncState.PENDING_UPSERT
            )
        )
        sessionRepository.currentSession()?.let { session ->
            memberDao.upsert(
                MemberEntity(
                    id = UUID.randomUUID().toString(),
                    groupId = id,
                    username = session.username,
                    createdAt = now,
                    updatedAt = now,
                    userId = session.userId,
                    membershipStatus = MembershipStatus.ACTIVE,
                    syncState = SyncState.PENDING_UPSERT
                )
            )
        }
        syncCoordinator.requestSync()
        return id
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.upsert(group.toEntity(syncState = SyncState.PENDING_UPSERT, updatedAt = System.currentTimeMillis()))
        syncCoordinator.requestSync()
    }

    override suspend fun deleteGroup(groupId: String) {
        val current = groupDao.getById(groupId) ?: return
        val now = System.currentTimeMillis()
        groupDao.upsert(current.copy(deletedAt = now, updatedAt = now, syncState = SyncState.PENDING_DELETE))
        syncCoordinator.requestSync()
    }

    override suspend fun leaveGroup(groupId: String) {
        val session = sessionRepository.currentSession() ?: return
        val current = memberDao.getByGroupAndUserId(groupId, session.userId) ?: return
        val now = System.currentTimeMillis()
        memberDao.upsert(current.copy(deletedAt = now, updatedAt = now, syncState = SyncState.PENDING_DELETE))
        syncCoordinator.requestSync()
    }

    override suspend fun getGroup(groupId: String): Group? = groupDao.getById(groupId)?.takeIf { it.deletedAt == null }?.toDomain()
}
