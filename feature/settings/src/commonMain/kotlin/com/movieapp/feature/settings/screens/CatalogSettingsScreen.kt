package com.movieapp.feature.settings.screens

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.settings_catalog
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.catalog_adult_subtitle
import com.movieapp.feature.settings.resources.catalog_adult_title
import com.movieapp.feature.settings.resources.catalog_intro
import com.movieapp.feature.settings.resources.catalog_language_title
import com.movieapp.feature.settings.resources.catalog_region_hint
import com.movieapp.feature.settings.resources.catalog_region_label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.feature.settings.SettingsUiEvent
import com.movieapp.feature.settings.SettingsViewModel

private val catalogLanguageOptions =
    listOf("en-US" to "English (US)", "es-ES" to "Español (España)", "fr-FR" to "Français")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSettingsScreen(onBackClick: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = stringResource(CoreRes.string.settings_catalog),
                onBackClick = onBackClick,
            )
        }
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = MovieAppSpacing.screenHorizontal)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
        ) {
            Text(
                text = stringResource(Res.string.catalog_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(Res.string.catalog_language_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Column(modifier = Modifier.selectableGroup()) {
                catalogLanguageOptions.forEach { (code, label) ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .selectable(
                                    selected = uiState.contentLanguage == code,
                                    onClick = {
                                        viewModel.onEvent(
                                            SettingsUiEvent.ContentLanguageChanged(code)
                                        )
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = uiState.contentLanguage == code, onClick = null)
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            OutlinedTextField(
                value = uiState.watchRegion,
                onValueChange = { viewModel.onEvent(SettingsUiEvent.WatchRegionChanged(it)) },
                label = { Text(stringResource(Res.string.catalog_region_label)) },
                supportingText = { Text(stringResource(Res.string.catalog_region_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.catalog_adult_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(Res.string.catalog_adult_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.adultContent,
                    onCheckedChange = {
                        viewModel.onEvent(SettingsUiEvent.AdultContentChanged(it))
                    },
                )
            }
        }
    }
}
