package com.movieapp.data.user.repository

import com.movieapp.core.common.Result
import com.movieapp.core.common.auth.AuthDeepLink
import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.domain.repository.AuthState
import com.movieapp.core.network.CreateSessionBody
import com.movieapp.core.network.DeleteSessionBody
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.user.auth.AuthSessionInvalidator
import com.movieapp.data.user.auth.TmdbSessionStorage
import com.movieapp.data.user.sync.TmdbUserLibrarySync
import io.ktor.http.encodeURLParameter
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val api: TmdbApiService,
    private val sessionStorage: TmdbSessionStorage,
    private val tmdbUserLibrarySync: Lazy<TmdbUserLibrarySync>,
) : AuthRepository, AuthSessionInvalidator {

    override val authState: Flow<AuthState> =
        sessionStorage.sessionState
            .filter { it.isInitialized }
            .map { state ->
                if (state.sessionId != null && state.accountId != null && state.username != null) {
                    AuthState.SignedIn(
                        accountId = state.accountId,
                        username = state.username,
                        name = state.name,
                        avatarPath = state.avatarPath,
                    )
                } else {
                    AuthState.SignedOut
                }
            }
            .distinctUntilChanged()

    override suspend fun createLoginApprovalUrl(): Result<String> {
        return try {
            val token = api.createRequestToken()
            if (!token.success) {
                return Result.Error(IllegalStateException("TMDB did not issue a request token"))
            }
            val state = generateLoginState()
            sessionStorage.savePendingLogin(token.requestToken, state)
            val redirect = AuthDeepLink.callbackUri(state).encodeURLParameter()
            val url =
                "https://www.themoviedb.org/authenticate/${token.requestToken}?redirect_to=$redirect"
            Result.Success(url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun completeLoginWithRequestToken(
        requestToken: String,
        state: String,
    ): Result<Unit> {
        val stored = sessionStorage.awaitInitialized()
        if (stored.sessionId != null) return Result.Success(Unit)
        if (requestToken != stored.pendingRequestToken || state != stored.pendingLoginState) {
            return Result.Error(InvalidAuthCallbackException())
        }
        return try {
            val session = api.createSession(CreateSessionBody(requestToken))
            if (!session.success) {
                return Result.Error(IllegalStateException("Session creation failed"))
            }
            val account = api.getAccount(session.sessionId)
            sessionStorage.saveSession(
                session.sessionId,
                account.id,
                account.username,
                account.name,
                account.avatarPath,
            )
            tmdbUserLibrarySync.value.pullAllAndMerge(account.id, session.sessionId)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e)
        } finally {
            sessionStorage.clearPendingLogin()
        }
    }

    override fun isSignedIn(): Boolean = sessionStorage.sessionState.value.sessionId != null

    override suspend fun logout() {
        val sid = sessionStorage.awaitInitialized().sessionId
        if (sid != null) {
            try {
                api.deleteSession(DeleteSessionBody(sid))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Best-effort remote logout; always clear local session.
            }
        }
        sessionStorage.clear()
    }

    override suspend fun onSessionInvalidated() {
        sessionStorage.clear()
    }

    private fun generateLoginState(): String = Uuid.random().toHexString()
}

class InvalidAuthCallbackException : IllegalStateException("Invalid TMDB auth callback state")
