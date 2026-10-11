package com.movieapp.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.StatsRepository
import com.movieapp.core.model.WatchStats
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetUserWatchStatsUseCaseTest {

    private val statsRepository = mockk<StatsRepository>()
    private val useCase = GetUserWatchStatsUseCase(statsRepository)

    @Test
    fun `invoke - emits stats from repository`() = runTest {
        val stats = WatchStats(totalWatchTimeMinutes = 152)
        every { statsRepository.getUserWatchStats() } returns flowOf(Result.Success(stats))

        useCase().test {
            assertThat(awaitItem()).isEqualTo(Result.Success(stats))
            awaitComplete()
        }
        verify(exactly = 1) { statsRepository.getUserWatchStats() }
    }

    @Test
    fun `invoke - emits error when repository fails`() = runTest {
        val error = IllegalStateException("stats unavailable")
        every { statsRepository.getUserWatchStats() } returns flowOf(Result.Error(error))

        useCase().test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Error::class.java)
            assertThat((item as Result.Error).exception).isSameInstanceAs(error)
            awaitComplete()
        }
    }
}
