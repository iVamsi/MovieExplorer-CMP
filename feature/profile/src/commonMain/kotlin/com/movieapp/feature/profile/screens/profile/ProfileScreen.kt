package com.movieapp.feature.profile.screens.profile

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.cd_navigate
import com.movieapp.core.ui.resources.cd_profile_avatar
import com.movieapp.core.ui.resources.profile_screen_title
import com.movieapp.core.ui.resources.profile_stat_collections
import com.movieapp.core.ui.resources.profile_stat_movies
import com.movieapp.core.ui.resources.profile_stat_series
import com.movieapp.core.ui.resources.settings_about
import com.movieapp.core.ui.resources.settings_account
import com.movieapp.core.ui.resources.settings_all
import com.movieapp.core.ui.resources.settings_help
import com.movieapp.core.ui.resources.settings_language
import com.movieapp.core.ui.resources.settings_section_title
import com.movieapp.core.ui.resources.settings_storage
import com.movieapp.core.ui.resources.settings_theme
import com.movieapp.feature.profile.resources.Res
import com.movieapp.feature.profile.resources.profile_sign_in_tmdb
import com.movieapp.feature.profile.resources.profile_signed_in_subtitle
import com.movieapp.feature.profile.resources.profile_signed_out_subtitle
import com.movieapp.feature.profile.resources.profile_signed_out_title
import com.movieapp.feature.profile.resources.profile_stats_basis
import com.movieapp.feature.profile.resources.profile_stats_decades
import com.movieapp.feature.profile.resources.profile_stats_empty
import com.movieapp.feature.profile.resources.profile_stats_genres
import com.movieapp.feature.profile.resources.profile_stats_person_count
import com.movieapp.feature.profile.resources.profile_stats_ratings
import com.movieapp.feature.profile.resources.profile_stats_title
import com.movieapp.feature.profile.resources.profile_stats_top_people
import com.movieapp.feature.profile.resources.profile_stats_watch_time
import org.jetbrains.compose.resources.StringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import com.movieapp.feature.profile.resources.profile_rating_star
import com.movieapp.feature.profile.resources.profile_watch_time_hours
import com.movieapp.feature.profile.resources.profile_watch_time_hours_minutes
import com.movieapp.feature.profile.resources.profile_watch_time_minutes
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.core.common.formatWithCommas
import com.movieapp.core.domain.repository.AuthState
import com.movieapp.core.model.DecadeCount
import com.movieapp.core.model.GenreCount
import com.movieapp.core.model.PersonCount
import com.movieapp.core.model.WatchStats
import com.movieapp.core.ui.designsystem.MovieAppSpacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onStorageClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPullRefreshing by viewModel.isPullRefreshing.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ProfileSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is ProfileSideEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = { viewModel.onEvent(ProfileUiEvent.PullRefresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        horizontal = MovieAppSpacing.screenHorizontal,
                        vertical = MovieAppSpacing.sectionVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
            ) {
                item(key = "title", contentType = "header") {
                    Text(
                        text = stringResource(CoreRes.string.profile_screen_title),
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                    )
                }

                item(key = "user_info", contentType = "account") {
                    UserInfoSection(
                        authState = authState,
                        onSignInClick = { viewModel.startTmdbLogin() },
                    )
                }

                item(key = "quick_stats", contentType = "stats") {
                    QuickStatsSection(
                        totalMovies = uiState.watchlistMovies.size,
                        totalSeries = uiState.watchlistTvSeries.size,
                        totalCollections = uiState.userCollections.size,
                    )
                }

                item(key = "profile_stats", contentType = "stats") {
                    ProfileStatsSection(stats = uiState.watchStats)
                }

                item(key = "settings", contentType = "settings") {
                    SettingsSection(
                        onSettingsClick = onSettingsClick,
                        onAccountClick = onAccountClick,
                        onThemeClick = onThemeClick,
                        onLanguageClick = onLanguageClick,
                        onStorageClick = onStorageClick,
                        onHelpClick = onHelpClick,
                        onAboutClick = onAboutClick,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun UserInfoSection(
    authState: AuthState,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentation = accountPresentation(authState)
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(MovieAppSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = stringResource(CoreRes.string.cd_profile_avatar),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.width(MovieAppSpacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    when (authState) {
                        is AuthState.SignedOut -> {
                            Text(
                                text = stringResource(Res.string.profile_signed_out_title),
                                style = MaterialTheme.typography.titleLargeEmphasized,
                            )
                            Text(
                                text = stringResource(Res.string.profile_signed_out_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        is AuthState.SignedIn -> {
                            Text(
                                text = presentation.displayName.orEmpty(),
                                style = MaterialTheme.typography.titleLargeEmphasized,
                            )
                            val subtitle =
                                authState.name?.takeIf { it.isNotBlank() }
                                    ?: stringResource(Res.string.profile_signed_in_subtitle)
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (authState is AuthState.SignedOut) {
                Spacer(modifier = Modifier.height(MovieAppSpacing.small))
                Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.profile_sign_in_tmdb))
                }
            }
        }
    }
}

@Composable
private fun QuickStatsSection(
    totalMovies: Int,
    totalSeries: Int,
    totalCollections: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                labelRes = CoreRes.string.profile_stat_movies,
                value = totalMovies.formatWithCommas(),
                icon = Icons.Filled.Movie,
            )
            StatItem(
                labelRes = CoreRes.string.profile_stat_series,
                value = totalSeries.formatWithCommas(),
                icon = Icons.Filled.Tv,
            )
            StatItem(
                labelRes = CoreRes.string.profile_stat_collections,
                value = totalCollections.formatWithCommas(),
                icon = Icons.Filled.Folder,
            )
        }
    }
}

@Composable
private fun StatItem(
    labelRes: StringResource,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(labelRes)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleLargeEmphasized)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileStatsSection(
    stats: WatchStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(MovieAppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
        ) {
            Text(
                text = stringResource(Res.string.profile_stats_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            Text(
                text = stringResource(Res.string.profile_stats_basis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (stats.isEmpty()) {
                Text(
                    text = stringResource(Res.string.profile_stats_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                StatsSummaryRow(totalWatchTimeMinutes = stats.totalWatchTimeMinutes)
                GenreRingChart(genres = stats.genreBreakdown.take(MAX_VISIBLE_STATS))
                StatsBarGroup(
                    title = stringResource(Res.string.profile_stats_decades),
                    rows = stats.decadeBreakdown.take(MAX_VISIBLE_STATS).map { it.toStatRow() },
                )
                StatsBarGroup(
                    title = stringResource(Res.string.profile_stats_ratings),
                    rows = stats.ratingHistogram.toRatingRows(),
                )
                TopPeopleList(people = stats.topPeople.take(MAX_VISIBLE_STATS))
            }
        }
    }
}

@Composable
private fun StatsSummaryRow(
    totalWatchTimeMinutes: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.profile_stats_watch_time),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = totalWatchTimeMinutes.formatWatchTime(),
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun GenreRingChart(
    genres: List<GenreCount>,
    modifier: Modifier = Modifier,
) {
    if (genres.isEmpty()) return

    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.outline,
        )
    val total = genres.sumOf { it.count }.coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(108.dp)) {
            var startAngle = -90f
            val strokeWidth = 14.dp.toPx()
            genres.forEachIndexed { index, genre ->
                val sweepAngle = 360f * genre.count / total
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    size = Size(size.width, size.height),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                startAngle += sweepAngle
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        ) {
            Text(
                text = stringResource(Res.string.profile_stats_genres),
                style = MaterialTheme.typography.titleSmall,
            )
            genres.forEachIndexed { index, genre ->
                LegendRow(
                    color = colors[index % colors.size],
                    label = genre.genreName,
                    value = genre.count.formatWithCommas(),
                )
            }
        }
    }
}

@Composable
private fun LegendRow(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(color))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatsBarGroup(
    title: String,
    rows: List<StatRow>,
    modifier: Modifier = Modifier,
) {
    if (rows.isEmpty()) return

    val maxCount = rows.maxOf { it.count }.coerceAtLeast(1)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        rows.forEach { row -> LinearStatRow(row = row, maxCount = maxCount) }
    }
}

@Composable
private fun LinearStatRow(
    row: StatRow,
    maxCount: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = row.count.toFloat() / maxCount
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(56.dp),
        )
        Box(
            modifier =
                Modifier.weight(1f)
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier =
                    Modifier.fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = row.count.formatWithCommas(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TopPeopleList(
    people: List<PersonCount>,
    modifier: Modifier = Modifier,
) {
    if (people.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        Text(
            text = stringResource(Res.string.profile_stats_top_people),
            style = MaterialTheme.typography.titleSmall,
        )
        people.forEach { person ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text =
                        stringResource(
                            Res.string.profile_stats_person_count,
                            person.role,
                            person.count,
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    onSettingsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onStorageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(MovieAppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(CoreRes.string.settings_section_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )

            SettingsItem(
                icon = Icons.Filled.Folder,
                titleRes = CoreRes.string.settings_all,
                onClick = onSettingsClick,
            )

            SettingsItem(
                icon = Icons.Filled.Person,
                titleRes = CoreRes.string.settings_account,
                onClick = onAccountClick,
            )

            SettingsItem(
                icon = Icons.Filled.Palette,
                titleRes = CoreRes.string.settings_theme,
                onClick = onThemeClick,
            )

            SettingsItem(
                icon = Icons.Filled.Language,
                titleRes = CoreRes.string.settings_language,
                onClick = onLanguageClick,
            )

            SettingsItem(
                icon = Icons.Filled.Storage,
                titleRes = CoreRes.string.settings_storage,
                onClick = onStorageClick,
            )

            SettingsItem(
                icon = Icons.Filled.Help,
                titleRes = CoreRes.string.settings_help,
                onClick = onHelpClick,
            )

            SettingsItem(
                icon = Icons.Filled.Info,
                titleRes = CoreRes.string.settings_about,
                onClick = onAboutClick,
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titleRes: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(titleRes)
    Row(
        modifier =
            modifier.fillMaxWidth().minimumInteractiveComponentSize().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(MovieAppSpacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = stringResource(CoreRes.string.cd_navigate),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun WatchStats.isEmpty(): Boolean =
    totalWatchTimeMinutes == 0L &&
        genreBreakdown.isEmpty() &&
        decadeBreakdown.isEmpty() &&
        topPeople.isEmpty() &&
        ratingHistogram.isEmpty()

@Composable
private fun Long.formatWatchTime(): String {
    val hours = this / MINUTES_PER_HOUR
    val minutes = this % MINUTES_PER_HOUR
    return when {
        hours > 0 && minutes > 0 -> stringResource(Res.string.profile_watch_time_hours_minutes, hours, minutes)
        hours > 0 -> stringResource(Res.string.profile_watch_time_hours, hours)
        else -> stringResource(Res.string.profile_watch_time_minutes, minutes)
    }
}

private fun DecadeCount.toStatRow(): StatRow = StatRow(label = decade, count = count)

@Composable
private fun Map<Int, Int>.toRatingRows(): List<StatRow> =
    entries.sortedBy { it.key }.map {
        StatRow(label = pluralStringResource(Res.plurals.profile_rating_star, it.key, it.key), count = it.value)
    }

private data class StatRow(val label: String, val count: Int)

private const val MAX_VISIBLE_STATS = 5
private const val MINUTES_PER_HOUR = 60L
