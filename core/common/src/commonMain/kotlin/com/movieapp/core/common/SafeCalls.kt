package com.movieapp.core.common

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Runs [block] and wraps the outcome in [Result.Success] or [Result.Error].
 *
 * [CancellationException] is always rethrown so coroutine cancellation is never swallowed and
 * misreported as a data error.
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }

/**
 * Builds the standard network-with-cache-fallback flow: emits [Result.Loading], then the outcome of
 * [fetch] as [Result.Success]. When [fetch] fails (other than cancellation), [fallback] is
 * consulted with the failure; a non-null value is emitted as [Result.Success], otherwise the
 * original failure is emitted as [Result.Error]. A failing [fallback] also surfaces the original
 * failure.
 *
 * Apply `flowOn(dispatcher)` at the call site as usual.
 */
fun <T> safeResultFlow(
    fetch: suspend () -> T,
    fallback: suspend (Throwable) -> T? = { null },
): Flow<Result<T>> = flow {
    emit(Result.Loading)
    when (val fetched = safeCall(fetch)) {
        is Result.Success -> emit(fetched)
        is Result.Error -> {
            val recovered = safeCall { fallback(fetched.exception) }.getDataOrNull()
            if (recovered != null) {
                emit(Result.Success(recovered))
            } else {
                emit(Result.Error(fetched.exception))
            }
        }
        is Result.Loading -> Unit
    }
}
