package com.movieapp.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.movieapp.app.resources.Res
import com.movieapp.app.resources.biometric_recovery_disable
import com.movieapp.app.resources.biometric_recovery_message
import com.movieapp.app.resources.biometric_recovery_retry
import com.movieapp.app.resources.biometric_recovery_title
import com.movieapp.app.resources.biometric_prompt_subtitle
import com.movieapp.app.resources.biometric_prompt_title
import com.movieapp.core.ui.platform.dynamicColorScheme
import com.movieapp.core.ui.theme.CinematicDarkColors
import com.movieapp.core.ui.theme.CinematicLightColors
import com.movieapp.core.ui.theme.CinematicTheme
import com.movieapp.ui.navigation.MovieAppNavigation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: AppViewModel = koinViewModel()) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme =
        when (uiState.themeMode) {
            "light" -> false
            "dark" -> true
            else -> isSystemInDarkTheme()
        }
    val colors =
        (if (uiState.useWallpaperColors) dynamicColorScheme(darkTheme) else null)
            ?: if (darkTheme) CinematicDarkColors else CinematicLightColors
    val lockTitle = stringResource(Res.string.biometric_prompt_title)
    val lockSubtitle = stringResource(Res.string.biometric_prompt_subtitle)

    LaunchedEffect(viewModel) {
        DeepLinks.urls.collect { viewModel.onEvent(AppUiEvent.DeepLinkReceived(it)) }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.onEvent(AppUiEvent.EnteredForeground(lockTitle, lockSubtitle))
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onEvent(AppUiEvent.EnteredBackground)
    }

    CinematicTheme(darkTheme = darkTheme, colorScheme = colors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (uiState.isLocked) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                MovieAppNavigation(appSideEffects = viewModel.sideEffects)
            }
        }

        if (uiState.showLockRecovery) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(Res.string.biometric_recovery_title)) },
                text = { Text(stringResource(Res.string.biometric_recovery_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(AppUiEvent.RetryUnlock(lockTitle, lockSubtitle))
                        }
                    ) {
                        Text(stringResource(Res.string.biometric_recovery_retry))
                    }
                },
                dismissButton = {
                    if (uiState.canDisableLock) {
                        TextButton(onClick = { viewModel.onEvent(AppUiEvent.DisableLock) }) {
                            Text(stringResource(Res.string.biometric_recovery_disable))
                        }
                    }
                },
            )
        }
    }
}
