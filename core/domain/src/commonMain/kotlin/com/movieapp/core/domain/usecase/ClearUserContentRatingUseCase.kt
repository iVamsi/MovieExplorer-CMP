package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType

class ClearUserContentRatingUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(contentId: Int, contentType: ContentType): Result<Unit> =
        userRepository.removeRating(contentId, contentType)
}
