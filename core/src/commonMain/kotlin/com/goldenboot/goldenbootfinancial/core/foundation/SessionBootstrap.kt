package com.goldenboot.goldenbootfinancial.core.foundation

import com.goldenboot.goldenbootfinancial.core.network.ApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * What the app learns about the member at launch.
 *
 * A null field means that call failed (offline, server error); the app should treat unknown as
 * "do not nag" rather than as "incomplete".
 */
data class BootstrapResult(
    val profileSaved: Boolean?,
    val legal: LegalStatus?,
    val signedOut: Boolean
)

/** Launch-time checks: is the member signed in, do they have a profile, are the agreements done. */
class SessionBootstrap(private val api: FoundationApi) {

    suspend fun run(): BootstrapResult = coroutineScope {
        val status = async { attempt { api.profileStatus() } }
        val legal = async { attempt { api.legalStatus() } }
        val statusResult = status.await()
        val legalResult = legal.await()

        val unauthorized = listOf(statusResult, legalResult).any {
            (it.exceptionOrNull() as? ApiException)?.isUnauthorized == true
        }
        BootstrapResult(
            profileSaved = statusResult.getOrNull()?.saved,
            legal = legalResult.getOrNull(),
            signedOut = unauthorized
        )
    }

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        // Not just Exception: on the web a failed fetch surfaces as kotlin.Error.
        Result.failure(e)
    }
}
