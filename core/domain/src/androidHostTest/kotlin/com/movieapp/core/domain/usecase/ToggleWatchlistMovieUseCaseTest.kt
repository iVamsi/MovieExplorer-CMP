package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleWatchlistMovieUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = ToggleWatchlistMovieUseCase(userRepository)

    @Test
    fun `invoke - when not currently watchlisted - calls addToWatchlist`() = runTest {
        coEvery { userRepository.addToWatchlist(1, ContentType.MOVIE) } returns Result.Success(Unit)

        useCase(movieId = 1, isCurrentlyWatchlisted = false)

        coVerify { userRepository.addToWatchlist(1, ContentType.MOVIE) }
        coVerify(exactly = 0) { userRepository.removeFromWatchlist(any(), any()) }
    }

    @Test
    fun `invoke - when currently watchlisted - calls removeFromWatchlist`() = runTest {
        coEvery { userRepository.removeFromWatchlist(1, ContentType.MOVIE) } returns
            Result.Success(Unit)

        useCase(movieId = 1, isCurrentlyWatchlisted = true)

        coVerify { userRepository.removeFromWatchlist(1, ContentType.MOVIE) }
        coVerify(exactly = 0) { userRepository.addToWatchlist(any(), any()) }
    }

    @Test
    fun `invoke - always uses ContentType MOVIE`() = runTest {
        coEvery { userRepository.addToWatchlist(99, ContentType.MOVIE) } returns
            Result.Success(Unit)

        useCase(movieId = 99, isCurrentlyWatchlisted = false)

        coVerify { userRepository.addToWatchlist(99, ContentType.MOVIE) }
    }
}
