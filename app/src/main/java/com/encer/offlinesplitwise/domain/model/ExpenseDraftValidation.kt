package com.encer.offlinesplitwise.domain.model

import com.encer.offlinesplitwise.core.common.MessageKey

data class ExpenseDraftValidation(
    val isValid: Boolean,
    val messageKey: MessageKey? = null,
    val normalizedShares: List<ExpenseShare> = emptyList(),
)
