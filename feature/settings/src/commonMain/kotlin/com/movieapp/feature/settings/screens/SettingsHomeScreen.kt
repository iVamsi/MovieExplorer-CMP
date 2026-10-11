package com.movieapp.feature.settings.screens

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.settings_about
import com.movieapp.core.ui.resources.settings_account
import com.movieapp.core.ui.resources.settings_catalog
import com.movieapp.core.ui.resources.settings_help
import com.movieapp.core.ui.resources.settings_home_title
import com.movieapp.core.ui.resources.settings_language
import com.movieapp.core.ui.resources.settings_storage
import com.movieapp.core.ui.resources.settings_theme
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.settings_group_account
import com.movieapp.feature.settings.resources.settings_group_appearance
import com.movieapp.feature.settings.resources.settings_group_catalog
import com.movieapp.feature.settings.resources.settings_group_help
import com.movieapp.feature.settings.resources.settings_group_storage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.movieapp.core.ui.designsystem.MovieAppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    onBackClick: () -> Unit,
    onAccountClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onStorageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            SettingsTopBar(
                title = stringResource(CoreRes.string.settings_home_title),
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
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.large),
        ) {
            SettingsGroup(title = stringResource(Res.string.settings_group_account)) {
                SettingsNavigationRow(
                    icon = Icons.Filled.Person,
                    title = stringResource(CoreRes.string.settings_account),
                    onClick = onAccountClick,
                )
            }
            SettingsGroup(title = stringResource(Res.string.settings_group_catalog)) {
                SettingsNavigationRow(
                    icon = Icons.Filled.Map,
                    title = stringResource(CoreRes.string.settings_catalog),
                    onClick = onCatalogClick,
                )
                SettingsNavigationRow(
                    icon = Icons.Filled.Language,
                    title = stringResource(CoreRes.string.settings_language),
                    onClick = onLanguageClick,
                )
            }
            SettingsGroup(title = stringResource(Res.string.settings_group_appearance)) {
                SettingsNavigationRow(
                    icon = Icons.Filled.Palette,
                    title = stringResource(CoreRes.string.settings_theme),
                    onClick = onThemeClick,
                )
            }
            SettingsGroup(title = stringResource(Res.string.settings_group_storage)) {
                SettingsNavigationRow(
                    icon = Icons.Filled.Storage,
                    title = stringResource(CoreRes.string.settings_storage),
                    onClick = onStorageClick,
                )
            }
            SettingsGroup(title = stringResource(Res.string.settings_group_help)) {
                SettingsNavigationRow(
                    icon = Icons.Filled.Help,
                    title = stringResource(CoreRes.string.settings_help),
                    onClick = onHelpClick,
                )
                SettingsNavigationRow(
                    icon = Icons.Filled.Info,
                    title = stringResource(CoreRes.string.settings_about),
                    onClick = onAboutClick,
                )
            }
        }
    }
}
