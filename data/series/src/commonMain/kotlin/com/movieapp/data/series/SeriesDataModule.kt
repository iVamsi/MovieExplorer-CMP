package com.movieapp.data.series

import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.data.series.mapper.TvSeriesMapper
import com.movieapp.data.series.repository.SeriesRepositoryImpl
import org.koin.dsl.module

val seriesDataModule = module {
    factory { TvSeriesMapper(clock = get()) }
    single<SeriesRepository> {
        SeriesRepositoryImpl(
            apiService = get(),
            tvSeriesDao = get(),
            transactionRunner = get(),
            remoteKeysDao = get(),
            tvSeriesMapper = get(),
            clock = get(),
            preferencesRepository = get(),
            dispatchers = get(),
        )
    }
}
