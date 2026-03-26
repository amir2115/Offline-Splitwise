package com.encer.offlinesplitwise.features.group_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.domain.repository.ExpenseRepository
import com.encer.offlinesplitwise.domain.repository.GroupRepository
import com.encer.offlinesplitwise.domain.repository.MemberRepository
import com.encer.offlinesplitwise.domain.repository.SettlementRepository
import com.encer.offlinesplitwise.domain.usecase.ObserveGroupSummaryParams
import com.encer.offlinesplitwise.domain.usecase.ObserveGroupSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GroupDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    memberRepository: MemberRepository,
    expenseRepository: ExpenseRepository,
    settlementRepository: SettlementRepository,
    observeGroupSummaryUseCase: ObserveGroupSummaryUseCase
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    init {
        viewModelScope.launch { memberRepository.ensureSelfMember(groupId) }
    }

    val uiState: StateFlow<GroupDashboardUiState> = combine(
        groupRepository.observeGroups(),
        observeGroupSummaryUseCase(ObserveGroupSummaryParams(groupId)),
        memberRepository.observeMembers(groupId),
        expenseRepository.observeExpenses(groupId),
        settlementRepository.observeSettlements(groupId)
    ) { groups, summary, members, expenses, settlements ->
        GroupDashboardUiState(
            group = groups.firstOrNull { it.id == groupId },
            summary = summary,
            members = members,
            expenses = expenses,
            settlements = settlements,
            canCreateTransactions = members.size >= 2,
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
}
