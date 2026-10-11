package com.movieapp.core.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClearUserContentRatingUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = ClearUserContentRatingUseCase(userRepository)

    @Test
    fun `invoke - delegates to repository removeRating for movie`() = runTest {
        coEvery { userRepository.removeRating(42, ContentType.MOVIE) } returns Result.Success(Unit)

        val result = useCase(contentId = 42, contentType = ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify(exactly = 1) { userRepository.removeRating(42, ContentType.MOVIE) }
    }

    @Test
    fun `invoke - propagates error from repository`() = runTest {
        val error = IllegalStateException("db failure")
        coEvery { userRepository.removeRating(7, ContentType.TV_SERIES) } returns
            Result.Error(error)

        val result = useCase(contentId = 7, contentType = ContentType.TV_SERIES)

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isSameInstanceAs(error)
    }
}
