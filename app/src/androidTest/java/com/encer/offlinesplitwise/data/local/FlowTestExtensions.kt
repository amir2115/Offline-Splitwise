package com.encer.offlinesplitwise.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend fun <T> Flow<List<T>>.firstValue(): List<T> = first()
