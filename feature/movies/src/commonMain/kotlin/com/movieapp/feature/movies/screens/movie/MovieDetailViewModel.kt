package com.movieapp.feature.movies.screens.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.ClearUserContentRatingUseCase
import com.movieapp.core.domain.usecase.PrefetchContentForOfflineUseCase
import com.movieapp.core.domain.usecase.SetUserContentRatingUseCase
import com.movieapp.core.domain.usecase.ToggleFavoriteMovieUseCase
import com.movieapp.core.domain.usecase.ToggleWatchlistMovieUseCase
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Movie
import com.movieapp.core.model.Review
import com.movieapp.core.model.UserCollection
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.model.WatchProviders
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val movie: Movie? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isWatchlisted: Boolean = false,
    val isFavoriteMutationPending: Boolean = false,
    val isWatchlistMutationPending: Boolean = false,
    val userRatingOutOfFive: Int = 0,
    val watchProviders: WatchProviders? = null,
    val watchProvidersLoading: Boolean = false,
    val watchProvidersForRegion: List<WatchProvider> = emptyList(),
    val watchProviderLink: String? = null,
    val credits: Credits? = null,
    val keywords: List<Keyword> = emptyList(),
    val externalIds: ExternalIds? = null,
    val fromCacheOnly: Boolean = false,
    val isSavedOffline: Boolean = false,
    val isOfflineSaving: Boolean = false,
    val showAddToCollection: Boolean = false,
    val collections: List<UserCollection> = emptyList(),
    val isAddingToCollection: Boolean = false,
)

sealed interface MovieDetailUiEvent {
    data object ToggleFavorite : MovieDetailUiEvent

    data object ToggleWatchlist : MovieDetailUiEvent

    data object Retry : MovieDetailUiEvent

    data class SetUserRating(val outOfFive: Int) : MovieDetailUiEvent

    data object ClearUserRating : MovieDetailUiEvent

    data object SaveForOffline : MovieDetailUiEvent
    data object OpenAddToCollection : MovieDetailUiEvent

    data object DismissAddToCollection : MovieDetailUiEvent

    data class AddToCollection(val collectionId: String) : MovieDetailUiEvent

    data class CreateCollectionAndAdd(val name: String) : MovieDetailUiEvent
}

sealed interface MovieDetailSideEffect {
    data class ShowMessage(val message: String) : MovieDetailSideEffect
    data class FavoriteUpdated(val isFavorite: Boolean) : MovieDetailSideEffect

    data object FavoriteUpdateFailed : MovieDetailSideEffect

    data class WatchlistUpdated(val isWatchlisted: Boolean) : MovieDetailSideEffect

    data object WatchlistUpdateFailed : MovieDetailSideEffect
}

class MovieDetailViewModel(
    private val movieId: Int,
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val toggleFavoriteMovieUseCase: ToggleFavoriteMovieUseCase,
    private val toggleWatchlistMovieUseCase: ToggleWatchlistMovieUseCase,
    private val setUserContentRatingUseCase: SetUserContentRatingUseCase,
    private val clearUserContentRatingUseCase: ClearUserContentRatingUseCase,
    private val prefetchContentForOfflineUseCase: PrefetchContentForOfflineUseCase,
    private val offlineContentRepository: OfflineContentRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<MovieDetailSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()
    private val tmdbLanguage: Flow<String> = preferencesRepository.tmdbLanguage

    val similarMovies: Flow<PagingData<Movie>> =
        tmdbLanguage
            .distinctUntilChanged()
            .flatMapLatest { lang -> movieRepository.getSimilarMoviesPaged(movieId, lang) }
            .cachedIn(viewModelScope)

    val recommendedMovies: Flow<PagingData<Movie>> =
        tmdbLanguage
            .distinctUntilChanged()
            .flatMapLatest { lang -> movieRepository.getRecommendedMoviesPaged(movieId, lang) }
            .cachedIn(viewModelScope)

    val reviews: Flow<PagingData<Review>> =
        tmdbLanguage
            .distinctUntilChanged()
            .flatMapLatest { lang -> movieRepository.getMovieReviewsPaged(movieId, lang) }
            .cachedIn(viewModelScope)

    init {
        loadMovieDetails()
        checkUserStatus()
        observeOfflineStatus()
        loadAuxiliary()
        loadWatchProvidersFlow()
    }

    fun onEvent(event: MovieDetailUiEvent) {
        when (event) {
            is MovieDetailUiEvent.ToggleFavorite -> toggleFavorite()
            is MovieDetailUiEvent.ToggleWatchlist -> toggleWatchlist()
            is MovieDetailUiEvent.Retry -> {
                loadMovieDetails()
                loadAuxiliary()
                loadWatchProvidersFlow()
            }

            is MovieDetailUiEvent.SetUserRating -> setRating(event.outOfFive)
            is MovieDetailUiEvent.ClearUserRating -> clearRating()
            is MovieDetailUiEvent.SaveForOffline -> saveForOffline()
            is MovieDetailUiEvent.OpenAddToCollection -> openAddToCollection()
            is MovieDetailUiEvent.DismissAddToCollection ->
                _uiState.update { it.copy(showAddToCollection = false) }
            is MovieDetailUiEvent.AddToCollection -> addToCollection(event.collectionId)
            is MovieDetailUiEvent.CreateCollectionAndAdd -> createCollectionAndAdd(event.name)
        }
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            movieRepository.getMovieDetails(movieId).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success ->
                        _uiState.update {
                            it.copy(
                                movie = result.data,
                                isLoading = false,
                                error = null,
                                fromCacheOnly = false,
                            )
                        }

                    is Result.Error -> {
                        val snap =
                            movieRepository
                                .getMovieById(movieId)
                                .filter { it !is Result.Loading }
                                .first()
                        if (snap is Result.Success) {
                            _uiState.update {
                                it.copy(
                                    movie = snap.data,
                                    isLoading = false,
                                    error = null,
                                    fromCacheOnly = true,
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.exception.message ?: "Failed to load movie",
                                    fromCacheOnly = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            val isFav = userRepository.isFavorite(movieId, ContentType.MOVIE)
            val isWatch = userRepository.isInWatchlist(movieId, ContentType.MOVIE)
            val outOfFive =
                when (val r = userRepository.getUserRating(movieId, ContentType.MOVIE)) {
                    is Result.Success ->
                        (r.data ?: 0f).let { v -> (v / 2f).coerceIn(0f, 5f).toInt() }
                    else -> 0
                }
            _uiState.update {
                it.copy(
                    isFavorite = isFav,
                    isWatchlisted = isWatch,
                    userRatingOutOfFive = outOfFive,
                )
            }
        }
    }

    private fun observeOfflineStatus() {
        viewModelScope.launch {
            offlineContentRepository.observeOfflineContent(movieId, ContentType.MOVIE).collect {
                offlineContent ->
                _uiState.update { it.copy(isSavedOffline = offlineContent != null) }
            }
        }
    }

    private fun loadAuxiliary() {
        viewModelScope.launch {
            val lang = preferencesRepository.tmdbLanguage.first()
            when (val res = movieRepository.getMovieCredits(movieId, lang)) {
                is Result.Success -> _uiState.update { it.copy(credits = res.data) }
                else -> Unit
            }
            when (val res = movieRepository.getMovieKeywords(movieId)) {
                is Result.Success -> _uiState.update { it.copy(keywords = res.data) }
                else -> Unit
            }
            when (val res = movieRepository.getMovieExternalIds(movieId)) {
                is Result.Success -> _uiState.update { it.copy(externalIds = res.data) }
                else -> Unit
            }
        }
    }

    private fun loadWatchProvidersFlow() {
        viewModelScope.launch {
            val region = preferencesRepository.watchRegion.first()
            _uiState.update { it.copy(watchProvidersLoading = true) }
            movieRepository.getMovieWatchProviders(movieId).collect { result ->
                when (result) {
                    is Result.Loading -> Unit
                    is Result.Success -> {
                        val country = result.data.results[region]
                        val merged = buildList {
                            country?.flatrate?.let { addAll(it) }
                            country?.rent?.let { addAll(it) }
                            country?.buy?.let { addAll(it) }
                        }
                            .distinctBy { it.providerId }
                        _uiState.update {
                            it.copy(
                                watchProviders = result.data,
                                watchProvidersLoading = false,
                                watchProvidersForRegion = merged,
                                watchProviderLink = country?.link,
                            )
                        }
                    }

                    is Result.Error -> _uiState.update { it.copy(watchProvidersLoading = false) }
                }
            }
        }
    }

    private fun toggleFavorite() {
        if (_uiState.value.isFavoriteMutationPending) return
        viewModelScope.launch {
            val isFavorite = _uiState.value.isFavorite
            _uiState.update { it.copy(isFavoriteMutationPending = true) }
            when (toggleFavoriteMovieUseCase(movieId, isFavorite)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isFavorite = !isFavorite,
                            isFavoriteMutationPending = false,
                        )
                    }
                    _sideEffect.send(
                        MovieDetailSideEffect.FavoriteUpdated(isFavorite = !isFavorite)
                    )
                }
                is Result.Error,
                Result.Loading -> {
                    _uiState.update { it.copy(isFavoriteMutationPending = false) }
                    _sideEffect.send(MovieDetailSideEffect.FavoriteUpdateFailed)
                }
            }
        }
    }

    private fun toggleWatchlist() {
        if (_uiState.value.isWatchlistMutationPending) return
        viewModelScope.launch {
            val isWatchlisted = _uiState.value.isWatchlisted
            _uiState.update { it.copy(isWatchlistMutationPending = true) }
            when (toggleWatchlistMovieUseCase(movieId, isWatchlisted)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isWatchlisted = !isWatchlisted,
                            isWatchlistMutationPending = false,
                        )
                    }
                    _sideEffect.send(
                        MovieDetailSideEffect.WatchlistUpdated(isWatchlisted = !isWatchlisted)
                    )
                }
                is Result.Error,
                Result.Loading -> {
                    _uiState.update { it.copy(isWatchlistMutationPending = false) }
                    _sideEffect.send(MovieDetailSideEffect.WatchlistUpdateFailed)
                }
            }
        }
    }

    private fun setRating(outOfFive: Int) {
        viewModelScope.launch {
            val outOfTen = (outOfFive * 2).coerceIn(2, 10).toFloat()
            when (setUserContentRatingUseCase(movieId, ContentType.MOVIE, outOfTen, null)) {
                is Result.Success -> _uiState.update { it.copy(userRatingOutOfFive = outOfFive) }
                is Result.Error ->
                    _sideEffect.send(MovieDetailSideEffect.ShowMessage("Could not save rating"))

                else -> Unit
            }
        }
    }

    private fun clearRating() {
        viewModelScope.launch {
            clearUserContentRatingUseCase(movieId, ContentType.MOVIE)
            _uiState.update { it.copy(userRatingOutOfFive = 0) }
        }
    }

    private fun saveForOffline() {
        if (_uiState.value.isOfflineSaving || _uiState.value.isSavedOffline) return
        viewModelScope.launch {
            prefetchContentForOfflineUseCase(movieId, ContentType.MOVIE).collect { result ->
                when (result) {
                    Result.Loading -> _uiState.update { it.copy(isOfflineSaving = true) }
                    is Result.Success -> {
                        _uiState.update { it.copy(isOfflineSaving = false, isSavedOffline = true) }
                        _sideEffect.send(MovieDetailSideEffect.ShowMessage("Saved for offline"))
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isOfflineSaving = false) }
                        _sideEffect.send(
                            MovieDetailSideEffect.ShowMessage("Could not save for offline")
                        )
                    }
                }
            }
        }
    }

    private fun openAddToCollection() {
        viewModelScope.launch {
            val collections = userRepository.getAllCollections().first()
            _uiState.update { it.copy(showAddToCollection = true, collections = collections) }
        }
    }

    private fun addToCollection(collectionId: String) {
        viewModelScope.launch {
            val collection = _uiState.value.collections.find { it.id == collectionId }
            _uiState.update { it.copy(isAddingToCollection = true) }
            val existing =
                userRepository.getCollectionItems(collectionId).first().any {
                    it.contentId == movieId && it.contentType == ContentType.MOVIE
                }
            if (existing) {
                _uiState.update {
                    it.copy(isAddingToCollection = false, showAddToCollection = false)
                }
                _sideEffect.send(
                    MovieDetailSideEffect.ShowMessage(
                        "Already in ${collection?.name ?: "collection"}"
                    )
                )
                return@launch
            }
            when (userRepository.addToCollection(collectionId, movieId, ContentType.MOVIE)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isAddingToCollection = false, showAddToCollection = false)
                    }
                    _sideEffect.send(
                        MovieDetailSideEffect.ShowMessage(
                            "Added to ${collection?.name ?: "collection"}"
                        )
                    )
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isAddingToCollection = false) }
                    _sideEffect.send(
                        MovieDetailSideEffect.ShowMessage("Could not add to collection")
                    )
                }
                else -> _uiState.update { it.copy(isAddingToCollection = false) }
            }
        }
    }

    private fun createCollectionAndAdd(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCollection = true) }
            when (val created = userRepository.createCollection(name, null, false)) {
                is Result.Success -> {
                    val collections = userRepository.getAllCollections().first()
                    _uiState.update { it.copy(collections = collections) }
                    when (
                        userRepository.addToCollection(created.data.id, movieId, ContentType.MOVIE)
                    ) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(isAddingToCollection = false, showAddToCollection = false)
                            }
                            _sideEffect.send(
                                MovieDetailSideEffect.ShowMessage("Added to ${created.data.name}")
                            )
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(isAddingToCollection = false) }
                            _sideEffect.send(
                                MovieDetailSideEffect.ShowMessage("Could not add to collection")
                            )
                        }
                        else -> _uiState.update { it.copy(isAddingToCollection = false) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isAddingToCollection = false) }
                    _sideEffect.send(
                        MovieDetailSideEffect.ShowMessage("Could not create collection")
                    )
                }
                else -> _uiState.update { it.copy(isAddingToCollection = false) }
            }
        }
    }
}
