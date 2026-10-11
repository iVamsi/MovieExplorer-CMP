package com.movieapp.core.domain.repository

import com.movieapp.core.common.Result
import com.movieapp.core.model.WatchStats
import kotlinx.coroutines.flow.Flow

fun interface StatsRepository {
    fun getUserWatchStats(): Flow<Result<WatchStats>>
}
