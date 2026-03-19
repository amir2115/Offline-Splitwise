package com.encer.offlinesplitwise.data.remote.network

import com.encer.offlinesplitwise.data.remote.model.ApiError
import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Response

private val errorJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class ErrorEnvelope(
    val detail: String? = null,
    val error: ErrorPayload? = null,
)

@Serializable
private data class ErrorPayload(
    val message: String? = null,
)

fun <T> Response<T>.requireBody(): T {
    val body = body()
    if (isSuccessful && body != null) return body
    val rawPayload = errorBody()?.string()
    throw ApiError(
        message = extractErrorMessage(code(), rawPayload),
        status = code(),
        payload = rawPayload,
    )
}

fun mapNetworkException(error: Throwable): ApiError {
    return when (error) {
        is ApiError -> error
        is IOException -> ApiError(error.message ?: error.javaClass.simpleName, -1, null)
        else -> ApiError(error.message ?: error.javaClass.simpleName, -1, null)
    }
}

private fun extractErrorMessage(status: Int, payload: String?): String {
    if (payload.isNullOrBlank()) return "HTTP $status"
    return runCatching {
        val envelope = errorJson.decodeFromString(ErrorEnvelope.serializer(), payload)
        envelope.detail ?: envelope.error?.message ?: "HTTP $status"
    }.getOrDefault("HTTP $status")
}
