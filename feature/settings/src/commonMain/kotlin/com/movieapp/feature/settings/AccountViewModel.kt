package com.movieapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.ui.security.BiometricAuthenticator
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.account_error_login_url
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

sealed interface AccountSideEffect {
    data class ShowMessage(val message: String) : AccountSideEffect

    data class OpenUrl(val url: String) : AccountSideEffect
}

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository,
    biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {

    private val _sideEffects = Channel<AccountSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<AccountSideEffect> = _sideEffects.receiveAsFlow()

    val authState = authRepository.authState

    val biometricLockEnabled = preferencesRepository.biometricLockEnabled

    val biometricAvailable = biometricAuthenticator.isAvailable()

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setBiometricLockEnabled(enabled) }
    }

    fun startTmdbLogin() {
        viewModelScope.launch {
            when (val r = authRepository.createLoginApprovalUrl()) {
                is Result.Success -> _sideEffects.send(AccountSideEffect.OpenUrl(r.data))
                is Result.Error ->
                    _sideEffects.send(
                        AccountSideEffect.ShowMessage(
                            r.exception.message ?: getString(Res.string.account_error_login_url)
                        )
                    )
                Result.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
