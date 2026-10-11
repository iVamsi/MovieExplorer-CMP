package com.movieapp.core.domain.repository

import com.movieapp.core.common.Result
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.OfflineContent
import kotlinx.coroutines.flow.Flow

interface OfflineContentRepository {
    fun prefetchContent(contentId: Int, contentType: ContentType): Flow<Result<OfflineContent>>

    fun getOfflineContent(): Flow<List<OfflineContent>>

    fun observeOfflineContent(contentId: Int, contentType: ContentType): Flow<OfflineContent?>

    suspend fun deleteOfflineContent(contentId: Int, contentType: ContentType): Result<Unit>

    suspend fun clearOfflineContent(): Result<Unit>
}
