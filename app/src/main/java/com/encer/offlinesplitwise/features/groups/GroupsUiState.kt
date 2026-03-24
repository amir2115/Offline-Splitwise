package com.encer.offlinesplitwise.features.groups

import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.model.GroupInvite

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val invites: List<GroupInvite> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
