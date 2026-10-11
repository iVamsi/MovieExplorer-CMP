package com.movieapp.feature.actors.screens.actor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.ToggleFavoriteActorUseCase
import com.movieapp.core.model.Actor
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActorDetailUiState(
    val actor: Actor? = null,
    val movieCredits: List<Movie> = emptyList(),
    val tvCredits: List<TvSeries> = emptyList(),
    val isFavorite: Boolean = false,
    val isFavoriteMutationPending: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface ActorDetailUiEvent {
    data object Retry : ActorDetailUiEvent

    data object ToggleFavorite : ActorDetailUiEvent
}

sealed interface ActorDetailSideEffect {
    data class FavoriteUpdated(val isFavorite: Boolean) : ActorDetailSideEffect

    data object FavoriteUpdateFailed : ActorDetailSideEffect
}

class ActorDetailViewModel
constructor(
    private val actorRepository: ActorRepository,
    private val userRepository: UserRepository,
    private val toggleFavoriteActorUseCase: ToggleFavoriteActorUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val actorId: Int = checkNotNull(savedStateHandle["actorId"])

    private val _uiState = MutableStateFlow(ActorDetailUiState())
    val uiState: StateFlow<ActorDetailUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ActorDetailSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadActorDetails()
    }

    fun onEvent(event: ActorDetailUiEvent) {
        when (event) {
            is ActorDetailUiEvent.Retry -> loadActorDetails()
            is ActorDetailUiEvent.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun toggleFavorite() {
        if (_uiState.value.isFavoriteMutationPending) return
        viewModelScope.launch {
            val current = _uiState.value.isFavorite
            _uiState.update { it.copy(isFavoriteMutationPending = true) }
            when (toggleFavoriteActorUseCase(actorId, current)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isFavorite = !current,
                            isFavoriteMutationPending = false,
                        )
                    }
                    _sideEffect.send(ActorDetailSideEffect.FavoriteUpdated(isFavorite = !current))
                }
                is Result.Error,
                Result.Loading -> {
                    _uiState.update { it.copy(isFavoriteMutationPending = false) }
                    _sideEffect.send(ActorDetailSideEffect.FavoriteUpdateFailed)
                }
            }
        }
    }

    private fun refreshFavoriteStatus() {
        viewModelScope.launch {
            val fav = userRepository.isFavorite(actorId, ContentType.ACTOR)
            _uiState.update { it.copy(isFavorite = fav) }
        }
    }

    private fun loadActorDetails() {
        viewModelScope.launch {
            actorRepository.getActorByIdFlow(actorId).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(actor = result.data, isLoading = false, error = null)
                        }
                        refreshFavoriteStatus()
                    }

                    is Result.Error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Failed to load actor",
                            )
                        }
                }
            }
        }

        viewModelScope.launch {
            actorRepository.getActorMovieCredits(actorId).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(movieCredits = result.data) }
                }
            }
        }

        viewModelScope.launch {
            actorRepository.getActorTvCredits(actorId).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(tvCredits = result.data) }
                }
            }
        }
    }
}
