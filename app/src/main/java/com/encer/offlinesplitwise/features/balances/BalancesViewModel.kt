package com.encer.offlinesplitwise.features.balances

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encer.offlinesplitwise.domain.repository.GroupRepository
import com.encer.offlinesplitwise.domain.usecase.ObserveGroupBalancesParams
import com.encer.offlinesplitwise.domain.usecase.ObserveGroupBalancesUseCase
import com.encer.offlinesplitwise.domain.usecase.SimplifyDebtsParams
import com.encer.offlinesplitwise.domain.usecase.SimplifyDebtsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BalancesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    observeGroupBalancesUseCase: ObserveGroupBalancesUseCase,
    simplifyDebtsUseCase: SimplifyDebtsUseCase
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    val uiState: StateFlow<BalancesUiState> = combine(
        groupRepository.observeGroups(),
        observeGroupBalancesUseCase(ObserveGroupBalancesParams(groupId))
    ) { groups, balances ->
        BalancesUiState(
            group = groups.firstOrNull { it.id == groupId },
            balances = balances,
            simplifiedTransfers = simplifyDebtsUseCase(SimplifyDebtsParams(balances))
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BalancesUiState())
}
