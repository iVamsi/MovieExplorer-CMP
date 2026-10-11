package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteMovieUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = ToggleFavoriteMovieUseCase(userRepository)

    @Test
    fun `invoke - when not currently favorite - calls addToFavorites`() = runTest {
        coEvery { userRepository.addToFavorites(1, ContentType.MOVIE) } returns Result.Success(Unit)

        useCase(movieId = 1, isCurrentlyFavorite = false)

        coVerify { userRepository.addToFavorites(1, ContentType.MOVIE) }
        coVerify(exactly = 0) { userRepository.removeFromFavorites(any(), any()) }
    }

    @Test
    fun `invoke - when currently favorite - calls removeFromFavorites`() = runTest {
        coEvery { userRepository.removeFromFavorites(1, ContentType.MOVIE) } returns
            Result.Success(Unit)

        useCase(movieId = 1, isCurrentlyFavorite = true)

        coVerify { userRepository.removeFromFavorites(1, ContentType.MOVIE) }
        coVerify(exactly = 0) { userRepository.addToFavorites(any(), any()) }
    }

    @Test
    fun `invoke - always uses ContentType MOVIE`() = runTest {
        coEvery { userRepository.addToFavorites(42, ContentType.MOVIE) } returns
            Result.Success(Unit)

        useCase(movieId = 42, isCurrentlyFavorite = false)

        coVerify { userRepository.addToFavorites(42, ContentType.MOVIE) }
    }
}
