package com.encer.splitwise.features.group_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.splitwise.data.preferences.SessionRepository
import com.encer.splitwise.data.sync.SyncCoordinator
import com.encer.splitwise.domain.repository.ExpenseRepository
import com.encer.splitwise.domain.repository.GroupRepository
import com.encer.splitwise.domain.repository.MemberRepository
import com.encer.splitwise.domain.repository.SettlementRepository
import com.encer.splitwise.domain.usecase.ObserveGroupSummaryParams
import com.encer.splitwise.domain.usecase.ObserveGroupSummaryUseCase
import com.encer.splitwise.domain.usecase.canCreateTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class GroupDashboardData(
    val group: com.encer.splitwise.domain.model.Group?,
    val summary: com.encer.splitwise.domain.model.GroupSummary,
    val members: List<com.encer.splitwise.domain.model.Member>,
    val expenses: List<com.encer.splitwise.domain.model.Expense>,
    val settlements: List<com.encer.splitwise.domain.model.Settlement>,
)

@HiltViewModel
class GroupDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    memberRepository: MemberRepository,
    expenseRepository: ExpenseRepository,
    settlementRepository: SettlementRepository,
    observeGroupSummaryUseCase: ObserveGroupSummaryUseCase,
    private val sessionRepository: SessionRepository,
    private val syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    init {
        viewModelScope.launch { memberRepository.ensureSelfMember(groupId) }
    }

    private val dashboardData = combine(
        groupRepository.observeGroups(),
        observeGroupSummaryUseCase(ObserveGroupSummaryParams(groupId)),
        memberRepository.observeMembers(groupId),
        expenseRepository.observeExpenses(groupId),
        settlementRepository.observeSettlements(groupId),
    ) { groups, summary, members, expenses, settlements ->
        GroupDashboardData(
            group = groups.firstOrNull { it.id == groupId },
            summary = summary,
            members = members,
            expenses = expenses,
            settlements = settlements,
        )
    }

    val uiState: StateFlow<GroupDashboardUiState> = combine(
        dashboardData,
        syncCoordinator.observeSyncStatus(),
        sessionRepository.observeSession(),
    ) { dashboardData, syncStatus, session ->
        GroupDashboardUiState(
            group = dashboardData.group,
            summary = dashboardData.summary,
            members = dashboardData.members,
            expenses = dashboardData.expenses,
            settlements = dashboardData.settlements,
            canCreateTransactions = canCreateTransaction(memberCount = dashboardData.members.size, isEdit = false),
            isRefreshing = syncStatus.isSyncing,
            refreshError = syncStatus.lastError,
            canRefresh = session != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupDashboardUiState())

    private val expenseRepositoryRef = expenseRepository
    private val settlementRepositoryRef = settlementRepository

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch { expenseRepositoryRef.deleteExpense(expenseId) }
    }

    fun deleteSettlement(settlementId: String) {
        viewModelScope.launch { settlementRepositoryRef.deleteSettlement(settlementId) }
    }

    fun refresh() {
        syncCoordinator.requestSync(forceNetworkRequest = true)
    }
}
