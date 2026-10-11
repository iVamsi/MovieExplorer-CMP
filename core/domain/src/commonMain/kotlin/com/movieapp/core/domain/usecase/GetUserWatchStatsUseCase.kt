package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.StatsRepository
import com.movieapp.core.model.WatchStats
import kotlinx.coroutines.flow.Flow

class GetUserWatchStatsUseCase(private val statsRepository: StatsRepository) {
    operator fun invoke(): Flow<Result<WatchStats>> = statsRepository.getUserWatchStats()
}
