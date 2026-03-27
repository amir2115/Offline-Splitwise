package com.encer.splitwise.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend fun <T> Flow<List<T>>.firstValue(): List<T> = first()
