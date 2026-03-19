package com.encer.offlinesplitwise.features.members

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.domain.repository.GroupRepository
import com.encer.offlinesplitwise.domain.repository.MemberRepository
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
    private val memberRepository: MemberRepository
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    init {
        viewModelScope.launch { memberRepository.ensureSelfMember(groupId) }
    }

    val uiState: StateFlow<MembersUiState> = combine(
        groupRepository.observeGroups(),
        memberRepository.observeMembers(groupId)
    ) { groups, members ->
        MembersUiState(group = groups.firstOrNull { it.id == groupId }, members = members)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MembersUiState())

    fun addMember(name: String) {
        if (name.trim().isEmpty()) return
        viewModelScope.launch { memberRepository.addMember(groupId, name) }
    }

    fun updateMember(member: Member) {
        if (member.name.trim().isEmpty()) return
        viewModelScope.launch { memberRepository.updateMember(member.copy(name = member.name.trim())) }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch { memberRepository.deleteMember(memberId) }
    }
}
