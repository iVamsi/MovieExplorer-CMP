package com.movieapp.feature.profile.screens.profile

import com.movieapp.feature.profile.resources.Res
import com.movieapp.feature.profile.resources.profile_error_login_url
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.domain.repository.AuthState
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.GetUserWatchStatsUseCase
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.UserCollection
import com.movieapp.core.model.WatchStats
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import co.touchlab.kermit.Logger

data class ProfileUiState(
    val watchlistMovies: List<Movie> = emptyList(),
    val watchlistTvSeries: List<TvSeries> = emptyList(),
    val userCollections: List<UserCollection> = emptyList(),
    val watchStats: WatchStats = WatchStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface ProfileSideEffect {
    data class ShowMessage(val message: String) : ProfileSideEffect

    data class OpenUrl(val url: String) : ProfileSideEffect
}

sealed interface ProfileUiEvent {
    data object PullRefresh : ProfileUiEvent
}

class ProfileViewModel(
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val userRepository: UserRepository,
    private val getUserWatchStatsUseCase: GetUserWatchStatsUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _sideEffects = Channel<ProfileSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    val authState: StateFlow<AuthState> =
        authRepository.authState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AuthState.SignedOut,
        )

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isPullRefreshing = MutableStateFlow(false)
    val isPullRefreshing: StateFlow<Boolean> = _isPullRefreshing.asStateFlow()

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.PullRefresh -> onPullRefresh()
        }
    }

    init {
        viewModelScope.launch {
            combine(
                    userRepository.getAllCollections(),
                    movieRepository.getWatchlistMovies(),
                    seriesRepository.getWatchlistSeries(),
                    getUserWatchStatsUseCase(),
                ) { collections, moviesResult, seriesResult, statsResult ->
                    var loading = false
                    var error: String? = null

                    val movies =
                        when (moviesResult) {
                            is Result.Success -> moviesResult.data
                            is Result.Error -> {
                                error = moviesResult.exception.message
                                emptyList()
                            }
                            Result.Loading -> {
                                loading = true
                                emptyList()
                            }
                        }

                    val series =
                        when (seriesResult) {
                            is Result.Success -> seriesResult.data
                            is Result.Error -> {
                                error = error ?: seriesResult.exception.message
                                emptyList()
                            }
                            Result.Loading -> {
                                loading = true
                                emptyList()
                            }
                        }

                    val stats =
                        when (statsResult) {
                            is Result.Success -> statsResult.data
                            is Result.Error -> {
                                error = error ?: statsResult.exception.message
                                WatchStats()
                            }
                            Result.Loading -> {
                                loading = true
                                WatchStats()
                            }
                        }

                    ProfileUiState(
                        userCollections = collections,
                        watchlistMovies = movies,
                        watchlistTvSeries = series,
                        watchStats = stats,
                        isLoading = loading,
                        error = error,
                    )
                }
                .collect { state -> _uiState.value = state }
        }
    }

    private fun onPullRefresh() {
        viewModelScope.launch {
            _isPullRefreshing.value = true
            try {
                movieRepository.refreshMovies()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e(e) { "Profile pull refresh" }
            } finally {
                _isPullRefreshing.value = false
            }
        }
    }

    fun startTmdbLogin() {
        viewModelScope.launch {
            when (val r = authRepository.createLoginApprovalUrl()) {
                is Result.Success -> _sideEffects.send(ProfileSideEffect.OpenUrl(r.data))
                is Result.Error ->
                    _sideEffects.send(
                        ProfileSideEffect.ShowMessage(
                            r.exception.message ?: getString(Res.string.profile_error_login_url)
                        )
                    )
                Result.Loading -> Unit
            }
        }
    }
}
