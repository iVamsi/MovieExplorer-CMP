package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType

class ToggleWatchlistMovieUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(movieId: Int, isCurrentlyWatchlisted: Boolean): Result<Unit> =
        if (isCurrentlyWatchlisted) {
            userRepository.removeFromWatchlist(movieId, ContentType.MOVIE)
        } else {
            userRepository.addToWatchlist(movieId, ContentType.MOVIE)
        }
}
