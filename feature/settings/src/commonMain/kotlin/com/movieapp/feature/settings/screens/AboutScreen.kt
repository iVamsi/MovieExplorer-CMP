package com.movieapp.feature.settings.screens

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.settings_about
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.about_app_name
import com.movieapp.feature.settings.resources.about_open_source_licenses
import com.movieapp.feature.settings.resources.about_rate_app
import com.movieapp.feature.settings.resources.about_version_label
import com.movieapp.feature.settings.resources.about_view_github
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.movieapp.core.ui.components.TmdbAttributionSection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(versionName: String, onBackClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = stringResource(CoreRes.string.settings_about),
                onBackClick = onBackClick,
            )
        }
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(
                        horizontal =
                            com.movieapp.core.ui.designsystem.MovieAppSpacing.screenHorizontal
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp),
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp).size(48.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.about_app_name),
                style = MaterialTheme.typography.headlineSmallEmphasized,
            )
            Text(
                text = stringResource(Res.string.about_version_label, versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column {
                    AboutLinkRow(
                        icon = Icons.Filled.Star,
                        label = stringResource(Res.string.about_rate_app),
                        onClick = {
                            uriHandler.openUri("https://play.google.com/store")
                        },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutLinkRow(
                        icon = Icons.Filled.Code,
                        label = stringResource(Res.string.about_view_github),
                        onClick = {
                            uriHandler.openUri("https://github.com")
                        },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutLinkRow(
                        icon = Icons.Filled.Info,
                        label = stringResource(Res.string.about_open_source_licenses),
                        onClick = {
                            uriHandler.openUri("https://github.com/iVamsi/MovieExplorer-CMP/blob/main/LICENSE")
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            TmdbAttributionSection()
        }
    }
}

@Composable
private fun AboutLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}
