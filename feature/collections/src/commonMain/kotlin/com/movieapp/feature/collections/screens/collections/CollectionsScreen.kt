package com.movieapp.feature.collections.screens.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movieapp.core.ui.resources.Res as CoreUiR
import com.movieapp.core.ui.resources.action_cancel
import com.movieapp.core.ui.resources.action_create
import com.movieapp.core.ui.resources.action_delete
import com.movieapp.core.ui.resources.action_save
import com.movieapp.core.ui.resources.cd_create_collection
import com.movieapp.core.ui.resources.cd_private
import com.movieapp.core.ui.resources.cd_public
import com.movieapp.core.ui.resources.cd_screen_collections
import com.movieapp.core.ui.resources.collection_items_count
import com.movieapp.core.ui.resources.field_description_optional
import com.movieapp.core.ui.resources.field_name
import com.movieapp.core.ui.resources.visibility_private
import com.movieapp.core.ui.resources.visibility_public
import com.movieapp.core.ui.components.AppErrorState
import com.movieapp.core.ui.components.AppInlineErrorState
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.utils.toImageUrl
import com.movieapp.feature.collections.resources.Res as CollectionsR
import com.movieapp.feature.collections.resources.collection_load_error
import com.movieapp.feature.collections.resources.collection_rename
import com.movieapp.feature.collections.resources.collection_view_all
import com.movieapp.feature.collections.resources.collections_content_load_error
import com.movieapp.feature.collections.resources.collections_create_saved
import com.movieapp.feature.collections.resources.collections_create_title
import com.movieapp.feature.collections.resources.collections_delete_message
import com.movieapp.feature.collections.resources.collections_delete_saved
import com.movieapp.feature.collections.resources.collections_delete_title
import com.movieapp.feature.collections.resources.collections_edit_saved
import com.movieapp.feature.collections.resources.collections_edit_title
import com.movieapp.feature.collections.resources.collections_empty_create_button
import com.movieapp.feature.collections.resources.collections_empty_subtitle
import com.movieapp.feature.collections.resources.collections_empty_title
import com.movieapp.feature.collections.resources.collections_heading
import com.movieapp.feature.collections.resources.collections_mutation_failed
import com.movieapp.feature.collections.resources.collections_saving

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionsScreen(
    onMovieClick: (Int) -> Unit = {},
    onSeriesClick: (Int) -> Unit = {},
    onCollectionClick: (String) -> Unit = {},
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCollection by remember { mutableStateOf<UserCollection?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val createSavedMessage = stringResource(CollectionsR.string.collections_create_saved)
    val editSavedMessage = stringResource(CollectionsR.string.collections_edit_saved)
    val deleteSavedMessage = stringResource(CollectionsR.string.collections_delete_saved)
    val mutationFailedMessage = stringResource(CollectionsR.string.collections_mutation_failed)

    LaunchedEffect(
        viewModel,
        onCollectionClick,
        onMovieClick,
        onSeriesClick,
        createSavedMessage,
        editSavedMessage,
        deleteSavedMessage,
        mutationFailedMessage,
    ) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CollectionsSideEffect.NavigateToCollectionDetail ->
                    onCollectionClick(effect.collectionId)
                is CollectionsSideEffect.NavigateToMovieDetail -> onMovieClick(effect.movieId)
                is CollectionsSideEffect.NavigateToSeriesDetail -> onSeriesClick(effect.seriesId)
                is CollectionsSideEffect.MutationSaved -> {
                    val message =
                        when (effect.mutation) {
                            CollectionsMutation.CREATE -> {
                                showCreateDialog = false
                                createSavedMessage
                            }
                            CollectionsMutation.EDIT -> {
                                showEditDialog = false
                                selectedCollection = null
                                editSavedMessage
                            }
                            CollectionsMutation.DELETE -> {
                                showDeleteDialog = false
                                selectedCollection = null
                                deleteSavedMessage
                            }
                        }
                    snackbarHostState.showSnackbar(message)
                }
                is CollectionsSideEffect.MutationFailed ->
                    snackbarHostState.showSnackbar(mutationFailedMessage)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    horizontal = MovieAppSpacing.screenHorizontal,
                    vertical = MovieAppSpacing.sectionVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
        ) {
            // Header with title and add button
            item(key = "collections_header", contentType = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(CollectionsR.string.collections_heading),
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                    )
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription =
                                stringResource(CoreUiR.string.cd_create_collection),
                        )
                    }
                }
            }

            if (
                uiState.partialError != null &&
                    uiState.collections.isNotEmpty() &&
                    !uiState.isLoading
            ) {
                item(key = "collections_partial_error", contentType = "error") {
                    AppInlineErrorState(
                        message =
                            stringResource(CollectionsR.string.collections_content_load_error),
                        onRetry = { viewModel.onEvent(CollectionsUiEvent.Retry) },
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    item(key = "collections_loading", contentType = "loading") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.extraLarge),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingIndicator()
                        }
                    }
                }
                uiState.error != null && uiState.collections.isEmpty() -> {
                    item(key = "collections_error", contentType = "error") {
                        AppErrorState(
                            message =
                                uiState.error?.takeIf(String::isNotBlank)
                                    ?: stringResource(CollectionsR.string.collection_load_error),
                            onRetry = { viewModel.onEvent(CollectionsUiEvent.Retry) },
                        )
                    }
                }
                uiState.collections.isEmpty() -> {
                    item(key = "collections_empty", contentType = "empty") {
                        EmptyCollectionsState(onCreate = { showCreateDialog = true })
                    }
                }
                else -> {
                    items(
                        items = uiState.collections,
                        key = { it.id },
                        contentType = { "collection" },
                    ) { collection ->
                        CollectionItem(
                            collection = collection,
                            onCollectionClick = {
                                viewModel.onEvent(
                                    CollectionsUiEvent.CollectionClicked(collection.id)
                                )
                            },
                            onEditClick = {
                                selectedCollection = collection
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                selectedCollection = collection
                                showDeleteDialog = true
                            },
                            onMovieClick = { id ->
                                viewModel.onEvent(CollectionsUiEvent.MovieClicked(id))
                            },
                            onSeriesClick = { id ->
                                viewModel.onEvent(CollectionsUiEvent.SeriesClicked(id))
                            },
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showCreateDialog) {
        CollectionDialog(
            title = stringResource(CollectionsR.string.collections_create_title),
            confirmLabel = stringResource(CoreUiR.string.action_create),
            isSaving = uiState.pendingMutation == CollectionsMutation.CREATE,
            onConfirm = { name, description, isPublic ->
                viewModel.onEvent(
                    CollectionsUiEvent.CreateCollection(
                        name = name,
                        description = description,
                        isPublic = isPublic,
                    )
                )
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    if (showEditDialog) {
        val collection = selectedCollection
        if (collection != null) {
            CollectionDialog(
                title = stringResource(CollectionsR.string.collections_edit_title),
                confirmLabel = stringResource(CoreUiR.string.action_save),
                initialName = collection.name,
                initialDescription = collection.description,
                initialIsPublic = collection.isPublic,
                isSaving = uiState.pendingMutation == CollectionsMutation.EDIT,
                onConfirm = { name, description, isPublic ->
                    viewModel.onEvent(
                        CollectionsUiEvent.EditCollection(
                            collectionId = collection.id,
                            name = name,
                            description = description,
                            isPublic = isPublic,
                        )
                    )
                },
                onDismiss = {
                    showEditDialog = false
                    selectedCollection = null
                },
            )
        }
    }

    if (showDeleteDialog) {
        val collection = selectedCollection
        if (collection != null) {
            val isDeleting = uiState.pendingMutation == CollectionsMutation.DELETE
            AlertDialog(
                onDismissRequest = {
                    if (!isDeleting) {
                        showDeleteDialog = false
                        selectedCollection = null
                    }
                },
                title = { Text(stringResource(CollectionsR.string.collections_delete_title)) },
                text = {
                    Text(
                        stringResource(
                            CollectionsR.string.collections_delete_message,
                            collection.name,
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            viewModel.onEvent(CollectionsUiEvent.DeleteCollection(collection.id))
                        },
                    ) {
                        Text(
                            stringResource(
                                if (isDeleting) {
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
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            showDeleteDialog = false
                            selectedCollection = null
                        },
                    ) {
                        Text(stringResource(CoreUiR.string.action_cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun CollectionDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialDescription: String = "",
    initialIsPublic: Boolean = false,
    isSaving: Boolean = false,
    onConfirm: (name: String, description: String, isPublic: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var isPublic by remember { mutableStateOf(initialIsPublic) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(CoreUiR.string.field_name)) },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(CoreUiR.string.field_description_optional)) },
                    minLines = 2,
                    maxLines = 4,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                ) {
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        enabled = !isSaving,
                    )
                    Text(
                        text =
                            stringResource(
                                if (isPublic) {
                                    CoreUiR.string.visibility_public
                                } else {
                                    CoreUiR.string.visibility_private
                                }
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim(), description.trim(), isPublic)
                },
                enabled = name.isNotBlank() && !isSaving,
            ) {
                Text(
                    if (isSaving) {
                        stringResource(CollectionsR.string.collections_saving)
                    } else {
                        confirmLabel
                    }
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
private fun CollectionItem(
    collection: UserCollection,
    onCollectionClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
) {
    Card(
        onClick = onCollectionClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(MovieAppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CollectionMosaic(
                    collection = collection,
                    onMovieClick = onMovieClick,
                    onSeriesClick = onSeriesClick,
                    modifier = Modifier.size(MovieAppSpacing.collectionMosaicSize),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                    Text(
                        text =
                            stringResource(
                                CoreUiR.string.collection_items_count,
                                collection.itemCount,
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onEditClick) {
                        Text(stringResource(CollectionsR.string.collection_rename))
                    }
                    TextButton(onClick = onDeleteClick) {
                        Text(
                            stringResource(CoreUiR.string.action_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Collection Description
            if (collection.description.isNotEmpty()) {
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onCollectionClick) {
                    Text(stringResource(CollectionsR.string.collection_view_all))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (collection.isPublic) Icons.Filled.Public else Icons.Filled.Lock,
                        contentDescription =
                            stringResource(
                                if (collection.isPublic) {
                                    CoreUiR.string.cd_public
                                } else {
                                    CoreUiR.string.cd_private
                                }
                            ),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
                }
            }
        }
    }
}

@Composable
private fun CollectionMosaic(
    collection: UserCollection,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artwork = collection.items.take(4)
    Box(
        modifier = modifier.clip(MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork.isEmpty()) {
            Icon(
                Icons.Filled.FolderOpen,
                contentDescription = stringResource(CoreUiR.string.cd_screen_collections),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MovieAppSpacing.spacious),
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                artwork.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        row.forEach { item ->
                            val path =
                                when (item) {
                                    is CollectionItem.MovieItem ->
                                        item.movie.posterPath ?: item.movie.backdropPath
                                    is CollectionItem.SeriesItem ->
                                        item.series.posterPath ?: item.series.backdropPath
                                }
                            AsyncImage(
                                model = path.toImageUrl(),
                                contentDescription =
                                    when (item) {
                                        is CollectionItem.MovieItem -> item.movie.title
                                        is CollectionItem.SeriesItem -> item.series.name
                                    },
                                modifier =
                                    Modifier.weight(1f).fillMaxSize().clickable {
                                        when (item) {
                                            is CollectionItem.MovieItem ->
                                                onMovieClick(item.movie.id)
                                            is CollectionItem.SeriesItem ->
                                                onSeriesClick(item.series.id)
                                        }
                                    },
                                contentScale = ContentScale.Crop,
                            )
                        }
                        repeat(2 - row.size) {
                            Box(modifier = Modifier.weight(1f).fillMaxSize())
                        }
                    }
                }
                repeat(2 - artwork.chunked(2).size) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Box(modifier = Modifier.weight(1f).fillMaxSize())
                        Box(modifier = Modifier.weight(1f).fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCollectionsState(onCreate: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Icon(
            Icons.Filled.FolderOpen,
            contentDescription = stringResource(CoreUiR.string.cd_screen_collections),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(CollectionsR.string.collections_empty_title),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )

        Text(
            text = stringResource(CollectionsR.string.collections_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(onClick = onCreate, modifier = Modifier.padding(top = 8.dp)) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(CoreUiR.string.cd_create_collection),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(CollectionsR.string.collections_empty_create_button))
        }
    }
}

// Data classes
data class UserCollection(
    val id: String,
    val name: String,
    val description: String,
    val itemCount: Int,
    val isPublic: Boolean = false,
    val items: List<CollectionItem> = emptyList(),
)

sealed class CollectionItem {
    data class MovieItem(val movie: com.movieapp.core.model.Movie) : CollectionItem()

    data class SeriesItem(val series: com.movieapp.core.model.TvSeries) : CollectionItem()
}
