package com.movieapp.core.domain.repository

import androidx.paging.PagingData
import com.movieapp.core.common.Result
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Image
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import kotlinx.coroutines.flow.Flow

interface ActorRepository {

    /** Get popular actors */
    fun getPopularActors(): Flow<PagingData<Actor>>

    /** Get actor by ID */
    suspend fun getActorById(actorId: Int): Result<Actor>

    /** Get actor by ID as Flow */
    fun getActorByIdFlow(actorId: Int): Flow<Result<Actor>>

    /** Search actors (paginated) */
    fun searchActors(query: String, language: String, region: String?): Flow<PagingData<Actor>>

    /** Search actors and return the first page as a simple list (for search screens) */
    fun searchActorsResults(query: String): Flow<Result<List<Actor>>>

    /** Get actor movie credits */
    fun getActorMovieCredits(actorId: Int): Flow<Result<List<Movie>>>

    /** Get actor TV credits */
    fun getActorTvCredits(actorId: Int): Flow<Result<List<TvSeries>>>

    /** Refresh actor data from remote */
    suspend fun refreshActors()

    /** Get favorite actors */
    fun getFavoriteActors(): Flow<Result<List<Actor>>>

    /** Profile / publicity stills for gallery (TMDB person images). */
    suspend fun getActorImages(actorId: Int): Result<List<Image>>
}
