package com.encer.offlinesplitwise.features.members

import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.data.sync.InvalidMemberUsernameIssue

data class MembersUiState(
    val group: Group? = null,
    val members: List<Member> = emptyList(),
    val invalidUsernameMembers: List<InvalidMemberUsernameIssue> = emptyList(),
)
