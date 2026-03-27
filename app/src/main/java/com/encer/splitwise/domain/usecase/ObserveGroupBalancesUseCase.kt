package com.encer.splitwise.domain.usecase

import com.encer.splitwise.domain.model.MemberBalance
import com.encer.splitwise.domain.repository.ExpenseRepository
import com.encer.splitwise.domain.repository.MemberRepository
import com.encer.splitwise.domain.repository.SettlementRepository
import com.encer.splitwise.domain.usecase.base.BaseUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ObserveGroupBalancesParams(val groupId: String)

class ObserveGroupBalancesUseCase @Inject constructor(
    private val memberRepository: MemberRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val balanceCalculator: BalanceCalculator
) : BaseUseCase<ObserveGroupBalancesParams, Flow<List<MemberBalance>>>() {
    override fun invoke(params: ObserveGroupBalancesParams): Flow<List<MemberBalance>> {
        val groupId = params.groupId
        return combine(
            memberRepository.observeMembers(groupId),
            expenseRepository.observeExpenses(groupId),
            settlementRepository.observeSettlements(groupId)
        ) { members, expenses, settlements ->
            balanceCalculator.calculateBalances(members, expenses, settlements)
        }
    }
}
