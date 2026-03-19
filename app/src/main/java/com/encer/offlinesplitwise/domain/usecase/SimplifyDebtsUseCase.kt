package com.encer.offlinesplitwise.domain.usecase

import com.encer.offlinesplitwise.domain.model.MemberBalance
import com.encer.offlinesplitwise.domain.model.SimplifiedTransfer
import com.encer.offlinesplitwise.domain.usecase.base.BaseUseCase
import javax.inject.Inject

data class SimplifyDebtsParams(val balances: List<MemberBalance>)

class SimplifyDebtsUseCase @Inject constructor(
    private val balanceCalculator: BalanceCalculator
) : BaseUseCase<SimplifyDebtsParams, List<SimplifiedTransfer>>() {
    override fun invoke(params: SimplifyDebtsParams): List<SimplifiedTransfer> =
        balanceCalculator.simplify(params.balances)
}
