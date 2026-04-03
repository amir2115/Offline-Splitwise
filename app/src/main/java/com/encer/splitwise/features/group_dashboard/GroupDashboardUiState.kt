package com.encer.splitwise.features.group_dashboard

import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.Group
import com.encer.splitwise.domain.model.GroupSummary
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.domain.model.Settlement

data class GroupDashboardUiState(
    val group: Group? = null,
    val summary: GroupSummary = GroupSummary(0, 0, 0, 0),
    val members: List<Member> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val canCreateTransactions: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshError: String? = null,
    val canRefresh: Boolean = false,
)
