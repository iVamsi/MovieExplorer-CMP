package com.movieapp.data.user.auth

import co.touchlab.kermit.Logger
import com.movieapp.core.common.dispatchers.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/** Platform storage for TMDB session credentials: Keystore-encrypted DataStore on Android, Keychain on iOS. */
interface SecureSessionStore {
    suspend fun read(): TmdbSessionState

    suspend fun write(state: TmdbSessionState)
}

/**
 * Persists TMDB session credentials through a [SecureSessionStore]. Initialization starts
 * asynchronously and is exposed through [sessionState] and [awaitInitialized], so construction
 * never blocks app startup. Writes wait for initialization, then persist the whole state at once.
 */
class TmdbSessionStorage(
    store: SecureSessionStore,
    dispatchers: AppDispatchers,
    appScope: CoroutineScope,
) {
    private val coordinator =
        SessionStateCoordinator(
            scope = appScope,
            dispatcher = dispatchers.io,
            load = store::read,
            persist = store::write,
            onInitializationFailure = { error ->
                Logger.withTag(TAG).e(error) { "Session initialization failed; re-authentication required" }
            },
        )

    val sessionState: StateFlow<TmdbSessionState> = coordinator.state

    suspend fun awaitInitialized(): TmdbSessionState = coordinator.awaitInitialized()

    suspend fun saveSession(
        sessionId: String,
        accountId: Int,
        username: String,
        name: String?,
        avatarPath: String?,
    ) {
        coordinator.update {
            it.copy(
                sessionId = sessionId,
                accountId = accountId,
                username = username,
                name = name,
                avatarPath = avatarPath,
            )
        }
    }

    suspend fun savePendingLogin(requestToken: String, state: String) {
        coordinator.update {
            it.copy(
                pendingRequestToken = requestToken,
                pendingLoginState = state,
            )
        }
    }

    suspend fun clearPendingLogin() {
        coordinator.update {
            it.copy(
                pendingRequestToken = null,
                pendingLoginState = null,
            )
        }
    }

    suspend fun clear() {
        coordinator.update { TmdbSessionState(isInitialized = true) }
    }

    private companion object {
        const val TAG = "TmdbSessionStorage"
    }
}
