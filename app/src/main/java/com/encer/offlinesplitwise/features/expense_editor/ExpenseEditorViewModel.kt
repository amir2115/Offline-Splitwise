package com.encer.offlinesplitwise.features.expense_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.core.common.MessageKey
import com.encer.offlinesplitwise.ui.formatting.normalizeAmountDigits
import com.encer.offlinesplitwise.ui.formatting.parseAmountInput
import com.encer.offlinesplitwise.ui.formatting.parseAmountInputOrNull
import com.encer.offlinesplitwise.domain.model.Expense
import com.encer.offlinesplitwise.domain.model.ExpenseDraft
import com.encer.offlinesplitwise.domain.model.ExpenseShare
import com.encer.offlinesplitwise.domain.model.SplitType
import com.encer.offlinesplitwise.domain.repository.ExpenseRepository
import com.encer.offlinesplitwise.domain.repository.MemberRepository
import com.encer.offlinesplitwise.domain.usecase.ValidateExpenseInputParams
import com.encer.offlinesplitwise.domain.usecase.ValidateExpenseInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val validateExpenseInputUseCase: ValidateExpenseInputUseCase
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
                    current.copy(
                        members = members.map { member ->
                            previous[member.id]?.copy(username = member.username) ?: MemberDraftUi(
                                memberId = member.id,
                                username = member.username,
                                includedInSplit = shareMap.containsKey(member.id) || expenseId == null,
                                payerAmountInput = payerMap[member.id]?.amount?.takeIf { it > 0 }?.toString().orEmpty(),
                                exactShareInput = shareMap[member.id]?.amount?.takeIf { it > 0 }?.toString().orEmpty()
                            )
                        },
                        loaded = true
                    )
                }
                if (expenseId == null && _uiState.value.members.all { !it.includedInSplit }) {
                    _uiState.update { state -> state.copy(members = state.members.map { it.copy(includedInSplit = true) }) }
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
                    current.copy(
                        title = expense.title,
                        note = expense.note.orEmpty(),
                        totalAmountInput = expense.totalAmount.toString(),
                        splitType = expense.splitType,
                        members = current.members.map { member ->
                            member.copy(
                                includedInSplit = shareMap.containsKey(member.memberId),
                                payerAmountInput = payerMap[member.memberId]?.amount?.takeIf { it > 0 }?.toString().orEmpty(),
                                exactShareInput = shareMap[member.memberId]?.amount?.takeIf { it > 0 }?.toString().orEmpty()
                            )
                        },
                        loaded = true
                    )
                }
            }
        } else {
            _uiState.update { it.copy(loaded = true) }
        }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value, message = null) }
    fun updateNote(value: String) = _uiState.update { it.copy(note = value, message = null) }
    fun updateTotal(value: String) = _uiState.update { it.copy(totalAmountInput = value.filter { ch -> ch.isDigit() || ch in '۰'..'۹' }, message = null) }
    fun updateSplitType(splitType: SplitType) = _uiState.update { it.copy(splitType = splitType, message = null) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }
    fun markSavedConsumed() = _uiState.update { it.copy(savedExpenseId = null) }
    fun toggleIncluded(memberId: String, included: Boolean) = _uiState.update { state ->
        state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(includedInSplit = included) else it })
    }
    fun updatePayer(memberId: String, value: String) = _uiState.update { state ->
        state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(payerAmountInput = value.filter { ch -> ch.isDigit() || ch in '۰'..'۹' }) else it }, message = null)
    }
    fun updateExactShare(memberId: String, value: String) = _uiState.update { state ->
        state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(exactShareInput = value.filter { ch -> ch.isDigit() || ch in '۰'..'۹' }) else it }, message = null)
    }

    fun save() {
        val state = _uiState.value
        val amountInputs = buildList {
            add(state.totalAmountInput)
            state.members.forEach { member ->
                add(member.payerAmountInput)
                if (state.splitType == SplitType.EXACT) add(member.exactShareInput)
            }
        }
        if (amountInputs.any { it.isNotBlank() && parseAmountInputOrNull(it) == null }) {
            _uiState.update { it.copy(message = MessageKey.EXPENSE_AMOUNT_TOO_LARGE) }
            return
        }
        val totalAmount = parseAmountInput(state.totalAmountInput)
        val payers = state.members.mapNotNull { member ->
            val amount = parseAmountInput(member.payerAmountInput)
            if (amount > 0) ExpenseShare(member.memberId, amount) else null
        }
        val rawShares = state.members.filter { it.includedInSplit }.map { member ->
            ExpenseShare(member.memberId, if (state.splitType == SplitType.EXACT) parseAmountInput(member.exactShareInput) else 0)
        }
        val validation = validateExpenseInputUseCase(
            ValidateExpenseInputParams(
                totalAmount = totalAmount,
                splitType = state.splitType,
                payers = payers,
                shares = rawShares,
            )
        )
        if (state.title.trim().isEmpty()) {
            _uiState.update { it.copy(message = MessageKey.EXPENSE_TITLE_REQUIRED) }
            return
        }
        if (!validation.isValid) {
            _uiState.update { it.copy(message = validation.messageKey) }
            return
        }
        viewModelScope.launch {
            val newId = expenseRepository.upsertExpense(
                draft = ExpenseDraft(groupId, state.title, state.note, totalAmount, state.splitType, payers, validation.normalizedShares),
                existingId = expenseId
            )
            _uiState.update { it.copy(savedExpenseId = newId, message = MessageKey.EXPENSE_SAVED) }
        }
    }

    fun assignFullAmountToPayer(memberId: String) {
        val state = _uiState.value
        val normalizedTotal = normalizeAmountDigits(state.totalAmountInput)
        val parsedTotal = parseAmountInputOrNull(state.totalAmountInput)
        val message = when {
            normalizedTotal.isBlank() -> MessageKey.EXPENSE_TOTAL_POSITIVE
            parsedTotal == null -> MessageKey.EXPENSE_AMOUNT_TOO_LARGE
            parsedTotal <= 0 -> MessageKey.EXPENSE_TOTAL_POSITIVE
            else -> null
        }
        if (message != null) {
            _uiState.update { it.copy(message = message) }
            return
        }
        _uiState.update { current ->
            current.copy(members = current.members.map { member -> member.copy(payerAmountInput = if (member.memberId == memberId) normalizedTotal else "") }, message = null)
        }
    }

    fun clearPayerAmount(memberId: String) = _uiState.update { state ->
        state.copy(members = state.members.map { if (it.memberId == memberId) it.copy(payerAmountInput = "") else it }, message = null)
    }

    fun delete() {
        val currentId = expenseId ?: return
        viewModelScope.launch {
            expenseRepository.deleteExpense(currentId)
            _uiState.update { it.copy(savedExpenseId = currentId) }
        }
    }
}
