package com.encer.offlinesplitwise.features.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.data.preferences.SessionRepository
import com.encer.offlinesplitwise.data.remote.mapper.toDomain
import com.encer.offlinesplitwise.data.remote.network.ApiClient
import com.encer.offlinesplitwise.data.sync.SyncCoordinator
import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.model.GroupInvite
import com.encer.offlinesplitwise.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val apiClient: ApiClient,
    private val sessionRepository: SessionRepository,
    private val syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val invites = MutableStateFlow<List<GroupInvite>>(emptyList())
    private val invitesLoading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    init {
        refreshInvites()
        viewModelScope.launch {
            sessionRepository.observeSession()
                .map { it?.userId }
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    refreshInvites()
                }
        }
    }

    private val loadingSignals = combine(
        invitesLoading,
        syncCoordinator.observeSyncStatus(),
        sessionRepository.observeSession(),
    ) { nextInvitesLoading, syncStatus, session ->
        Triple(nextInvitesLoading, syncStatus, session)
    }

    val uiState: StateFlow<GroupsUiState> = combine(
        groupRepository.observeGroups(),
        invites,
        error,
        loadingSignals,
    ) { groups, nextInvites, nextError, loadingState ->
        val (nextInvitesLoading, syncStatus, session) = loadingState
        val hasSession = session != null
        GroupsUiState(
            groups = groups,
            invites = nextInvites,
            isLoading = hasSession && (nextInvitesLoading || (groups.isEmpty() && syncStatus.isSyncing)),
            errorMessage = nextError,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupsUiState())

    fun createGroup(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { groupRepository.createGroup(clean) }
    }

    fun updateGroup(group: Group) {
        val clean = group.name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { groupRepository.updateGroup(group.copy(name = clean)) }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch { groupRepository.deleteGroup(groupId) }
    }

    fun refreshInvites() {
        if (sessionRepository.currentSession() == null) {
            invites.value = emptyList()
            invitesLoading.value = false
            return
        }
        viewModelScope.launch {
            invitesLoading.value = true
            runCatching { apiClient.listGroupInvites().map { it.toDomain() } }
                .onSuccess {
                    invites.value = it
                    error.value = null
                }
                .onFailure { error.value = it.message }
            invitesLoading.value = false
        }
    }

    fun acceptInvite(inviteId: String) {
        viewModelScope.launch {
            runCatching { apiClient.acceptGroupInvite(inviteId) }
                .onSuccess {
                    refreshInvites()
                    syncCoordinator.requestSync()
                }
                .onFailure { error.value = it.message }
        }
    }

    fun rejectInvite(inviteId: String) {
        viewModelScope.launch {
            runCatching { apiClient.rejectGroupInvite(inviteId) }
                .onSuccess {
                    refreshInvites()
                }
                .onFailure { error.value = it.message }
        }
    }
}
