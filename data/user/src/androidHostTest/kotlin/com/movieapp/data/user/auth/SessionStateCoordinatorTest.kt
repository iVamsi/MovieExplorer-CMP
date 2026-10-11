package com.movieapp.data.user.auth

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Test

class SessionStateCoordinatorTest {

    @Test
    fun `construction - persistence load suspended - does not block and exposes initializing state`() =
        runTest {
            val loadGate = CompletableDeferred<TmdbSessionState>()
            val coordinator =
                SessionStateCoordinator(
                    scope = backgroundScope,
                    dispatcher = StandardTestDispatcher(testScheduler),
                    load = { loadGate.await() },
                    persist = {},
                )

            assertThat(coordinator.state.value.isInitialized).isFalse()

            loadGate.complete(TmdbSessionState(sessionId = "session"))
            testScheduler.advanceUntilIdle()

            assertThat(coordinator.awaitInitialized().sessionId).isEqualTo("session")
        }

    @Test
    fun `updates - concurrent writes - persist atomically`() = runTest {
        var activeWrites = 0
        var maximumActiveWrites = 0
        val persisted = mutableListOf<TmdbSessionState>()
        val coordinator =
            SessionStateCoordinator(
                scope = backgroundScope,
                dispatcher = StandardTestDispatcher(testScheduler),
                load = { TmdbSessionState() },
                persist = { state ->
                    activeWrites++
                    maximumActiveWrites = maxOf(maximumActiveWrites, activeWrites)
                    yield()
                    persisted += state
                    activeWrites--
                },
            )
        testScheduler.advanceUntilIdle()

        val first = async { coordinator.update { it.copy(sessionId = "first") } }
        val second = async { coordinator.update { it.copy(username = "user") } }
        first.await()
        second.await()

        assertThat(maximumActiveWrites).isEqualTo(1)
        assertThat(persisted).hasSize(2)
        assertThat(coordinator.state.value.sessionId).isEqualTo("first")
        assertThat(coordinator.state.value.username).isEqualTo("user")
    }
}
