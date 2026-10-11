package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType

class ToggleFavoriteMovieUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(movieId: Int, isCurrentlyFavorite: Boolean): Result<Unit> =
        if (isCurrentlyFavorite) {
            userRepository.removeFromFavorites(movieId, ContentType.MOVIE)
        } else {
            userRepository.addToFavorites(movieId, ContentType.MOVIE)
        }
}
