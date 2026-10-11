package com.movieapp.feature.settings.screens

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.settings_language
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.language_choose_hint
import com.movieapp.feature.settings.resources.language_option_english
import com.movieapp.feature.settings.resources.language_option_spanish
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.feature.settings.SettingsUiEvent
import com.movieapp.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(onBackClick: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = stringResource(CoreRes.string.settings_language),
                onBackClick = onBackClick,
            )
        }
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(
                        horizontal =
                            com.movieapp.core.ui.designsystem.MovieAppSpacing.screenHorizontal
                    )
        ) {
            Text(
                text = stringResource(Res.string.language_choose_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(
                modifier = Modifier.height(com.movieapp.core.ui.designsystem.MovieAppSpacing.medium)
            )
            SettingsGroup(title = stringResource(CoreRes.string.settings_language)) {
                Column(modifier = Modifier.selectableGroup()) {
                    LanguageOption(
                        label = stringResource(Res.string.language_option_english),
                        selected = uiState.language == "en",
                        onSelect = { viewModel.onEvent(SettingsUiEvent.AppLanguageChanged("en")) },
                    )
                    LanguageOption(
                        label = stringResource(Res.string.language_option_spanish),
                        selected = uiState.language == "es",
                        onSelect = { viewModel.onEvent(SettingsUiEvent.AppLanguageChanged("es")) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
