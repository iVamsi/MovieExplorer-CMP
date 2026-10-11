package com.movieapp.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.Image
import com.movieapp.core.model.ImageResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImageGalleryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val images: List<Image> = emptyList(),
)

sealed interface ImageGalleryUiEvent {
    data object Retry : ImageGalleryUiEvent
}

class ImageGalleryViewModel(
    private val kind: String,
    private val contentId: Int,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val actorRepository: ActorRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageGalleryUiState())
    val uiState: StateFlow<ImageGalleryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.tmdbLanguage.collectLatest { language -> load(language) }
        }
    }

    fun onEvent(event: ImageGalleryUiEvent) {
        when (event) {
            ImageGalleryUiEvent.Retry -> {
                viewModelScope.launch { load(preferencesRepository.tmdbLanguage.first()) }
            }
        }
    }

    private suspend fun load(language: String) {
        if (contentId == 0) {
            _uiState.update {
                it.copy(isLoading = false, error = "Invalid content", images = emptyList())
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        when (kind) {
            "person" -> {
                when (val result = actorRepository.getActorImages(contentId)) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, images = result.data, error = null)
                        }
                    }

                    is Result.Error -> {
                        Logger.withTag("ImageGalleryViewModel").e(result.exception) {
                            "Actor image gallery load failed"
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Could not load images",
                                images = emptyList(),
                            )
                        }
                    }

                    Result.Loading -> Unit
                }
            }

            "movie",
            "tv" -> {
                val result: Result<ImageResponse> =
                    when (kind) {
                        "movie" -> movieRepository.getMovieImages(contentId, language)
                        "tv" -> seriesRepository.getTvSeriesImages(contentId, language)
                        else -> Result.Error(IllegalStateException("Unknown media type"))
                    }
                when (result) {
                    is Result.Success -> {
                        val merged = buildList {
                            addAll(result.data.backdrops)
                            addAll(result.data.posters)
                        }
                            .distinctBy { it.filePath }
                            .sortedByDescending { it.voteAverage }
                        _uiState.update {
                            it.copy(isLoading = false, images = merged, error = null)
                        }
                    }

                    is Result.Error -> {
                        Logger.withTag("ImageGalleryViewModel").e(result.exception) {
                            "Image gallery load failed"
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Could not load images",
                                images = emptyList(),
                            )
                        }
                    }

                    Result.Loading -> Unit
                }
            }

            else -> {
                _uiState.update {
                    it.copy(isLoading = false, error = "Unknown media type", images = emptyList())
                }
            }
        }
    }
}

