package com.movieapp.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.OfflineContent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PrefetchContentForOfflineUseCaseTest {

    private val offlineContentRepository = mockk<OfflineContentRepository>()
    private val useCase = PrefetchContentForOfflineUseCase(offlineContentRepository)

    @Test
    fun `invoke - delegates prefetch to repository`() = runTest {
        val offlineContent =
            OfflineContent(
                contentId = 1,
                contentType = ContentType.MOVIE,
                title = "The Matrix",
                posterPath = "/poster.jpg",
                prefetchedAt = 100L,
                posterCached = true,
                backdropCached = true,
            )
        every { offlineContentRepository.prefetchContent(1, ContentType.MOVIE) } returns
            flowOf(Result.Loading, Result.Success(offlineContent))

        useCase(contentId = 1, contentType = ContentType.MOVIE).test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            assertThat(awaitItem()).isEqualTo(Result.Success(offlineContent))
            awaitComplete()
        }
        verify(exactly = 1) { offlineContentRepository.prefetchContent(1, ContentType.MOVIE) }
    }
}
