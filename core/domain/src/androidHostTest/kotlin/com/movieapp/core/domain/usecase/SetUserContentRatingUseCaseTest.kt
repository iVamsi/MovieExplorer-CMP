package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetUserContentRatingUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = SetUserContentRatingUseCase(userRepository)

    @Test
    fun `invoke - forwards contentId contentType rating and notes to rateContent`() = runTest {
        coEvery {
            userRepository.rateContent(
                contentId = 5,
                contentType = ContentType.MOVIE,
                rating = 8.0f,
                notes = "great",
            )
        } returns Result.Success(Unit)

        useCase(
            contentId = 5,
            contentType = ContentType.MOVIE,
            ratingOutOfTen = 8.0f,
            notes = "great",
        )

        coVerify(exactly = 1) {
            userRepository.rateContent(
                contentId = 5,
                contentType = ContentType.MOVIE,
                rating = 8.0f,
                notes = "great",
            )
        }
    }

    @Test
    fun `invoke - defaults notes to null when omitted`() = runTest {
        coEvery {
            userRepository.rateContent(
                contentId = 3,
                contentType = ContentType.TV_SERIES,
                rating = 6.5f,
                notes = null,
            )
        } returns Result.Success(Unit)

        useCase(contentId = 3, contentType = ContentType.TV_SERIES, ratingOutOfTen = 6.5f)

        coVerify(exactly = 1) {
            userRepository.rateContent(
                contentId = 3,
                contentType = ContentType.TV_SERIES,
                rating = 6.5f,
                notes = null,
            )
        }
    }
}
