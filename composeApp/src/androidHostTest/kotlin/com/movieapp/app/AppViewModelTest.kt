package com.movieapp.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.testing.FakeAuthRepository
import com.movieapp.core.testing.FixedClock
import com.movieapp.core.ui.security.AppLockAuthResult
import com.movieapp.core.ui.security.BiometricAuthenticator
import com.movieapp.app.resources.auth_login_denied
import com.movieapp.app.resources.auth_login_success
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deep link - approved callback - completes login and reports success`() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = createViewModel(authRepository)

        viewModel.onEvent(
            AppUiEvent.DeepLinkReceived(
                "movieapp://auth/callback?approved=true&request_token=token&state=state"
            )
        )

        viewModel.sideEffects.test {
            assertThat(awaitItem()).isEqualTo(
                AppSideEffect.ShowMessage(com.movieapp.app.resources.Res.string.auth_login_success)
            )
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(authRepository.completedLogins).containsExactly("token" to "state")
    }

    @Test
    fun `deep link - denied callback - does not complete login`() = runTest {
        val authRepository = FakeAuthRepository().apply { completeLoginResult = Result.Success(Unit) }
        val viewModel = createViewModel(authRepository)

        viewModel.onEvent(
            AppUiEvent.DeepLinkReceived("movieapp://auth/callback?approved=false&state=state")
        )

        viewModel.sideEffects.test {
            assertThat(awaitItem()).isEqualTo(
                AppSideEffect.ShowMessage(com.movieapp.app.resources.Res.string.auth_login_denied)
            )
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(authRepository.completedLogins).isEmpty()
    }

    @Test
    fun `foreground - lock enabled - authenticates and unlocks`() = runTest {
        val authenticator = mockk<BiometricAuthenticator>()
        coEvery { authenticator.authenticate(any(), any()) } returns AppLockAuthResult.Succeeded
        val preferences = preferencesRepository()
        preferences.setBiometricLockEnabled(true)
        val viewModel =
            AppViewModel(
                preferencesRepository = preferences,
                authRepository = FakeAuthRepository(),
                biometricAuthenticator = authenticator,
                clock = FixedClock(Instant.parse("2026-10-10T12:00:00Z")),
            )

        viewModel.onEvent(AppUiEvent.EnteredForeground("Unlock", "Confirm identity"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLocked).isFalse()
        assertThat(viewModel.uiState.value.showLockRecovery).isFalse()
    }

    private fun createViewModel(authRepository: FakeAuthRepository) =
        AppViewModel(
            preferencesRepository = preferencesRepository(),
            authRepository = authRepository,
            biometricAuthenticator = mockk(relaxed = true),
            clock = FixedClock(Instant.parse("2026-10-10T12:00:00Z")),
        )

    private fun preferencesRepository(): PreferencesRepository =
        PreferencesRepository(
            PreferenceDataStoreFactory.create(scope = kotlinx.coroutines.CoroutineScope(dispatcher)) {
                temporaryFolder.newFolder().resolve("preferences.preferences_pb")
            }
        )
}
