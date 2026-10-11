package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType

class SetUserContentRatingUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        contentId: Int,
        contentType: ContentType,
        ratingOutOfTen: Float,
        notes: String? = null,
    ): Result<Unit> =
        userRepository.rateContent(
            contentId = contentId,
            contentType = contentType,
            rating = ratingOutOfTen,
            notes = notes,
        )
}
