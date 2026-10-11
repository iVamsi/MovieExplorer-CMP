package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType

class ToggleFavoriteActorUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(actorId: Int, isCurrentlyFavorite: Boolean): Result<Unit> =
        if (isCurrentlyFavorite) {
            userRepository.removeFromFavorites(actorId, ContentType.ACTOR)
        } else {
            userRepository.addToFavorites(actorId, ContentType.ACTOR)
        }
}
