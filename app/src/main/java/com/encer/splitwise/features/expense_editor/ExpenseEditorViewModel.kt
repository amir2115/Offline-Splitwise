package com.encer.splitwise.features.expense_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.splitwise.core.common.MessageKey
import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.ExpenseDraft
import com.encer.splitwise.domain.model.ExpenseShare
import com.encer.splitwise.domain.model.SplitType
import com.encer.splitwise.domain.repository.ExpenseRepository
import com.encer.splitwise.domain.repository.MemberRepository
import com.encer.splitwise.domain.usecase.ValidateExpenseInputParams
import com.encer.splitwise.domain.usecase.ValidateExpenseInputUseCase
import com.encer.splitwise.domain.usecase.canCreateTransaction
import com.encer.splitwise.ui.formatting.normalizeAmountDigits
import com.encer.splitwise.ui.formatting.parseAmountInput
import com.encer.splitwise.ui.formatting.parseAmountInputOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExpenseEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val memberRepository: MemberRepository,
    private val expenseRepository: ExpenseRepository,
    private val validateExpenseInputUseCase: ValidateExpenseInputUseCase,
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val expenseId: String? = savedStateHandle["expenseId"]

    private val _uiState = MutableStateFlow(ExpenseEditorUiState(isEdit = expenseId != null))
    val uiState: StateFlow<ExpenseEditorUiState> = _uiState
    private var pendingExpense: Expense? = null

    init {
        viewModelScope.launch { memberRepository.ensureSelfMember(groupId) }
        viewModelScope.launch {
            memberRepository.observeMembers(groupId).collect { members ->
                val previous = _uiState.value.members.associateBy { it.memberId }
                val payerMap = pendingExpense?.payers?.associateBy { it.memberId }.orEmpty()
                val shareMap = pendingExpense?.shares?.associateBy { it.memberId }.orEmpty()
                _uiState.update { current ->
                    recompute(
                        current.copy(
                            members = members.map { member ->
                                previous[member.id]?.copy(username = member.username) ?: MemberDraftUi(
                                    memberId = member.id,
                                    username = member.username,
                                    includedInSplit = shareMap.containsKey(member.id) || expenseId == null,
                                    payerAmountInput = payerMap[member.id]?.amount?.takeIf { it > 0 }?.toString().orEmpty(),
                                    exactShareInput = shareMap[member.id]?.amount?.takeIf { it > 0 }?.toString().orEmpty(),
                                )
                            },
                            loaded = true,
                            canCreateTransaction = canCreateTransaction(memberCount = members.size, isEdit = expenseId != null),
                        )
                    )
                }
                if (expenseId == null && _uiState.value.members.all { !it.includedInSplit }) {
                    _uiState.update { state -> recompute(state.copy(members = state.members.map { it.copy(includedInSplit = true) })) }
                }
            }
        }
        if (expenseId != null) {
            viewModelScope.launch {
                val expense = expenseRepository.getExpense(expenseId) ?: return@launch
                pendingExpense = expense
                val payerMap = expense.payers.associateBy { it.memberId }
                val shareMap = expense.shares.associateBy { it.memberId }
                _uiState.update { current ->
                    recompute(
                        current.copy(
                            title = expense.title,
                            note = expense.note.orEmpty(),
                            totalAmountInput = expense.totalAmount.toString(),
                            splitType = expense.splitType,
                            // Tax/service metadata are not persisted yet, so edit starts without them.
                            taxEnabled = false,
                            taxPercentInput = "",
                            serviceCharges = emptyList(),
                            members = current.members.map { member ->
                                member.copy(
                                    includedInSplit = shareMap.containsKey(member.memberId),
                                    payerAmountInput = payerMap[member.memberId]?.amount?.takeIf { it > 0 }?.toString().orEmpty(),
                                    exactShareInput = shareMap[member.memberId]?.amount?.takeIf { it > 0 }?.toString().orEmpty(),
                                )
                            },
                            loaded = true,
                        )
                    )
                }
            }
        } else {
            _uiState.update { recompute(it.copy(loaded = true)) }
        }
    }

    fun updateTitle(value: String) = _uiState.update { recompute(it.copy(title = value, message = null)) }
    fun updateNote(value: String) = _uiState.update { recompute(it.copy(note = value, message = null)) }
    fun updateTotal(value: String) = _uiState.update { recompute(it.copy(totalAmountInput = sanitizeAmount(value), message = null)) }
    fun updateSplitType(splitType: SplitType) = _uiState.update { recompute(it.copy(splitType = splitType, message = null)) }
    fun clearMessage() = _uiState.update { recompute(it.copy(message = null)) }
    fun markSavedConsumed() = _uiState.update { recompute(it.copy(savedExpenseId = null)) }

    fun updateTaxEnabled(enabled: Boolean) = _uiState.update {
        recompute(
            it.copy(
                taxEnabled = enabled,
                taxPercentInput = if (enabled) it.taxPercentInput else "",
                message = null,
            )
        )
    }

    fun updateTaxPercent(value: String) = _uiState.update {
        recompute(it.copy(taxPercentInput = sanitizeAmount(value), message = null))
    }

    fun addServiceCharge() = _uiState.update {
        recompute(
            it.copy(
                serviceCharges = it.serviceCharges + ServiceChargeDraftUi(id = UUID.randomUUID().toString()),
                message = null,
            )
        )
    }

    fun removeServiceCharge(id: String) = _uiState.update {
        recompute(it.copy(serviceCharges = it.serviceCharges.filterNot { charge -> charge.id == id }, message = null))
    }

    fun updateServiceChargeTitle(id: String, value: String) = _uiState.update {
        recompute(
            it.copy(
                serviceCharges = it.serviceCharges.map { charge ->
                    if (charge.id == id) charge.copy(title = value) else charge
                },
                message = null,
            )
        )
    }

    fun updateServiceChargeAmount(id: String, value: String) = _uiState.update {
        recompute(
            it.copy(
                serviceCharges = it.serviceCharges.map { charge ->
                    if (charge.id == id) charge.copy(amountInput = sanitizeAmount(value)) else charge
                },
                message = null,
            )
        )
    }

    fun toggleServiceChargeMember(id: String, memberId: String) = _uiState.update {
        recompute(
            it.copy(
                serviceCharges = it.serviceCharges.map { charge ->
                    if (charge.id != id) {
                        charge
                    } else {
                        charge.copy(
                            selectedMemberIds = charge.selectedMemberIds.toMutableSet().apply {
                                if (!add(memberId)) remove(memberId)
                            }
                        )
                    }
                },
                message = null,
            )
        )
    }

    fun toggleIncluded(memberId: String, included: Boolean) = _uiState.update { state ->
        recompute(state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(includedInSplit = included) else it }))
    }

    fun updatePayer(memberId: String, value: String) = _uiState.update { state ->
        recompute(
            state.copy(
                members = state.members.map { if (it.memberId == memberId) it.copy(payerAmountInput = sanitizeAmount(value)) else it },
                message = null,
            )
        )
    }

    fun updateExactShare(memberId: String, value: String) = _uiState.update { state ->
        recompute(
            state.copy(
                members = state.members.map { if (it.memberId == memberId) it.copy(exactShareInput = sanitizeAmount(value)) else it },
                message = null,
            )
        )
    }

    fun applySuggestedPayer(memberId: String) = _uiState.update { state ->
        val target = state.members.firstOrNull { it.memberId == memberId }?.suggestedRemainingPayer ?: return@update state
        recompute(state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(payerAmountInput = target.toString()) else it }, message = null))
    }

    fun applySuggestedShare(memberId: String) = _uiState.update { state ->
        val target = state.members.firstOrNull { it.memberId == memberId }?.suggestedRemainingShare ?: return@update state
        recompute(state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(exactShareInput = target.toString()) else it }, message = null))
    }

    fun applyEqualRemainingShare(memberId: String) = _uiState.update { state ->
        val target = state.members.firstOrNull { it.memberId == memberId }?.equalRemainingShare ?: return@update state
        recompute(state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(exactShareInput = target.toString()) else it }, message = null))
    }

    fun assignFullAmountToPayer(memberId: String) {
        val state = _uiState.value
        if (!state.canCreateTransaction) return
        val normalizedTotal = normalizeAmountDigits(state.totalAmountInput)
        val parsedTotal = parseAmountInputOrNull(state.totalAmountInput)
        val message = when {
            normalizedTotal.isBlank() -> MessageKey.EXPENSE_TOTAL_POSITIVE
            parsedTotal == null -> MessageKey.EXPENSE_AMOUNT_TOO_LARGE
            parsedTotal <= 0 -> MessageKey.EXPENSE_TOTAL_POSITIVE
            else -> null
        }
        if (message != null) {
            _uiState.update { recompute(it.copy(message = message)) }
            return
        }
        _uiState.update { current ->
            recompute(
                current.copy(
                    members = current.members.map { member ->
                        member.copy(payerAmountInput = if (member.memberId == memberId) normalizedTotal else "")
                    },
                    message = null,
                )
            )
        }
    }

    fun clearPayerAmount(memberId: String) = _uiState.update { state ->
        if (!state.canCreateTransaction) state else recompute(
            state.copy(
                members = state.members.map { if (it.memberId == memberId) it.copy(payerAmountInput = "") else it },
                message = null,
            )
        )
    }

    fun save() {
        val state = _uiState.value
        if (!state.canCreateTransaction) return

        val amountInputs = buildList {
            add(state.totalAmountInput)
            if (state.taxEnabled) add(state.taxPercentInput)
            state.serviceCharges.forEach { charge -> add(charge.amountInput) }
            state.members.forEach { member ->
                add(member.payerAmountInput)
                if (state.splitType == SplitType.EXACT) add(member.exactShareInput)
            }
        }
        if (amountInputs.any { it.isNotBlank() && parseAmountInputOrNull(it) == null }) {
            _uiState.update { recompute(it.copy(message = MessageKey.EXPENSE_AMOUNT_TOO_LARGE)) }
            return
        }
        if (state.hasInvalidTaxPercent) {
            _uiState.update { recompute(it.copy(message = MessageKey.EXPENSE_TAX_PERCENT_INVALID)) }
            return
        }
        if (state.hasInvalidServiceCharges) {
            _uiState.update { recompute(it.copy(message = MessageKey.EXPENSE_SERVICE_CHARGE_INVALID)) }
            return
        }
        if (state.title.trim().isEmpty()) {
            _uiState.update { recompute(it.copy(message = MessageKey.EXPENSE_TITLE_REQUIRED)) }
            return
        }

        val totalAmount = parseAmountInput(state.totalAmountInput)
        val payers = state.members.mapNotNull { member ->
            val amount = parseAmountInput(member.payerAmountInput)
            if (amount > 0) ExpenseShare(member.memberId, amount) else null
        }
        val finalShares = state.members
            .filter { it.includedInSplit }
            .map { member -> ExpenseShare(member.memberId, member.finalSharePreview) }
            .filter { it.amount > 0 }

        if (state.remainingPayerAmount != 0 || state.isPayerOverflow) {
            _uiState.update { recompute(it.copy(message = MessageKey.EXPENSE_PAYER_TOTAL_MISMATCH)) }
            return
        }
        if (state.remainingShareAmount != 0 || state.isShareOverflow) {
            _uiState.update { recompute(it.copy(message = MessageKey.EXPENSE_SHARE_TOTAL_MISMATCH)) }
            return
        }

        val persistedSplitType = if (state.taxEnabled || state.serviceCharges.isNotEmpty()) SplitType.EXACT else state.splitType
        val validation = validateExpenseInputUseCase(
            ValidateExpenseInputParams(
                totalAmount = totalAmount,
                splitType = persistedSplitType,
                payers = payers,
                shares = finalShares,
            )
        )
        if (!validation.isValid) {
            _uiState.update { recompute(it.copy(message = validation.messageKey)) }
            return
        }

        viewModelScope.launch {
            val newId = expenseRepository.upsertExpense(
                draft = ExpenseDraft(
                    groupId = groupId,
                    title = state.title,
                    note = state.note,
                    totalAmount = totalAmount,
                    splitType = persistedSplitType,
                    payers = payers,
                    shares = validation.normalizedShares,
                ),
                existingId = expenseId
            )
            _uiState.update { recompute(it.copy(savedExpenseId = newId, message = MessageKey.EXPENSE_SAVED)) }
        }
    }

    fun delete() {
        val currentId = expenseId ?: return
        viewModelScope.launch {
            expenseRepository.deleteExpense(currentId)
            _uiState.update { recompute(it.copy(savedExpenseId = currentId)) }
        }
    }

    private fun recompute(state: ExpenseEditorUiState): ExpenseEditorUiState {
        val totalAmount = parseAmountInput(state.totalAmountInput)
        val computed = computeExpenseEditorState(
            totalAmount = totalAmount,
            splitType = state.splitType,
            members = state.members,
            taxEnabled = state.taxEnabled,
            taxPercentInput = state.taxPercentInput,
            serviceCharges = state.serviceCharges,
        )
        val blankShareMembers = state.members.filter {
            it.includedInSplit && it.exactShareInput.isBlank()
        }.sortedBy { it.memberId }

        val members = state.members.map { member ->
            val payerWithoutCurrent = computed.payerTotal - parseAmountInput(member.payerAmountInput)
            val remainingPayer = totalAmount - payerWithoutCurrent
            val baseShareTarget = computed.baseAmountPreview
            val baseShareWithoutCurrent = state.members
                .filter { it.includedInSplit && it.memberId != member.memberId }
                .sumOf { parseAmountInput(it.exactShareInput) }
            val remainingBaseShare = baseShareTarget - baseShareWithoutCurrent
            val equalRemainingShare = if (
                state.splitType == SplitType.EXACT &&
                member.includedInSplit &&
                member.exactShareInput.isBlank() &&
                computed.remainingBaseShareAmount > 0 &&
                blankShareMembers.isNotEmpty()
            ) {
                val base = computed.remainingBaseShareAmount / blankShareMembers.size
                val extraCount = computed.remainingBaseShareAmount % blankShareMembers.size
                val index = blankShareMembers.indexOfFirst { it.memberId == member.memberId }
                if (index >= 0) base + if (index < extraCount) 1 else 0 else null
            } else {
                null
            }
            val breakdown = computed.memberBreakdowns[member.memberId] ?: MemberCostBreakdown()
            member.copy(
                suggestedRemainingPayer = remainingPayer.takeIf { totalAmount > 0 && it > 0 },
                suggestedRemainingShare = remainingBaseShare.takeIf {
                    state.splitType == SplitType.EXACT && member.includedInSplit && baseShareTarget > 0 && it > 0
                },
                equalRemainingShare = equalRemainingShare,
                baseSharePreview = breakdown.baseShare,
                taxSharePreview = breakdown.taxShare,
                serviceChargeSharePreview = breakdown.serviceChargeShare,
                finalSharePreview = breakdown.finalShare,
            )
        }
        return state.copy(
            members = members,
            payerTotal = computed.payerTotal,
            shareTotal = computed.baseShareTotal,
            remainingPayerAmount = computed.remainingPayerAmount,
            remainingShareAmount = computed.remainingBaseShareAmount,
            isPayerOverflow = computed.isPayerOverflow,
            isShareOverflow = computed.isBaseShareOverflow,
            baseAmountPreview = computed.baseAmountPreview,
            taxAmountPreview = computed.taxAmountPreview,
            serviceChargeTotalPreview = computed.serviceChargeTotalPreview,
            finalShareTotal = computed.finalShareTotal,
            hasInvalidServiceCharges = computed.hasInvalidServiceCharges,
            hasInvalidTaxPercent = computed.hasInvalidTaxPercent,
            isAmountsReady = computed.remainingPayerAmount == 0 &&
                !computed.isPayerOverflow &&
                computed.remainingBaseShareAmount == 0 &&
                !computed.isBaseShareOverflow &&
                !computed.hasInvalidServiceCharges &&
                !computed.hasInvalidTaxPercent &&
                computed.finalShareTotal == totalAmount,
        )
    }

    private fun sanitizeAmount(value: String): String = value.filter { ch -> ch.isDigit() || ch in '۰'..'۹' }
}
