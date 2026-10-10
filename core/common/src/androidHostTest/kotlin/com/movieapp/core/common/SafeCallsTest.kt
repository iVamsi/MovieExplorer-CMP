package com.movieapp.core.common

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SafeCallsTest {

    @Test
    fun `safeCall - block succeeds - returns Success with value`() = runTest {
        val result = safeCall { 42 }

        assertThat(result).isEqualTo(Result.Success(42))
    }

    @Test
    fun `safeCall - block throws - returns Error with exception`() = runTest {
        val exception = IOException("network down")

        val result = safeCall<Int> { throw exception }

        assertThat(result).isEqualTo(Result.Error(exception))
    }

    @Test
    fun `safeCall - block throws CancellationException - rethrows`() = runTest {
        var rethrown = false

        try {
            safeCall<Int> { throw CancellationException("cancelled") }
        } catch (e: CancellationException) {
            rethrown = true
        }

        assertThat(rethrown).isTrue()
    }

    @Test
    fun `safeCall - coroutine cancelled inside block - cancellation propagates`() = runTest {
        var caughtAsError = false

        val job = launch {
            val result = safeCall {
                this@launch.cancel()
                throw CancellationException("scope cancelled")
            }
            caughtAsError = result is Result.Error
        }
        job.join()

        assertThat(job.isCancelled).isTrue()
        assertThat(caughtAsError).isFalse()
    }

    @Test
    fun `safeResultFlow - fetch succeeds - emits Loading then Success`() = runTest {
        safeResultFlow(fetch = { "data" }).test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            assertThat(awaitItem()).isEqualTo(Result.Success("data"))
            awaitComplete()
        }
    }

    @Test
    fun `safeResultFlow - fetch fails without fallback - emits Loading then Error`() = runTest {
        val exception = IOException("network down")

        safeResultFlow<String>(fetch = { throw exception }).test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            assertThat(awaitItem()).isEqualTo(Result.Error(exception))
            awaitComplete()
        }
    }

    @Test
    fun `safeResultFlow - fetch fails with fallback value - emits Success with fallback`() =
        runTest {
            safeResultFlow(
                    fetch = { throw IOException("network down") },
                    fallback = { "cached" },
                )
                .test {
                    assertThat(awaitItem()).isEqualTo(Result.Loading)
                    assertThat(awaitItem()).isEqualTo(Result.Success("cached"))
                    awaitComplete()
                }
        }

    @Test
    fun `safeResultFlow - fallback receives original exception`() = runTest {
        val exception = IOException("network down")
        var received: Throwable? = null

        safeResultFlow<String>(
                fetch = { throw exception },
                fallback = { e ->
                    received = e
                    null
                },
            )
            .test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)
                assertThat(awaitItem()).isEqualTo(Result.Error(exception))
                awaitComplete()
            }

        assertThat(received).isSameInstanceAs(exception)
    }

    @Test
    fun `safeResultFlow - fallback also throws - emits Error with original exception`() = runTest {
        val original = IOException("network down")

        safeResultFlow<String>(
                fetch = { throw original },
                fallback = { throw IllegalStateException("cache corrupt") },
            )
            .test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)
                assertThat(awaitItem()).isEqualTo(Result.Error(original))
                awaitComplete()
            }
    }

    @Test
    fun `safeResultFlow - fetch throws CancellationException - flow is cancelled not Error`() =
        runTest {
            val emitted = mutableListOf<Result<String>>()
            var cancelled = false

            try {
                safeResultFlow<String>(fetch = { throw CancellationException("cancelled") })
                    .collect { emitted.add(it) }
            } catch (e: CancellationException) {
                cancelled = true
            }

            assertThat(cancelled).isTrue()
            assertThat(emitted).containsExactly(Result.Loading)
        }
}
