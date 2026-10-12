package com.goldenboot.goldenbootfinancial.core.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/** A non-2xx answer from the BFF. [message] is the BFF's `error` text when it sent one. */
class ApiException(val status: Int, message: String) : Exception(message) {
    val isUnauthorized: Boolean get() = status == 401
    val isNotFound: Boolean get() = status == 404
}

/** Throws [ApiException] unless the response is 2xx. */
suspend fun HttpResponse.ensureSuccess() {
    if (status.isSuccess()) return
    val message = runCatching {
        (Json.parseToJsonElement(bodyAsText()).jsonObject["error"] as? JsonPrimitive)?.content
    }.getOrNull()
    throw ApiException(status.value, message ?: "Request failed (${status.value})")
}
