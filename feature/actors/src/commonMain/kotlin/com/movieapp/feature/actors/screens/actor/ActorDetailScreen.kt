package com.movieapp.feature.actors.screens.actor

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.actor_born_on
import com.movieapp.core.ui.resources.actor_died_on
import com.movieapp.core.ui.resources.actor_from
import com.movieapp.core.ui.resources.cd_actor_photo
import com.movieapp.core.ui.resources.cd_add_favorite
import com.movieapp.core.ui.resources.cd_back
import com.movieapp.core.ui.resources.cd_remove_favorite
import com.movieapp.core.ui.resources.month_names_short
import com.movieapp.core.ui.resources.detail_favorite
import com.movieapp.core.ui.resources.detail_favorited
import com.movieapp.core.ui.resources.detail_overview_unavailable
import com.movieapp.core.ui.resources.detail_photos
import com.movieapp.core.ui.resources.detail_view_gallery
import com.movieapp.core.ui.resources.favorite_added_message
import com.movieapp.core.ui.resources.favorite_removed_message
import com.movieapp.core.ui.resources.favorite_update_failed_message
import com.movieapp.core.ui.resources.label_biography
import com.movieapp.core.ui.resources.label_movies_subsection
import com.movieapp.core.ui.resources.label_tv_series_subsection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movieapp.core.common.formatShort
import com.movieapp.core.common.toLocalDateOrNull
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.ui.components.AppErrorState
import com.movieapp.core.ui.components.ContentSection
import com.movieapp.core.ui.components.ExpandableDetailText
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.components.shimmer.rememberShimmerBrush
import com.movieapp.core.ui.designsystem.MovieAppHeroColors
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.heroScrimBrush
import com.movieapp.core.ui.utils.toImageUrl

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActorDetailScreen(
    actorId: Int,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onOpenImageGallery: (Int) -> Unit = {},
    viewModel: ActorDetailViewModel = koinViewModel { parametersOf(actorId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ActorDetailSideEffect.FavoriteUpdated ->
                    snackbarHostState.showSnackbar(
                        getString(
                            if (effect.isFavorite) {
                                CoreRes.string.favorite_added_message
                            } else {
                                CoreRes.string.favorite_removed_message
                            }
                        )
                    )
                ActorDetailSideEffect.FavoriteUpdateFailed ->
                    snackbarHostState.showSnackbar(
                        getString(CoreRes.string.favorite_update_failed_message)
                    )
            }
        }
    }

    when {
        uiState.isLoading && uiState.actor == null -> {
            ActorDetailSkeleton()
        }

        uiState.error != null && uiState.actor == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.onEvent(ActorDetailUiEvent.Retry) },
                )
            }
        }

        uiState.actor != null -> {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ActorDetailTopBar(title = uiState.actor!!.name, onBackClick = onBackClick)
                },
            ) { paddingValues ->
                ActorDetailContent(
                    actor = uiState.actor!!,
                    isFavorite = uiState.isFavorite,
                    isFavoriteMutationPending = uiState.isFavoriteMutationPending,
                    movieCredits = uiState.movieCredits,
                    tvCredits = uiState.tvCredits,
                    onToggleFavorite = { viewModel.onEvent(ActorDetailUiEvent.ToggleFavorite) },
                    onMovieClick = onMovieClick,
                    onSeriesClick = onSeriesClick,
                    onOpenImageGallery = onOpenImageGallery,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }

        else -> {
            ActorDetailSkeleton()
        }
    }
}

@Composable
internal fun ActorDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = MovieAppSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(MovieAppSpacing.minTouchTarget),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(CoreRes.string.cd_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(end = MovieAppSpacing.medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun ActorDetailSkeleton() {
    val brush = rememberShimmerBrush()
    Column(
        modifier = Modifier.fillMaxSize().padding(MovieAppSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Spacer(modifier = Modifier.height(MovieAppSpacing.large))
        Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(brush))
        Box(
            modifier =
                Modifier.width(180.dp)
                    .height(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(brush)
        )
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(brush)
        )
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(brush)
        )
        Box(
            modifier =
                Modifier.fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(brush)
        )
    }
}

@Composable
internal fun ActorDetailContent(
    actor: Actor,
    isFavorite: Boolean,
    movieCredits: List<Movie>,
    tvCredits: List<TvSeries>,
    onToggleFavorite: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onOpenImageGallery: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isFavoriteMutationPending: Boolean = false,
) {
    val biography = actor.biography.orEmpty()
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("actor_detail_list"),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        item(key = "hero", contentType = "hero") {
            ActorIdentityHero(
                actor = actor,
                isFavorite = isFavorite,
                isFavoriteMutationPending = isFavoriteMutationPending,
                onToggleFavorite = onToggleFavorite,
            )
        }

        // Biography comes immediately after the identity header.
        if (biography.isNotBlank()) {
            item(key = "biography", contentType = "section") {
                ContentSection(
                    title = stringResource(CoreRes.string.label_biography),
                    modifier = Modifier.testTag("actor_biography"),
                ) {
                    ExpandableDetailText(
                        text = biography,
                        emptyText = stringResource(CoreRes.string.detail_overview_unavailable),
                    )
                }
            }
        }

        item(key = "photos", contentType = "section") {
            ContentSection(
                title = stringResource(CoreRes.string.detail_photos),
                modifier = Modifier.testTag("actor_photos"),
            ) {
                TextButton(
                    onClick = { onOpenImageGallery(actor.id) },
                    modifier = Modifier.heightIn(min = MovieAppSpacing.minTouchTarget),
                ) {
                    Text(stringResource(CoreRes.string.detail_view_gallery))
                }
            }
        }

        // Movie Credits
        if (movieCredits.isNotEmpty()) {
            item(key = "movie_credits", contentType = "credits") {
                ContentSection(title = stringResource(CoreRes.string.label_movies_subsection)) {
                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                        contentPadding =
                            PaddingValues(horizontal = MovieAppSpacing.screenHorizontal),
                    ) {
                        items(
                            movieCredits.take(20),
                            key = { it.id },
                            contentType = { "movie_credit" },
                        ) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                modifier = Modifier.width(MovieAppSpacing.posterWidth),
                            )
                        }
                    }
                }
            }
        }

        // TV Credits
        if (tvCredits.isNotEmpty()) {
            item(key = "tv_credits", contentType = "credits") {
                ContentSection(title = stringResource(CoreRes.string.label_tv_series_subsection)) {
                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                        contentPadding =
                            PaddingValues(horizontal = MovieAppSpacing.screenHorizontal),
                    ) {
                        items(
                            tvCredits.take(20),
                            key = { it.id },
                            contentType = { "series_credit" },
                        ) { series ->
                            SeriesCard(
                                series = series,
                                onClick = { onSeriesClick(series.id) },
                                modifier = Modifier.width(MovieAppSpacing.posterWidth),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActorIdentityHero(
    actor: Actor,
    isFavorite: Boolean,
    isFavoriteMutationPending: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().heightIn(min = MovieAppSpacing.actorHeroHeightDp)) {
        AsyncImage(
            model = actor.profilePath?.toImageUrl(),
            contentDescription = stringResource(CoreRes.string.cd_actor_photo, actor.name),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(modifier = Modifier.fillMaxSize().background(heroScrimBrush(scrimBottomAlpha = 0.78f)))
        Column(
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(MovieAppSpacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        ) {
            Text(
                text = actor.name,
                modifier = Modifier.fillMaxWidth().testTag("actor_identity_name"),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MovieAppHeroColors.foreground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            ActorFacts(actor = actor)
            Button(
                onClick = onToggleFavorite,
                enabled = !isFavoriteMutationPending,
                modifier = Modifier.heightIn(min = MovieAppSpacing.minTouchTarget),
            ) {
                Icon(
                    imageVector =
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription =
                        stringResource(
                            if (isFavorite) CoreRes.string.cd_remove_favorite
                            else CoreRes.string.cd_add_favorite
                        ),
                )
                Text(
                    stringResource(
                        if (isFavorite) CoreRes.string.detail_favorited
                        else CoreRes.string.detail_favorite
                    )
                )
            }
        }
    }
}

@Composable
private fun ActorFacts(actor: Actor, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.extraSmall),
    ) {
        if (actor.knownForDepartment.isNotBlank()) {
            Text(
                text = actor.knownForDepartment,
                style = MaterialTheme.typography.bodyLarge,
                color = MovieAppHeroColors.foreground,
            )
        }
        val monthNames = stringArrayResource(CoreRes.array.month_names_short)
        actor.birthday?.let { birthday ->
            Text(
                text = stringResource(CoreRes.string.actor_born_on, birthday.toLocalDateOrNull()?.formatShort(monthNames) ?: birthday),
                style = MaterialTheme.typography.bodyMedium,
                color = MovieAppHeroColors.foreground.copy(alpha = 0.82f),
            )
        }
        actor.deathday?.let { deathday ->
            Text(
                text = stringResource(CoreRes.string.actor_died_on, deathday.toLocalDateOrNull()?.formatShort(monthNames) ?: deathday),
                style = MaterialTheme.typography.bodyMedium,
                color = MovieAppHeroColors.foreground.copy(alpha = 0.82f),
            )
        }
        actor.placeOfBirth
            ?.takeIf { it.isNotBlank() }
            ?.let { place ->
                Text(
                    text = stringResource(CoreRes.string.actor_from, place),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovieAppHeroColors.foreground.copy(alpha = 0.82f),
                )
            }
    }
}
