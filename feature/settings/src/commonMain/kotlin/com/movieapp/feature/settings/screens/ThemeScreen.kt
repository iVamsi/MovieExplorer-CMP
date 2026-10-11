package com.movieapp.feature.settings.screens

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.cd_back
import com.movieapp.core.ui.resources.settings_theme
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.theme_choose_hint
import com.movieapp.feature.settings.resources.theme_option_dark
import com.movieapp.feature.settings.resources.theme_option_light
import com.movieapp.feature.settings.resources.theme_option_system
import com.movieapp.feature.settings.resources.theme_wallpaper_colors
import com.movieapp.feature.settings.resources.theme_wallpaper_colors_hint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.platform.supportsDynamicColor
import com.movieapp.core.ui.theme.CinematicDarkColors
import com.movieapp.core.ui.theme.CinematicLightColors
import com.movieapp.feature.settings.SettingsUiEvent
import com.movieapp.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(onBackClick: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreRes.string.settings_theme)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreRes.string.cd_back),
                        )
                    }
                },
                windowInsets = WindowInsets(0),
            )
        }
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(MovieAppSpacing.medium)
        ) {
            Text(
                text = stringResource(Res.string.theme_choose_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(MovieAppSpacing.medium))
            Column(modifier = Modifier.selectableGroup()) {
                ThemeOption(
                    label = stringResource(Res.string.theme_option_light),
                    mode = "light",
                    selected = uiState.themeMode == "light",
                    onSelect = { viewModel.onEvent(SettingsUiEvent.ThemeModeChanged("light")) },
                )
                ThemeOption(
                    label = stringResource(Res.string.theme_option_dark),
                    mode = "dark",
                    selected = uiState.themeMode == "dark",
                    onSelect = { viewModel.onEvent(SettingsUiEvent.ThemeModeChanged("dark")) },
                )
                ThemeOption(
                    label = stringResource(Res.string.theme_option_system),
                    mode = "system",
                    selected = uiState.themeMode == "system",
                    onSelect = { viewModel.onEvent(SettingsUiEvent.ThemeModeChanged("system")) },
                )
            }
            if (supportsDynamicColor) {
                Spacer(modifier = Modifier.height(MovieAppSpacing.large))
                val wallpaperColorsLabel = stringResource(Res.string.theme_wallpaper_colors)
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .toggleable(
                                value = uiState.useWallpaperColors,
                                onValueChange = {
                                    viewModel.onEvent(SettingsUiEvent.WallpaperColorsChanged(it))
                                },
                                role = Role.Switch,
                            )
                            .semantics { this.contentDescription = wallpaperColorsLabel },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            wallpaperColorsLabel,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(Res.string.theme_wallpaper_colors_hint),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.useWallpaperColors,
                        onCheckedChange = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(label: String, mode: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
                .padding(vertical = MovieAppSpacing.itemGapCompact),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(MovieAppSpacing.medium))
        Row(
            modifier =
                Modifier.size(
                    width = MovieAppSpacing.themePreviewWidth,
                    height = MovieAppSpacing.themePreviewHeight,
                )
        ) {
            val schemes =
                when (mode) {
                    "light" -> listOf(CinematicLightColors)
                    "dark" -> listOf(CinematicDarkColors)
                    else -> listOf(CinematicLightColors, CinematicDarkColors)
                }
            schemes.forEach { colors ->
                Column(
                    modifier =
                        Modifier.weight(1f)
                            .height(MovieAppSpacing.themePreviewHeight)
                            .background(colors.background)
                            .padding(MovieAppSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
                ) {
                    Box(
                        Modifier.fillMaxWidth()
                            .height(MovieAppSpacing.extraSmall)
                            .background(colors.primary)
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .weight(1f)
                            .background(colors.surfaceVariant, MaterialTheme.shapes.extraSmall)
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .height(MovieAppSpacing.extraSmall)
                            .background(colors.onSurface)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(MovieAppSpacing.medium))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}
