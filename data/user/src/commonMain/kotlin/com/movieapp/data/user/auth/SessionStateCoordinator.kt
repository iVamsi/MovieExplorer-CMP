package com.movieapp.data.user.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSessionState(
    val isInitialized: Boolean = false,
    val sessionId: String? = null,
    val accountId: Int? = null,
    val username: String? = null,
    val name: String? = null,
    val avatarPath: String? = null,
    val pendingRequestToken: String? = null,
    val pendingLoginState: String? = null,
)

internal class SessionStateCoordinator(
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    private val load: suspend () -> TmdbSessionState,
    private val persist: suspend (TmdbSessionState) -> Unit,
    private val onInitializationFailure: (Throwable) -> Unit = {},
) {
    private val mutex = Mutex()
    private val initialized = CompletableDeferred<Unit>()
    private val _state = MutableStateFlow(TmdbSessionState())
    val state: StateFlow<TmdbSessionState> = _state.asStateFlow()

    init {
        scope.launch(dispatcher) {
            try {
                mutex.withLock {
                    _state.value = load().copy(isInitialized = true)
                }
                initialized.complete(Unit)
            } catch (e: CancellationException) {
                initialized.completeExceptionally(e)
                throw e
            } catch (e: Exception) {
                onInitializationFailure(e)
                _state.value = TmdbSessionState(isInitialized = true)
                initialized.complete(Unit)
            }
        }
    }

    suspend fun awaitInitialized(): TmdbSessionState {
        initialized.await()
        return state.value
    }

    suspend fun update(transform: (TmdbSessionState) -> TmdbSessionState) {
        initialized.await()
        mutex.withLock {
            val next = transform(state.value).copy(isInitialized = true)
            persist(next)
            _state.value = next
        }
    }
}
