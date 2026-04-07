package com.encer.splitwise.data.remote.network

import com.encer.splitwise.data.remote.datasource.*
import com.encer.splitwise.data.remote.model.*
import com.encer.splitwise.data.preferences.AuthSession
import com.encer.splitwise.data.preferences.SessionRepository
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ApiClient @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val membersRemoteDataSource: MembersRemoteDataSource,
    private val syncRemoteDataSource: SyncRemoteDataSource,
    private val groupInvitesRemoteDataSource: GroupInvitesRemoteDataSource,
    private val healthRemoteDataSource: HealthRemoteDataSource,
    private val sessionRepository: SessionRepository,
) {
    private val refreshMutex = Mutex()

    suspend fun register(name: String, username: String, password: String): AuthResponse =
        authRemoteDataSource.register(AuthRegisterRequest(name = name, username = username, password = password))

    suspend fun login(username: String, password: String): AuthResponse =
        authRemoteDataSource.login(AuthLoginRequest(username = username, password = password))

    suspend fun me(): ApiUser = executeAuthenticated { authRemoteDataSource.me() }

    suspend fun createMember(groupId: String, username: String, isArchived: Boolean = false): RemoteAddMemberResponse =
        executeAuthenticated {
            membersRemoteDataSource.create(
                RemoteMemberCreateRequest(
                    groupId = groupId,
                    username = username,
                    isArchived = isArchived,
                )
            )
        }

    suspend fun sync(request: SyncRequestEnvelope): SyncResponse =
        executeAuthenticated { syncRemoteDataSource.sync(request) }

    suspend fun importData(request: SyncImportRequest): SyncResponse =
        executeAuthenticated { syncRemoteDataSource.importData(request) }

    suspend fun listGroupInvites(status: String = "pending"): List<RemoteGroupInvite> =
        executeAuthenticated { groupInvitesRemoteDataSource.list(status) }

    suspend fun acceptGroupInvite(inviteId: String): RemoteGroupInvite =
        executeAuthenticated { groupInvitesRemoteDataSource.accept(inviteId) }

    suspend fun rejectGroupInvite(inviteId: String): RemoteGroupInvite =
        executeAuthenticated { groupInvitesRemoteDataSource.reject(inviteId) }

    suspend fun health(): HealthCheckResponse = healthRemoteDataSource.health()

    private suspend fun <T> executeAuthenticated(block: suspend () -> T): T {
        return try {
            block()
        } catch (error: ApiError) {
            if (error.status == 401 && refreshTokens()) block() else throw error
        }
    }

    private suspend fun refreshTokens(): Boolean = refreshMutex.withLock {
        val refreshToken = sessionRepository.currentRefreshToken() ?: return false
        return runCatching {
            authRemoteDataSource.refresh(TokenRefreshRequest(refreshToken))
        }.map { payload ->
            sessionRepository.updateTokens(payload.accessToken, payload.refreshToken)
            true
        }.getOrElse {
            sessionRepository.clearSession()
            false
        }
    }
}
