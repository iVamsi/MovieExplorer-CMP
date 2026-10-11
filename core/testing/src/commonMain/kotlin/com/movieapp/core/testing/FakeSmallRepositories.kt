package com.movieapp.core.testing

import com.movieapp.core.common.Result
import com.movieapp.core.common.network.NetworkConnectivity
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.StatsRepository
import com.movieapp.core.domain.repository.WatchProviderRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.OfflineContent
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.model.WatchStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeOfflineContentRepository : OfflineContentRepository {
    val offlineContent = MutableStateFlow<List<OfflineContent>>(emptyList())
    var prefetchResult: Result<OfflineContent>? = null
    var unitResult: Result<Unit> = Result.Success(Unit)
    val prefetchCalls = mutableListOf<Pair<Int, ContentType>>()
    val deleteCalls = mutableListOf<Pair<Int, ContentType>>()
    var clearCalls = 0

    override fun prefetchContent(contentId: Int, contentType: ContentType): Flow<Result<OfflineContent>> {
        prefetchCalls += contentId to contentType
        val result = prefetchResult ?: Result.Success(OfflineContent(contentId = contentId, contentType = contentType))
        return flowOf(result)
    }

    override fun getOfflineContent(): Flow<List<OfflineContent>> = offlineContent

    override fun observeOfflineContent(contentId: Int, contentType: ContentType): Flow<OfflineContent?> =
        offlineContent.map { list -> list.firstOrNull { it.contentId == contentId && it.contentType == contentType } }

    override suspend fun deleteOfflineContent(contentId: Int, contentType: ContentType): Result<Unit> {
        deleteCalls += contentId to contentType
        return unitResult
    }

    override suspend fun clearOfflineContent(): Result<Unit> {
        clearCalls++
        offlineContent.value = emptyList()
        return unitResult
    }
}

class FakeStatsRepository : StatsRepository {
    var statsResult: Result<WatchStats> = Result.Success(WatchStats())

    override fun getUserWatchStats(): Flow<Result<WatchStats>> = flowOf(statsResult)
}

class FakeWatchProviderRepository : WatchProviderRepository {
    var catalogResult: Result<List<WatchProvider>> = Result.Success(emptyList())

    override fun getWatchProviderCatalog(): Flow<Result<List<WatchProvider>>> = flowOf(catalogResult)
}

class FakeNetworkConnectivity(online: Boolean = true) : NetworkConnectivity {
    override val isOnline = MutableStateFlow(online)
}
