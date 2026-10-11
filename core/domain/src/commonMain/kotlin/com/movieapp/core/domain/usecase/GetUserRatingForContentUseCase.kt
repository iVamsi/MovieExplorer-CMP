package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType

class GetUserRatingForContentUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(contentId: Int, contentType: ContentType): Result<Float?> =
        userRepository.getUserRating(contentId, contentType)
}
