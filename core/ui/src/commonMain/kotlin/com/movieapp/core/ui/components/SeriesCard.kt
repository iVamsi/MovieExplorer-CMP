@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.movieapp.core.ui.components

import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.cd_no_image
import com.movieapp.core.ui.resources.cd_series_poster
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.movieapp.core.common.Constants
import com.movieapp.core.model.TvSeries
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.cinematicPressFeedback
import com.movieapp.core.ui.theme.CinematicTheme
import com.movieapp.core.ui.utils.toImageUrl

@Composable
fun SeriesCard(series: TvSeries, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactions = remember { MutableInteractionSource() }
    val sharedContentKey = seriesSharedContentKey(series.id)
    val sharedSourceToken = remember { Any() }
    val sharedOnClick =
        rememberCinematicSharedContentClick(sharedContentKey, sharedSourceToken, onClick)
    Card(
        onClick = sharedOnClick,
        modifier =
            modifier
                .cinematicSharedBounds(sharedContentKey, sharedSourceToken)
                .cinematicPressFeedback(interactions),
        interactionSource = interactions,
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .aspectRatio(MovieAppSpacing.posterAspectRatio)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val posterPath = series.posterPath ?: series.backdropPath
                val imageSize =
                    if (series.posterPath != null) Constants.ImageSize.POSTER_MEDIUM
                    else Constants.ImageSize.BACKDROP_MEDIUM
                val imageUrl = posterPath?.toImageUrl(imageSize)
                val context = LocalPlatformContext.current

                if (imageUrl != null) {
                    AsyncImage(
                        model =
                            ImageRequest.Builder(context)
                                .data(imageUrl)
                                .size(Size(500, 750))
                                .crossfade(false)
                                .build(),
                        contentDescription = stringResource(Res.string.cd_series_poster, series.name),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.BrokenImage,
                            contentDescription = stringResource(Res.string.cd_no_image),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(MovieAppSpacing.spacious),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.small),
                verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.extraSmall),
            ) {
                Text(
                    text = series.name,
                    style = MaterialTheme.typography.bodyMediumEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2,
                )

                PosterMetadata(releaseDate = series.firstAirDate, rating = series.voteAverage)
            }
        }
    }
}

@Preview(name = "Series light", widthDp = 144, showBackground = true)
@Preview(
    name = "Series narrow and large text",
    widthDp = 120,
    fontScale = 2.0f,
    showBackground = true,
)
@Composable
private fun SeriesCardPreview() {
    CinematicTheme {
        SeriesCard(
            series =
                TvSeries(
                    id = 2,
                    name = "A Very Long Series Title Across Two Lines",
                    firstAirDate = "2026-06-01",
                    voteAverage = 8.4,
                ),
            onClick = {},
        )
    }
}

@Preview(name = "Series dark", widthDp = 144, showBackground = true)
@Composable
private fun SeriesCardDarkPreview() {
    CinematicTheme(darkTheme = true) {
        SeriesCard(
            series =
                TvSeries(
                    id = 2,
                    name = "A Very Long Series Title Across Two Lines",
                    firstAirDate = "2026-06-01",
                    voteAverage = 8.4,
                ),
            onClick = {},
        )
    }
}
