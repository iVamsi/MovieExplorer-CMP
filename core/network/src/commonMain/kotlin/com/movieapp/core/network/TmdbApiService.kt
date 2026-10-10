package com.movieapp.core.network

import com.movieapp.core.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

// TMDB v3 API. See TmdbApiInventory.kt for production vs uncalled endpoints documentation.
class TmdbApiService(private val client: HttpClient) {
    // Movies
    suspend fun getPopularMovies(
        page: Int = 1,
        language: String = "en-US",
        region: String? = null,
    ): PagedResponse<Movie> =
        client.get("movie/popular") {
            parameter("page", page)
            parameter("language", language)
            parameter("region", region)
        }.body()

    suspend fun getTopRatedMovies(
        page: Int = 1,
        language: String = "en-US",
        region: String? = null,
    ): PagedResponse<Movie> =
        client.get("movie/top_rated") {
            parameter("page", page)
            parameter("language", language)
            parameter("region", region)
        }.body()

    suspend fun getUpcomingMovies(
        page: Int = 1,
        language: String = "en-US",
        region: String? = null,
    ): PagedResponse<Movie> =
        client.get("movie/upcoming") {
            parameter("page", page)
            parameter("language", language)
            parameter("region", region)
        }.body()

    suspend fun getNowPlayingMovies(
        page: Int = 1,
        language: String = "en-US",
        region: String? = null,
    ): PagedResponse<Movie> =
        client.get("movie/now_playing") {
            parameter("page", page)
            parameter("language", language)
            parameter("region", region)
        }.body()

    suspend fun getMovieDetails(
        movieId: Int,
        language: String = "en-US",
        appendToResponse: String? = null,
    ): Movie =
        client.get("movie/$movieId") {
            parameter("language", language)
            parameter("append_to_response", appendToResponse)
        }.body()

    suspend fun getMovieCredits(
        movieId: Int,
        language: String = "en-US",
    ): Credits =
        client.get("movie/$movieId/credits") {
            parameter("language", language)
        }.body()

    suspend fun getMovieVideos(
        movieId: Int,
        language: String = "en-US",
    ): VideoResponse =
        client.get("movie/$movieId/videos") {
            parameter("language", language)
        }.body()

    suspend fun getMovieImages(
        movieId: Int,
        language: String = "en-US",
        includeImageLanguage: String? = null,
    ): ImageResponse =
        client.get("movie/$movieId/images") {
            parameter("language", language)
            parameter("include_image_language", includeImageLanguage)
        }.body()

    suspend fun getSimilarMovies(
        movieId: Int,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<Movie> =
        client.get("movie/$movieId/similar") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getMovieRecommendations(
        movieId: Int,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<Movie> =
        client.get("movie/$movieId/recommendations") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getMovieReviews(
        movieId: Int,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<Review> =
        client.get("movie/$movieId/reviews") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getMovieKeywords(movieId: Int): KeywordResponse =
        client.get("movie/$movieId/keywords").body()

    suspend fun getMovieExternalIds(movieId: Int): ExternalIds =
        client.get("movie/$movieId/external_ids").body()

    suspend fun getMovieWatchProviders(movieId: Int): WatchProviders =
        client.get("movie/$movieId/watch/providers").body()

    // TV Series
    suspend fun getPopularTvSeries(
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TvSeries> =
        client.get("tv/popular") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getTopRatedTvSeries(
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TvSeries> =
        client.get("tv/top_rated") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getOnTheAirTvSeries(
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TvSeries> =
        client.get("tv/on_the_air") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getAiringTodayTvSeries(
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TvSeries> =
        client.get("tv/airing_today") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getTvSeriesDetails(
        tvId: Int,
        language: String = "en-US",
        appendToResponse: String? = null,
    ): TvSeries =
        client.get("tv/$tvId") {
            parameter("language", language)
            parameter("append_to_response", appendToResponse)
        }.body()

    suspend fun getTvSeriesCredits(
        tvId: Int,
        language: String = "en-US",
    ): Credits =
        client.get("tv/$tvId/credits") {
            parameter("language", language)
        }.body()

    suspend fun getTvSeriesVideos(
        tvId: Int,
        language: String = "en-US",
    ): VideoResponse =
        client.get("tv/$tvId/videos") {
            parameter("language", language)
        }.body()

    suspend fun getTvSeriesImages(
        tvId: Int,
        language: String = "en-US",
        includeImageLanguage: String? = null,
    ): ImageResponse =
        client.get("tv/$tvId/images") {
            parameter("language", language)
            parameter("include_image_language", includeImageLanguage)
        }.body()

    suspend fun getSimilarTvSeries(
        tvId: Int,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TvSeries> =
        client.get("tv/$tvId/similar") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getTvSeriesRecommendations(
        tvId: Int,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TvSeries> =
        client.get("tv/$tvId/recommendations") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getTvSeriesReviews(
        tvId: Int,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<Review> =
        client.get("tv/$tvId/reviews") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getTvSeriesKeywords(tvId: Int): KeywordResponse =
        client.get("tv/$tvId/keywords").body()

    suspend fun getTvSeriesExternalIds(tvId: Int): ExternalIds =
        client.get("tv/$tvId/external_ids").body()

    suspend fun getTvSeriesWatchProviders(tvId: Int): WatchProviders =
        client.get("tv/$tvId/watch/providers").body()

    // Actors
    suspend fun getPopularActors(
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<Actor> =
        client.get("person/popular") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getActorDetails(
        personId: Int,
        language: String = "en-US",
        appendToResponse: String? = null,
    ): Actor =
        client.get("person/$personId") {
            parameter("language", language)
            parameter("append_to_response", appendToResponse)
        }.body()

    suspend fun getActorMovieCredits(
        personId: Int,
        language: String = "en-US",
    ): ActorMovieCredits =
        client.get("person/$personId/movie_credits") {
            parameter("language", language)
        }.body()

    suspend fun getActorTvCredits(
        personId: Int,
        language: String = "en-US",
    ): ActorTvCredits =
        client.get("person/$personId/tv_credits") {
            parameter("language", language)
        }.body()

    suspend fun getActorImages(personId: Int): ActorImageResponse =
        client.get("person/$personId/images").body()

    suspend fun getActorTaggedImages(
        personId: Int,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TaggedImage> =
        client.get("person/$personId/tagged_images") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getActorExternalIds(personId: Int): ExternalIds =
        client.get("person/$personId/external_ids").body()

    // Search
    suspend fun searchMovies(
        query: String,
        page: Int = 1,
        language: String = "en-US",
        includeAdult: Boolean = false,
        region: String? = null,
        year: Int? = null,
        primaryReleaseYear: Int? = null,
    ): PagedResponse<Movie> =
        client.get("search/movie") {
            parameter("query", query)
            parameter("page", page)
            parameter("language", language)
            parameter("include_adult", includeAdult)
            parameter("region", region)
            parameter("year", year)
            parameter("primary_release_year", primaryReleaseYear)
        }.body()

    suspend fun searchTvSeries(
        query: String,
        page: Int = 1,
        language: String = "en-US",
        includeAdult: Boolean = false,
        firstAirDateYear: Int? = null,
    ): PagedResponse<TvSeries> =
        client.get("search/tv") {
            parameter("query", query)
            parameter("page", page)
            parameter("language", language)
            parameter("include_adult", includeAdult)
            parameter("first_air_date_year", firstAirDateYear)
        }.body()

    suspend fun searchActors(
        query: String,
        page: Int = 1,
        language: String = "en-US",
        includeAdult: Boolean = false,
        region: String? = null,
    ): PagedResponse<Actor> =
        client.get("search/person") {
            parameter("query", query)
            parameter("page", page)
            parameter("language", language)
            parameter("include_adult", includeAdult)
            parameter("region", region)
        }.body()

    suspend fun multiSearch(
        query: String,
        page: Int = 1,
        language: String = "en-US",
        includeAdult: Boolean = false,
        region: String? = null,
    ): MultiSearchResult =
        client.get("search/multi") {
            parameter("query", query)
            parameter("page", page)
            parameter("language", language)
            parameter("include_adult", includeAdult)
            parameter("region", region)
        }.body()

    // Genres
    suspend fun getMovieGenres(language: String = "en-US"): GenreResponse =
        client.get("genre/movie/list") {
            parameter("language", language)
        }.body()

    suspend fun getTvGenres(language: String = "en-US"): GenreResponse =
        client.get("genre/tv/list") {
            parameter("language", language)
        }.body()

    // Trending
    suspend fun getTrendingMovies(
        timeWindow: String,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<Movie> =
        client.get("trending/movie/$timeWindow") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getTrendingTvSeries(
        timeWindow: String,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<TvSeries> =
        client.get("trending/tv/$timeWindow") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    suspend fun getTrendingActors(
        timeWindow: String,
        page: Int = 1,
        language: String = "en-US",
    ): PagedResponse<Actor> =
        client.get("trending/person/$timeWindow") {
            parameter("page", page)
            parameter("language", language)
        }.body()

    // Discover
    suspend fun discoverMovies(
        page: Int = 1,
        language: String = "en-US",
        sortBy: String? = null,
        includeAdult: Boolean = false,
        includeVideo: Boolean = false,
        year: Int? = null,
        primaryReleaseYear: Int? = null,
        withGenres: String? = null,
        withPeople: String? = null,
        withCompanies: String? = null,
        withKeywords: String? = null,
        withRuntimeGte: Int? = null,
        withRuntimeLte: Int? = null,
        withReleaseType: String? = null,
        withOriginalLanguage: String? = null,
        voteAverageGte: Double? = null,
        voteAverageLte: Double? = null,
        voteCountGte: Int? = null,
        voteCountLte: Int? = null,
        releaseDateGte: String? = null,
        releaseDateLte: String? = null,
        withWatchProviders: String? = null,
        watchRegion: String? = null,
        withWatchMonetizationTypes: String? = null,
        withoutGenres: String? = null,
        withoutKeywords: String? = null,
        withoutCompanies: String? = null,
        withoutPeople: String? = null,
        withoutWatchProviders: String? = null,
        region: String? = null,
    ): PagedResponse<Movie> =
        client.get("discover/movie") {
            parameter("page", page)
            parameter("language", language)
            parameter("sort_by", sortBy)
            parameter("include_adult", includeAdult)
            parameter("include_video", includeVideo)
            parameter("year", year)
            parameter("primary_release_year", primaryReleaseYear)
            parameter("with_genres", withGenres)
            parameter("with_people", withPeople)
            parameter("with_companies", withCompanies)
            parameter("with_keywords", withKeywords)
            parameter("with_runtime_gte", withRuntimeGte)
            parameter("with_runtime_lte", withRuntimeLte)
            parameter("with_release_type", withReleaseType)
            parameter("with_original_language", withOriginalLanguage)
            parameter("vote_average_gte", voteAverageGte)
            parameter("vote_average_lte", voteAverageLte)
            parameter("vote_count_gte", voteCountGte)
            parameter("vote_count_lte", voteCountLte)
            parameter("release_date_gte", releaseDateGte)
            parameter("release_date_lte", releaseDateLte)
            parameter("with_watch_providers", withWatchProviders)
            parameter("watch_region", watchRegion)
            parameter("with_watch_monetization_types", withWatchMonetizationTypes)
            parameter("without_genres", withoutGenres)
            parameter("without_keywords", withoutKeywords)
            parameter("without_companies", withoutCompanies)
            parameter("without_people", withoutPeople)
            parameter("without_watch_providers", withoutWatchProviders)
            parameter("region", region)
        }.body()

    suspend fun discoverTvSeries(
        page: Int = 1,
        language: String = "en-US",
        sortBy: String? = null,
        includeAdult: Boolean = false,
        includeNullFirstAirDates: Boolean = false,
        year: Int? = null,
        firstAirDateYear: Int? = null,
        withGenres: String? = null,
        withNetworks: String? = null,
        withCompanies: String? = null,
        withKeywords: String? = null,
        withRuntimeGte: Int? = null,
        withRuntimeLte: Int? = null,
        withOriginalLanguage: String? = null,
        voteAverageGte: Double? = null,
        voteAverageLte: Double? = null,
        voteCountGte: Int? = null,
        voteCountLte: Int? = null,
        firstAirDateGte: String? = null,
        firstAirDateLte: String? = null,
        withWatchProviders: String? = null,
        watchRegion: String? = null,
        withWatchMonetizationTypes: String? = null,
        withoutGenres: String? = null,
        withoutKeywords: String? = null,
        withoutCompanies: String? = null,
        withoutNetworks: String? = null,
        withoutWatchProviders: String? = null,
        withType: String? = null,
        withStatus: String? = null,
        withOriginCountry: String? = null,
    ): PagedResponse<TvSeries> =
        client.get("discover/tv") {
            parameter("page", page)
            parameter("language", language)
            parameter("sort_by", sortBy)
            parameter("include_adult", includeAdult)
            parameter("include_null_first_air_dates", includeNullFirstAirDates)
            parameter("year", year)
            parameter("first_air_date_year", firstAirDateYear)
            parameter("with_genres", withGenres)
            parameter("with_networks", withNetworks)
            parameter("with_companies", withCompanies)
            parameter("with_keywords", withKeywords)
            parameter("with_runtime_gte", withRuntimeGte)
            parameter("with_runtime_lte", withRuntimeLte)
            parameter("with_original_language", withOriginalLanguage)
            parameter("vote_average_gte", voteAverageGte)
            parameter("vote_average_lte", voteAverageLte)
            parameter("vote_count_gte", voteCountGte)
            parameter("vote_count_lte", voteCountLte)
            parameter("first_air_date_gte", firstAirDateGte)
            parameter("first_air_date_lte", firstAirDateLte)
            parameter("with_watch_providers", withWatchProviders)
            parameter("watch_region", watchRegion)
            parameter("with_watch_monetization_types", withWatchMonetizationTypes)
            parameter("without_genres", withoutGenres)
            parameter("without_keywords", withoutKeywords)
            parameter("without_companies", withoutCompanies)
            parameter("without_networks", withoutNetworks)
            parameter("without_watch_providers", withoutWatchProviders)
            parameter("with_type", withType)
            parameter("with_status", withStatus)
            parameter("with_origin_country", withOriginCountry)
        }.body()

    // Authentication & account (session_id from login; see TMDB auth docs)
    suspend fun createRequestToken(): RequestTokenResponse =
        client.get("authentication/token/new").body()

    suspend fun createSession(body: CreateSessionBody): SessionResponse =
        client.post("authentication/session/new") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun deleteSession(body: DeleteSessionBody): TmdbPostSuccess =
        client.delete("authentication/session") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getAccount(sessionId: String): TmdbAccountDto =
        client.get("account") {
            parameter("session_id", sessionId)
        }.body()

    suspend fun getFavoriteMovies(
        accountId: Int,
        sessionId: String,
        page: Int = 1,
        sortBy: String = "created_at.asc",
    ): PagedResponse<Movie> =
        client.get("account/$accountId/favorite/movies") {
            parameter("session_id", sessionId)
            parameter("page", page)
            parameter("sort_by", sortBy)
        }.body()

    suspend fun getFavoriteTv(
        accountId: Int,
        sessionId: String,
        page: Int = 1,
        sortBy: String = "created_at.asc",
    ): PagedResponse<TvSeries> =
        client.get("account/$accountId/favorite/tv") {
            parameter("session_id", sessionId)
            parameter("page", page)
            parameter("sort_by", sortBy)
        }.body()

    suspend fun getWatchlistMovies(
        accountId: Int,
        sessionId: String,
        page: Int = 1,
        sortBy: String = "created_at.asc",
    ): PagedResponse<Movie> =
        client.get("account/$accountId/watchlist/movies") {
            parameter("session_id", sessionId)
            parameter("page", page)
            parameter("sort_by", sortBy)
        }.body()

    suspend fun getWatchlistTv(
        accountId: Int,
        sessionId: String,
        page: Int = 1,
        sortBy: String = "created_at.asc",
    ): PagedResponse<TvSeries> =
        client.get("account/$accountId/watchlist/tv") {
            parameter("session_id", sessionId)
            parameter("page", page)
            parameter("sort_by", sortBy)
        }.body()

    suspend fun getFavoritePeople(
        accountId: Int,
        sessionId: String,
        page: Int = 1,
        sortBy: String = "created_at.asc",
    ): PagedResponse<Actor> =
        client.get("account/$accountId/favorite/person") {
            parameter("session_id", sessionId)
            parameter("page", page)
            parameter("sort_by", sortBy)
        }.body()

    suspend fun getRatedMovies(
        accountId: Int,
        sessionId: String,
        page: Int = 1,
        sortBy: String = "created_at.asc",
    ): PagedResponse<Movie> =
        client.get("account/$accountId/rated/movies") {
            parameter("session_id", sessionId)
            parameter("page", page)
            parameter("sort_by", sortBy)
        }.body()

    suspend fun getRatedTv(
        accountId: Int,
        sessionId: String,
        page: Int = 1,
        sortBy: String = "created_at.asc",
    ): PagedResponse<TvSeries> =
        client.get("account/$accountId/rated/tv") {
            parameter("session_id", sessionId)
            parameter("page", page)
            parameter("sort_by", sortBy)
        }.body()

    suspend fun postAccountFavorite(
        accountId: Int,
        sessionId: String,
        body: AccountFavoriteBody,
    ): TmdbPostSuccess =
        client.post("account/$accountId/favorite") {
            parameter("session_id", sessionId)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postAccountWatchlist(
        accountId: Int,
        sessionId: String,
        body: AccountWatchlistBody,
    ): TmdbPostSuccess =
        client.post("account/$accountId/watchlist") {
            parameter("session_id", sessionId)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postMovieRating(
        movieId: Int,
        sessionId: String,
        body: MovieTvRatingBody,
    ): TmdbPostSuccess =
        client.post("movie/$movieId/rating") {
            parameter("session_id", sessionId)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun deleteMovieRating(
        movieId: Int,
        sessionId: String,
    ): TmdbPostSuccess =
        client.delete("movie/$movieId/rating") {
            parameter("session_id", sessionId)
        }.body()

    suspend fun postTvRating(
        tvId: Int,
        sessionId: String,
        body: MovieTvRatingBody,
    ): TmdbPostSuccess =
        client.post("tv/$tvId/rating") {
            parameter("session_id", sessionId)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun deleteTvRating(
        tvId: Int,
        sessionId: String,
    ): TmdbPostSuccess =
        client.delete("tv/$tvId/rating") {
            parameter("session_id", sessionId)
        }.body()
}

@Serializable data class KeywordResponse(val keywords: List<Keyword>)

@Serializable data class GenreResponse(val genres: List<Genre>)
