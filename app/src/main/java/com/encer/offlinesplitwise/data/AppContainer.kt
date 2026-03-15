package com.encer.offlinesplitwise.data

import android.content.Context
import com.encer.offlinesplitwise.data.local.OfflineSplitwiseDatabase
import com.encer.offlinesplitwise.data.preferences.SettingsRepository
import com.encer.offlinesplitwise.data.repository.DefaultExpenseRepository
import com.encer.offlinesplitwise.data.repository.DefaultGroupRepository
import com.encer.offlinesplitwise.data.repository.DefaultMemberRepository
import com.encer.offlinesplitwise.data.repository.DefaultSettlementRepository
import com.encer.offlinesplitwise.domain.BalanceCalculator
import com.encer.offlinesplitwise.domain.ExpenseRepository
import com.encer.offlinesplitwise.domain.GroupRepository
import com.encer.offlinesplitwise.domain.MemberRepository
import com.encer.offlinesplitwise.domain.ObserveGroupBalancesUseCase
import com.encer.offlinesplitwise.domain.ObserveGroupSummaryUseCase
import com.encer.offlinesplitwise.domain.SettlementRepository
import com.encer.offlinesplitwise.domain.SimplifyDebtsUseCase
import com.encer.offlinesplitwise.domain.ValidateExpenseInputUseCase

class AppContainer(context: Context) {
    private val database = OfflineSplitwiseDatabase.create(context)
    val settingsRepository = SettingsRepository(context)

    val groupRepository: GroupRepository = DefaultGroupRepository(database.groupDao())
    val memberRepository: MemberRepository = DefaultMemberRepository(database.memberDao())
    val expenseRepository: ExpenseRepository = DefaultExpenseRepository(
        expenseDao = database.expenseDao(),
        transactionDao = database.transactionDao()
    )
    val settlementRepository: SettlementRepository = DefaultSettlementRepository(database.settlementDao())

    private val balanceCalculator = BalanceCalculator()

    val observeGroupBalancesUseCase = ObserveGroupBalancesUseCase(
        memberRepository = memberRepository,
        expenseRepository = expenseRepository,
        settlementRepository = settlementRepository,
        balanceCalculator = balanceCalculator
    )
    val observeGroupSummaryUseCase = ObserveGroupSummaryUseCase(
        memberRepository = memberRepository,
        expenseRepository = expenseRepository,
        settlementRepository = settlementRepository,
        observeGroupBalancesUseCase = observeGroupBalancesUseCase
    )
    val simplifyDebtsUseCase = SimplifyDebtsUseCase(balanceCalculator = balanceCalculator)
    val validateExpenseInputUseCase = ValidateExpenseInputUseCase()
}
