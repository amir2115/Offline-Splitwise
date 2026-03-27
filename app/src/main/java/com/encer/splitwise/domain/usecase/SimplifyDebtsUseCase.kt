package com.encer.splitwise.domain.usecase

import com.encer.splitwise.domain.model.MemberBalance
import com.encer.splitwise.domain.model.SimplifiedTransfer
import com.encer.splitwise.domain.usecase.base.BaseUseCase
import javax.inject.Inject

data class SimplifyDebtsParams(val balances: List<MemberBalance>)

class SimplifyDebtsUseCase @Inject constructor(
    private val balanceCalculator: BalanceCalculator
) : BaseUseCase<SimplifyDebtsParams, List<SimplifiedTransfer>>() {
    override fun invoke(params: SimplifyDebtsParams): List<SimplifiedTransfer> =
        balanceCalculator.simplify(params.balances)
}
