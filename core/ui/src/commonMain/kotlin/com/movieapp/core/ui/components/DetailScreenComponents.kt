package com.movieapp.core.ui.components

import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.add_to_collection_title
import com.movieapp.core.ui.resources.cd_add_favorite
import com.movieapp.core.ui.resources.cd_add_watchlist
import com.movieapp.core.ui.resources.cd_back
import com.movieapp.core.ui.resources.cd_more_actions
import com.movieapp.core.ui.resources.cd_remove_favorite
import com.movieapp.core.ui.resources.cd_remove_watchlist
import com.movieapp.core.ui.resources.detail_favorite
import com.movieapp.core.ui.resources.detail_favorited
import com.movieapp.core.ui.resources.detail_in_watchlist
import com.movieapp.core.ui.resources.detail_more_actions
import com.movieapp.core.ui.resources.detail_rate
import com.movieapp.core.ui.resources.detail_save_offline
import com.movieapp.core.ui.resources.detail_saved_offline
import com.movieapp.core.ui.resources.detail_share
import com.movieapp.core.ui.resources.detail_show_less
import com.movieapp.core.ui.resources.detail_show_more
import com.movieapp.core.ui.resources.detail_watch_trailer
import com.movieapp.core.ui.resources.label_watchlist
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.movieapp.core.ui.designsystem.MovieAppMotion
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.motionDurationMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.cd_back))
            }
        },
        actions = {
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, stringResource(Res.string.cd_more_actions))
            }
        },
    )
}

@Composable
fun DetailPrimaryActions(
    isWatchlisted: Boolean,
    isFavorite: Boolean,
    hasTrailer: Boolean,
    onToggleWatchlist: () -> Unit,
    onWatchTrailer: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRate: () -> Unit,
    modifier: Modifier = Modifier,
    watchlistEnabled: Boolean = true,
    favoriteEnabled: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        ) {
            Button(
                onClick = onToggleWatchlist,
                enabled = watchlistEnabled,
                modifier = Modifier.weight(1f).heightIn(min = MovieAppSpacing.minTouchTarget),
            ) {
                Icon(
                    if (isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription =
                        stringResource(
                            if (isWatchlisted) Res.string.cd_remove_watchlist
                            else Res.string.cd_add_watchlist
                        ),
                )
                Text(
                    stringResource(
                        if (isWatchlisted) Res.string.detail_in_watchlist
                        else Res.string.label_watchlist
                    )
                )
            }
            if (hasTrailer) {
                OutlinedButton(
                    onClick = onWatchTrailer,
                    modifier = Modifier.weight(1f).heightIn(min = MovieAppSpacing.minTouchTarget),
                ) {
                    Text(stringResource(Res.string.detail_watch_trailer))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        ) {
            TextButton(
                onClick = onToggleFavorite,
                enabled = favoriteEnabled,
                modifier = Modifier.weight(1f).heightIn(min = MovieAppSpacing.minTouchTarget),
            ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription =
                        stringResource(
                            if (isFavorite) Res.string.cd_remove_favorite
                            else Res.string.cd_add_favorite
                        ),
                )
                Text(
                    stringResource(
                        if (isFavorite) Res.string.detail_favorited else Res.string.detail_favorite
                    )
                )
            }
            TextButton(
                onClick = onRate,
                modifier = Modifier.weight(1f).heightIn(min = MovieAppSpacing.minTouchTarget),
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Text(stringResource(Res.string.detail_rate))
            }
        }
    }
}

@Composable
fun DetailIdentityTransition(
    contentKey: Any,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val durationMillis = motionDurationMillis(MovieAppMotion.transitionMillis)
    key(contentKey) {
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }
        AnimatedVisibility(
            visibleState = visibleState,
            modifier = modifier.cinematicSharedBounds(contentKey),
            enter = fadeIn(animationSpec = tween(durationMillis = durationMillis)),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailActionsSheet(
    visible: Boolean,
    isSavedOffline: Boolean,
    isOfflineSaving: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onAddToCollection: () -> Unit,
    onSaveOffline: () -> Unit,
) {
    if (!visible) return
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(Res.string.detail_more_actions),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = MovieAppSpacing.screenHorizontal),
        )
        DetailActionRow(Res.string.detail_share, Icons.Default.Share) {
            onDismiss()
            onShare()
        }
        DetailActionRow(Res.string.add_to_collection_title, Icons.Default.PlaylistAdd) {
            onDismiss()
            onAddToCollection()
        }
        DetailActionRow(
            labelRes =
                if (isSavedOffline) Res.string.detail_saved_offline else Res.string.detail_save_offline,
            icon = Icons.Default.Download,
            enabled = !isOfflineSaving && !isSavedOffline,
        ) {
            onDismiss()
            onSaveOffline()
        }
    }
}

@Composable
private fun DetailActionRow(
    labelRes: StringResource,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(labelRes)) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(min = MovieAppSpacing.minTouchTarget)
                .clickable(enabled = enabled, onClick = onClick),
    )
}

@Composable
fun ExpandableDetailText(
    text: String,
    emptyText: String,
    modifier: Modifier = Modifier,
    collapsedLines: Int = 4,
) {
    var expanded by remember(text) { mutableStateOf(false) }
    var hasOverflow by remember(text) { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = text.ifBlank { emptyText },
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { hasOverflow = it.hasVisualOverflow || expanded },
        )
        if (hasOverflow) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.heightIn(min = MovieAppSpacing.minTouchTarget),
            ) {
                Text(
                    stringResource(
                        if (expanded) Res.string.detail_show_less else Res.string.detail_show_more
                    )
                )
            }
        }
    }
}
