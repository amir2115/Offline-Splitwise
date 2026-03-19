package com.encer.offlinesplitwise.data.sync

import androidx.room.withTransaction
import com.encer.offlinesplitwise.data.local.dao.ExpenseDao
import com.encer.offlinesplitwise.data.local.dao.GroupDao
import com.encer.offlinesplitwise.data.local.dao.MemberDao
import com.encer.offlinesplitwise.data.local.db.OfflineSplitwiseDatabase
import com.encer.offlinesplitwise.data.local.dao.SettlementDao
import com.encer.offlinesplitwise.data.local.dao.SyncDao
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.data.local.dao.TransactionDao
import com.encer.offlinesplitwise.data.preferences.AuthSession
import com.encer.offlinesplitwise.data.preferences.SessionRepository
import com.encer.offlinesplitwise.data.remote.mapper.toEntity
import com.encer.offlinesplitwise.data.remote.mapper.toPayerEntities
import com.encer.offlinesplitwise.data.remote.mapper.toRemotePayload
import com.encer.offlinesplitwise.data.remote.mapper.toShareEntities
import com.encer.offlinesplitwise.data.remote.model.AuthResponse
import com.encer.offlinesplitwise.data.remote.network.ApiClient
import com.encer.offlinesplitwise.data.remote.network.formatIsoInstant
import com.encer.offlinesplitwise.data.remote.network.parseIsoInstant
import com.encer.offlinesplitwise.data.remote.model.ApiError
import com.encer.offlinesplitwise.data.remote.model.SyncImportRequest
import com.encer.offlinesplitwise.data.remote.model.SyncPushPayload
import com.encer.offlinesplitwise.data.remote.model.SyncRequestEnvelope
import com.encer.offlinesplitwise.data.remote.model.SyncResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val lastError: String? = null,
)

class SyncCoordinator(
    private val database: OfflineSplitwiseDatabase,
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

    suspend fun restoreSessionAndSync() {
        val session = sessionRepository.currentSession() ?: return
        if (!networkMonitor.refreshApiReachability()) return
        runCatching { apiClient.me() }
            .onFailure {
                if (it is ApiError && it.status == 401) {
                    sessionRepository.clearSession()
                    _syncStatus.value = SyncStatus(lastError = it.message)
                }
            }
            .onSuccess { remoteUser ->
                sessionRepository.saveSession(
                    session.copy(
                        userId = remoteUser.id,
                        name = remoteUser.name,
                        username = remoteUser.username,
                    )
                )
                syncIfPossible()
            }
    }

    fun requestSync() {
        scope.launch(Dispatchers.IO) {
            syncIfPossible()
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

    suspend fun syncIfPossible() {
        sessionRepository.currentSession() ?: return
        if (!networkMonitor.refreshApiReachability()) return

        syncMutex.withLock {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastError = null)
            runCatching {
                if (sessionRepository.observeLastSyncedAt().value == null && hasAnyLocalData()) {
                    runInitialImportIfPossible()
                } else {
                    performIncrementalSync()
                }
            }.onFailure { error ->
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    lastError = error.message ?: "Sync failed",
                )
            }.onSuccess {
                _syncStatus.value = SyncStatus(
                    isSyncing = false,
                    lastSyncedAt = sessionRepository.observeLastSyncedAt().value,
                    lastError = null,
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
            sessionRepository.saveSession(session)
            if (networkMonitor.refreshApiReachability()) {
                runCatching {
                    if (hasAnyLocalData() && sessionRepository.observeLastSyncedAt().value == null) {
                        runInitialImportIfPossible()
                    } else {
                        performIncrementalSync()
                    }
                }.onFailure { error ->
                    _syncStatus.value = _syncStatus.value.copy(
                        isSyncing = false,
                        lastSyncedAt = sessionRepository.observeLastSyncedAt().value,
                        lastError = error.message ?: "Sync failed",
                    )
                }
            }
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
        database.withTransaction {
            if (response.changes.groups.isNotEmpty()) groupDao.upsertAll(response.changes.groups.map { it.toEntity() })
            if (response.changes.members.isNotEmpty()) memberDao.upsertAll(response.changes.members.map { it.toEntity() })
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
            if (response.changes.deletedMemberIds.isNotEmpty()) memberDao.hardDeleteByIds(response.changes.deletedMemberIds)
            if (response.changes.deletedGroupIds.isNotEmpty()) groupDao.hardDeleteByIds(response.changes.deletedGroupIds)
        }

        sessionRepository.setLastSyncedAt(parseIsoInstant(response.nextCursor))
    }

    private suspend fun hasAnyLocalData(): Boolean {
        return groupDao.countAll() > 0 ||
            memberDao.getAll().isNotEmpty() ||
            expenseDao.getAll().isNotEmpty() ||
            settlementDao.getAll().isNotEmpty()
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
