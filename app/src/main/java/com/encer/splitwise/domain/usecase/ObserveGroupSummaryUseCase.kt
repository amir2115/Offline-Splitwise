package com.encer.splitwise.domain.usecase

import com.encer.splitwise.domain.model.GroupSummary
import com.encer.splitwise.domain.repository.ExpenseRepository
import com.encer.splitwise.domain.repository.MemberRepository
import com.encer.splitwise.domain.repository.SettlementRepository
import com.encer.splitwise.domain.usecase.base.BaseUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ObserveGroupSummaryParams(val groupId: String)

class ObserveGroupSummaryUseCase @Inject constructor(
    private val memberRepository: MemberRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val observeGroupBalancesUseCase: ObserveGroupBalancesUseCase
) : BaseUseCase<ObserveGroupSummaryParams, Flow<GroupSummary>>() {
    override fun invoke(params: ObserveGroupSummaryParams): Flow<GroupSummary> {
        val groupId = params.groupId
        return combine(
            memberRepository.observeMembers(groupId),
            expenseRepository.observeExpenses(groupId),
            settlementRepository.observeSettlements(groupId),
            observeGroupBalancesUseCase(ObserveGroupBalancesParams(groupId))
        ) { members, expenses, settlements, balances ->
            GroupSummary(
                totalExpenses = expenses.sumOf { it.totalAmount },
                totalSettlements = settlements.sumOf { it.amount },
                membersCount = members.size,
                openBalancesCount = balances.count { it.netBalance != 0 }
            )
        }
    }
}
