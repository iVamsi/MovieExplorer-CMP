package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteActorUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = ToggleFavoriteActorUseCase(userRepository)

    @Test
    fun `invoke - when not currently favorite - calls addToFavorites with ACTOR`() = runTest {
        coEvery { userRepository.addToFavorites(11, ContentType.ACTOR) } returns
            Result.Success(Unit)

        useCase(actorId = 11, isCurrentlyFavorite = false)

        coVerify(exactly = 1) { userRepository.addToFavorites(11, ContentType.ACTOR) }
        coVerify(exactly = 0) { userRepository.removeFromFavorites(any(), any()) }
    }

    @Test
    fun `invoke - when currently favorite - calls removeFromFavorites with ACTOR`() = runTest {
        coEvery { userRepository.removeFromFavorites(11, ContentType.ACTOR) } returns
            Result.Success(Unit)

        useCase(actorId = 11, isCurrentlyFavorite = true)

        coVerify(exactly = 1) { userRepository.removeFromFavorites(11, ContentType.ACTOR) }
        coVerify(exactly = 0) { userRepository.addToFavorites(any(), any()) }
    }
}
