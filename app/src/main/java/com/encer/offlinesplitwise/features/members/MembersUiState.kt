package com.encer.offlinesplitwise.features.members

import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.model.Member

data class MembersUiState(
    val group: Group? = null,
    val members: List<Member> = emptyList(),
)
