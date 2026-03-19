package com.encer.offlinesplitwise.features.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {
    val uiState: StateFlow<GroupsUiState> = groupRepository.observeGroups()
        .combine(MutableStateFlow<String?>(null)) { groups, error ->
            GroupsUiState(groups = groups, errorMessage = error)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupsUiState())

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
}
