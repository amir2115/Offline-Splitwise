package com.encer.offlinesplitwise.features.group_dashboard

import com.encer.offlinesplitwise.domain.model.Expense
import com.encer.offlinesplitwise.domain.model.Group
import com.encer.offlinesplitwise.domain.model.GroupSummary
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.domain.model.Settlement

data class GroupDashboardUiState(
    val group: Group? = null,
    val summary: GroupSummary = GroupSummary(0, 0, 0, 0),
    val members: List<Member> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val canCreateTransactions: Boolean = false,
)
