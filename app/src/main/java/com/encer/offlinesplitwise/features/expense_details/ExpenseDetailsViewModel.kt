package com.encer.offlinesplitwise.features.expense_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.domain.repository.ExpenseRepository
import com.encer.offlinesplitwise.domain.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExpenseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val expenseId: String = checkNotNull(savedStateHandle["expenseId"])

    private val _uiState = MutableStateFlow(ExpenseDetailsUiState())
    val uiState: StateFlow<ExpenseDetailsUiState> = _uiState

    init {
        viewModelScope.launch {
            memberRepository.observeMembers(groupId).collect { members ->
                _uiState.update { it.copy(members = members) }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(expense = expenseRepository.getExpense(expenseId)) }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch { expenseRepository.deleteExpense(expenseId) }
    }
}
