package com.movieapp.feature.collections.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.UserCollection as RepoUserCollection
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionsUiState(
    val collections: List<UserCollection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val partialError: String? = null,
    val pendingMutation: CollectionsMutation? = null,
)

private data class CollectionLoadResult(
    val collection: UserCollection,
    val error: String?,
    val isLoading: Boolean,
)

enum class CollectionsMutation {
    CREATE,
    EDIT,
    DELETE,
}

sealed interface CollectionsUiEvent {
    data object Retry : CollectionsUiEvent

    data class CreateCollection(val name: String, val description: String, val isPublic: Boolean) :
        CollectionsUiEvent

    data class DeleteCollection(val collectionId: String) : CollectionsUiEvent

    data class EditCollection(
        val collectionId: String,
        val name: String,
        val description: String,
        val isPublic: Boolean,
    ) : CollectionsUiEvent

    data class CollectionClicked(val collectionId: String) : CollectionsUiEvent

    data class MovieClicked(val movieId: Int) : CollectionsUiEvent

    data class SeriesClicked(val seriesId: Int) : CollectionsUiEvent
}

sealed interface CollectionsSideEffect {
    data class NavigateToCollectionDetail(val collectionId: String) : CollectionsSideEffect

    data class NavigateToMovieDetail(val movieId: Int) : CollectionsSideEffect

    data class NavigateToSeriesDetail(val seriesId: Int) : CollectionsSideEffect

    data class MutationSaved(val mutation: CollectionsMutation) : CollectionsSideEffect

    data class MutationFailed(val mutation: CollectionsMutation) : CollectionsSideEffect
}

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionsViewModel(
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<CollectionsSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var domainCollections: List<RepoUserCollection> = emptyList()
    private var loadJob: Job? = null

    fun onEvent(event: CollectionsUiEvent) {
        when (event) {
            CollectionsUiEvent.Retry -> loadCollections()
            is CollectionsUiEvent.CreateCollection ->
                createCollection(event.name, event.description, event.isPublic)
            is CollectionsUiEvent.DeleteCollection -> deleteCollection(event.collectionId)
            is CollectionsUiEvent.EditCollection ->
                editCollection(event.collectionId, event.name, event.description, event.isPublic)
            is CollectionsUiEvent.CollectionClicked ->
                viewModelScope.launch {
                    _sideEffect.send(
                        CollectionsSideEffect.NavigateToCollectionDetail(event.collectionId)
                    )
                }
            is CollectionsUiEvent.MovieClicked ->
                viewModelScope.launch {
                    _sideEffect.send(CollectionsSideEffect.NavigateToMovieDetail(event.movieId))
                }
            is CollectionsUiEvent.SeriesClicked ->
                viewModelScope.launch {
                    _sideEffect.send(CollectionsSideEffect.NavigateToSeriesDetail(event.seriesId))
                }
        }
    }

    init {
        loadCollections()
    }

    private fun loadCollections() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null, partialError = null) }
        loadJob = viewModelScope.launch {
            try {
                userRepository
                    .getAllCollections()
                    .flatMapLatest { collections ->
                        domainCollections = collections
                        if (collections.isEmpty()) {
                            flowOf(emptyList())
                        } else {
                            combine(
                                collections.map { collection ->
                                    combine(
                                        movieRepository.getMoviesInCollection(collection.id),
                                        seriesRepository.getSeriesInCollection(collection.id),
                                    ) { movies, series ->
                                        CollectionLoadResult(
                                            collection =
                                                collection.toScreenModel(
                                                    movies =
                                                        (movies as? Result.Success)?.data.orEmpty(),
                                                    series =
                                                        (series as? Result.Success)?.data.orEmpty(),
                                                ),
                                            error =
                                                when {
                                                    movies is Result.Error ->
                                                        movies.exception.message.orEmpty()
                                                    series is Result.Error ->
                                                        series.exception.message.orEmpty()
                                                    else -> null
                                                },
                                            isLoading =
                                                movies is Result.Loading ||
                                                    series is Result.Loading,
                                        )
                                    }
                                }
                            ) {
                                it.toList()
                            }
                        }
                    }
                    .collect { results ->
                        _uiState.update {
                            it.copy(
                                collections = results.map(CollectionLoadResult::collection),
                                isLoading = results.any(CollectionLoadResult::isLoading),
                                error = null,
                                partialError =
                                    results.firstNotNullOfOrNull(CollectionLoadResult::error),
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message.orEmpty()) }
            }
        }
    }

    private fun createCollection(name: String, description: String, isPublic: Boolean) {
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionsMutation.CREATE) }
            finishMutation(
                mutation = CollectionsMutation.CREATE,
                result =
                    userRepository.createCollection(
                        name = name,
                        description = description.ifBlank { null },
                        isPublic = isPublic,
                    ),
            )
        }
    }

    private fun deleteCollection(collectionId: String) {
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingMutation = CollectionsMutation.DELETE) }
            finishMutation(
                mutation = CollectionsMutation.DELETE,
                result = userRepository.deleteCollection(collectionId),
            )
        }
    }

    private fun editCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
    ) {
        if (_uiState.value.pendingMutation != null) return
        viewModelScope.launch {
            val existing = domainCollections.find { it.id == collectionId } ?: return@launch
            _uiState.update { it.copy(pendingMutation = CollectionsMutation.EDIT) }
            val updated =
                existing.copy(
                    name = name,
                    description = description.ifBlank { null },
                    isPublic = isPublic,
                    updatedAt = clock.now().toEpochMilliseconds(),
                )
            finishMutation(
                mutation = CollectionsMutation.EDIT,
                result = userRepository.updateCollection(updated),
            )
        }
    }

    private suspend fun finishMutation(mutation: CollectionsMutation, result: Result<*>) {
        _uiState.update { it.copy(pendingMutation = null) }
        when (result) {
            is Result.Success -> _sideEffect.send(CollectionsSideEffect.MutationSaved(mutation))
            is Result.Error,
            Result.Loading -> _sideEffect.send(CollectionsSideEffect.MutationFailed(mutation))
        }
    }
}

private fun RepoUserCollection.toScreenModel(
    movies: List<Movie>,
    series: List<TvSeries>,
) =
    UserCollection(
        id = id,
        name = name,
        description = description ?: "",
        itemCount = itemCount,
        isPublic = isPublic,
        items = movies.map(CollectionItem::MovieItem) + series.map(CollectionItem::SeriesItem),
    )
