package com.movieapp.feature.collections.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.CollectionItem
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.UserCollection
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionMovieRow(val movie: Movie, val item: CollectionItem)

data class CollectionSeriesRow(val series: TvSeries, val item: CollectionItem)

data class CollectionDetailUiState(
    val collection: UserCollection? = null,
    val movieRows: List<CollectionMovieRow> = emptyList(),
    val seriesRows: List<CollectionSeriesRow> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val contentError: String? = null,
    val pendingMutation: CollectionDetailMutation? = null,
)

enum class CollectionDetailMutation {
    RENAME,
    DELETE,
    REMOVE_ITEM,
    UPDATE_ITEM,
}

sealed interface CollectionDetailUiEvent {
    data class RenameCollection(val name: String) : CollectionDetailUiEvent

    data object DeleteCollection : CollectionDetailUiEvent

    data class RemoveMovie(val movieId: Int) : CollectionDetailUiEvent

    data class RemoveSeries(val seriesId: Int) : CollectionDetailUiEvent

    data class ToggleItemWatched(val itemId: String, val watched: Boolean) : CollectionDetailUiEvent

    data class SetItemRating(val itemId: String, val outOfFive: Int) : CollectionDetailUiEvent

    data class SetItemNotes(val itemId: String, val notes: String?) : CollectionDetailUiEvent

    data object Retry : CollectionDetailUiEvent
}

sealed interface CollectionDetailSideEffect {
    data class MutationSaved(val mutation: CollectionDetailMutation) : CollectionDetailSideEffect

    data class MutationFailed(val mutation: CollectionDetailMutation) : CollectionDetailSideEffect
}

class CollectionDetailViewModel(
    private val collectionId: String,
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
) : ViewModel() {

    private var loadJob: Job? = null
    private var contentLoading = true

    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<CollectionDetailSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadCollectionDetail()
    }

    fun onEvent(event: CollectionDetailUiEvent) {
        when (event) {
            is CollectionDetailUiEvent.RenameCollection -> renameCollection(event.name)
            is CollectionDetailUiEvent.DeleteCollection -> deleteCollection()
            is CollectionDetailUiEvent.RemoveMovie -> removeMovie(event.movieId)
            is CollectionDetailUiEvent.RemoveSeries -> removeSeries(event.seriesId)
            is CollectionDetailUiEvent.ToggleItemWatched ->
                toggleWatched(event.itemId, event.watched)
            is CollectionDetailUiEvent.SetItemRating -> setItemRating(event.itemId, event.outOfFive)
            is CollectionDetailUiEvent.SetItemNotes -> setItemNotes(event.itemId, event.notes)
            is CollectionDetailUiEvent.Retry -> loadCollectionDetail()
        }
    }

    private fun loadCollectionDetail() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            contentLoading = true
            _uiState.update { it.copy(isLoading = true, error = null, contentError = null) }

            launch {
                userRepository.getCollectionById(collectionId).collect { result ->
                    when (result) {
                        is Result.Success ->
                            _uiState.update {
                                it.copy(collection = result.data, isLoading = contentLoading)
                            }
                        is Result.Error ->
                            _uiState.update {
                                it.copy(
                                    error = result.exception.message.orEmpty(),
                                    isLoading = false,
                                )
                            }

                        is Result.Loading -> {}
                    }
                }
            }

            combine(
                    userRepository.getCollectionItems(collectionId),
                    movieRepository.getMoviesInCollection(collectionId),
                    seriesRepository.getSeriesInCollection(collectionId),
                ) { items, moviesRes, seriesRes ->
                    Triple(items, moviesRes, seriesRes)
                }
                .collect { (items, moviesRes, seriesRes) ->
                    val listError =
                        when {
                            moviesRes is Result.Error -> moviesRes.exception.message.orEmpty()
                            seriesRes is Result.Error -> seriesRes.exception.message.orEmpty()
                            else -> null
                        }
                    val listLoading = moviesRes is Result.Loading || seriesRes is Result.Loading
                    contentLoading = listLoading
                    val movies = (moviesRes as? Result.Success)?.data.orEmpty()
                    val seriesList = (seriesRes as? Result.Success)?.data.orEmpty()
                    val movieRows =
                        items
                            .filter { it.contentType == ContentType.MOVIE }
                            .sortedByDescending { it.addedAt }
                            .mapNotNull { item ->
                                movies
                                    .find { it.id == item.contentId }
                                    ?.let { m -> CollectionMovieRow(movie = m, item = item) }
                            }
                    val seriesRows =
                        items
                            .filter { it.contentType == ContentType.TV_SERIES }
                            .sortedByDescending { it.addedAt }
                            .mapNotNull { item ->
                                seriesList
                                    .find { it.id == item.contentId }
                                    ?.let { s -> CollectionSeriesRow(series = s, item = item) }
                            }
                    _uiState.update { state ->
                        state.copy(
                            movieRows = movieRows,
                            seriesRows = seriesRows,
                            isLoading =
                                listLoading || (state.collection == null && state.error == null),
                            contentError = listError,
                        )
                    }
                }
        }
    }

    private fun findItem(itemId: String): CollectionItem? {
        val rows = _uiState.value
        return rows.movieRows.find { it.item.id == itemId }?.item
            ?: rows.seriesRows.find { it.item.id == itemId }?.item
    }

    private fun removeMovie(movieId: Int) {
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionDetailMutation.REMOVE_ITEM) }
            finishMutation(
                mutation = CollectionDetailMutation.REMOVE_ITEM,
                result =
                    userRepository.removeFromCollection(
                        collectionId,
                        movieId,
                        ContentType.MOVIE,
                    ),
            )
        }
    }

    private fun renameCollection(name: String) {
        val collection = _uiState.value.collection ?: return
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionDetailMutation.RENAME) }
            finishMutation(
                mutation = CollectionDetailMutation.RENAME,
                result = userRepository.updateCollection(collection.copy(name = trimmedName)),
            )
        }
    }

    private fun deleteCollection() {
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionDetailMutation.DELETE) }
            finishMutation(
                mutation = CollectionDetailMutation.DELETE,
                result = userRepository.deleteCollection(collectionId),
            )
        }
    }

    private fun removeSeries(seriesId: Int) {
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionDetailMutation.REMOVE_ITEM) }
            finishMutation(
                mutation = CollectionDetailMutation.REMOVE_ITEM,
                result =
                    userRepository.removeFromCollection(
                        collectionId,
                        seriesId,
                        ContentType.TV_SERIES,
                    ),
            )
        }
    }

    private fun toggleWatched(itemId: String, watched: Boolean) {
        val item = findItem(itemId) ?: return
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionDetailMutation.UPDATE_ITEM) }
            finishMutation(
                mutation = CollectionDetailMutation.UPDATE_ITEM,
                result =
                    userRepository.updateCollectionItem(
                        itemId = itemId,
                        userNotes = item.userNotes,
                        userRating = item.userRating,
                        isWatched = watched,
                    ),
            )
        }
    }

    private fun setItemRating(itemId: String, outOfFive: Int) {
        val item = findItem(itemId) ?: return
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionDetailMutation.UPDATE_ITEM) }
            val tenScale = if (outOfFive <= 0) null else (outOfFive * 2).coerceIn(2, 10)
            finishMutation(
                mutation = CollectionDetailMutation.UPDATE_ITEM,
                result =
                    userRepository.updateCollectionItem(
                        itemId = itemId,
                        userNotes = item.userNotes,
                        userRating = tenScale,
                        isWatched = item.isWatched,
                    ),
            )
        }
    }

    private fun setItemNotes(itemId: String, notes: String?) {
        val item = findItem(itemId) ?: return
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionDetailMutation.UPDATE_ITEM) }
            finishMutation(
                mutation = CollectionDetailMutation.UPDATE_ITEM,
                result =
                    userRepository.updateCollectionItem(
                        itemId = itemId,
                        userNotes = notes?.takeIf { it.isNotBlank() },
                        userRating = item.userRating,
                        isWatched = item.isWatched,
                    ),
            )
        }
    }

    private suspend fun finishMutation(mutation: CollectionDetailMutation, result: Result<*>) {
        _uiState.update { it.copy(pendingMutation = null) }
        when (result) {
            is Result.Success ->
                _sideEffect.send(CollectionDetailSideEffect.MutationSaved(mutation))
            is Result.Error,
            Result.Loading -> _sideEffect.send(CollectionDetailSideEffect.MutationFailed(mutation))
        }
    }
}
