package com.movieapp.feature.series.screens.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.ClearUserContentRatingUseCase
import com.movieapp.core.domain.usecase.PrefetchContentForOfflineUseCase
import com.movieapp.core.domain.usecase.SetUserContentRatingUseCase
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Review
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.UserCollection
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.model.WatchProviders
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeriesDetailUiState(
    val series: TvSeries? = null,
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

sealed interface SeriesDetailUiEvent {
    data object ToggleFavorite : SeriesDetailUiEvent

    data object ToggleWatchlist : SeriesDetailUiEvent

    data object Retry : SeriesDetailUiEvent

    data class SetUserRating(val outOfFive: Int) : SeriesDetailUiEvent

    data object ClearUserRating : SeriesDetailUiEvent

    data object SaveForOffline : SeriesDetailUiEvent

    data object OpenAddToCollection : SeriesDetailUiEvent

    data object DismissAddToCollection : SeriesDetailUiEvent

    data class AddToCollection(val collectionId: String) : SeriesDetailUiEvent

    data class CreateCollectionAndAdd(val name: String) : SeriesDetailUiEvent
}

sealed interface SeriesDetailSideEffect {
    data class ShowMessage(val message: String) : SeriesDetailSideEffect

    data class FavoriteUpdated(val isFavorite: Boolean) : SeriesDetailSideEffect

    data object FavoriteUpdateFailed : SeriesDetailSideEffect

    data class WatchlistUpdated(val isWatchlisted: Boolean) : SeriesDetailSideEffect

    data object WatchlistUpdateFailed : SeriesDetailSideEffect
}

class SeriesDetailViewModel(
    private val seriesId: Int,
    private val seriesRepository: SeriesRepository,
    private val userRepository: UserRepository,
    private val setUserContentRatingUseCase: SetUserContentRatingUseCase,
    private val clearUserContentRatingUseCase: ClearUserContentRatingUseCase,
    private val prefetchContentForOfflineUseCase: PrefetchContentForOfflineUseCase,
    private val offlineContentRepository: OfflineContentRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<SeriesDetailSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    private val tmdbLanguage: Flow<String> = preferencesRepository.tmdbLanguage

    val similarSeries: Flow<PagingData<TvSeries>> =
        tmdbLanguage
            .distinctUntilChanged()
            .flatMapLatest { lang -> seriesRepository.getSimilarSeries(seriesId, lang) }
            .cachedIn(viewModelScope)

    val recommendedSeries: Flow<PagingData<TvSeries>> =
        tmdbLanguage
            .distinctUntilChanged()
            .flatMapLatest { lang -> seriesRepository.getRecommendedSeries(seriesId, lang) }
            .cachedIn(viewModelScope)

    val reviews: Flow<PagingData<Review>> =
        tmdbLanguage
            .distinctUntilChanged()
            .flatMapLatest { lang -> seriesRepository.getTvSeriesReviewsPaged(seriesId, lang) }
            .cachedIn(viewModelScope)

    init {
        loadSeries()
        checkUserStatus()
        observeOfflineStatus()
        loadAuxiliary()
        loadWatchProviders()
    }

    fun onEvent(event: SeriesDetailUiEvent) {
        when (event) {
            SeriesDetailUiEvent.ToggleFavorite -> toggleFavorite()
            SeriesDetailUiEvent.ToggleWatchlist -> toggleWatchlist()
            SeriesDetailUiEvent.Retry -> {
                loadSeries()
                loadAuxiliary()
                loadWatchProviders()
            }

            is SeriesDetailUiEvent.SetUserRating -> setRating(event.outOfFive)
            SeriesDetailUiEvent.ClearUserRating -> clearRating()
            SeriesDetailUiEvent.SaveForOffline -> saveForOffline()
            SeriesDetailUiEvent.OpenAddToCollection -> openAddToCollection()
            SeriesDetailUiEvent.DismissAddToCollection ->
                _uiState.update { it.copy(showAddToCollection = false) }
            is SeriesDetailUiEvent.AddToCollection -> addToCollection(event.collectionId)
            is SeriesDetailUiEvent.CreateCollectionAndAdd -> createCollectionAndAdd(event.name)
        }
    }

    private fun loadSeries() {
        viewModelScope.launch {
            seriesRepository.getSeriesByIdFlow(seriesId).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success ->
                        _uiState.update {
                            it.copy(
                                series = result.data,
                                isLoading = false,
                                error = null,
                                fromCacheOnly = false,
                            )
                        }

                    is Result.Error -> {
                        when (
                            val remote = runCatching {
                                seriesRepository.getSeriesById(seriesId)
                            }
                                .getOrNull()
                        ) {
                            is Result.Success ->
                                _uiState.update {
                                    it.copy(
                                        series = remote.data,
                                        isLoading = false,
                                        error = null,
                                        fromCacheOnly = true,
                                    )
                                }

                            else ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = result.exception.message ?: "Failed to load series",
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
            val isFav = userRepository.isFavorite(seriesId, ContentType.TV_SERIES)
            val isWatch = userRepository.isInWatchlist(seriesId, ContentType.TV_SERIES)
            val outOfFive =
                when (val r = userRepository.getUserRating(seriesId, ContentType.TV_SERIES)) {
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
            offlineContentRepository
                .observeOfflineContent(seriesId, ContentType.TV_SERIES)
                .collect { offlineContent ->
                    _uiState.update { it.copy(isSavedOffline = offlineContent != null) }
                }
        }
    }

    private fun loadAuxiliary() {
        viewModelScope.launch {
            val lang = preferencesRepository.tmdbLanguage.first()
            when (val res = seriesRepository.getTvSeriesCredits(seriesId, lang)) {
                is Result.Success -> _uiState.update { it.copy(credits = res.data) }
                else -> Unit
            }
            when (val res = seriesRepository.getTvSeriesKeywords(seriesId)) {
                is Result.Success -> _uiState.update { it.copy(keywords = res.data) }
                else -> Unit
            }
            when (val res = seriesRepository.getTvSeriesExternalIds(seriesId)) {
                is Result.Success -> _uiState.update { it.copy(externalIds = res.data) }
                else -> Unit
            }
        }
    }

    private fun loadWatchProviders() {
        viewModelScope.launch {
            val region = preferencesRepository.watchRegion.first()
            _uiState.update { it.copy(watchProvidersLoading = true) }
            seriesRepository.getTvWatchProviders(seriesId).collect { result ->
                when (result) {
                    is Result.Loading -> Unit
                    is Result.Success -> {
                        val country =
                            result.data.results[region]
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
            val result =
                if (isFavorite) {
                    userRepository.removeFromFavorites(seriesId, ContentType.TV_SERIES)
                } else {
                    userRepository.addToFavorites(seriesId, ContentType.TV_SERIES)
                }
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isFavorite = !isFavorite,
                            isFavoriteMutationPending = false,
                        )
                    }
                    _sideEffect.send(
                        SeriesDetailSideEffect.FavoriteUpdated(isFavorite = !isFavorite)
                    )
                }
                is Result.Error,
                Result.Loading -> {
                    _uiState.update { it.copy(isFavoriteMutationPending = false) }
                    _sideEffect.send(SeriesDetailSideEffect.FavoriteUpdateFailed)
                }
            }
        }
    }

    private fun toggleWatchlist() {
        if (_uiState.value.isWatchlistMutationPending) return
        viewModelScope.launch {
            val isWatchlisted = _uiState.value.isWatchlisted
            _uiState.update { it.copy(isWatchlistMutationPending = true) }
            val result =
                if (isWatchlisted) {
                    userRepository.removeFromWatchlist(seriesId, ContentType.TV_SERIES)
                } else {
                    userRepository.addToWatchlist(seriesId, ContentType.TV_SERIES)
                }
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isWatchlisted = !isWatchlisted,
                            isWatchlistMutationPending = false,
                        )
                    }
                    _sideEffect.send(
                        SeriesDetailSideEffect.WatchlistUpdated(isWatchlisted = !isWatchlisted)
                    )
                }
                is Result.Error,
                Result.Loading -> {
                    _uiState.update { it.copy(isWatchlistMutationPending = false) }
                    _sideEffect.send(SeriesDetailSideEffect.WatchlistUpdateFailed)
                }
            }
        }
    }

    private fun setRating(outOfFive: Int) {
        viewModelScope.launch {
            val outOfTen = (outOfFive * 2).coerceIn(2, 10).toFloat()
            when (setUserContentRatingUseCase(seriesId, ContentType.TV_SERIES, outOfTen, null)) {
                is Result.Success -> _uiState.update { it.copy(userRatingOutOfFive = outOfFive) }
                is Result.Error ->
                    _sideEffect.send(SeriesDetailSideEffect.ShowMessage("Could not save rating"))
                else -> Unit
            }
        }
    }

    private fun clearRating() {
        viewModelScope.launch {
            clearUserContentRatingUseCase(seriesId, ContentType.TV_SERIES)
            _uiState.update { it.copy(userRatingOutOfFive = 0) }
        }
    }

    private fun saveForOffline() {
        if (_uiState.value.isOfflineSaving || _uiState.value.isSavedOffline) return
        viewModelScope.launch {
            prefetchContentForOfflineUseCase(seriesId, ContentType.TV_SERIES).collect { result ->
                when (result) {
                    Result.Loading -> _uiState.update { it.copy(isOfflineSaving = true) }
                    is Result.Success -> {
                        _uiState.update { it.copy(isOfflineSaving = false, isSavedOffline = true) }
                        _sideEffect.send(SeriesDetailSideEffect.ShowMessage("Saved for offline"))
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isOfflineSaving = false) }
                        _sideEffect.send(
                            SeriesDetailSideEffect.ShowMessage("Could not save for offline")
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
                    it.contentId == seriesId && it.contentType == ContentType.TV_SERIES
                }
            if (existing) {
                _uiState.update {
                    it.copy(isAddingToCollection = false, showAddToCollection = false)
                }
                _sideEffect.send(
                    SeriesDetailSideEffect.ShowMessage(
                        "Already in ${collection?.name ?: "collection"}"
                    )
                )
                return@launch
            }
            when (userRepository.addToCollection(collectionId, seriesId, ContentType.TV_SERIES)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isAddingToCollection = false, showAddToCollection = false)
                    }
                    _sideEffect.send(
                        SeriesDetailSideEffect.ShowMessage(
                            "Added to ${collection?.name ?: "collection"}"
                        )
                    )
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isAddingToCollection = false) }
                    _sideEffect.send(
                        SeriesDetailSideEffect.ShowMessage("Could not add to collection")
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
                        userRepository.addToCollection(
                            created.data.id,
                            seriesId,
                            ContentType.TV_SERIES,
                        )
                    ) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(isAddingToCollection = false, showAddToCollection = false)
                            }
                            _sideEffect.send(
                                SeriesDetailSideEffect.ShowMessage("Added to ${created.data.name}")
                            )
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(isAddingToCollection = false) }
                            _sideEffect.send(
                                SeriesDetailSideEffect.ShowMessage("Could not add to collection")
                            )
                        }
                        else -> _uiState.update { it.copy(isAddingToCollection = false) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isAddingToCollection = false) }
                    _sideEffect.send(
                        SeriesDetailSideEffect.ShowMessage("Could not create collection")
                    )
                }
                else -> _uiState.update { it.copy(isAddingToCollection = false) }
            }
        }
    }
}
