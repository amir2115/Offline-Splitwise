package com.encer.splitwise.domain.model

import com.encer.splitwise.core.common.MessageKey

data class ExpenseDraftValidation(
    val isValid: Boolean,
    val messageKey: MessageKey? = null,
    val normalizedShares: List<ExpenseShare> = emptyList(),
)
