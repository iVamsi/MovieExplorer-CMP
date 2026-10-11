package com.movieapp.feature.collections.screens.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movieapp.core.ui.resources.Res as CoreUiR
import com.movieapp.core.ui.resources.action_cancel
import com.movieapp.core.ui.resources.action_delete
import com.movieapp.core.ui.resources.action_save
import com.movieapp.core.ui.resources.cd_back
import com.movieapp.core.ui.resources.cd_remove_from_collection
import com.movieapp.core.ui.resources.cd_screen_collections
import com.movieapp.core.ui.resources.clear_user_rating
import com.movieapp.core.ui.resources.collection_items_count
import com.movieapp.core.ui.resources.field_name
import com.movieapp.core.ui.resources.notes_label
import com.movieapp.core.ui.resources.save_notes
import com.movieapp.core.ui.resources.visibility_private
import com.movieapp.core.ui.resources.visibility_public
import com.movieapp.core.ui.components.AppErrorState
import com.movieapp.core.ui.components.AppInlineErrorState
import com.movieapp.core.ui.components.AppInlineLoader
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.MovieCardSkeleton
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.components.UserRatingControl
import com.movieapp.core.ui.components.shimmer.rememberShimmerBrush
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.PosterGridCells
import com.movieapp.core.ui.utils.toImageUrl
import com.movieapp.feature.collections.resources.Res as CollectionsR
import com.movieapp.feature.collections.resources.collection_content_load_error
import com.movieapp.feature.collections.resources.collection_detail_empty
import com.movieapp.feature.collections.resources.collection_detail_movies
import com.movieapp.feature.collections.resources.collection_detail_title
import com.movieapp.feature.collections.resources.collection_detail_tv_series
import com.movieapp.feature.collections.resources.collection_item_saved
import com.movieapp.feature.collections.resources.collection_load_error
import com.movieapp.feature.collections.resources.collection_mark_watched
import com.movieapp.feature.collections.resources.collection_mutation_failed
import com.movieapp.feature.collections.resources.collection_remove_item_message
import com.movieapp.feature.collections.resources.collection_remove_item_title
import com.movieapp.feature.collections.resources.collection_remove_saved
import com.movieapp.feature.collections.resources.collection_rename
import com.movieapp.feature.collections.resources.collection_rename_saved
import com.movieapp.feature.collections.resources.collection_watched
import com.movieapp.feature.collections.resources.collections_delete_message
import com.movieapp.feature.collections.resources.collections_delete_saved
import com.movieapp.feature.collections.resources.collections_delete_title
import com.movieapp.feature.collections.resources.collections_saving

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBackClick: () -> Unit = {},
    onCollectionDeleted: (String) -> Unit = { onBackClick() },
    onMovieClick: (Int) -> Unit = {},
    onSeriesClick: (Int) -> Unit = {},
    viewModel: CollectionDetailViewModel = koinViewModel { parametersOf(collectionId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteCollectionDialog by remember { mutableStateOf(false) }
    var pendingItemRemoval by remember { mutableStateOf<PendingItemRemoval?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val renameSavedMessage = stringResource(CollectionsR.string.collection_rename_saved)
    val deleteSavedMessage = stringResource(CollectionsR.string.collections_delete_saved)
    val removeSavedMessage = stringResource(CollectionsR.string.collection_remove_saved)
    val itemSavedMessage = stringResource(CollectionsR.string.collection_item_saved)
    val mutationFailedMessage = stringResource(CollectionsR.string.collection_mutation_failed)

    LaunchedEffect(
        viewModel,
        onBackClick,
        onCollectionDeleted,
        renameSavedMessage,
        deleteSavedMessage,
        removeSavedMessage,
        itemSavedMessage,
        mutationFailedMessage,
    ) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CollectionDetailSideEffect.MutationSaved -> {
                    val message =
                        when (effect.mutation) {
                            CollectionDetailMutation.RENAME -> {
                                showRenameDialog = false
                                renameSavedMessage
                            }
                            CollectionDetailMutation.DELETE -> {
                                showDeleteCollectionDialog = false
                                onCollectionDeleted(deleteSavedMessage)
                                null
                            }
                            CollectionDetailMutation.REMOVE_ITEM -> {
                                pendingItemRemoval = null
                                removeSavedMessage
                            }
                            CollectionDetailMutation.UPDATE_ITEM -> itemSavedMessage
                        }
                    message?.let { snackbarHostState.showSnackbar(it) }
                }
                is CollectionDetailSideEffect.MutationFailed ->
                    snackbarHostState.showSnackbar(mutationFailedMessage)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = {
                        Text(
                            text =
                                uiState.collection?.name
                                    ?: stringResource(CollectionsR.string.collection_detail_title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(CoreUiR.string.cd_back),
                            )
                        }
                    },
                )
                if (uiState.pendingMutation != null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.collection == null -> {
                CollectionDetailSkeleton(modifier = Modifier.padding(paddingValues))
            }

            uiState.error != null && uiState.collection == null -> {
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(paddingValues)
                            .padding(MovieAppSpacing.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    AppErrorState(
                        message =
                            uiState.error?.takeIf(String::isNotBlank)
                                ?: stringResource(CollectionsR.string.collection_load_error),
                        onRetry = { viewModel.onEvent(CollectionDetailUiEvent.Retry) },
                    )
                }
            }

            else -> {
                val hasListContent =
                    uiState.movieRows.isNotEmpty() || uiState.seriesRows.isNotEmpty()
                val collectionRefreshing =
                    uiState.isLoading && uiState.collection != null && hasListContent
                PullToRefreshBox(
                    isRefreshing = collectionRefreshing,
                    onRefresh = { viewModel.onEvent(CollectionDetailUiEvent.Retry) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                ) {
                    CollectionDetailContent(
                        uiState = uiState,
                        onMovieClick = onMovieClick,
                        onSeriesClick = onSeriesClick,
                        onRenameCollection = { showRenameDialog = true },
                        onDeleteCollection = { showDeleteCollectionDialog = true },
                        onRemoveMovie = { movieId, title ->
                            pendingItemRemoval = PendingItemRemoval.Movie(movieId, title)
                        },
                        onRemoveSeries = { seriesId, title ->
                            pendingItemRemoval = PendingItemRemoval.Series(seriesId, title)
                        },
                        onToggleWatched = { itemId, watched ->
                            viewModel.onEvent(
                                CollectionDetailUiEvent.ToggleItemWatched(itemId, watched)
                            )
                        },
                        onSetRating = { itemId, stars ->
                            viewModel.onEvent(CollectionDetailUiEvent.SetItemRating(itemId, stars))
                        },
                        onSaveNotes = { itemId, notes ->
                            viewModel.onEvent(CollectionDetailUiEvent.SetItemNotes(itemId, notes))
                        },
                        onRetry = { viewModel.onEvent(CollectionDetailUiEvent.Retry) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    val collection = uiState.collection
    if (showRenameDialog && collection != null) {
        RenameCollectionDialog(
            initialName = collection.name,
            isSaving = uiState.pendingMutation == CollectionDetailMutation.RENAME,
            onConfirm = { name ->
                viewModel.onEvent(CollectionDetailUiEvent.RenameCollection(name))
            },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showDeleteCollectionDialog && collection != null) {
        DeleteCollectionDialog(
            collectionName = collection.name,
            isSaving = uiState.pendingMutation == CollectionDetailMutation.DELETE,
            onConfirm = {
                viewModel.onEvent(CollectionDetailUiEvent.DeleteCollection)
            },
            onDismiss = { showDeleteCollectionDialog = false },
        )
    }

    pendingItemRemoval?.let { removal ->
        RemoveCollectionItemDialog(
            itemTitle = removal.title,
            isSaving = uiState.pendingMutation == CollectionDetailMutation.REMOVE_ITEM,
            onConfirm = {
                when (removal) {
                    is PendingItemRemoval.Movie ->
                        viewModel.onEvent(CollectionDetailUiEvent.RemoveMovie(removal.id))
                    is PendingItemRemoval.Series ->
                        viewModel.onEvent(CollectionDetailUiEvent.RemoveSeries(removal.id))
                }
            },
            onDismiss = { pendingItemRemoval = null },
        )
    }
}

private sealed interface PendingItemRemoval {
    val id: Int
    val title: String

    data class Movie(override val id: Int, override val title: String) : PendingItemRemoval

    data class Series(override val id: Int, override val title: String) : PendingItemRemoval
}

@Composable
private fun RenameCollectionDialog(
    initialName: String,
    isSaving: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(CollectionsR.string.collection_rename)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(CoreUiR.string.field_name)) },
                singleLine = true,
                enabled = !isSaving,
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && !isSaving,
                onClick = { onConfirm(name.trim()) },
            ) {
                Text(
                    stringResource(
                        if (isSaving) {
                            CollectionsR.string.collections_saving
                        } else {
                            CoreUiR.string.action_save
                        }
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(CoreUiR.string.action_cancel))
            }
        },
    )
}

@Composable
private fun DeleteCollectionDialog(
    collectionName: String,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(CollectionsR.string.collections_delete_title)) },
        text = {
            Text(stringResource(CollectionsR.string.collections_delete_message, collectionName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text(
                    text =
                        stringResource(
                            if (isSaving) {
                                CollectionsR.string.collections_saving
                            } else {
                                CoreUiR.string.action_delete
                            }
                        ),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(CoreUiR.string.action_cancel))
            }
        },
    )
}

@Composable
private fun RemoveCollectionItemDialog(
    itemTitle: String,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(CollectionsR.string.collection_remove_item_title)) },
        text = {
            Text(stringResource(CollectionsR.string.collection_remove_item_message, itemTitle))
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text(
                    text =
                        stringResource(
                            if (isSaving) {
                                CollectionsR.string.collections_saving
                            } else {
                                CoreUiR.string.cd_remove_from_collection
                            }
                        ),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(CoreUiR.string.action_cancel))
            }
        },
    )
}

@Composable
private fun CollectionDetailSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier.fillMaxSize().padding(MovieAppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Box(
            modifier =
                Modifier.fillMaxWidth(0.6f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
        )
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
        )
        Box(
            modifier =
                Modifier.fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
        )
        LazyVerticalGrid(
            columns = PosterGridCells,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(9) { MovieCardSkeleton() }
        }
    }
}

@Composable
private fun CollectionDetailContent(
    uiState: CollectionDetailUiState,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onRenameCollection: () -> Unit,
    onDeleteCollection: () -> Unit,
    onRemoveMovie: (Int, String) -> Unit,
    onRemoveSeries: (Int, String) -> Unit,
    onToggleWatched: (String, Boolean) -> Unit,
    onSetRating: (String, Int) -> Unit,
    onSaveNotes: (String, String?) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val collection = uiState.collection
    val isEmpty =
        uiState.movieRows.isEmpty() &&
            uiState.seriesRows.isEmpty() &&
            !uiState.isLoading &&
            uiState.contentError == null

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = PosterGridCells,
        contentPadding =
            PaddingValues(
                horizontal = MovieAppSpacing.screenHorizontal,
                vertical = MovieAppSpacing.sectionVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
    ) {
        if (collection != null) {
            item(
                key = "collection_header",
                contentType = "header",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                CollectionInfoCard(
                    collection = collection,
                    movieRows = uiState.movieRows,
                    seriesRows = uiState.seriesRows,
                    onRenameCollection = onRenameCollection,
                    onDeleteCollection = onDeleteCollection,
                )
            }
        }

        if (uiState.contentError != null) {
            item(
                key = "collection_content_error",
                contentType = "error",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                AppInlineErrorState(
                    message = stringResource(CollectionsR.string.collection_content_load_error),
                    onRetry = onRetry,
                )
            }
        }

        if (uiState.isLoading && uiState.movieRows.isEmpty() && uiState.seriesRows.isEmpty()) {
            item(
                key = "collection_content_loading",
                contentType = "loading",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                AppInlineLoader()
            }
        }

        if (uiState.movieRows.isNotEmpty()) {
            item(
                key = "movies_heading",
                contentType = "heading",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text =
                        stringResource(
                            CollectionsR.string.collection_detail_movies,
                            uiState.movieRows.size,
                        ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(
                items = uiState.movieRows,
                key = { it.item.id },
                contentType = { "movie" },
            ) { row ->
                CollectionMovieTile(
                    row = row,
                    onPosterClick = { onMovieClick(row.movie.id) },
                    onRemove = { onRemoveMovie(row.movie.id, row.movie.title) },
                    onToggleWatched = onToggleWatched,
                    onSetRating = onSetRating,
                    onSaveNotes = onSaveNotes,
                )
            }
        }

        if (uiState.seriesRows.isNotEmpty()) {
            item(
                key = "series_heading",
                contentType = "heading",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text =
                        stringResource(
                            CollectionsR.string.collection_detail_tv_series,
                            uiState.seriesRows.size,
                        ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(
                items = uiState.seriesRows,
                key = { it.item.id },
                contentType = { "series" },
            ) { row ->
                CollectionSeriesTile(
                    row = row,
                    onPosterClick = { onSeriesClick(row.series.id) },
                    onRemove = { onRemoveSeries(row.series.id, row.series.name) },
                    onToggleWatched = onToggleWatched,
                    onSetRating = onSetRating,
                    onSaveNotes = onSaveNotes,
                )
            }
        }

        if (isEmpty) {
            item(
                key = "collection_empty",
                contentType = "empty",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = MovieAppSpacing.spacious),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(CollectionsR.string.collection_detail_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionMovieTile(
    row: CollectionMovieRow,
    onPosterClick: () -> Unit,
    onRemove: () -> Unit,
    onToggleWatched: (String, Boolean) -> Unit,
    onSetRating: (String, Int) -> Unit,
    onSaveNotes: (String, String?) -> Unit,
) {
    val ratingOutOfFive = (row.item.userRating ?: 0).let { r -> (r / 2).coerceIn(0, 5) }
    var notesDraft by
        remember(row.item.id, row.item.userNotes) { mutableStateOf(row.item.userNotes.orEmpty()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(MovieAppSpacing.itemGapCompact),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                verticalAlignment = Alignment.Top,
            ) {
                MovieCard(
                    movie = row.movie,
                    onClick = onPosterClick,
                    modifier = Modifier.width(100.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = row.movie.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FilterChip(
                        selected = row.item.isWatched,
                        onClick = { onToggleWatched(row.item.id, !row.item.isWatched) },
                        label = {
                            Text(
                                stringResource(
                                    if (row.item.isWatched) {
                                        CollectionsR.string.collection_watched
                                    } else {
                                        CollectionsR.string.collection_mark_watched
                                    }
                                )
                            )
                        },
                    )
                    UserRatingControl(
                        ratingOutOfFive = ratingOutOfFive,
                        onRatingSelected = { stars -> onSetRating(row.item.id, stars) },
                    )
                    TextButton(onClick = { onSetRating(row.item.id, 0) }) {
                        Text(stringResource(CoreUiR.string.clear_user_rating))
                    }
                    OutlinedTextField(
                        value = notesDraft,
                        onValueChange = { notesDraft = it },
                        label = { Text(stringResource(CoreUiR.string.notes_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                    )
                    TextButton(onClick = { onSaveNotes(row.item.id, notesDraft) }) {
                        Text(stringResource(CoreUiR.string.save_notes))
                    }
                }
                TextButton(onClick = onRemove) {
                    Text(
                        text = stringResource(CoreUiR.string.cd_remove_from_collection),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionSeriesTile(
    row: CollectionSeriesRow,
    onPosterClick: () -> Unit,
    onRemove: () -> Unit,
    onToggleWatched: (String, Boolean) -> Unit,
    onSetRating: (String, Int) -> Unit,
    onSaveNotes: (String, String?) -> Unit,
) {
    val ratingOutOfFive = (row.item.userRating ?: 0).let { r -> (r / 2).coerceIn(0, 5) }
    var notesDraft by
        remember(row.item.id, row.item.userNotes) { mutableStateOf(row.item.userNotes.orEmpty()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(MovieAppSpacing.itemGapCompact),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                verticalAlignment = Alignment.Top,
            ) {
                SeriesCard(
                    series = row.series,
                    onClick = onPosterClick,
                    modifier = Modifier.width(100.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = row.series.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FilterChip(
                        selected = row.item.isWatched,
                        onClick = { onToggleWatched(row.item.id, !row.item.isWatched) },
                        label = {
                            Text(
                                stringResource(
                                    if (row.item.isWatched) {
                                        CollectionsR.string.collection_watched
                                    } else {
                                        CollectionsR.string.collection_mark_watched
                                    }
                                )
                            )
                        },
                    )
                    UserRatingControl(
                        ratingOutOfFive = ratingOutOfFive,
                        onRatingSelected = { stars -> onSetRating(row.item.id, stars) },
                    )
                    TextButton(onClick = { onSetRating(row.item.id, 0) }) {
                        Text(stringResource(CoreUiR.string.clear_user_rating))
                    }
                    OutlinedTextField(
                        value = notesDraft,
                        onValueChange = { notesDraft = it },
                        label = { Text(stringResource(CoreUiR.string.notes_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                    )
                    TextButton(onClick = { onSaveNotes(row.item.id, notesDraft) }) {
                        Text(stringResource(CoreUiR.string.save_notes))
                    }
                }
                TextButton(onClick = onRemove) {
                    Text(
                        text = stringResource(CoreUiR.string.cd_remove_from_collection),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionInfoCard(
    collection: com.movieapp.core.model.UserCollection,
    movieRows: List<CollectionMovieRow>,
    seriesRows: List<CollectionSeriesRow>,
    onRenameCollection: () -> Unit,
    onDeleteCollection: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(MovieAppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
        ) {
            CollectionDetailMosaic(movieRows = movieRows, seriesRows = seriesRows)
            Text(
                text = collection.name,
                style = MaterialTheme.typography.headlineMediumEmphasized,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            collection.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector =
                        if (collection.isPublic) Icons.Filled.Public else Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text =
                        stringResource(
                            if (collection.isPublic) {
                                CoreUiR.string.visibility_public
                            } else {
                                CoreUiR.string.visibility_private
                            }
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text =
                        stringResource(CoreUiR.string.collection_items_count, collection.itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                TextButton(onClick = onRenameCollection) {
                    Text(stringResource(CollectionsR.string.collection_rename))
                }
                TextButton(onClick = onDeleteCollection) {
                    Text(
                        text = stringResource(CollectionsR.string.collections_delete_title),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private data class DetailMosaicArtwork(val path: String?, val contentDescription: String)

@Composable
private fun CollectionDetailMosaic(
    movieRows: List<CollectionMovieRow>,
    seriesRows: List<CollectionSeriesRow>,
) {
    val artwork = buildList {
        movieRows.forEach { row ->
            add(
                DetailMosaicArtwork(
                    path = row.movie.posterPath ?: row.movie.backdropPath,
                    contentDescription = row.movie.title,
                )
            )
        }
        seriesRows.forEach { row ->
            add(
                DetailMosaicArtwork(
                    path = row.series.posterPath ?: row.series.backdropPath,
                    contentDescription = row.series.name,
                )
            )
        }
    }
        .take(4)
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .aspectRatio(16f / 7f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork.isEmpty()) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = stringResource(CoreUiR.string.cd_screen_collections),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MovieAppSpacing.spacious),
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                for (rowIndex in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (columnIndex in 0 until 2) {
                            val image = artwork.getOrNull(rowIndex * 2 + columnIndex)
                            if (image == null) {
                                Box(modifier = Modifier.weight(1f).fillMaxSize())
                            } else {
                                AsyncImage(
                                    model = image.path.toImageUrl(),
                                    contentDescription = image.contentDescription,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
