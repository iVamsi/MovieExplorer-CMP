package com.movieapp.core.ui.components

import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.cd_open_in_browser
import com.movieapp.core.ui.resources.ic_tmdb_logo
import com.movieapp.core.ui.resources.tmdb_api_legal_notice
import com.movieapp.core.ui.resources.tmdb_logo
import com.movieapp.core.ui.resources.tmdb_open_website
import com.movieapp.core.ui.resources.tmdb_powered_by
import com.movieapp.core.ui.resources.tmdb_website_display
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movieapp.core.ui.designsystem.MovieAppSpacing

/**
 * Displays all TMDB attribution required by the TMDB API Terms of Use (Section 3).
 *
 * Requirements met:
 * 1. Official TMDB "Primary long (blue)" SVG logo is used to identify use of TMDB. Source:
 *    https://www.themoviedb.org/about/logos-attribution
 * 2. Logo is less prominent than the app's own branding.
 * 3. The required verbatim legal notice is displayed prominently.
 *
 * See https://www.themoviedb.org/api-terms-of-use
 */
@Composable
fun TmdbAttributionSection(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
        ) {
            Text(
                text = stringResource(Res.string.tmdb_powered_by),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Official TMDB "Primary long (blue)" logo (ic_tmdb_logo.xml).
            // Sourced from https://www.themoviedb.org/about/logos-attribution
            // Per ToS the logo must be less prominent than the app's own branding.
            // Tapping opens themoviedb.org.
            // 48dp interactive target; logo artwork stays visually smaller per TMDB ToS.
            Box(
                modifier =
                    Modifier.minimumInteractiveComponentSize().clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(Res.string.tmdb_open_website),
                    ) {
                        uriHandler.openUri("https://www.themoviedb.org")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_tmdb_logo),
                    contentDescription = stringResource(Res.string.tmdb_logo),
                    modifier = Modifier.height(20.dp),
                )
            }

            // Required verbatim notice — TMDB API ToS Section 3
            Text(
                text = stringResource(Res.string.tmdb_api_legal_notice),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Clickable link to TMDB website
            TextButton(
                onClick = {
                    uriHandler.openUri("https://www.themoviedb.org")
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInBrowser,
                    contentDescription = stringResource(Res.string.cd_open_in_browser),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.tmdb_website_display),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
