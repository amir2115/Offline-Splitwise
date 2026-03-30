package com.encer.splitwise.features.expense_editor

import com.encer.splitwise.domain.model.ExpenseShare
import com.encer.splitwise.domain.model.SplitType
import com.encer.splitwise.ui.formatting.parseAmountInput
import kotlin.math.roundToInt

data class ExpenseEditorComputedState(
    val payerTotal: Int = 0,
    val baseShareTotal: Int = 0,
    val finalShareTotal: Int = 0,
    val remainingPayerAmount: Int = 0,
    val remainingBaseShareAmount: Int = 0,
    val isPayerOverflow: Boolean = false,
    val isBaseShareOverflow: Boolean = false,
    val taxAmountPreview: Int = 0,
    val baseAmountPreview: Int = 0,
    val serviceChargeTotalPreview: Int = 0,
    val hasInvalidServiceCharges: Boolean = false,
    val hasInvalidTaxPercent: Boolean = false,
    val memberBreakdowns: Map<String, MemberCostBreakdown> = emptyMap(),
)

data class MemberCostBreakdown(
    val baseShare: Int = 0,
    val taxShare: Int = 0,
    val serviceChargeShare: Int = 0,
    val finalShare: Int = 0,
)

fun computeExpenseEditorState(
    totalAmount: Int,
    splitType: SplitType,
    members: List<MemberDraftUi>,
    taxEnabled: Boolean,
    taxPercentInput: String,
    serviceCharges: List<ServiceChargeDraftUi>,
): ExpenseEditorComputedState {
    val payerTotal = members.sumOf { parseAmountInput(it.payerAmountInput) }
    val includedMembers = members.filter { it.includedInSplit }.sortedBy { it.memberId }
    val serviceChargeAllocations = serviceCharges.map { charge ->
        val amount = parseAmountInput(charge.amountInput)
        val selectedIds = includedMembers.map { it.memberId }.filter { it in charge.selectedMemberIds }
        ServiceChargeAllocation(
            charge = charge,
            amount = amount,
            selectedMemberIds = selectedIds,
            perMemberAmounts = if (amount > 0 && selectedIds.isNotEmpty()) splitAmountDeterministically(amount, selectedIds) else emptyMap(),
            isValid = amount > 0 && selectedIds.isNotEmpty()
        )
    }
    val hasInvalidServiceCharges = serviceChargeAllocations.any { allocation ->
        allocation.charge.title.isNotBlank() ||
            allocation.charge.amountInput.isNotBlank() ||
            allocation.charge.selectedMemberIds.isNotEmpty()
    } && serviceChargeAllocations.any { !it.isValid }
    val serviceChargeTotalPreview = serviceChargeAllocations.filter { it.isValid }.sumOf { it.amount }

    val taxPercentValue = parseAmountInput(taxPercentInput)
    val hasInvalidTaxPercent = taxEnabled && taxPercentInput.isNotBlank() && taxPercentValue !in 0..100
    val taxableTotal = (totalAmount - serviceChargeTotalPreview).coerceAtLeast(0)
    val taxAmountPreview = when {
        !taxEnabled -> 0
        taxableTotal <= 0 -> 0
        hasInvalidTaxPercent -> 0
        else -> {
            val baseAmount = ((taxableTotal * 100.0) / (100.0 + taxPercentValue)).roundToInt().coerceIn(0, taxableTotal)
            taxableTotal - baseAmount
        }
    }
    val baseAmountPreview = when {
        taxableTotal <= 0 -> 0
        else -> taxableTotal - taxAmountPreview
    }

    val baseShares = when (splitType) {
        SplitType.EQUAL -> splitAmountDeterministically(baseAmountPreview, includedMembers.map { it.memberId })
        SplitType.EXACT -> includedMembers.associate { it.memberId to parseAmountInput(it.exactShareInput) }
    }
    val baseShareTotal = baseShares.values.sum()
    val taxShares = if (taxEnabled && taxAmountPreview > 0 && baseShareTotal > 0) {
        distributeProportionally(
            total = taxAmountPreview,
            weights = baseShares.filterValues { it > 0 }
        )
    } else {
        includedMembers.associate { it.memberId to 0 }
    }
    val serviceShares = mutableMapOf<String, Int>()
    includedMembers.forEach { serviceShares[it.memberId] = 0 }
    serviceChargeAllocations.filter { it.isValid }.forEach { allocation ->
        allocation.perMemberAmounts.forEach { (memberId, amount) ->
            serviceShares[memberId] = (serviceShares[memberId] ?: 0) + amount
        }
    }

    val memberBreakdowns = includedMembers.associate { member ->
        val baseShare = baseShares[member.memberId] ?: 0
        val taxShare = taxShares[member.memberId] ?: 0
        val serviceShare = serviceShares[member.memberId] ?: 0
        member.memberId to MemberCostBreakdown(
            baseShare = baseShare,
            taxShare = taxShare,
            serviceChargeShare = serviceShare,
            finalShare = baseShare + taxShare + serviceShare
        )
    }
    val finalShareTotal = memberBreakdowns.values.sumOf { it.finalShare }

    return ExpenseEditorComputedState(
        payerTotal = payerTotal,
        baseShareTotal = baseShareTotal,
        finalShareTotal = finalShareTotal,
        remainingPayerAmount = totalAmount - payerTotal,
        remainingBaseShareAmount = baseAmountPreview - baseShareTotal,
        isPayerOverflow = payerTotal > totalAmount,
        isBaseShareOverflow = baseShareTotal > baseAmountPreview,
        taxAmountPreview = taxAmountPreview,
        baseAmountPreview = baseAmountPreview,
        serviceChargeTotalPreview = serviceChargeTotalPreview,
        hasInvalidServiceCharges = hasInvalidServiceCharges,
        hasInvalidTaxPercent = hasInvalidTaxPercent,
        memberBreakdowns = memberBreakdowns,
    )
}

fun splitAmountDeterministically(total: Int, memberIds: List<String>): Map<String, Int> {
    if (total <= 0 || memberIds.isEmpty()) return memberIds.associateWith { 0 }
    val sorted = memberIds.sorted()
    val base = total / sorted.size
    val extraCount = total % sorted.size
    return sorted.mapIndexed { index, memberId ->
        memberId to (base + if (index < extraCount) 1 else 0)
    }.toMap()
}

fun distributeProportionally(total: Int, weights: Map<String, Int>): Map<String, Int> {
    if (total <= 0 || weights.isEmpty()) return weights.keys.associateWith { 0 }
    val normalized = weights.filterValues { it > 0 }
    if (normalized.isEmpty()) return weights.keys.associateWith { 0 }
    val weightTotal = normalized.values.sum()
    val provisional = normalized.map { (memberId, weight) ->
        val scaled = weight.toDouble() * total / weightTotal
        DistributedAmount(
            memberId = memberId,
            baseAmount = scaled.toInt(),
            remainder = scaled - scaled.toInt(),
        )
    }
    var remaining = total - provisional.sumOf { it.baseAmount }
    val bonuses = provisional
        .sortedWith(compareByDescending<DistributedAmount> { it.remainder }.thenBy { it.memberId })
        .associate { item ->
            val bonus = if (remaining > 0) {
                remaining -= 1
                1
            } else {
                0
            }
            item.memberId to bonus
        }
    return weights.keys.sorted().associateWith { memberId ->
        val item = provisional.firstOrNull { it.memberId == memberId }
        if (item == null) 0 else item.baseAmount + (bonuses[item.memberId] ?: 0)
    }
}

private data class ServiceChargeAllocation(
    val charge: ServiceChargeDraftUi,
    val amount: Int,
    val selectedMemberIds: List<String>,
    val perMemberAmounts: Map<String, Int>,
    val isValid: Boolean,
)

private data class DistributedAmount(
    val memberId: String,
    val baseAmount: Int,
    val remainder: Double,
)
