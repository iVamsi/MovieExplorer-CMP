package com.movieapp.core.testing

import androidx.paging.PagingData
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Image
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeActorRepository : ActorRepository {
    var actorsPaged: Flow<PagingData<Actor>> = flowOf(PagingData.from(FakeData.fakeActors))
    var actorResult: Result<Actor> = Result.Success(FakeData.fakeActors.first())
    var actorsResult: Result<List<Actor>> = Result.Success(FakeData.fakeActors)
    var movieCreditsResult: Result<List<Movie>> = Result.Success(FakeData.fakeMovies)
    var tvCreditsResult: Result<List<TvSeries>> = Result.Success(FakeData.fakeTvSeries)
    var imagesResult: Result<List<Image>> = Result.Success(emptyList())
    val refreshCalls = mutableListOf<Unit>()

    override fun getPopularActors(): Flow<PagingData<Actor>> = actorsPaged
    override suspend fun getActorById(actorId: Int): Result<Actor> = actorResult
    override fun getActorByIdFlow(actorId: Int): Flow<Result<Actor>> = flowOf(actorResult)
    override fun searchActors(query: String, language: String, region: String?): Flow<PagingData<Actor>> = actorsPaged
    override fun searchActorsResults(query: String): Flow<Result<List<Actor>>> = flowOf(actorsResult)
    override fun getActorMovieCredits(actorId: Int): Flow<Result<List<Movie>>> = flowOf(movieCreditsResult)
    override fun getActorTvCredits(actorId: Int): Flow<Result<List<TvSeries>>> = flowOf(tvCreditsResult)

    override suspend fun refreshActors() {
        refreshCalls += Unit
    }

    override fun getFavoriteActors(): Flow<Result<List<Actor>>> = flowOf(actorsResult)
    override suspend fun getActorImages(actorId: Int): Result<List<Image>> = imagesResult
}
