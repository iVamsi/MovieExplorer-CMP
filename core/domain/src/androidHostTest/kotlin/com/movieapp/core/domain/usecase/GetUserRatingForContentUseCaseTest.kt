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

class GetUserRatingForContentUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = GetUserRatingForContentUseCase(userRepository)

    @Test
    fun `invoke - returns rating from repository`() = runTest {
        coEvery { userRepository.getUserRating(1, ContentType.MOVIE) } returns Result.Success(7.5f)

        val result = useCase(contentId = 1, contentType = ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(7.5f)
        coVerify(exactly = 1) { userRepository.getUserRating(1, ContentType.MOVIE) }
    }

    @Test
    fun `invoke - returns null when repository has no rating`() = runTest {
        coEvery { userRepository.getUserRating(9, ContentType.TV_SERIES) } returns
            Result.Success(null)

        val result = useCase(contentId = 9, contentType = ContentType.TV_SERIES)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isNull()
    }
}
