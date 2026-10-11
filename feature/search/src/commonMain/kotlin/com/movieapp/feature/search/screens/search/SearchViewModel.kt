package com.movieapp.feature.search.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.movieapp.core.common.Result
import com.movieapp.core.common.network.NetworkConnectivity
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.WatchProviderRepository
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Genre
import com.movieapp.core.model.Movie
import com.movieapp.core.model.MultiSearchListRow
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.WatchProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchFilter(
    val year: Int? = null,
    val ratingGte: Double? = null,
    val genreId: Int? = null,
    val watchProviderIds: Set<Int> = emptySet(),
) {
    fun isEmpty() =
        year == null && ratingGte == null && genreId == null && watchProviderIds.isEmpty()
}
enum class SearchFilterField {
    YEAR,
    RATING,
    GENRE,
    PROVIDERS,
}

fun SearchFilter.without(field: SearchFilterField): SearchFilter =
    when (field) {
        SearchFilterField.YEAR -> copy(year = null)
        SearchFilterField.RATING -> copy(ratingGte = null)
        SearchFilterField.GENRE -> copy(genreId = null)
        SearchFilterField.PROVIDERS -> copy(watchProviderIds = emptySet())
    }

fun isValidFilterYear(year: String): Boolean =
    year.isBlank() ||
        (year.length == 4 && year.all(Char::isDigit) && year.toIntOrNull()?.let { it > 0 } == true)

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter(),
    val isFilterSheetVisible: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val tvSeries: List<TvSeries> = emptyList(),
    val actors: List<Actor> = emptyList(),
    val discoverGenres: List<Genre> = emptyList(),
    val watchProviders: List<WatchProvider> = emptyList(),
    val isWatchProvidersLoading: Boolean = false,
    val watchProvidersError: String? = null,
    val recentSearchQueries: List<String> = emptyList(),
    val trendingMovies: List<Movie> = emptyList(),
    val trendingTvSeries: List<TvSeries> = emptyList(),
    /** No validated network path (user may still see cached lists). */
    val isOffline: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface SearchUiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent

    data class FilterChanged(val filter: SearchFilter) : SearchUiEvent

    data class FilterSheetVisibilityChanged(val visible: Boolean) : SearchUiEvent

    data object DiscoverPullRefresh : SearchUiEvent

    data object SearchCommitted : SearchUiEvent

    data class RecentSearchClick(val query: String) : SearchUiEvent

    data object ClearRecentSearches : SearchUiEvent

    data class MovieClicked(val movieId: Int) : SearchUiEvent

    data class SeriesClicked(val seriesId: Int) : SearchUiEvent

    data class ActorClicked(val actorId: Int) : SearchUiEvent
}

sealed interface SearchSideEffect {
    data class NavigateToMovieDetail(val movieId: Int) : SearchSideEffect

    data class NavigateToSeriesDetail(val seriesId: Int) : SearchSideEffect

    data class NavigateToActorDetail(val actorId: Int) : SearchSideEffect
}

class SearchViewModel(
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val actorRepository: ActorRepository,
    private val watchProviderRepository: WatchProviderRepository,
    private val preferencesRepository: PreferencesRepository,
    networkConnectivity: NetworkConnectivity,
) : ViewModel() {

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> onQueryChange(event.query)
            is SearchUiEvent.FilterChanged -> onFilterChange(event.filter)
            is SearchUiEvent.FilterSheetVisibilityChanged ->
                onFilterSheetVisibilityChange(event.visible)
            SearchUiEvent.DiscoverPullRefresh -> onDiscoverPullRefresh()
            SearchUiEvent.SearchCommitted -> onSearchCommitted()
            is SearchUiEvent.RecentSearchClick -> onRecentSearchClick(event.query)
            SearchUiEvent.ClearRecentSearches -> onClearRecentSearches()
            is SearchUiEvent.MovieClicked ->
                viewModelScope.launch {
                    _sideEffect.send(SearchSideEffect.NavigateToMovieDetail(event.movieId))
                }
            is SearchUiEvent.SeriesClicked ->
                viewModelScope.launch {
                    _sideEffect.send(SearchSideEffect.NavigateToSeriesDetail(event.seriesId))
                }
            is SearchUiEvent.ActorClicked ->
                viewModelScope.launch {
                    _sideEffect.send(SearchSideEffect.NavigateToActorDetail(event.actorId))
                }
        }
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<SearchSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    private val _queryFlow = MutableStateFlow("")
    private val _filterFlow = MutableStateFlow(SearchFilter())

    private val debouncedQuery =
        _queryFlow
            .debounce(300L)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val movieSearchPaged: Flow<PagingData<Movie>> =
        combine(
                debouncedQuery,
                preferencesRepository.tmdbLanguage,
                preferencesRepository.watchRegion,
            ) { query, lang, region ->
                Triple(query.trim(), lang, region)
            }
            .distinctUntilChanged()
            .flatMapLatest { (query, lang, region) ->
                if (query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    movieRepository.searchMoviesPaged(query, lang, region)
                }
            }
            .cachedIn(viewModelScope)

    val multiSearchPaged: Flow<PagingData<MultiSearchListRow>> =
        combine(
                debouncedQuery,
                preferencesRepository.tmdbLanguage,
                preferencesRepository.watchRegion,
            ) { query, lang, region ->
                Triple(query.trim(), lang, region)
            }
            .distinctUntilChanged()
            .flatMapLatest { (query, lang, region) ->
                if (query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    movieRepository.multiSearchPaged(query, lang, region)
                }
            }
            .cachedIn(viewModelScope)

    val tvSearchPaged: Flow<PagingData<TvSeries>> =
        combine(debouncedQuery, preferencesRepository.tmdbLanguage) { query, lang ->
                query.trim() to lang
            }
            .distinctUntilChanged()
            .flatMapLatest { (query, lang) ->
                if (query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    seriesRepository.searchSeries(query, lang)
                }
            }
            .cachedIn(viewModelScope)

    val actorSearchPaged: Flow<PagingData<Actor>> =
        combine(
                debouncedQuery,
                preferencesRepository.tmdbLanguage,
                preferencesRepository.watchRegion,
            ) { query, lang, region ->
                Triple(query.trim(), lang, region)
            }
            .distinctUntilChanged()
            .flatMapLatest { (query, lang, region) ->
                if (query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    actorRepository.searchActors(query, lang, region)
                }
            }
            .cachedIn(viewModelScope)

    init {
        networkConnectivity.isOnline
            .onEach { online -> _uiState.update { it.copy(isOffline = !online) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            combine(
                    _queryFlow.debounce(300L).distinctUntilChanged(),
                    _filterFlow,
                    preferencesRepository.tmdbLanguage,
                ) { query, filter, lang ->
                    Triple(query, filter, lang)
                }
                .collectLatest { (query, filter, lang) ->
                    val movieRes =
                        movieRepository
                            .getMovieGenresList(lang)
                            .filterNot { it is Result.Loading }
                            .first()
                    val tvRes =
                        seriesRepository
                            .getTvGenresList(lang)
                            .filterNot { it is Result.Loading }
                            .first()
                    val merged =
                        buildMap<Int, Genre> {
                            if (movieRes is Result.Success) {
                                movieRes.data.forEach { g -> put(g.id, g) }
                            }
                            if (tvRes is Result.Success) {
                                tvRes.data.forEach { g -> put(g.id, g) }
                            }
                        }
                    _uiState.update {
                        it.copy(discoverGenres = merged.values.sortedBy { g -> g.name })
                    }

                    when {
                        query.isNotBlank() -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = null,
                                    movies = emptyList(),
                                    tvSeries = emptyList(),
                                    actors = emptyList(),
                                    trendingMovies = emptyList(),
                                    trendingTvSeries = emptyList(),
                                )
                            }
                        }

                        !filter.isEmpty() -> discoverContent(filter)
                        else -> loadTrendingSpotlight()
                    }
                }
        }

        viewModelScope.launch {
            combine(preferencesRepository.tmdbLanguage, preferencesRepository.watchRegion) { _, _ ->
                    Unit
                }
                .collectLatest { loadWatchProviderCatalog() }
        }

        viewModelScope.launch {
            preferencesRepository.recentSearchQueries.collect { recent ->
                _uiState.update { it.copy(recentSearchQueries = recent) }
            }
        }
    }

    private fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        _queryFlow.value = query
    }

    private fun onFilterChange(filter: SearchFilter) {
        _uiState.update { it.copy(filter = filter) }
        _filterFlow.value = filter
    }

    private fun onFilterSheetVisibilityChange(isVisible: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = isVisible) }
    }

    private fun onDiscoverPullRefresh() {
        viewModelScope.launch {
            val filter = _filterFlow.value
            when {
                _queryFlow.value.isNotBlank() -> Unit
                !filter.isEmpty() -> discoverContent(filter)
                else -> loadTrendingSpotlight()
            }
        }
    }

    private fun onSearchCommitted() {
        val q = _uiState.value.query.trim()
        if (q.isBlank()) return
        viewModelScope.launch { preferencesRepository.addRecentSearchQuery(q) }
    }

    private fun onRecentSearchClick(query: String) {
        onQueryChange(query)
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch { preferencesRepository.addRecentSearchQuery(trimmed) }
        }
    }

    private fun onClearRecentSearches() {
        viewModelScope.launch { preferencesRepository.clearRecentSearchQueries() }
    }

    private suspend fun loadTrendingSpotlight() {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                movies = emptyList(),
                tvSeries = emptyList(),
                actors = emptyList(),
            )
        }
        try {
            coroutineScope {
                val moviesDeferred = async {
                    movieRepository.getTrendingMovies().filterNot { it is Result.Loading }.first()
                }
                val tvDeferred = async {
                    seriesRepository
                        .getTrendingTvSeriesPage()
                        .filterNot { it is Result.Loading }
                        .first()
                }
                val moviesResult = moviesDeferred.await()
                val tvResult = tvDeferred.await()
                val movies = (moviesResult as? Result.Success)?.data ?: emptyList()
                val tv = (tvResult as? Result.Success)?.data ?: emptyList()
                val err =
                    when {
                        moviesResult is Result.Error && tvResult is Result.Error ->
                            moviesResult.exception.message
                        else -> null
                    }
                _uiState.update {
                    it.copy(
                        trendingMovies = movies.take(20),
                        trendingTvSeries = tv.take(20),
                        isLoading = false,
                        error = err,
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message,
                    trendingMovies = emptyList(),
                    trendingTvSeries = emptyList(),
                )
            }
        }
    }

    private suspend fun discoverContent(filter: SearchFilter) {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                trendingMovies = emptyList(),
                trendingTvSeries = emptyList(),
            )
        }
        try {
            val watchProviderIds = filter.watchProviderIds.sorted().takeIf { it.isNotEmpty() }
            val watchRegion =
                if (watchProviderIds == null) null else preferencesRepository.watchRegion.first()
            coroutineScope {
                val moviesDeferred = async {
                    movieRepository
                        .discoverMovies(
                            year = filter.year,
                            ratingGte = filter.ratingGte,
                            genreIds = filter.genreId?.let { listOf(it) },
                            watchProviderIds = watchProviderIds,
                            watchRegion = watchRegion,
                        )
                        .filterNot { it is Result.Loading }
                        .first()
                }
                val seriesDeferred = async {
                    seriesRepository
                        .discoverSeries(
                            year = filter.year,
                            ratingGte = filter.ratingGte,
                            genreIds = filter.genreId?.let { listOf(it) },
                            watchProviderIds = watchProviderIds,
                            watchRegion = watchRegion,
                        )
                        .filterNot { it is Result.Loading }
                        .first()
                }

                val moviesResult = moviesDeferred.await()
                val seriesResult = seriesDeferred.await()

                _uiState.update {
                    it.copy(
                        movies = (moviesResult as? Result.Success)?.data ?: emptyList(),
                        tvSeries = (seriesResult as? Result.Success)?.data ?: emptyList(),
                        actors = emptyList(),
                        isLoading = false,
                        error =
                            when {
                                moviesResult is Result.Error -> moviesResult.exception.message
                                seriesResult is Result.Error -> seriesResult.exception.message
                                else -> null
                            },
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private suspend fun loadWatchProviderCatalog() {
        _uiState.update { it.copy(isWatchProvidersLoading = true, watchProvidersError = null) }
        when (
            val result =
                watchProviderRepository
                    .getWatchProviderCatalog()
                    .filterNot { it is Result.Loading }
                    .first()
        ) {
            is Result.Success ->
                _uiState.update {
                    it.copy(
                        watchProviders = result.data,
                        isWatchProvidersLoading = false,
                        watchProvidersError = null,
                    )
                }
            is Result.Error ->
                _uiState.update {
                    it.copy(
                        watchProviders = emptyList(),
                        isWatchProvidersLoading = false,
                        watchProvidersError = result.exception.message,
                    )
                }
            Result.Loading -> Unit
        }
    }
}
