package com.encer.offlinesplitwise.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.encer.offlinesplitwise.data.AppContainer
import com.encer.offlinesplitwise.domain.Expense
import com.encer.offlinesplitwise.domain.ExpenseDraft
import com.encer.offlinesplitwise.domain.ExpenseShare
import com.encer.offlinesplitwise.domain.Group
import com.encer.offlinesplitwise.domain.GroupSummary
import com.encer.offlinesplitwise.domain.Member
import com.encer.offlinesplitwise.domain.MemberBalance
import com.encer.offlinesplitwise.domain.Settlement
import com.encer.offlinesplitwise.domain.SimplifiedTransfer
import com.encer.offlinesplitwise.domain.SplitType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val errorMessage: String? = null,
)

class GroupsViewModel(
    private val appContainer: AppContainer
) : ViewModel() {
    val uiState: StateFlow<GroupsUiState> = appContainer.groupRepository.observeGroups()
        .combine(MutableStateFlow<String?>(null)) { groups, error ->
            GroupsUiState(groups = groups, errorMessage = error)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupsUiState())

    fun createGroup(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            appContainer.groupRepository.createGroup(clean)
        }
    }

    fun updateGroup(group: Group) {
        val clean = group.name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            appContainer.groupRepository.updateGroup(group.copy(name = clean))
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            appContainer.groupRepository.deleteGroup(groupId)
        }
    }
}

data class GroupDashboardUiState(
    val group: Group? = null,
    val summary: GroupSummary = GroupSummary(0, 0, 0, 0),
    val members: List<Member> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
)

class GroupDashboardViewModel(
    private val groupId: Long,
    private val appContainer: AppContainer
) : ViewModel() {
    val uiState: StateFlow<GroupDashboardUiState> = combine(
        appContainer.groupRepository.observeGroups(),
        appContainer.observeGroupSummaryUseCase(groupId),
        appContainer.memberRepository.observeMembers(groupId),
        appContainer.expenseRepository.observeExpenses(groupId),
        appContainer.settlementRepository.observeSettlements(groupId)
    ) { groups, summary, members, expenses, settlements ->
        GroupDashboardUiState(
            group = groups.firstOrNull { it.id == groupId },
            summary = summary,
            members = members,
            expenses = expenses,
            settlements = settlements
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupDashboardUiState())

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch { appContainer.expenseRepository.deleteExpense(expenseId) }
    }

    fun deleteSettlement(settlementId: Long) {
        viewModelScope.launch { appContainer.settlementRepository.deleteSettlement(settlementId) }
    }
}

data class MembersUiState(
    val group: Group? = null,
    val members: List<Member> = emptyList(),
)

class MembersViewModel(
    private val groupId: Long,
    private val appContainer: AppContainer
) : ViewModel() {
    val uiState: StateFlow<MembersUiState> = combine(
        appContainer.groupRepository.observeGroups(),
        appContainer.memberRepository.observeMembers(groupId)
    ) { groups, members ->
        MembersUiState(group = groups.firstOrNull { it.id == groupId }, members = members)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MembersUiState())

    fun addMember(name: String) {
        if (name.trim().isEmpty()) return
        viewModelScope.launch { appContainer.memberRepository.addMember(groupId, name) }
    }

    fun updateMember(member: Member) {
        if (member.name.trim().isEmpty()) return
        viewModelScope.launch { appContainer.memberRepository.updateMember(member.copy(name = member.name.trim())) }
    }

    fun deleteMember(memberId: Long) {
        viewModelScope.launch { appContainer.memberRepository.deleteMember(memberId) }
    }
}

data class MemberDraftUi(
    val memberId: Long,
    val name: String,
    val includedInSplit: Boolean = true,
    val payerAmountInput: String = "",
    val exactShareInput: String = "",
)

data class ExpenseEditorUiState(
    val isEdit: Boolean = false,
    val title: String = "",
    val note: String = "",
    val totalAmountInput: String = "",
    val splitType: SplitType = SplitType.EQUAL,
    val members: List<MemberDraftUi> = emptyList(),
    val message: String? = null,
    val savedExpenseId: Long? = null,
    val loaded: Boolean = false,
)

class ExpenseEditorViewModel(
    private val groupId: Long,
    private val expenseId: Long?,
    private val appContainer: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseEditorUiState(isEdit = expenseId != null))
    val uiState: StateFlow<ExpenseEditorUiState> = _uiState

    private var membersLoaded = false
    private var expenseLoaded = false

    init {
        viewModelScope.launch {
            appContainer.memberRepository.observeMembers(groupId).collect { members ->
                val previous = _uiState.value.members.associateBy { it.memberId }
                _uiState.update { current ->
                    current.copy(
                        members = members.map { member ->
                            previous[member.id] ?: MemberDraftUi(memberId = member.id, name = member.name)
                        },
                        loaded = membersLoaded || members.isNotEmpty() || expenseId == null
                    )
                }
                membersLoaded = true
                if (expenseId == null && _uiState.value.members.all { !it.includedInSplit }) {
                    _uiState.update { state ->
                        state.copy(members = state.members.map { it.copy(includedInSplit = true) })
                    }
                }
            }
        }
        if (expenseId != null) {
            viewModelScope.launch {
                val expense = appContainer.expenseRepository.getExpense(expenseId) ?: return@launch
                val payerMap = expense.payers.associateBy { it.memberId }
                val shareMap = expense.shares.associateBy { it.memberId }
                _uiState.update { current ->
                    current.copy(
                        title = expense.title,
                        note = expense.note,
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
                expenseLoaded = true
            }
        } else {
            expenseLoaded = true
            _uiState.update { it.copy(loaded = true) }
        }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun updateNote(value: String) = _uiState.update { it.copy(note = value) }
    fun updateTotal(value: String) = _uiState.update { it.copy(totalAmountInput = value.filter { ch -> ch.isDigit() || ch in '۰'..'۹' }) }
    fun updateSplitType(splitType: SplitType) = _uiState.update { it.copy(splitType = splitType) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }
    fun markSavedConsumed() = _uiState.update { it.copy(savedExpenseId = null) }

    fun toggleIncluded(memberId: Long, included: Boolean) {
        _uiState.update { state ->
            state.copy(members = state.members.map { member ->
                if (member.memberId == memberId) {
                    member.copy(includedInSplit = included)
                } else {
                    member
                }
            })
        }
    }

    fun updatePayer(memberId: Long, value: String) {
        _uiState.update { state ->
            state.copy(members = state.members.map { member ->
                if (member.memberId == memberId) member.copy(payerAmountInput = value.filter { it.isDigit() || it in '۰'..'۹' }) else member
            })
        }
    }

    fun updateExactShare(memberId: Long, value: String) {
        _uiState.update { state ->
            state.copy(members = state.members.map { member ->
                if (member.memberId == memberId) member.copy(exactShareInput = value.filter { it.isDigit() || it in '۰'..'۹' }) else member
            })
        }
    }

    fun save() {
        val state = _uiState.value
        val totalAmount = parseAmountInput(state.totalAmountInput)
        val payers = state.members.mapNotNull { member ->
            val amount = parseAmountInput(member.payerAmountInput)
            if (amount > 0) ExpenseShare(member.memberId, amount) else null
        }
        val rawShares = state.members.filter { it.includedInSplit }.map { member ->
            ExpenseShare(
                memberId = member.memberId,
                amount = if (state.splitType == SplitType.EXACT) parseAmountInput(member.exactShareInput) else 0
            )
        }
        val validation = appContainer.validateExpenseInputUseCase(
            totalAmount = totalAmount,
            splitType = state.splitType,
            payers = payers,
            shares = rawShares
        )
        if (state.title.trim().isEmpty()) {
            _uiState.update { it.copy(message = "عنوان خرج را وارد کنید.") }
            return
        }
        if (!validation.isValid) {
            _uiState.update { it.copy(message = validation.message) }
            return
        }

        viewModelScope.launch {
            val newId = appContainer.expenseRepository.upsertExpense(
                draft = ExpenseDraft(
                    groupId = groupId,
                    title = state.title,
                    note = state.note,
                    totalAmount = totalAmount,
                    splitType = state.splitType,
                    payers = payers,
                    shares = validation.normalizedShares
                ),
                existingId = expenseId
            )
            _uiState.update { it.copy(savedExpenseId = newId, message = "خرج ذخیره شد.") }
        }
    }

    fun delete() {
        val currentId = expenseId ?: return
        viewModelScope.launch {
            appContainer.expenseRepository.deleteExpense(currentId)
            _uiState.update { it.copy(savedExpenseId = -1L) }
        }
    }
}

data class SettlementEditorUiState(
    val isEdit: Boolean = false,
    val members: List<Member> = emptyList(),
    val fromMemberId: Long? = null,
    val toMemberId: Long? = null,
    val amountInput: String = "",
    val note: String = "",
    val message: String? = null,
    val saved: Boolean = false,
)

class SettlementEditorViewModel(
    private val groupId: Long,
    private val settlementId: Long?,
    private val appContainer: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettlementEditorUiState(isEdit = settlementId != null))
    val uiState: StateFlow<SettlementEditorUiState> = _uiState

    init {
        viewModelScope.launch {
            appContainer.memberRepository.observeMembers(groupId).collect { members ->
                _uiState.update { current ->
                    current.copy(
                        members = members,
                        fromMemberId = current.fromMemberId ?: members.firstOrNull()?.id,
                        toMemberId = current.toMemberId ?: members.drop(1).firstOrNull()?.id ?: members.firstOrNull()?.id
                    )
                }
            }
        }
        if (settlementId != null) {
            viewModelScope.launch {
                val settlement = appContainer.settlementRepository.getSettlement(settlementId) ?: return@launch
                _uiState.update {
                    it.copy(
                        fromMemberId = settlement.fromMemberId,
                        toMemberId = settlement.toMemberId,
                        amountInput = settlement.amount.toString(),
                        note = settlement.note
                    )
                }
            }
        }
    }

    fun setFromMember(memberId: Long) = _uiState.update { it.copy(fromMemberId = memberId) }
    fun setToMember(memberId: Long) = _uiState.update { it.copy(toMemberId = memberId) }
    fun setAmount(input: String) = _uiState.update { it.copy(amountInput = input.filter { ch -> ch.isDigit() || ch in '۰'..'۹' }) }
    fun setNote(note: String) = _uiState.update { it.copy(note = note) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun save() {
        val state = _uiState.value
        val amount = parseAmountInput(state.amountInput)
        val fromMemberId = state.fromMemberId
        val toMemberId = state.toMemberId
        when {
            fromMemberId == null || toMemberId == null -> _uiState.update { it.copy(message = "دو نفر را انتخاب کنید.") }
            fromMemberId == toMemberId -> _uiState.update { it.copy(message = "پرداخت‌کننده و دریافت‌کننده باید متفاوت باشند.") }
            amount <= 0 -> _uiState.update { it.copy(message = "مبلغ تسویه باید بیشتر از صفر باشد.") }
            else -> {
                viewModelScope.launch {
                    appContainer.settlementRepository.upsertSettlement(
                        Settlement(
                            id = settlementId ?: 0,
                            groupId = groupId,
                            fromMemberId = fromMemberId,
                            toMemberId = toMemberId,
                            amount = amount,
                            note = state.note.trim(),
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    _uiState.update { it.copy(saved = true, message = "تسویه ذخیره شد.") }
                }
            }
        }
    }

    fun delete() {
        val currentId = settlementId ?: return
        viewModelScope.launch {
            appContainer.settlementRepository.deleteSettlement(currentId)
            _uiState.update { it.copy(saved = true) }
        }
    }
}

data class BalancesUiState(
    val group: Group? = null,
    val balances: List<MemberBalance> = emptyList(),
    val simplifiedTransfers: List<SimplifiedTransfer> = emptyList(),
)

class BalancesViewModel(
    private val groupId: Long,
    private val appContainer: AppContainer
) : ViewModel() {
    val uiState: StateFlow<BalancesUiState> = combine(
        appContainer.groupRepository.observeGroups(),
        appContainer.observeGroupBalancesUseCase(groupId)
    ) { groups, balances ->
        BalancesUiState(
            group = groups.firstOrNull { it.id == groupId },
            balances = balances,
            simplifiedTransfers = appContainer.simplifyDebtsUseCase(balances)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BalancesUiState())
}

fun groupsViewModelFactory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { GroupsViewModel(appContainer) }
}

fun groupDashboardFactory(groupId: Long, appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { GroupDashboardViewModel(groupId, appContainer) }
}

fun membersViewModelFactory(groupId: Long, appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { MembersViewModel(groupId, appContainer) }
}

fun expenseEditorFactory(groupId: Long, expenseId: Long?, appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { ExpenseEditorViewModel(groupId, expenseId, appContainer) }
}

fun settlementEditorFactory(groupId: Long, settlementId: Long?, appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { SettlementEditorViewModel(groupId, settlementId, appContainer) }
}

fun balancesFactory(groupId: Long, appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { BalancesViewModel(groupId, appContainer) }
}
