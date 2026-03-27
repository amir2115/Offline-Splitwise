package com.encer.splitwise.data.remote.model

class ApiError(
    message: String,
    val status: Int,
    val payload: String? = null,
) : Exception(message)
