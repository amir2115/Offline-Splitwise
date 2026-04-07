package com.encer.splitwise.features.members

import com.encer.splitwise.domain.model.Group
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.data.sync.InvalidMemberUsernameIssue

data class MembersUiState(
    val group: Group? = null,
    val members: List<Member> = emptyList(),
    val invalidUsernameMembers: List<InvalidMemberUsernameIssue> = emptyList(),
    val actionError: Throwable? = null,
    val isSubmittingMember: Boolean = false,
)
