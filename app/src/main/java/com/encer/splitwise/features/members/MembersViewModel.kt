package com.encer.splitwise.features.members

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.splitwise.data.sync.SyncCoordinator
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.domain.repository.GroupRepository
import com.encer.splitwise.domain.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MembersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    init {
        viewModelScope.launch { memberRepository.ensureSelfMember(groupId) }
    }

    val uiState: StateFlow<MembersUiState> = combine(
        groupRepository.observeGroups(),
        memberRepository.observeMembers(groupId),
        syncCoordinator.observeSyncStatus(),
    ) { groups, members, syncStatus ->
        MembersUiState(
            group = groups.firstOrNull { it.id == groupId },
            members = members,
            invalidUsernameMembers = syncStatus.invalidMemberUsernames.filter { issue ->
                members.any { member -> member.id == issue.memberId }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MembersUiState())

    fun addMember(username: String) {
        if (username.trim().isEmpty()) return
        viewModelScope.launch { memberRepository.addMember(groupId, username) }
    }

    fun updateMember(member: Member) {
        if (member.username.trim().isEmpty()) return
        viewModelScope.launch { memberRepository.updateMember(member.copy(username = member.username.trim().lowercase())) }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch { memberRepository.deleteMember(memberId) }
    }
}
