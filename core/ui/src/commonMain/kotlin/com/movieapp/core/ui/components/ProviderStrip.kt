package com.movieapp.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.utils.toImageUrl

@Composable
fun ProviderStrip(
    providers: List<WatchProvider>,
    regionLink: String?,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        providers.forEach { provider ->
            AssistChip(
                onClick = {
                    val url = regionLink?.takeIf { it.isNotBlank() } ?: "https://www.themoviedb.org"
                    uriHandler.openUri(url)
                },
                label = {
                    Text(text = provider.providerName, style = MaterialTheme.typography.labelLarge)
                },
                leadingIcon = {
                    AsyncImage(
                        model = provider.logoPath.toImageUrl(),
                        contentDescription = provider.providerName,
                        modifier = Modifier.size(24.dp),
                    )
                },
                colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
        }
    }
}
