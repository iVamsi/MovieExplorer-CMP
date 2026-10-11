package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.OfflineContent
import kotlinx.coroutines.flow.Flow

class PrefetchContentForOfflineUseCase(private val offlineContentRepository: OfflineContentRepository) {
    operator fun invoke(
        contentId: Int,
        contentType: ContentType,
    ): Flow<Result<OfflineContent>> =
        offlineContentRepository.prefetchContent(contentId = contentId, contentType = contentType)
}
