package com.goldenboot.goldenbootfinancial.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Result wrapper for network-bound operations.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Exception, val cachedData: Any? = null) : NetworkResult<Nothing>()
}

/**
 * Ktor HttpClient plugin that ensures only one active request per URL + method.
 * If a duplicate request is made while one is in-flight, the previous one is cancelled.
 *
 * Installed on the HttpClient in the core Koin module — works automatically for all
 * network calls made through the shared client.
 */
val DeduplicatedRequests = createClientPlugin("DeduplicatedRequests") {
    val mutex = Mutex()
    val activeRequests = mutableMapOf<String, Job>()

    onRequest { request, _ ->
        val key = "${request.method.value}:${request.url.buildString()}"

        mutex.withLock {
            activeRequests[key]?.cancel()
        }

        val job = request.executionContext
        mutex.withLock {
            activeRequests[key] = job
        }

        job.invokeOnCompletion {
            // Clean up when request completes (success, failure, or cancellation)
            CoroutineScope(Dispatchers.Default).launch {
                mutex.withLock {
                    if (activeRequests[key] === job) {
                        activeRequests.remove(key)
                    }
                }
            }
        }
    }
}

/**
 * Generic network-bound resource handler.
 *
 * Features:
 * - Duplicate call cancellation handled by Ktor's DeduplicatedRequests plugin (by URL)
 * - Retries handled by Ktor's HttpRequestRetry plugin
 * - Caches successful responses to local DB
 * - Falls back to cached DB data on failure
 *
 * Usage:
 * ```
 * val resource = NetworkBoundResource(
 *     networkCall = { api.fetchAll() },
 *     saveToDb = { items ->
 *         dao.deleteAll()
 *         dao.insertAll(items.map { Entity.fromDto(it) })
 *     },
 *     loadFromDb = { dao.getAll() }
 * )
 *
 * when (val result = resource.fetch()) {
 *     is NetworkResult.Success -> // use result.data
 *     is NetworkResult.Error -> // handle error, cachedData may be available
 * }
 * ```
 */
class NetworkBoundResource<NetworkResponse, DbEntity>(
    private val networkCall: suspend () -> NetworkResponse,
    private val saveToDb: suspend (NetworkResponse) -> Unit,
    private val loadFromDb: suspend () -> DbEntity
) {
    /**
     * Fetches data from network.
     * On success: saves to DB and returns fresh data.
     * On failure: falls back to cached DB data.
     */
    suspend fun fetch(): NetworkResult<NetworkResponse> {
        return try {
            val response = networkCall()
            saveToDb(response)
            NetworkResult.Success(response)
        } catch (e: Exception) {
            // Network failed after Ktor retries — fall back to cached data
            try {
                val cached = loadFromDb()
                NetworkResult.Error(exception = e, cachedData = cached)
            } catch (dbException: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }
}

/**
 * Simplified network-only fetch (no DB caching).
 *
 * Deduplication and retries are handled by Ktor plugins on the HttpClient.
 */
suspend fun <T> networkFetch(
    call: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        // Not just Exception: on the web a failed fetch surfaces as kotlin.Error.
        NetworkResult.Error(exception = e as? Exception ?: Exception(e.message, e))
    }
}
