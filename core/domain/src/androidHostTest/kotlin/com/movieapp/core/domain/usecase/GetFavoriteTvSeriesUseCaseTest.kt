package com.movieapp.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.TvSeries
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetFavoriteTvSeriesUseCaseTest {

    private val seriesRepository = mockk<SeriesRepository>()
    private val useCase = GetFavoriteTvSeriesUseCase(seriesRepository)

    @Test
    fun `invoke - emits success list from repository`() = runTest {
        val series = listOf(TvSeries(id = 1, name = "A"), TvSeries(id = 2, name = "B"))
        every { seriesRepository.getFavoriteSeries() } returns flowOf(Result.Success(series))

        useCase().test {
            assertThat(awaitItem()).isEqualTo(Result.Success(series))
            awaitComplete()
        }
        verify(exactly = 1) { seriesRepository.getFavoriteSeries() }
    }

    @Test
    fun `invoke - emits error when repository fails`() = runTest {
        val error = IllegalStateException("boom")
        every { seriesRepository.getFavoriteSeries() } returns flowOf(Result.Error(error))

        useCase().test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Error::class.java)
            assertThat((item as Result.Error).exception).isSameInstanceAs(error)
            awaitComplete()
        }
    }
}
