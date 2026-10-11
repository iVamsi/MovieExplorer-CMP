package com.movieapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.app.resources.Res
import com.movieapp.app.resources.auth_login_denied
import com.movieapp.app.resources.auth_login_error
import com.movieapp.app.resources.auth_login_success
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.ui.security.AppLockAuthResult
import com.movieapp.core.ui.security.BiometricAuthenticator
import com.movieapp.security.AppLockPolicy
import com.movieapp.security.AppLockState
import com.movieapp.ui.navigation.ActorDetailRoute
import com.movieapp.ui.navigation.CollectionDetailRoute
import com.movieapp.ui.navigation.LibraryRoute
import com.movieapp.ui.navigation.MovieDetailRoute
import com.movieapp.ui.navigation.SearchRoute
import com.movieapp.ui.navigation.SeriesDetailRoute
import com.movieapp.ui.navigation.UpcomingRoute
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class AppUiState(
    val themeMode: String = "system",
    val useWallpaperColors: Boolean = false,
    val isLocked: Boolean = true,
    val showLockRecovery: Boolean = false,
    val canDisableLock: Boolean = false,
)

sealed interface AppUiEvent {
    data class DeepLinkReceived(val url: String) : AppUiEvent

    data class EnteredForeground(val title: String, val subtitle: String) : AppUiEvent

    data object EnteredBackground : AppUiEvent

    data class RetryUnlock(val title: String, val subtitle: String) : AppUiEvent

    data object DisableLock : AppUiEvent
}

sealed interface AppSideEffect {
    data class Navigate(val route: Any) : AppSideEffect

    data class ShowMessage(val message: StringResource) : AppSideEffect
}

class AppViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val clock: Clock,
) : ViewModel() {

    private val appLockPolicy = AppLockPolicy(BACKGROUND_THRESHOLD_MS)
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<AppSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<AppSideEffect> = _sideEffects.receiveAsFlow()

    private var lastBackgroundedAt = 0L
    private var authenticating = false

    init {
        viewModelScope.launch {
            combine(
                preferencesRepository.themeMode,
                preferencesRepository.useWallpaperColors,
            ) { themeMode, useWallpaperColors -> themeMode to useWallpaperColors }
                .collect { (themeMode, useWallpaperColors) ->
                    _uiState.update {
                        it.copy(
                            themeMode = themeMode,
                            useWallpaperColors = useWallpaperColors,
                        )
                    }
                }
        }
    }

    fun onEvent(event: AppUiEvent) {
        when (event) {
            is AppUiEvent.DeepLinkReceived -> handleDeepLink(event.url)
            is AppUiEvent.EnteredForeground -> enterForeground(event.title, event.subtitle)
            AppUiEvent.EnteredBackground -> enterBackground()
            is AppUiEvent.RetryUnlock -> authenticate(event.title, event.subtitle)
            AppUiEvent.DisableLock -> disableLock()
        }
    }

    private fun handleDeepLink(url: String) {
        when (val deepLink = parseDeepLink(url) ?: return) {
            is DeepLink.AuthCallback -> completeLogin(deepLink)
            is DeepLink.Movie -> navigate(MovieDetailRoute(deepLink.id))
            is DeepLink.Series -> navigate(SeriesDetailRoute(deepLink.id))
            is DeepLink.Actor -> navigate(ActorDetailRoute(deepLink.id))
            is DeepLink.Collection -> navigate(CollectionDetailRoute(deepLink.id))
            DeepLink.Search -> navigate(SearchRoute)
            DeepLink.Library -> navigate(LibraryRoute())
            DeepLink.Upcoming -> navigate(UpcomingRoute)
        }
    }

    private fun completeLogin(callback: DeepLink.AuthCallback) {
        if (authRepository.isSignedIn()) return
        if (!callback.approved) {
            showMessage(Res.string.auth_login_denied)
            return
        }
        val requestToken = callback.requestToken
        val state = callback.state
        if (requestToken.isNullOrBlank() || state.isNullOrBlank()) {
            showMessage(Res.string.auth_login_error)
            return
        }
        viewModelScope.launch {
            when (authRepository.completeLoginWithRequestToken(requestToken, state)) {
                is Result.Success -> _sideEffects.send(AppSideEffect.ShowMessage(Res.string.auth_login_success))
                is Result.Error -> _sideEffects.send(AppSideEffect.ShowMessage(Res.string.auth_login_error))
                Result.Loading -> Unit
            }
        }
    }

    private fun enterForeground(title: String, subtitle: String) {
        viewModelScope.launch {
            val state =
                appLockPolicy.onStart(
                    lockEnabled = preferencesRepository.biometricLockEnabled.first(),
                    lastBackgroundedAt = lastBackgroundedAt,
                    now = clock.now().toEpochMilliseconds(),
                )
            val locked = state == AppLockState.Locked
            _uiState.update { it.copy(isLocked = locked, showLockRecovery = false) }
            if (locked) authenticate(title, subtitle)
        }
    }

    private fun enterBackground() {
        viewModelScope.launch {
            lastBackgroundedAt = clock.now().toEpochMilliseconds()
            if (preferencesRepository.biometricLockEnabled.first()) {
                _uiState.update { it.copy(isLocked = true, showLockRecovery = false) }
            }
        }
    }

    private fun authenticate(title: String, subtitle: String) {
        if (authenticating) return
        authenticating = true
        _uiState.update { it.copy(showLockRecovery = false) }
        viewModelScope.launch {
            val result =
                biometricAuthenticator.authenticate(
                    title = title,
                    subtitle = subtitle,
                )
            authenticating = false
            _uiState.update {
                it.copy(
                    isLocked = appLockPolicy.afterAuthentication(result) == AppLockState.Locked,
                    showLockRecovery = result != AppLockAuthResult.Succeeded,
                    canDisableLock = result == AppLockAuthResult.Unavailable,
                )
            }
        }
    }

    private fun disableLock() {
        if (!_uiState.value.canDisableLock) return
        viewModelScope.launch {
            preferencesRepository.setBiometricLockEnabled(false)
            _uiState.update { it.copy(isLocked = false, showLockRecovery = false) }
        }
    }

    private fun navigate(route: Any) {
        viewModelScope.launch { _sideEffects.send(AppSideEffect.Navigate(route)) }
    }

    private fun showMessage(message: StringResource) {
        viewModelScope.launch { _sideEffects.send(AppSideEffect.ShowMessage(message)) }
    }

    private companion object {
        const val BACKGROUND_THRESHOLD_MS = 30_000L
    }
}
