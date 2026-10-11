package com.movieapp.data.user.repository

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.network.CreateSessionBody
import com.movieapp.core.network.RequestTokenResponse
import com.movieapp.core.network.SessionResponse
import com.movieapp.core.network.TmdbAccountDto
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.user.auth.TmdbSessionState
import com.movieapp.data.user.auth.TmdbSessionStorage
import com.movieapp.data.user.sync.TmdbUserLibrarySync
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.net.URLDecoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private val api = mockk<TmdbApiService>(relaxed = true)
    private val sessionStorage = mockk<TmdbSessionStorage>(relaxed = true)
    private val tmdbUserLibrarySync = mockk<TmdbUserLibrarySync>(relaxed = true)
    private val lazySync = lazy { tmdbUserLibrarySync }

    @Before
    fun setup() {
        every { sessionStorage.sessionState } returns
            MutableStateFlow(TmdbSessionState(isInitialized = true))
        coEvery { sessionStorage.awaitInitialized() } returns TmdbSessionState(isInitialized = true)
    }

    @Test
    fun `createLoginApprovalUrl - request token succeeds - stores pending token and state`() =
        runTest {
            coEvery { api.createRequestToken() } returns
                RequestTokenResponse(success = true, requestToken = REQUEST_TOKEN)
            val tokenSlot = slot<String>()
            val stateSlot = slot<String>()
            coEvery {
                sessionStorage.savePendingLogin(capture(tokenSlot), capture(stateSlot))
            } returns Unit
            val repository = createRepository()

            val result = repository.createLoginApprovalUrl()

            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat(tokenSlot.captured).isEqualTo(REQUEST_TOKEN)
            assertThat(stateSlot.captured).isNotEmpty()
            val url = (result as Result.Success).data
            val redirect =
                URLDecoder.decode(url.substringAfter("redirect_to="), Charsets.UTF_8.name())
            assertThat(redirect).isEqualTo("movieapp://auth/callback?state=${stateSlot.captured}")
        }

    @Test
    fun `completeLoginWithRequestToken - state mismatch - rejects callback`() = runTest {
        coEvery { sessionStorage.awaitInitialized() } returns pendingLoginState()
        val repository = createRepository()

        val result = repository.completeLoginWithRequestToken(REQUEST_TOKEN, "wrong-state")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isInstanceOf(InvalidAuthCallbackException::class.java)
        coVerify(exactly = 0) { api.createSession(any()) }
    }

    @Test
    fun `completeLoginWithRequestToken - token mismatch - rejects callback`() = runTest {
        coEvery { sessionStorage.awaitInitialized() } returns pendingLoginState()
        val repository = createRepository()

        val result = repository.completeLoginWithRequestToken("wrong-token", STATE)

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isInstanceOf(InvalidAuthCallbackException::class.java)
        coVerify(exactly = 0) { api.createSession(any()) }
    }

    @Test
    fun `completeLoginWithRequestToken - pending login matches - creates session and clears pending login`() =
        runTest {
            coEvery { sessionStorage.awaitInitialized() } returns pendingLoginState()
            coEvery { api.createSession(CreateSessionBody(REQUEST_TOKEN)) } returns
                SessionResponse(success = true, sessionId = SESSION_ID)
            coEvery { api.getAccount(SESSION_ID) } returns
                TmdbAccountDto(
                    id = ACCOUNT_ID,
                    username = USERNAME,
                    name = NAME,
                    avatarPath = AVATAR_PATH,
                )
            val repository = createRepository()

            val result = repository.completeLoginWithRequestToken(REQUEST_TOKEN, STATE)

            assertThat(result).isInstanceOf(Result.Success::class.java)
            coVerifyOrder {
                api.createSession(CreateSessionBody(REQUEST_TOKEN))
                api.getAccount(SESSION_ID)
                tmdbUserLibrarySync.pullAllAndMerge(ACCOUNT_ID, SESSION_ID)
            }
            coVerify {
                sessionStorage.saveSession(SESSION_ID, ACCOUNT_ID, USERNAME, NAME, AVATAR_PATH)
                sessionStorage.clearPendingLogin()
            }
        }

    private fun createRepository(): AuthRepositoryImpl {
        return AuthRepositoryImpl(
            api = api,
            sessionStorage = sessionStorage,
            tmdbUserLibrarySync = lazySync,
        )
    }

    private fun pendingLoginState() =
        TmdbSessionState(
            isInitialized = true,
            pendingRequestToken = REQUEST_TOKEN,
            pendingLoginState = STATE,
        )

    private companion object {
        const val REQUEST_TOKEN = "request-token"
        const val STATE = "state"
        const val SESSION_ID = "session-id"
        const val ACCOUNT_ID = 42
        const val USERNAME = "testuser"
        const val NAME = "Test User"
        const val AVATAR_PATH = "/avatar.png"
    }
}
