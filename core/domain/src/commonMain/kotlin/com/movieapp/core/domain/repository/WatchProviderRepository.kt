package com.movieapp.core.domain.repository

import com.movieapp.core.common.Result
import com.movieapp.core.model.WatchProvider
import kotlinx.coroutines.flow.Flow

fun interface WatchProviderRepository {
    fun getWatchProviderCatalog(): Flow<Result<List<WatchProvider>>>
}
