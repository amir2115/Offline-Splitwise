package com.encer.offlinesplitwise.features.groups

import com.encer.offlinesplitwise.domain.model.Group

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val errorMessage: String? = null,
)
