package com.movieapp.feature.series.screens.series

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.platform.rememberShareText
import com.movieapp.core.ui.resources.browse_load_error
import com.movieapp.core.ui.resources.clear_user_rating
import com.movieapp.core.ui.resources.detail_cast
import com.movieapp.core.ui.resources.detail_cast_unavailable
import com.movieapp.core.ui.resources.detail_details
import com.movieapp.core.ui.resources.detail_keywords
import com.movieapp.core.ui.resources.detail_label_episodes
import com.movieapp.core.ui.resources.detail_label_networks
import com.movieapp.core.ui.resources.detail_label_status
import com.movieapp.core.ui.resources.detail_links
import com.movieapp.core.ui.resources.detail_loading_providers
import com.movieapp.core.ui.resources.detail_loading_short
import com.movieapp.core.ui.resources.detail_overview
import com.movieapp.core.ui.resources.detail_overview_unavailable
import com.movieapp.core.ui.resources.detail_photos
import com.movieapp.core.ui.resources.detail_providers_unavailable
import com.movieapp.core.ui.resources.detail_recommended
import com.movieapp.core.ui.resources.detail_related_unavailable
import com.movieapp.core.ui.resources.detail_reviews
import com.movieapp.core.ui.resources.detail_reviews_unavailable
import com.movieapp.core.ui.resources.detail_seasons
import com.movieapp.core.ui.resources.detail_similar
import com.movieapp.core.ui.resources.detail_view_gallery
import com.movieapp.core.ui.resources.detail_where_to_watch
import com.movieapp.core.ui.resources.detail_your_rating
import com.movieapp.core.ui.resources.favorite_added_message
import com.movieapp.core.ui.resources.favorite_removed_message
import com.movieapp.core.ui.resources.favorite_update_failed_message
import com.movieapp.core.ui.resources.open_in_imdb
import com.movieapp.core.ui.resources.share_series_template
import com.movieapp.core.ui.resources.watchlist_added_message
import com.movieapp.core.ui.resources.watchlist_removed_message
import com.movieapp.core.ui.resources.watchlist_update_failed_message
import com.movieapp.feature.series.resources.Res
import com.movieapp.feature.series.resources.cd_genre
import com.movieapp.feature.series.resources.cd_keyword
import com.movieapp.feature.series.resources.cd_open_actor_profile
import com.movieapp.feature.series.resources.cd_series_backdrop
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.movieapp.core.common.formatWithCommas
import com.movieapp.core.model.CastMember
import com.movieapp.core.model.Network
import com.movieapp.core.model.Review
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.Video
import com.movieapp.core.ui.components.AddToCollectionBottomSheet
import com.movieapp.core.ui.components.AppErrorState
import com.movieapp.core.ui.components.AppInlineErrorState
import com.movieapp.core.ui.components.ContentSection
import com.movieapp.core.ui.components.DetailActionsSheet
import com.movieapp.core.ui.components.DetailIdentityTransition
import com.movieapp.core.ui.components.DetailPrimaryActions
import com.movieapp.core.ui.components.DetailTopBar
import com.movieapp.core.ui.components.ExpandableDetailText
import com.movieapp.core.ui.components.OfflineCachedHint
import com.movieapp.core.ui.components.ProviderStrip
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.components.UserRatingControl
import com.movieapp.core.ui.components.seriesSharedContentKey
import com.movieapp.core.ui.components.shimmer.rememberShimmerBrush
import com.movieapp.core.ui.designsystem.MovieAppHeroColors
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.heroDetailBackdropScrimBrush
import com.movieapp.core.ui.designsystem.motionDurationMillis
import com.movieapp.core.ui.utils.formatRating
import com.movieapp.core.ui.utils.formatReleaseYear
import com.movieapp.core.ui.utils.toImageUrl
import kotlinx.coroutines.flow.Flow

@Composable
fun SeriesDetailScreen(
    seriesId: Int,
    onBackClick: () -> Unit,
    onSeriesClick: (Int) -> Unit,
    onActorClick: (Int) -> Unit,
    onOpenImageGallery: (Int) -> Unit = {},
    viewModel: SeriesDetailViewModel = koinViewModel { parametersOf(seriesId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SeriesDetailSideEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
                is SeriesDetailSideEffect.FavoriteUpdated ->
                    snackbarHostState.showSnackbar(getString(
                                if (effect.isFavorite) {
                                    CoreRes.string.favorite_added_message
                                } else {
                                    CoreRes.string.favorite_removed_message
                                }
                            ))
                SeriesDetailSideEffect.FavoriteUpdateFailed ->
                    snackbarHostState.showSnackbar(getString(CoreRes.string.favorite_update_failed_message))
                is SeriesDetailSideEffect.WatchlistUpdated ->
                    snackbarHostState.showSnackbar(getString(
                                if (effect.isWatchlisted) {
                                    CoreRes.string.watchlist_added_message
                                } else {
                                    CoreRes.string.watchlist_removed_message
                                }
                            ))
                SeriesDetailSideEffect.WatchlistUpdateFailed ->
                    snackbarHostState.showSnackbar(getString(CoreRes.string.watchlist_update_failed_message))
            }
        }
    }

    when {
        uiState.isLoading && uiState.series == null -> SeriesDetailSkeleton()

        uiState.error != null && uiState.series == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.onEvent(SeriesDetailUiEvent.Retry) },
                )
            }
        }

        uiState.series != null -> {
            SeriesDetailContent(
                uiState = uiState,
                similarSeriesFlow = viewModel.similarSeries,
                recommendedSeriesFlow = viewModel.recommendedSeries,
                reviewsFlow = viewModel.reviews,
                onEvent = viewModel::onEvent,
                onBackClick = onBackClick,
                onSeriesClick = onSeriesClick,
                onActorClick = onActorClick,
                onOpenImageGallery = onOpenImageGallery,
                snackbarHostState = snackbarHostState,
            )
        }

        else -> SeriesDetailSkeleton()
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
internal fun SeriesDetailContent(
    uiState: SeriesDetailUiState,
    similarSeriesFlow: Flow<PagingData<TvSeries>>,
    recommendedSeriesFlow: Flow<PagingData<TvSeries>>,
    reviewsFlow: Flow<PagingData<Review>>,
    onEvent: (SeriesDetailUiEvent) -> Unit,
    onBackClick: () -> Unit,
    onSeriesClick: (Int) -> Unit,
    onActorClick: (Int) -> Unit,
    onOpenImageGallery: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val series = uiState.series!!
    val similarItems = similarSeriesFlow.collectAsLazyPagingItems()
    val recommendedItems = recommendedSeriesFlow.collectAsLazyPagingItems()
    val reviewItems = reviewsFlow.collectAsLazyPagingItems()
    val uriHandler = LocalUriHandler.current
    val share = rememberShareText()
    val shareText = stringResource(CoreRes.string.share_series_template, series.name, series.id)
    val listState = rememberLazyListState()
    val parallaxEnabled = motionDurationMillis() > 0
    val backdropUrl = series.backdropPath?.toImageUrl()
    var showMoreActions by remember { mutableStateOf(false) }
    val heroParallaxOffset by
        remember(parallaxEnabled) {
            derivedStateOf {
                if (parallaxEnabled && listState.firstVisibleItemIndex == 0) {
                    listState.firstVisibleItemScrollOffset * 0.5f
                } else {
                    0f
                }
            }
        }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(
                title = series.name,
                onBackClick = onBackClick,
                onMoreClick = { showMoreActions = true },
            )
        },
    ) { paddingValues ->
        val trailer =
            series.videos
                ?.results
                ?.filter { it.site == "YouTube" }
                ?.let { ytVideos ->
                    ytVideos.firstOrNull { it.type == "Trailer" && it.official }
                        ?: ytVideos.firstOrNull { it.type == "Trailer" }
                        ?: ytVideos.firstOrNull { it.official }
                        ?: ytVideos.firstOrNull()
                }

        val detailRefreshing =
            listOf(similarItems, recommendedItems, reviewItems).any {
                it.loadState.refresh is LoadState.Loading && it.itemCount > 0
            }

        PullToRefreshBox(
            isRefreshing = detailRefreshing,
            onRefresh = {
                similarItems.refresh()
                recommendedItems.refresh()
                reviewItems.refresh()
            },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
            ) {
                item(key = "hero", contentType = "hero") {
                    DetailIdentityTransition(contentKey = seriesSharedContentKey(series.id)) {
                        SeriesHeroSection(
                            series = series,
                            trailer = trailer,
                            onShowTrailer = {
                                trailer?.let { uriHandler.openUri("https://www.youtube.com/watch?v=${it.key}") }
                            },
                            backdropUrl = backdropUrl,
                            heroParallaxOffset = heroParallaxOffset,
                            isWatchlisted = uiState.isWatchlisted,
                            isWatchlistMutationPending = uiState.isWatchlistMutationPending,
                            onToggleWatchlist = { onEvent(SeriesDetailUiEvent.ToggleWatchlist) },
                            isFavorite = uiState.isFavorite,
                            isFavoriteMutationPending = uiState.isFavoriteMutationPending,
                            onToggleFavorite = { onEvent(SeriesDetailUiEvent.ToggleFavorite) },
                            onRate = {
                                listState.requestScrollToItem(if (uiState.fromCacheOnly) 5 else 4)
                            },
                        )
                    }
                }

                // Keep the synopsis in the initial reading path, before supporting metadata.
                item(key = "overview", contentType = "section") {
                    SeriesOverviewSection(
                        overview = series.overview,
                        modifier = Modifier.testTag("detail_overview"),
                    )
                }

                if (uiState.fromCacheOnly) {
                    item(key = "offline_hint", contentType = "hint") { OfflineCachedHint() }
                }

                item(key = "watch_providers", contentType = "section") {
                    ContentSection(title = stringResource(CoreRes.string.detail_where_to_watch)) {
                        when {
                            uiState.watchProvidersLoading ->
                                Text(stringResource(CoreRes.string.detail_loading_providers))
                            uiState.watchProvidersForRegion.isNotEmpty() ->
                                ProviderStrip(
                                    providers = uiState.watchProvidersForRegion,
                                    regionLink = uiState.watchProviderLink,
                                )
                            else ->
                                Text(stringResource(CoreRes.string.detail_providers_unavailable))
                        }
                    }
                }

                item(key = "cast", contentType = "section") {
                    ContentSection(title = stringResource(CoreRes.string.detail_cast)) {
                        val cast = uiState.credits?.cast?.take(24).orEmpty()
                        if (cast.isEmpty())
                            Text(stringResource(CoreRes.string.detail_cast_unavailable))
                        else
                            LazyRow(
                                horizontalArrangement =
                                    Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                            ) {
                                items(cast, key = { it.id }, contentType = { "cast" }) { member ->
                                    SeriesCastChip(
                                        member = member,
                                        onClick = { onActorClick(member.id) },
                                    )
                                }
                            }
                    }
                }

                item(key = "rating", contentType = "section") {
                    ContentSection(title = stringResource(CoreRes.string.detail_your_rating)) {
                        UserRatingControl(
                            ratingOutOfFive = uiState.userRatingOutOfFive,
                            onRatingSelected = { stars ->
                                onEvent(SeriesDetailUiEvent.SetUserRating(stars))
                            },
                        )
                        TextButton(onClick = { onEvent(SeriesDetailUiEvent.ClearUserRating) }) {
                            Text(stringResource(CoreRes.string.clear_user_rating))
                        }
                    }
                }

                item(key = "reviews_header", contentType = "header") {
                    Text(
                        stringResource(CoreRes.string.detail_reviews),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        modifier = Modifier.padding(horizontal = MovieAppSpacing.screenHorizontal),
                    )
                }
                if (reviewItems.loadState.refresh is LoadState.Error) {
                    item(key = "reviews_error", contentType = "error") {
                        AppInlineErrorState(
                            message = stringResource(CoreRes.string.browse_load_error),
                            onRetry = reviewItems::retry,
                            modifier =
                                Modifier.padding(horizontal = MovieAppSpacing.screenHorizontal),
                        )
                    }
                } else if (
                    reviewItems.itemCount == 0 && reviewItems.loadState.refresh is LoadState.Loading
                ) {
                    item(key = "reviews_loading", contentType = "loading") {
                        Text(
                            stringResource(CoreRes.string.detail_loading_short),
                            modifier =
                                Modifier.padding(horizontal = MovieAppSpacing.screenHorizontal),
                        )
                    }
                } else if (
                    reviewItems.itemCount == 0 &&
                        reviewItems.loadState.refresh is LoadState.NotLoading
                ) {
                    item(key = "reviews_empty", contentType = "empty") {
                        Text(
                            stringResource(CoreRes.string.detail_reviews_unavailable),
                            modifier =
                                Modifier.padding(horizontal = MovieAppSpacing.screenHorizontal),
                        )
                    }
                }
                items(
                    count = reviewItems.itemCount,
                    key = reviewItems.itemKey { it.id },
                    contentType = { "review" },
                ) { index ->
                    reviewItems[index]?.let { review ->
                        Column(
                            Modifier.fillMaxWidth()
                                .padding(horizontal = MovieAppSpacing.screenHorizontal)
                        ) {
                            Text(review.author, style = MaterialTheme.typography.titleSmall)
                            ExpandableDetailText(
                                text = review.content,
                                emptyText =
                                    stringResource(CoreRes.string.detail_reviews_unavailable),
                                collapsedLines = 4,
                            )
                        }
                    }
                }
                if (reviewItems.loadState.append is LoadState.Error) {
                    item(key = "reviews_append_error", contentType = "error") {
                        AppInlineErrorState(
                            message = stringResource(CoreRes.string.browse_load_error),
                            onRetry = reviewItems::retry,
                            modifier =
                                Modifier.padding(horizontal = MovieAppSpacing.screenHorizontal),
                        )
                    }
                }

                item(key = "photos", contentType = "section") {
                    ContentSection(title = stringResource(CoreRes.string.detail_photos)) {
                        TextButton(onClick = { onOpenImageGallery(series.id) }) {
                            Text(stringResource(CoreRes.string.detail_view_gallery))
                        }
                    }
                }

                item(key = "similar", contentType = "related") {
                    ContentSection(title = stringResource(CoreRes.string.detail_similar)) {
                        when {
                            similarItems.loadState.refresh is LoadState.Error ||
                                similarItems.loadState.append is LoadState.Error ->
                                AppInlineErrorState(
                                    message = stringResource(CoreRes.string.browse_load_error),
                                    onRetry = similarItems::retry,
                                )
                            similarItems.itemCount == 0 &&
                                similarItems.loadState.refresh is LoadState.Loading ->
                                Text(stringResource(CoreRes.string.detail_loading_short))
                            similarItems.itemCount == 0 &&
                                similarItems.loadState.refresh is LoadState.NotLoading ->
                                Text(stringResource(CoreRes.string.detail_related_unavailable))
                        }
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = similarItems.itemCount,
                                key = similarItems.itemKey { it.id },
                                contentType = { "series" },
                            ) { index ->
                                similarItems[index]?.let { s ->
                                    SeriesCard(
                                        series = s,
                                        onClick = { onSeriesClick(s.id) },
                                        modifier = Modifier.width(150.dp),
                                    )
                                }
                            }
                        }
                        similarItems.loadState.append
                            .takeIf { it is LoadState.Loading }
                            ?.let {
                                Text(
                                    stringResource(CoreRes.string.detail_loading_short),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                    }
                }

                item(key = "recommended", contentType = "related") {
                    ContentSection(title = stringResource(CoreRes.string.detail_recommended)) {
                        when {
                            recommendedItems.loadState.refresh is LoadState.Error ||
                                recommendedItems.loadState.append is LoadState.Error ->
                                AppInlineErrorState(
                                    message = stringResource(CoreRes.string.browse_load_error),
                                    onRetry = recommendedItems::retry,
                                )
                            recommendedItems.itemCount == 0 &&
                                recommendedItems.loadState.refresh is LoadState.Loading ->
                                Text(stringResource(CoreRes.string.detail_loading_short))
                            recommendedItems.itemCount == 0 &&
                                recommendedItems.loadState.refresh is LoadState.NotLoading ->
                                Text(stringResource(CoreRes.string.detail_related_unavailable))
                        }
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = recommendedItems.itemCount,
                                key = recommendedItems.itemKey { it.id },
                                contentType = { "series" },
                            ) { index ->
                                recommendedItems[index]?.let { s ->
                                    SeriesCard(
                                        series = s,
                                        onClick = { onSeriesClick(s.id) },
                                        modifier = Modifier.width(150.dp),
                                    )
                                }
                            }
                        }
                        recommendedItems.loadState.append
                            .takeIf { it is LoadState.Loading }
                            ?.let {
                                Text(
                                    stringResource(CoreRes.string.detail_loading_short),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                    }
                }

                item(key = "details", contentType = "metadata") {
                    SeriesDetailsSection(
                        status = series.status,
                        numberOfEpisodes = series.numberOfEpisodes,
                        networks = series.networks,
                    )
                }

                if (uiState.keywords.isNotEmpty()) {
                    item(key = "keywords", contentType = "metadata") {
                        ContentSection(title = stringResource(CoreRes.string.detail_keywords)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
                                verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
                            ) {
                                for (kw in uiState.keywords) {
                                    val kwCd = stringResource(Res.string.cd_keyword, kw.name)
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.semantics { contentDescription = kwCd },
                                    ) {
                                        Text(
                                            text = kw.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = MovieAppSpacing.medium,
                                                    vertical = MovieAppSpacing.small,
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                uiState.externalIds?.imdbId?.let { imdb ->
                    item(key = "links", contentType = "metadata") {
                        ContentSection(title = stringResource(CoreRes.string.detail_links)) {
                            SuggestionChip(
                                onClick = {
                                    uriHandler.openUri("https://www.imdb.com/title/$imdb")
                                },
                                label = { Text(stringResource(CoreRes.string.open_in_imdb)) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddToCollection) {
        AddToCollectionBottomSheet(
            collections = uiState.collections,
            isSubmitting = uiState.isAddingToCollection,
            onDismiss = { onEvent(SeriesDetailUiEvent.DismissAddToCollection) },
            onSelectCollection = {
                onEvent(SeriesDetailUiEvent.AddToCollection(it.id))
            },
            onCreateAndAdd = { onEvent(SeriesDetailUiEvent.CreateCollectionAndAdd(it)) },
        )
    }
    DetailActionsSheet(
        visible = showMoreActions,
        isSavedOffline = uiState.isSavedOffline,
        isOfflineSaving = uiState.isOfflineSaving,
        onDismiss = { showMoreActions = false },
        onShare = { share(shareText) },
        onAddToCollection = { onEvent(SeriesDetailUiEvent.OpenAddToCollection) },
        onSaveOffline = { onEvent(SeriesDetailUiEvent.SaveForOffline) },
    )
}

@Composable
private fun SeriesHeroSection(
    series: TvSeries,
    trailer: Video?,
    onShowTrailer: () -> Unit,
    backdropUrl: String?,
    heroParallaxOffset: Float,
    isWatchlisted: Boolean,
    isWatchlistMutationPending: Boolean,
    onToggleWatchlist: () -> Unit,
    isFavorite: Boolean,
    isFavoriteMutationPending: Boolean,
    onToggleFavorite: () -> Unit,
    onRate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().heightIn(min = MovieAppSpacing.heroHeightDp)) {
        AsyncImage(
            model = backdropUrl,
            contentDescription = stringResource(Res.string.cd_series_backdrop, series.name),
            modifier =
                Modifier.fillMaxSize().graphicsLayer {
                    translationY = heroParallaxOffset
                    scaleX = 1.06f
                    scaleY = 1.06f
                },
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(heroDetailBackdropScrimBrush())
        )
        Column(
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(MovieAppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
                ) {
                    Text(
                        text = series.name,
                        modifier = Modifier.fillMaxWidth().testTag("detail_hero_title"),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = MovieAppHeroColors.foreground,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
                    ) {
                        series.firstAirDate?.let { date ->
                            Text(
                                text = date.formatReleaseYear(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MovieAppHeroColors.foreground.copy(alpha = 0.82f),
                            )
                        }
                        Text(
                            text = "★ ${series.voteAverage.formatRating()}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MovieAppHeroColors.foreground,
                        )
                        series.numberOfSeasons?.let { seasons ->
                            if (seasons > 0) {
                                Text(
                                    text =
                                        pluralStringResource(
                                            CoreRes.plurals.detail_seasons,
                                            seasons,
                                            seasons,
                                        ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MovieAppHeroColors.foreground.copy(alpha = 0.82f),
                                )
                            }
                        }
                    }
                    if (series.genres.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)
                        ) {
                            items(series.genres, key = { it.id }) { genre ->
                                val genreCd = stringResource(Res.string.cd_genre, genre.name)
                                Surface(
                                    modifier =
                                        Modifier.semantics { contentDescription = genreCd },
                                    shape = MaterialTheme.shapes.small,
                                    color =
                                        MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Text(
                                        text = genre.name,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp,
                                            ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MovieAppHeroColors.foreground,
                                    )
                                }
                            }
                        }
                    }
                }
                DetailPrimaryActions(
                    isWatchlisted = isWatchlisted,
                    isFavorite = isFavorite,
                    hasTrailer = trailer != null,
                    onToggleWatchlist = onToggleWatchlist,
                    onWatchTrailer = onShowTrailer,
                    onToggleFavorite = onToggleFavorite,
                    onRate = onRate,
                    watchlistEnabled = !isWatchlistMutationPending,
                    favoriteEnabled = !isFavoriteMutationPending,
                )
            }
        }
    }
}

@Composable
private fun SeriesOverviewSection(overview: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = MovieAppSpacing.screenHorizontal)) {
        Text(
            text = stringResource(CoreRes.string.detail_overview),
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExpandableDetailText(
            text = overview,
            emptyText = stringResource(CoreRes.string.detail_overview_unavailable),
        )
    }
}

@Composable
private fun SeriesDetailsSection(
    status: String?,
    numberOfEpisodes: Int?,
    networks: List<Network>,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = MovieAppSpacing.screenHorizontal)) {
        Text(
            text = stringResource(CoreRes.string.detail_details),
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
        status?.let {
            if (it.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
                ) {
                    Text(
                        stringResource(CoreRes.string.detail_label_status),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        numberOfEpisodes?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
            ) {
                Text(
                    stringResource(CoreRes.string.detail_label_episodes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(it.formatWithCommas(), style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (networks.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
            ) {
                Text(
                    stringResource(CoreRes.string.detail_label_networks),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    networks.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SeriesCastChip(member: CastMember, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val castCd = stringResource(Res.string.cd_open_actor_profile, member.name)
    Column(
        modifier =
            modifier.width(88.dp).clickable(onClick = onClick).semantics(mergeDescendants = true) {
                contentDescription = castCd
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = member.profilePath?.toImageUrl(),
            contentDescription = null,
            modifier = Modifier.size(72.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SeriesDetailSkeleton() {
    val brush = rememberShimmerBrush()
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(MovieAppSpacing.detailHeroSkeletonHeightDp)
                    .background(brush)
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
        ) {
            Box(
                modifier =
                    Modifier.fillMaxWidth(0.75f)
                        .height(28.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(brush)
            )
            Box(
                modifier =
                    Modifier.fillMaxWidth(0.45f)
                        .height(18.dp)
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
                    Modifier.fillMaxWidth()
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(brush)
            )
            Box(
                modifier =
                    Modifier.fillMaxWidth(0.8f)
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(brush)
            )
        }
    }
}
