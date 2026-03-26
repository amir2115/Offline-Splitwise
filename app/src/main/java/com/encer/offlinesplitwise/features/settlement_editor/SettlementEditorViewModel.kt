package com.encer.offlinesplitwise.features.settlement_editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.core.common.MessageKey
import com.encer.offlinesplitwise.ui.formatting.parseAmountInputOrNull
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.domain.model.MemberBalance
import com.encer.offlinesplitwise.domain.model.Settlement
import com.encer.offlinesplitwise.domain.repository.MemberRepository
import com.encer.offlinesplitwise.domain.repository.SettlementRepository
import com.encer.offlinesplitwise.domain.usecase.ObserveGroupBalancesParams
import com.encer.offlinesplitwise.domain.usecase.ObserveGroupBalancesUseCase
import com.encer.offlinesplitwise.domain.usecase.SimplifyDebtsParams
import com.encer.offlinesplitwise.domain.usecase.SimplifyDebtsUseCase
import com.encer.offlinesplitwise.domain.usecase.canCreateTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettlementEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val memberRepository: MemberRepository,
    private val settlementRepository: SettlementRepository,
    private val observeGroupBalancesUseCase: ObserveGroupBalancesUseCase,
    private val simplifyDebtsUseCase: SimplifyDebtsUseCase
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val settlementId: String? = savedStateHandle["settlementId"]
    private val initialFromMemberId: String? = savedStateHandle["fromMemberId"]
    private val initialToMemberId: String? = savedStateHandle["toMemberId"]
    private val initialAmountInput: String? = savedStateHandle["amount"]

    private val _uiState = MutableStateFlow(
        SettlementEditorUiState(
            isEdit = settlementId != null,
            fromMemberId = initialFromMemberId,
            toMemberId = initialToMemberId,
            amountInput = initialAmountInput.orEmpty()
        )
    )
    val uiState: StateFlow<SettlementEditorUiState> = _uiState
    private var existingCreatedAt: Long? = null
    private var latestBalances: List<MemberBalance> = emptyList()
    private var userAdjustedSelection =
        settlementId != null || initialFromMemberId != null || initialToMemberId != null || !initialAmountInput.isNullOrBlank()

    init {
        viewModelScope.launch { memberRepository.ensureSelfMember(groupId) }
        viewModelScope.launch {
            memberRepository.observeMembers(groupId).collect { members ->
                _uiState.update { current ->
                    val preferredPair = preferredSettlementPair(members, latestBalances, current.fromMemberId, current.toMemberId, userAdjustedSelection)
                    current.copy(
                        members = members,
                        fromMemberId = preferredPair.first,
                        toMemberId = preferredPair.second,
                        suggestedAmount = computeSuggestedAmount(preferredPair.first, preferredPair.second, latestBalances),
                        canCreateTransaction = canCreateTransaction(memberCount = members.size, isEdit = settlementId != null),
                    )
                }
            }
        }
        viewModelScope.launch {
            observeGroupBalancesUseCase(ObserveGroupBalancesParams(groupId)).collect { balances ->
                latestBalances = balances
                _uiState.update { current ->
                    val preferredPair = preferredSettlementPair(current.members, balances, current.fromMemberId, current.toMemberId, userAdjustedSelection || current.amountInput.isNotBlank())
                    current.copy(
                        fromMemberId = preferredPair.first,
                        toMemberId = preferredPair.second,
                        suggestedAmount = computeSuggestedAmount(preferredPair.first, preferredPair.second, balances)
                    )
                }
            }
        }
        if (settlementId != null) {
            viewModelScope.launch {
                val settlement = settlementRepository.getSettlement(settlementId) ?: return@launch
                existingCreatedAt = settlement.createdAt
                _uiState.update { it.copy(fromMemberId = settlement.fromMemberId, toMemberId = settlement.toMemberId, amountInput = settlement.amount.toString(), note = settlement.note.orEmpty()) }
            }
        }
    }

    fun setFromMember(memberId: String) = _uiState.update { current ->
        if (!current.canCreateTransaction) return@update current
        userAdjustedSelection = true
        val nextToMemberId = if (current.toMemberId == memberId) current.fromMemberId else current.toMemberId
        current.copy(fromMemberId = memberId, toMemberId = nextToMemberId, suggestedAmount = computeSuggestedAmount(memberId, nextToMemberId, latestBalances), message = null)
    }

    fun setToMember(memberId: String) = _uiState.update { current ->
        if (!current.canCreateTransaction) return@update current
        userAdjustedSelection = true
        val nextFromMemberId = if (current.fromMemberId == memberId) current.toMemberId else current.fromMemberId
        current.copy(fromMemberId = nextFromMemberId, toMemberId = memberId, suggestedAmount = computeSuggestedAmount(nextFromMemberId, memberId, latestBalances), message = null)
    }

    fun setAmount(input: String) = _uiState.update { current ->
        if (!current.canCreateTransaction) current else current.copy(amountInput = input.filter { ch -> ch.isDigit() || ch in '۰'..'۹' }, message = null)
    }
    fun setSuggestedAmount() = _uiState.update { current ->
        if (!current.canCreateTransaction) current else current.suggestedAmount?.let { current.copy(amountInput = it.toString(), message = null) } ?: current
    }
    fun setNote(note: String) = _uiState.update { current ->
        if (!current.canCreateTransaction) current else current.copy(note = note, message = null)
    }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun save() {
        val state = _uiState.value
        if (!state.canCreateTransaction) return
        val amount = parseAmountInputOrNull(state.amountInput)
        val resolvedAmount = amount ?: 0
        when {
            state.fromMemberId == null || state.toMemberId == null -> _uiState.update { it.copy(message = MessageKey.SETTLEMENT_SELECT_TWO_MEMBERS) }
            state.fromMemberId == state.toMemberId -> _uiState.update { it.copy(message = MessageKey.SETTLEMENT_MEMBERS_MUST_DIFFER) }
            amount == null && state.amountInput.isNotBlank() -> _uiState.update { it.copy(message = MessageKey.SETTLEMENT_AMOUNT_TOO_LARGE) }
            resolvedAmount <= 0 -> _uiState.update { it.copy(message = MessageKey.SETTLEMENT_AMOUNT_POSITIVE) }
            else -> viewModelScope.launch {
                settlementRepository.upsertSettlement(
                    Settlement(
                        id = settlementId.orEmpty(),
                        groupId = groupId,
                        fromMemberId = state.fromMemberId,
                        toMemberId = state.toMemberId,
                        amount = resolvedAmount,
                        note = state.note.trim(),
                        createdAt = existingCreatedAt ?: System.currentTimeMillis(),
                        updatedAt = existingCreatedAt ?: System.currentTimeMillis()
                    )
                )
                _uiState.update { it.copy(saved = true, message = MessageKey.SETTLEMENT_SAVED) }
            }
        }
    }

    fun delete() {
        val currentId = settlementId ?: return
        viewModelScope.launch {
            settlementRepository.deleteSettlement(currentId)
            _uiState.update { it.copy(saved = true) }
        }
    }

    private fun computeSuggestedAmount(fromMemberId: String?, toMemberId: String?, balances: List<MemberBalance>): Int? {
        if (fromMemberId == null || toMemberId == null || fromMemberId == toMemberId) return null
        simplifyDebtsUseCase(SimplifyDebtsParams(balances)).firstOrNull { it.fromMemberId == fromMemberId && it.toMemberId == toMemberId }?.let { return it.amount }
        val payerBalance = balances.firstOrNull { it.memberId == fromMemberId }?.netBalance ?: return null
        val receiverBalance = balances.firstOrNull { it.memberId == toMemberId }?.netBalance ?: return null
        if (payerBalance >= 0 || receiverBalance <= 0) return null
        return minOf(-payerBalance, receiverBalance).takeIf { it > 0 }
    }

    private fun preferredSettlementPair(
        members: List<Member>,
        balances: List<MemberBalance>,
        currentFromMemberId: String?,
        currentToMemberId: String?,
        keepCurrentSelection: Boolean
    ): Pair<String?, String?> {
        val currentPairIsValid = currentFromMemberId != null && currentToMemberId != null && currentFromMemberId != currentToMemberId &&
            members.any { it.id == currentFromMemberId } && members.any { it.id == currentToMemberId }
        if (keepCurrentSelection && currentPairIsValid) return currentFromMemberId to currentToMemberId
        val suggestedTransfer = simplifyDebtsUseCase(SimplifyDebtsParams(balances)).firstOrNull()
        if (suggestedTransfer != null &&
            members.any { it.id == suggestedTransfer.fromMemberId } &&
            members.any { it.id == suggestedTransfer.toMemberId }
        ) return suggestedTransfer.fromMemberId to suggestedTransfer.toMemberId
        if (currentPairIsValid) return currentFromMemberId to currentToMemberId
        val fallbackFrom = members.firstOrNull()?.id
        val fallbackTo = members.firstOrNull { it.id != fallbackFrom }?.id
        return fallbackFrom to fallbackTo
    }
}
