package com.encer.splitwise.data.sync

import androidx.room.withTransaction
import com.encer.splitwise.data.local.dao.ExpenseDao
import com.encer.splitwise.data.local.dao.GroupDao
import com.encer.splitwise.data.local.dao.MemberDao
import com.encer.splitwise.data.local.db.SplitwiseDatabase
import com.encer.splitwise.data.local.dao.SettlementDao
import com.encer.splitwise.data.local.dao.SyncDao
import com.encer.splitwise.data.local.entity.MemberEntity
import com.encer.splitwise.data.local.entity.SyncState
import com.encer.splitwise.data.local.dao.TransactionDao
import com.encer.splitwise.data.preferences.AuthSession
import com.encer.splitwise.data.preferences.SessionRepository
import com.encer.splitwise.data.remote.mapper.toEntity
import com.encer.splitwise.data.remote.mapper.toPayerEntities
import com.encer.splitwise.data.remote.mapper.toRemotePayload
import com.encer.splitwise.data.remote.mapper.toShareEntities
import com.encer.splitwise.data.remote.model.AuthResponse
import com.encer.splitwise.data.remote.network.ApiClient
import com.encer.splitwise.data.remote.network.formatIsoInstant
import com.encer.splitwise.data.remote.network.parseIsoInstant
import com.encer.splitwise.data.remote.model.ApiError
import com.encer.splitwise.data.remote.model.SyncImportRequest
import com.encer.splitwise.data.remote.model.SyncPushPayload
import com.encer.splitwise.data.remote.model.SyncRequestEnvelope
import com.encer.splitwise.data.remote.model.SyncResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class InvalidMemberUsernameIssue(
    val memberId: String,
    val username: String,
)

data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val lastError: String? = null,
    val invalidMemberUsernames: List<InvalidMemberUsernameIssue> = emptyList(),
)

private val syncErrorJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class SyncErrorEnvelope(
    val error: SyncErrorPayload? = null,
)

@Serializable
private data class SyncErrorPayload(
    val code: String? = null,
    val message: String? = null,
    val details: List<InvalidMemberUsernamePayload> = emptyList(),
)

@Serializable
private data class InvalidMemberUsernamePayload(
    @SerialName("member_id") val memberId: String,
    val username: String,
)

class SyncCoordinator(
    private val database: SplitwiseDatabase,
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val expenseDao: ExpenseDao,
    private val transactionDao: TransactionDao,
    private val settlementDao: SettlementDao,
    private val syncDao: SyncDao,
    private val sessionRepository: SessionRepository,
    private val apiClient: ApiClient,
    private val networkMonitor: NetworkMonitor,
    private val scope: CoroutineScope,
) {
    private val syncMutex = Mutex()
    private val _syncStatus = MutableStateFlow(SyncStatus(lastSyncedAt = sessionRepository.observeLastSyncedAt().value))

    fun observeSyncStatus(): StateFlow<SyncStatus> = _syncStatus
    fun isOnline(): Boolean = networkMonitor.isOnline()
    fun isApiReachable(): Boolean = networkMonitor.isApiAvailable()

    suspend fun restoreSessionAndSync(forceNetworkRequest: Boolean = false) {
        val session = sessionRepository.currentSession() ?: return
        if (!forceNetworkRequest && !networkMonitor.hasInternetConnection()) return
        val remoteUser = runCatching { apiClient.me() }
            .onFailure {
                if (it is ApiError && it.status == 401) {
                    sessionRepository.clearSession()
                    _syncStatus.value = SyncStatus(lastError = it.message, invalidMemberUsernames = emptyList())
                }
            }
            .getOrNull() ?: run {
            if (sessionRepository.currentSession() != null) {
                syncIfPossible(forceNetworkRequest = forceNetworkRequest)
            }
            return
        }
        prepareLocalStateForAuthenticatedUser(remoteUser.id)
        sessionRepository.saveSession(
            session.copy(
                userId = remoteUser.id,
                name = remoteUser.name,
                username = remoteUser.username,
            )
        )
        syncIfPossible(forceNetworkRequest = forceNetworkRequest)
    }

    fun requestSync(forceNetworkRequest: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            syncIfPossible(forceNetworkRequest = forceNetworkRequest)
        }
    }

    suspend fun login(username: String, password: String): Result<AuthSession> = authenticate {
        apiClient.login(username, password)
    }

    suspend fun register(name: String, username: String, password: String): Result<AuthSession> = authenticate {
        apiClient.register(name, username, password)
    }

    fun logout() {
        sessionRepository.clearSession()
        _syncStatus.value = SyncStatus()
    }

    suspend fun syncIfPossible(forceNetworkRequest: Boolean = false) {
        sessionRepository.currentSession() ?: return
        if (!forceNetworkRequest && !networkMonitor.hasInternetConnection()) return

        syncMutex.withLock {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastError = null)
            runCatching {
                if (sessionRepository.observeLastSyncedAt().value == null && hasAnyLocalData()) {
                    runInitialImportIfPossible()
                } else {
                    performIncrementalSync()
                }
            }.onFailure { error ->
                // FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY)
                val blockers = parseInvalidMemberIssues(error)
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    lastError = error.message ?: "Sync failed",
                    invalidMemberUsernames = blockers,
                )
            }.onSuccess {
                _syncStatus.value = SyncStatus(
                    isSyncing = false,
                    lastSyncedAt = sessionRepository.observeLastSyncedAt().value,
                    lastError = null,
                    invalidMemberUsernames = emptyList(),
                )
            }
        }
    }

    private suspend fun authenticate(block: suspend () -> AuthResponse): Result<AuthSession> {
        return runCatching {
            val response = block()
            val session = AuthSession(
                accessToken = response.tokens.accessToken,
                refreshToken = response.tokens.refreshToken,
                userId = response.user.id,
                name = response.user.name,
                username = response.user.username,
            )
            prepareLocalStateForAuthenticatedUser(session.userId)
            sessionRepository.saveSession(session)
            session
        }
    }

    private suspend fun runInitialImportIfPossible() {
        val payload = buildSyncPayload(includeAllLocalData = true)
        if (payload == null) {
            performIncrementalSync()
            return
        }
        try {
            val response = apiClient.importData(payload)
            applySyncResponse(response)
        } catch (error: ApiError) {
            if (error.status in listOf(400, 409, 422)) {
                performIncrementalSync()
            } else {
                throw error
            }
        }
    }

    private suspend fun performIncrementalSync() {
        val pushPayload = buildSyncPayload(includeAllLocalData = false)
        val response = apiClient.sync(
            SyncRequestEnvelope(
                deviceId = sessionRepository.getDeviceId(),
                lastSyncedAt = sessionRepository.observeLastSyncedAt().value?.let(::formatIsoInstant),
                push = pushPayload?.toPushPayload(),
            )
        )
        applySyncResponse(response)
    }

    private suspend fun buildSyncPayload(includeAllLocalData: Boolean): SyncImportRequest? {
        val groups = if (includeAllLocalData) groupDao.getAll() else groupDao.getBySyncState(SyncState.PENDING_UPSERT)
        val members = if (includeAllLocalData) memberDao.getAll() else memberDao.getBySyncState(SyncState.PENDING_UPSERT)
        val expenses = if (includeAllLocalData) expenseDao.getAll() else expenseDao.getBySyncState(SyncState.PENDING_UPSERT)
        val settlements = if (includeAllLocalData) settlementDao.getAll() else settlementDao.getBySyncState(SyncState.PENDING_UPSERT)
        val deletedGroups = if (includeAllLocalData) groups.filter { it.deletedAt != null }.map { it.id } else groupDao.getBySyncState(SyncState.PENDING_DELETE).map { it.id }
        val deletedMembers = if (includeAllLocalData) members.filter { it.deletedAt != null }.map { it.id } else memberDao.getBySyncState(SyncState.PENDING_DELETE).map { it.id }
        val deletedExpenses = if (includeAllLocalData) expenses.filter { it.deletedAt != null }.map { it.id } else expenseDao.getBySyncState(SyncState.PENDING_DELETE).map { it.id }
        val deletedSettlements = if (includeAllLocalData) settlements.filter { it.deletedAt != null }.map { it.id } else settlementDao.getBySyncState(SyncState.PENDING_DELETE).map { it.id }

        val activeGroups = groups.filter { it.deletedAt == null }
        val activeMembers = members.filter { it.deletedAt == null }
        val activeExpenses = expenses.filter { it.deletedAt == null }
        val activeSettlements = settlements.filter { it.deletedAt == null }

        if (
            !includeAllLocalData &&
            activeGroups.isEmpty() &&
            activeMembers.isEmpty() &&
            activeExpenses.isEmpty() &&
            activeSettlements.isEmpty() &&
            deletedGroups.isEmpty() &&
            deletedMembers.isEmpty() &&
            deletedExpenses.isEmpty() &&
            deletedSettlements.isEmpty()
        ) {
            return null
        }

        return SyncImportRequest(
            deviceId = sessionRepository.getDeviceId(),
            groups = activeGroups.map { it.toRemotePayload() },
            members = activeMembers.map { it.toRemotePayload() },
            expenses = activeExpenses.map { expense ->
                expense.toRemotePayload(
                    payers = transactionDao.getPayers(expense.id),
                    shares = transactionDao.getShares(expense.id),
                )
            },
            settlements = activeSettlements.map { it.toRemotePayload() },
            deletedGroupIds = deletedGroups,
            deletedMemberIds = deletedMembers,
            deletedExpenseIds = deletedExpenses,
            deletedSettlementIds = deletedSettlements,
        )
    }

    private suspend fun applySyncResponse(response: SyncResponse) {
        val serverTimestamp = parseIsoInstant(response.serverTime)
        database.withTransaction {
            if (response.changes.groups.isNotEmpty()) groupDao.upsertAll(response.changes.groups.map { it.toEntity() })
            if (response.changes.members.isNotEmpty()) {
                memberDao.upsertAll(response.changes.members.map { it.toEntity() })
            }
            ensureReferencedMembersExist(response, serverTimestamp)
            if (response.changes.expenses.isNotEmpty()) {
                syncDao.replaceExpenses(
                    expenses = response.changes.expenses.map { it.toEntity() },
                    payers = response.changes.expenses.flatMap { it.toPayerEntities() },
                    shares = response.changes.expenses.flatMap { it.toShareEntities() },
                )
            }
            if (response.changes.settlements.isNotEmpty()) {
                settlementDao.upsertAll(response.changes.settlements.map { it.toEntity() })
            }
            if (response.changes.deletedExpenseIds.isNotEmpty()) expenseDao.hardDeleteExpenses(response.changes.deletedExpenseIds)
            if (response.changes.deletedSettlementIds.isNotEmpty()) settlementDao.hardDeleteByIds(response.changes.deletedSettlementIds)
            if (response.changes.deletedMemberIds.isNotEmpty()) {
                applyDeletedMembers(
                    memberIds = response.changes.deletedMemberIds,
                    deletedAt = serverTimestamp,
                )
            }
            if (response.changes.deletedGroupIds.isNotEmpty()) groupDao.hardDeleteByIds(response.changes.deletedGroupIds)

            response.changes.members.forEach { member ->
                member.userId?.let { userId ->
                    cleanupDuplicateMembers(
                        groupId = member.groupId,
                        userId = userId,
                        keepId = member.id,
                    )
                }
            }
        }

        sessionRepository.setLastSyncedAt(parseIsoInstant(response.nextCursor))
        sessionRepository.currentSession()?.userId?.let(sessionRepository::setDataOwnerUserId)
    }

    private suspend fun hasAnyLocalData(): Boolean {
        return groupDao.countAll() > 0 ||
            memberDao.getAll().isNotEmpty() ||
            expenseDao.getAll().isNotEmpty() ||
            settlementDao.getAll().isNotEmpty()
    }

    private suspend fun prepareLocalStateForAuthenticatedUser(userId: String) {
        withContext(Dispatchers.IO) {
            val currentOwner = sessionRepository.currentDataOwnerUserId()
            if (currentOwner == null || currentOwner == userId) return@withContext
            database.clearAllTables()
            sessionRepository.setLastSyncedAt(null)
            _syncStatus.value = SyncStatus()
        }
    }

    private suspend fun cleanupDuplicateMembers(groupId: String, userId: String, keepId: String) {
        val duplicateIds = memberDao.getDuplicateIdsByGroupAndUserId(
            groupId = groupId,
            userId = userId,
            keepId = keepId,
        )
        if (duplicateIds.isEmpty()) return
        val removableIds = duplicateIds.filterNot { duplicateId ->
            syncDao.hasMemberReferences(duplicateId)
        }
        if (removableIds.isNotEmpty()) {
            memberDao.hardDeleteByIds(removableIds)
        }
    }

    private suspend fun ensureReferencedMembersExist(response: SyncResponse, timestamp: Long) {
        val memberGroupIds = linkedMapOf<String, String>()

        response.changes.expenses.forEach { expense ->
            expense.payers.forEach { payer -> memberGroupIds.putIfAbsent(payer.memberId, expense.groupId) }
            expense.shares.forEach { share -> memberGroupIds.putIfAbsent(share.memberId, expense.groupId) }
        }
        response.changes.settlements.forEach { settlement ->
            memberGroupIds.putIfAbsent(settlement.fromMemberId, settlement.groupId)
            memberGroupIds.putIfAbsent(settlement.toMemberId, settlement.groupId)
        }

        if (memberGroupIds.isEmpty()) return

        val placeholders = memberGroupIds.mapNotNull { (memberId, groupId) ->
            if (memberDao.getById(memberId) != null) return@mapNotNull null
            MemberEntity(
                id = memberId,
                groupId = groupId,
                username = memberId,
                createdAt = timestamp,
                updatedAt = timestamp,
                deletedAt = timestamp,
                isArchived = true,
                userId = null,
                membershipStatus = com.encer.splitwise.domain.model.MembershipStatus.ACTIVE,
                syncState = SyncState.SYNCED,
            )
        }
        if (placeholders.isNotEmpty()) {
            memberDao.upsertAll(placeholders)
        }
    }

    private suspend fun applyDeletedMembers(memberIds: List<String>, deletedAt: Long) {
        val (referencedIds, removableIds) = memberIds.distinct().partition { memberId ->
            syncDao.hasMemberReferences(memberId)
        }
        if (referencedIds.isNotEmpty()) {
            memberDao.markDeletedByIds(
                memberIds = referencedIds,
                deletedAt = deletedAt,
                updatedAt = deletedAt,
            )
        }
        if (removableIds.isNotEmpty()) {
            memberDao.hardDeleteByIds(removableIds)
        }
    }
}

private fun SyncImportRequest.toPushPayload() = SyncPushPayload(
    deviceId = deviceId,
    groups = groups,
    members = members,
    expenses = expenses,
    settlements = settlements,
    deletedGroupIds = deletedGroupIds,
    deletedMemberIds = deletedMemberIds,
    deletedExpenseIds = deletedExpenseIds,
    deletedSettlementIds = deletedSettlementIds,
)

private fun parseInvalidMemberIssues(error: Throwable): List<InvalidMemberUsernameIssue> {
    val apiError = error as? ApiError ?: return emptyList()
    val payload = apiError.payload ?: return emptyList()
    return runCatching {
        val envelope = syncErrorJson.decodeFromString(SyncErrorEnvelope.serializer(), payload)
        val details = envelope.error?.takeIf { it.code == "invalid_member_usernames" }?.details.orEmpty()
        details.map { InvalidMemberUsernameIssue(memberId = it.memberId, username = it.username) }
    }.getOrDefault(emptyList())
}
