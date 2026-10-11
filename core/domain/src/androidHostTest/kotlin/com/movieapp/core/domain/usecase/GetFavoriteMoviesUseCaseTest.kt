package com.movieapp.core.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.model.Movie
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetFavoriteMoviesUseCaseTest {

    private val movieRepository = mockk<MovieRepository>()
    private val useCase = GetFavoriteMoviesUseCase(movieRepository)

    @Test
    fun `invoke - emits success list from repository`() = runTest {
        val movies = listOf(Movie(id = 1, title = "A"), Movie(id = 2, title = "B"))
        every { movieRepository.getFavoriteMovies() } returns flowOf(Result.Success(movies))

        useCase().test {
            assertThat(awaitItem()).isEqualTo(Result.Success(movies))
            awaitComplete()
        }
        verify(exactly = 1) { movieRepository.getFavoriteMovies() }
    }

    @Test
    fun `invoke - emits error when repository fails`() = runTest {
        val error = IllegalStateException("boom")
        every { movieRepository.getFavoriteMovies() } returns flowOf(Result.Error(error))

        useCase().test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Error::class.java)
            assertThat((item as Result.Error).exception).isSameInstanceAs(error)
            awaitComplete()
        }
    }
}
