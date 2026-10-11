package com.movieapp.feature.movies.screens.upcoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.core.common.formatShort
import com.movieapp.core.common.toLocalDateOrNull
import com.movieapp.core.model.Movie
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.cd_back
import com.movieapp.core.ui.resources.month_names_short
import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.upcoming_all_dates
import com.movieapp.feature.movies.resources.upcoming_calendar_title
import com.movieapp.feature.movies.resources.upcoming_tba
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UpcomingCalendarScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpcomingCalendarViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UpcomingCalendarContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onMovieClick = onMovieClick,
        onRefresh = { viewModel.onEvent(UpcomingCalendarUiEvent.Refresh) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UpcomingCalendarContent(
    uiState: UpcomingCalendarUiState,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthNames = stringArrayResource(CoreRes.array.month_names_short)
    val fallback = stringResource(Res.string.upcoming_tba)
    val grouped = uiState.movies.groupBy { movie ->
        movie.releaseDate?.toLocalDateOrNull()?.monthLabel(monthNames) ?: fallback
    }
    var selectedMonth by remember(grouped.keys) { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(stringResource(Res.string.upcoming_calendar_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(CoreRes.string.cd_back))
                    }
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(MovieAppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
            ) {
                if (grouped.isNotEmpty()) {
                    item(key = "month_selector", contentType = "filter") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                            item(key = "all_months", contentType = "month_filter") {
                                FilterChip(
                                    selected = selectedMonth == null,
                                    onClick = { selectedMonth = null },
                                    label = { Text(stringResource(Res.string.upcoming_all_dates)) },
                                )
                            }
                            items(grouped.keys.toList(), key = { it }) { month ->
                                FilterChip(
                                    selected = selectedMonth == month,
                                    onClick = { selectedMonth = month },
                                    label = { Text(month) },
                                )
                            }
                        }
                    }
                }
                uiState.error?.let { message ->
                    item(key = "upcoming_error", contentType = "status") {
                        Text(message, color = MaterialTheme.colorScheme.error)
                    }
                }
                grouped.forEach { (month, movies) ->
                    if (selectedMonth == null || selectedMonth == month) {
                        item(key = "month_$month", contentType = "month") {
                            Text(month, style = MaterialTheme.typography.titleLarge)
                        }
                        items(movies, key = { it.id }, contentType = { "movie" }) { movie ->
                            UpcomingMovieRow(movie, monthNames, fallback) { onMovieClick(movie.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingMovieRow(
    movie: Movie,
    monthNames: List<String>,
    fallback: String,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    movie.releaseDate?.toLocalDateOrNull()?.formatShort(monthNames) ?: fallback,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun LocalDate.monthLabel(monthNames: List<String>): String = "${monthNames[month.ordinal]} $year"
