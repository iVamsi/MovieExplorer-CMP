package com.movieapp.core.domain.usecase

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.model.Movie
import kotlinx.coroutines.flow.Flow

class GetFavoriteMoviesUseCase(private val movieRepository: MovieRepository) {
    operator fun invoke(): Flow<Result<List<Movie>>> = movieRepository.getFavoriteMovies()
}
