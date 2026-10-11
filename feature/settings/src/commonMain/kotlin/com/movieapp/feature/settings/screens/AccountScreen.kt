package com.movieapp.feature.settings.screens

import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.account_screen_title
import com.movieapp.feature.settings.resources.account_sign_in_tmdb
import com.movieapp.feature.settings.resources.account_sign_out
import com.movieapp.feature.settings.resources.account_signed_out_body
import com.movieapp.feature.settings.resources.account_signed_out_title
import com.movieapp.feature.settings.resources.account_sync_summary
import com.movieapp.feature.settings.resources.account_username_label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.movieapp.feature.settings.AccountSideEffect
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.core.domain.repository.AuthState
import com.movieapp.core.ui.components.TmdbAttributionSection
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.feature.settings.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onBackClick: () -> Unit, viewModel: AccountViewModel = koinViewModel()) {
    val authState by
        viewModel.authState.collectAsStateWithLifecycle(initialValue = AuthState.SignedOut)
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is AccountSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is AccountSideEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SettingsTopBar(
                title = stringResource(Res.string.account_screen_title),
                onBackClick = onBackClick,
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = MovieAppSpacing.screenHorizontal)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
        ) {
            when (val s = authState) {
                is AuthState.SignedOut -> {
                    Text(
                        text = stringResource(Res.string.account_signed_out_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(Res.string.account_signed_out_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(
                        onClick = { viewModel.startTmdbLogin() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.account_sign_in_tmdb))
                    }
                }

                is AuthState.SignedIn -> {
                    Text(
                        text = stringResource(Res.string.account_username_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(text = s.username, style = MaterialTheme.typography.titleLarge)
                    s.name
                        ?.takeIf { it.isNotBlank() }
                        ?.let { name ->
                            Text(text = name, style = MaterialTheme.typography.bodyLarge)
                        }
                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.account_sign_out))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            Text(
                text = stringResource(Res.string.account_sync_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            TmdbAttributionSection(modifier = Modifier.fillMaxWidth())
        }
    }
}

