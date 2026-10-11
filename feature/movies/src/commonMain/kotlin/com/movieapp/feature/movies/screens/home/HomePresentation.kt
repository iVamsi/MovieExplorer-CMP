@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.movieapp.feature.movies.screens.home

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.action_retry
import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.home_section_empty
import com.movieapp.feature.movies.resources.home_section_load_error
import com.movieapp.feature.movies.resources.home_spotlight_metadata
import com.movieapp.feature.movies.resources.home_view_details
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.stringResource
import coil3.compose.AsyncImage
import com.movieapp.core.common.Constants
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.designsystem.MovieAppHeroColors
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.cinematicHeroScrimBrush
import com.movieapp.core.ui.utils.toImageUrl

@Composable
internal fun CinematicSpotlight(
    title: String,
    backdropPath: String,
    category: String,
    year: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MovieAppSpacing.detailHeroSkeletonHeightDp)
                .clip(MaterialTheme.shapes.extraLarge)
    ) {
        AsyncImage(
            model = backdropPath.toImageUrl(Constants.ImageSize.BACKDROP_LARGE),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(Modifier.matchParentSize().background(cinematicHeroScrimBrush()))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(MovieAppSpacing.large)) {
            Text(
                text =
                    if (year.isBlank()) {
                        category
                    } else {
                        stringResource(Res.string.home_spotlight_metadata, category, year)
                    },
                color = MovieAppHeroColors.onSurface.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = title,
                color = MovieAppHeroColors.onSurface,
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )
            Button(
                onClick = onClick,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MovieAppHeroColors.accent,
                        contentColor = MovieAppHeroColors.surface,
                    ),
            ) {
                Text(stringResource(Res.string.home_view_details))
            }
        }
    }
}

@Composable
internal fun LandscapeMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(MovieAppSpacing.posterWidth * 1.7f)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(MovieAppSpacing.posterCorner))
                .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = movie.backdropPath.toImageUrl(Constants.ImageSize.BACKDROP_MEDIUM),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(Modifier.matchParentSize().background(cinematicHeroScrimBrush()))
        Text(
            text = movie.title,
            modifier = Modifier.align(Alignment.BottomStart).padding(MovieAppSpacing.small),
            color = MovieAppHeroColors.onSurface,
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
    }
}

@Composable
internal fun LandscapeSeriesCard(
    series: TvSeries,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(MovieAppSpacing.posterWidth * 1.7f)
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = series.backdropPath.toImageUrl(Constants.ImageSize.BACKDROP_MEDIUM),
            contentDescription = series.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(Modifier.matchParentSize().background(cinematicHeroScrimBrush()))
        Text(
            text = series.name,
            modifier = Modifier.align(Alignment.BottomStart).padding(MovieAppSpacing.small),
            color = MovieAppHeroColors.onSurface,
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
    }
}

@Composable
internal fun RankedMovieCard(
    rank: Int,
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            rank.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        MovieCard(
            movie = movie,
            onClick = onClick,
            modifier = Modifier.width(MovieAppSpacing.posterWidth),
        )
    }
}

@Composable
internal fun RankedSeriesCard(
    rank: Int,
    series: TvSeries,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            rank.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        com.movieapp.core.ui.components.SeriesCard(
            series = series,
            onClick = onClick,
            modifier = Modifier.width(MovieAppSpacing.posterWidth),
        )
    }
}

@Composable
internal fun HomeFeedError(
    title: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = MovieAppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.home_section_load_error, title),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(CoreRes.string.action_retry))
        }
    }
}

@Composable
internal fun HomeFeedEmpty(title: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.home_section_empty, title),
        modifier = modifier.fillMaxWidth().padding(vertical = MovieAppSpacing.small),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}
