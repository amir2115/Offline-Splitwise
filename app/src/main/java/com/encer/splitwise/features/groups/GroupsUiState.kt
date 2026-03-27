package com.encer.splitwise.features.groups

import com.encer.splitwise.domain.model.Group
import com.encer.splitwise.domain.model.GroupInvite

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val invites: List<GroupInvite> = emptyList(),
    val isLoading: Boolean = false,
    val canLeaveGroups: Boolean = false,
    val currentUserId: String? = null,
    val errorMessage: String? = null,
)
