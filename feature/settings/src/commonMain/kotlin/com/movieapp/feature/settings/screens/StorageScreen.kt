package com.movieapp.feature.settings.screens

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.settings_storage
import com.movieapp.feature.settings.resources.Res
import com.movieapp.feature.settings.resources.storage_cache_cleared
import com.movieapp.feature.settings.resources.storage_cache_description
import com.movieapp.feature.settings.resources.storage_cache_mb_used
import com.movieapp.feature.settings.resources.storage_clear_cache
import com.movieapp.feature.settings.resources.storage_clear_cache_hint
import com.movieapp.feature.settings.resources.storage_image_cache_title
import com.movieapp.feature.settings.resources.storage_offline_clear_all
import com.movieapp.feature.settings.resources.storage_offline_delete
import com.movieapp.feature.settings.resources.storage_offline_empty
import com.movieapp.feature.settings.resources.storage_offline_fallback_title
import com.movieapp.feature.settings.resources.storage_offline_library_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.core.model.OfflineContent
import com.movieapp.feature.settings.SettingsSideEffect
import com.movieapp.feature.settings.SettingsUiEvent
import com.movieapp.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val cacheClearedMessage = stringResource(Res.string.storage_cache_cleared)

    LaunchedEffect(viewModel, cacheClearedMessage) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                SettingsSideEffect.ImageCacheCleared ->
                    snackbarHostState.showSnackbar(cacheClearedMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SettingsTopBar(
                title = stringResource(CoreRes.string.settings_storage),
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        StorageContent(
            cacheSizeMb = uiState.cacheSizeMb,
            offlineContent = uiState.offlineContent,
            onDelete = { content ->
                viewModel.onEvent(
                    SettingsUiEvent.DeleteOfflineContent(content.contentId, content.contentType)
                )
            },
            onClearAll = { viewModel.onEvent(SettingsUiEvent.ClearOfflineContent) },
            onClearCache = { viewModel.onEvent(SettingsUiEvent.ClearImageCache) },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
internal fun StorageContent(
    cacheSizeMb: Long,
    offlineContent: List<OfflineContent>,
    onDelete: (OfflineContent) -> Unit,
    onClearAll: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = com.movieapp.core.ui.designsystem.MovieAppSpacing.screenHorizontal
                )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.storage_image_cache_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.storage_cache_mb_used, cacheSizeMb),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = stringResource(Res.string.storage_cache_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OfflineLibrarySection(
            offlineContent = offlineContent,
            onDelete = onDelete,
            onClearAll = onClearAll,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClearCache,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
        ) {
            Text(stringResource(Res.string.storage_clear_cache))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.storage_clear_cache_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OfflineLibrarySection(
    offlineContent: List<OfflineContent>,
    onDelete: (OfflineContent) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.storage_offline_library_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (offlineContent.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text(stringResource(Res.string.storage_offline_clear_all))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (offlineContent.isEmpty()) {
                Text(
                    text = stringResource(Res.string.storage_offline_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                offlineContent.take(MAX_VISIBLE_OFFLINE_ITEMS).forEach { content ->
                    OfflineContentRow(content = content, onDelete = { onDelete(content) })
                }
            }
        }
    }
}

@Composable
private fun OfflineContentRow(
    content: OfflineContent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text =
                    content.title.ifBlank {
                        stringResource(Res.string.storage_offline_fallback_title, content.contentId)
                    },
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = content.contentType.name.replace('_', ' '),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(Res.string.storage_offline_delete),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private const val MAX_VISIBLE_OFFLINE_ITEMS = 5
