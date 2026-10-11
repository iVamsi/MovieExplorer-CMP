package com.movieapp.data.actors.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.movieapp.core.common.Constants
import com.movieapp.core.common.Result
import com.movieapp.core.common.dispatchers.AppDispatchers
import com.movieapp.core.common.getDataOrNull
import com.movieapp.core.common.getExceptionOrNull
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.common.safeCall
import com.movieapp.core.common.safeResultFlow
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.ActorDao
import com.movieapp.core.database.entity.ActorEntity
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Image
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TaggedImage
import com.movieapp.core.model.TvSeries
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.actors.mapper.ActorMapper
import com.movieapp.data.actors.mapper.toDomainImage
import com.movieapp.data.actors.mediator.ActorRemoteMediator
import com.movieapp.data.actors.paging.ActorSearchPagingSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import co.touchlab.kermit.Logger

@OptIn(ExperimentalPagingApi::class)
class ActorRepositoryImpl(
    private val apiService: TmdbApiService,
    private val actorDao: ActorDao,
    private val transactionRunner: TransactionRunner,
    private val remoteKeysDao: RemoteKeysDao,
    private val actorMapper: ActorMapper,
    private val preferencesRepository: PreferencesRepository,
    private val dispatchers: AppDispatchers,
) : ActorRepository {

    private suspend fun tmdbLanguage(): String = preferencesRepository.tmdbLanguage.first()

    private suspend fun tmdbRegion(): String = preferencesRepository.watchRegion.first()

    override fun getPopularActors(): Flow<PagingData<Actor>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                remoteMediator = ActorRemoteMediator(apiService, transactionRunner, remoteKeysDao, actorDao, actorMapper),
                pagingSourceFactory = { actorDao.getActorsPagingSource() },
            )
            .flow
            .map { pagingData -> pagingData.map { actorMapper.mapToDomain(it) } }

    override suspend fun getActorById(actorId: Int): Result<Actor> =
        withContext(dispatchers.io) {
            val fetched = safeCall {
                val actor = apiService.getActorDetails(actorId, language = tmdbLanguage())
                actorDao.insertActor(actorMapper.mapToEntity(actor))
                actor
            }
            if (fetched is Result.Error) {
                val cached = actorDao.getActorById(actorId)
                if (cached != null) Result.Success(actorMapper.mapToDomain(cached)) else fetched
            } else {
                fetched
            }
        }

    override fun getActorByIdFlow(actorId: Int): Flow<Result<Actor>> =
        flow {
            emit(Result.Loading)
            emit(getActorById(actorId))
        }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override fun searchActors(
        query: String,
        language: String,
        region: String?,
    ): Flow<PagingData<Actor>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = {
                    ActorSearchPagingSource(apiService, query, language, region)
                },
            )
            .flow

    override fun searchActorsResults(query: String): Flow<Result<List<Actor>>> =
        safeResultFlow(
                fetch = {
                    apiService
                        .searchActors(
                            query = query,
                            page = 1,
                            language = tmdbLanguage(),
                            region = tmdbRegion(),
                        )
                        .results
                },
                fallback = {
                    val cached = actorDao.searchActors(query).first()
                    if (cached.isNotEmpty()) actorMapper.mapToDomainList(cached) else null
                },
            )
            .flowOn(dispatchers.io)

    override fun getActorMovieCredits(actorId: Int): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val credits = apiService.getActorMovieCredits(actorId)
                    credits.cast.map { cast ->
                        Movie(
                            id = cast.id,
                            title = cast.title,
                            overview = cast.overview,
                            posterPath = cast.posterPath,
                            backdropPath = cast.backdropPath,
                            releaseDate = cast.releaseDate,
                            voteAverage = cast.voteAverage,
                            voteCount = cast.voteCount,
                            popularity = cast.popularity,
                            adult = cast.adult,
                            originalLanguage = cast.originalLanguage,
                            originalTitle = cast.originalTitle,
                            video = cast.video,
                            genreIds = cast.genreIds,
                        )
                    }
                }
            )
            .flowOn(dispatchers.io)

    override fun getActorTvCredits(actorId: Int): Flow<Result<List<TvSeries>>> =
        safeResultFlow(
                fetch = {
                    val credits = apiService.getActorTvCredits(actorId)
                    credits.cast.map { cast ->
                        TvSeries(
                            id = cast.id,
                            name = cast.name,
                            overview = cast.overview,
                            posterPath = cast.posterPath,
                            backdropPath = cast.backdropPath,
                            firstAirDate = cast.firstAirDate,
                            lastAirDate = null,
                            voteAverage = cast.voteAverage,
                            voteCount = cast.voteCount,
                            popularity = cast.popularity,
                            adult = cast.adult,
                            originalLanguage = cast.originalLanguage,
                            originalName = cast.originalName,
                            originCountry = cast.originCountry,
                            genreIds = cast.genreIds,
                        )
                    }
                }
            )
            .flowOn(dispatchers.io)

    override suspend fun getActorImages(actorId: Int): Result<List<Image>> =
        withContext(dispatchers.io) {
            safeCall {
                val profileResponse = apiService.getActorImages(actorId)
                val profileImages = profileResponse.profiles.map { it.toDomainImage() }
                val taggedResult = safeCall { fetchTaggedImagesAsDomain(actorId) }
                val taggedFailure = taggedResult.getExceptionOrNull()
                if (taggedFailure != null) {
                    Logger.withTag("ActorRepositoryImpl").w(taggedFailure) { "Tagged images failed" }
                }
                val taggedImages = taggedResult.getDataOrNull().orEmpty()
                (profileImages + taggedImages)
                    .distinctBy { it.filePath }
                    .sortedByDescending { it.voteAverage }
            }
        }

    private suspend fun fetchTaggedImagesAsDomain(actorId: Int): List<Image> {
        val lang = tmdbLanguage()
        val buffer = mutableListOf<TaggedImage>()
        var page = 1
        val maxPages = 5
        while (page <= maxPages) {
            val resp = apiService.getActorTaggedImages(actorId, page = page, language = lang)
            if (resp.results.isEmpty()) break
            buffer.addAll(resp.results)
            if (resp.totalPages == 0 || page >= resp.totalPages) break
            page++
        }
        return buffer.mapNotNull { it.toDomainImage() }
    }

    override suspend fun refreshActors() =
        withContext(dispatchers.io) {
            try {
                val response = apiService.getPopularActors(page = 1)
                actorDao.deleteAllActors()
                actorDao.insertActors(actorMapper.mapToEntityList(response.results))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Ignore refresh failures; cached data remains valid
            }
        }

    override fun getFavoriteActors(): Flow<Result<List<Actor>>> =
        actorDao
            .getFavoriteActors()
            .map<List<ActorEntity>, Result<List<Actor>>> { entities ->
                Result.Success(actorMapper.mapToDomainList(entities))
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)
}
