package com.movieapp.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.ui.security.BiometricAuthenticator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val biometricAuthenticator = mockk<BiometricAuthenticator>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AccountViewModel(authRepository, preferencesRepository, biometricAuthenticator)

    @Test
    fun `biometricAvailable - exposes authenticator capability`() {
        io.mockk.every { biometricAuthenticator.isAvailable() } returns true

        assertThat(createViewModel().biometricAvailable).isTrue()
    }

    @Test
    fun `setBiometricLockEnabled - persists flag via preferences`() = runTest {
        val viewModel = createViewModel()

        viewModel.setBiometricLockEnabled(true)

        coVerify { preferencesRepository.setBiometricLockEnabled(true) }
    }

    @Test
    fun `logout - delegates to auth repository`() = runTest {
        val viewModel = createViewModel()

        viewModel.logout()

        coVerify { authRepository.logout() }
    }

    @Test
    fun `startTmdbLogin - on login url failure - emits message to user`() = runTest {
        coEvery { authRepository.createLoginApprovalUrl() } returns
            Result.Error(IllegalStateException("network down"))
        val viewModel = createViewModel()

        viewModel.startTmdbLogin()

        viewModel.sideEffects.test {
            assertThat(awaitItem()).isEqualTo(AccountSideEffect.ShowMessage("network down"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startTmdbLogin - on login url success - emits open url`() = runTest {
        coEvery { authRepository.createLoginApprovalUrl() } returns Result.Success("https://tmdb/auth")
        val viewModel = createViewModel()

        viewModel.startTmdbLogin()

        viewModel.sideEffects.test {
            assertThat(awaitItem()).isEqualTo(AccountSideEffect.OpenUrl("https://tmdb/auth"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `biometricLockEnabled - re-exposes preference flow`() = runTest {
        coEvery { preferencesRepository.biometricLockEnabled } returns flowOf(true, false)
        val viewModel = createViewModel()

        viewModel.biometricLockEnabled.test {
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}
