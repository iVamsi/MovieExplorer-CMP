package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.TvSeries
import kotlinx.coroutines.flow.Flow

class GetFavoriteTvSeriesUseCase(private val seriesRepository: SeriesRepository) {
    operator fun invoke(): Flow<Result<List<TvSeries>>> = seriesRepository.getFavoriteSeries()
}
