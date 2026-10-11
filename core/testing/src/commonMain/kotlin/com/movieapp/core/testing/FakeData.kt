package com.movieapp.core.testing

import com.movieapp.core.model.*

object FakeData {

    // Fixed epoch-millis timestamps to avoid non-deterministic test data
    private const val BASE_TIME = 1_700_000_000_000L // 2023-11-14T22:13:20Z
    private const val ONE_DAY_MS = 86_400_000L
    private const val HALF_DAY_MS = 43_200_000L
    private const val TWO_HOURS_MS = 7_200_000L
    private const val ONE_HOUR_MS = 3_600_000L
    private const val HALF_HOUR_MS = 1_800_000L

    val fakeGenres =
        listOf(
            Genre(28, "Action"),
            Genre(12, "Adventure"),
            Genre(16, "Animation"),
            Genre(35, "Comedy"),
            Genre(80, "Crime"),
            Genre(99, "Documentary"),
            Genre(18, "Drama"),
            Genre(10751, "Family"),
            Genre(14, "Fantasy"),
            Genre(36, "History"),
            Genre(27, "Horror"),
            Genre(10402, "Music"),
            Genre(9648, "Mystery"),
            Genre(10749, "Romance"),
            Genre(878, "Science Fiction"),
            Genre(10770, "TV Movie"),
            Genre(53, "Thriller"),
            Genre(10752, "War"),
            Genre(37, "Western"),
        )

    val fakeMovies =
        listOf(
            Movie(
                id = 1,
                title = "The Dark Knight",
                overview = "Batman raises the stakes in his war on crime.",
                posterPath = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                backdropPath = "/hqkIcbrOHL86UncnHIsHVcVmzue.jpg",
                releaseDate = "2008-07-18",
                voteAverage = 8.5,
                voteCount = 25000,
                popularity = 85.5,
                adult = false,
                originalLanguage = "en",
                originalTitle = "The Dark Knight",
                video = false,
                genreIds = listOf(28, 80, 18),
                genres = listOf(fakeGenres[0], fakeGenres[4], fakeGenres[6]),
                runtime = 152,
                status = "Released",
                tagline = "Why So Serious?",
                budget = 185000000,
                revenue = 1004558444,
                homepage = "https://www.thedarkknight.com",
            ),
            Movie(
                id = 2,
                title = "Inception",
                overview = "A thief who steals corporate secrets through dream-sharing technology.",
                posterPath = "/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
                backdropPath = "/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
                releaseDate = "2010-07-16",
                voteAverage = 8.4,
                voteCount = 28000,
                popularity = 90.2,
                adult = false,
                originalLanguage = "en",
                originalTitle = "Inception",
                video = false,
                genreIds = listOf(28, 878, 53),
                genres = listOf(fakeGenres[0], fakeGenres[14], fakeGenres[16]),
                runtime = 148,
                status = "Released",
                tagline = "Your mind is the scene of the crime.",
                budget = 160000000,
                revenue = 836836967,
                homepage = "https://www.inceptionmovie.com",
            ),
            Movie(
                id = 3,
                title = "Pulp Fiction",
                overview =
                    "The lives of two mob hitmen, a boxer, a gangster and his wife intertwine.",
                posterPath = "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
                backdropPath = "/4cDFJr4H91XNk0sJvNqnfD4Tf7f.jpg",
                releaseDate = "1994-10-14",
                voteAverage = 8.9,
                voteCount = 24000,
                popularity = 75.8,
                adult = false,
                originalLanguage = "en",
                originalTitle = "Pulp Fiction",
                video = false,
                genreIds = listOf(80, 18),
                genres = listOf(fakeGenres[4], fakeGenres[6]),
                runtime = 154,
                status = "Released",
                tagline = "Just because you are a character doesn't mean you have character.",
                budget = 8000000,
                revenue = 213928762,
                homepage = "https://www.pulpfiction.com",
            ),
        )

    val fakeTvSeries =
        listOf(
            TvSeries(
                id = 1,
                name = "Breaking Bad",
                overview = "A high school chemistry teacher diagnosed with inoperable lung cancer.",
                posterPath = "/ggFHVNu6YYI5L9pCfOacjizRGt.jpg",
                backdropPath = "/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
                firstAirDate = "2008-01-20",
                lastAirDate = "2013-09-29",
                voteAverage = 9.5,
                voteCount = 15000,
                popularity = 95.2,
                adult = false,
                originalLanguage = "en",
                originalName = "Breaking Bad",
                originCountry = listOf("US"),
                genreIds = listOf(80, 18),
                genres = listOf(fakeGenres[4], fakeGenres[6]),
                episodeRunTime = listOf(45, 47),
                numberOfEpisodes = 62,
                numberOfSeasons = 5,
                status = "Ended",
                tagline = "All Hail the King",
                type = "Scripted",
                homepage = "https://www.amc.com/shows/breaking-bad",
                inProduction = false,
                languages = listOf("en", "es", "de"),
            ),
            TvSeries(
                id = 2,
                name = "Game of Thrones",
                overview =
                    "Nine noble families fight for control over the mythical lands of Westeros.",
                posterPath = "/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg",
                backdropPath = "/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg",
                firstAirDate = "2011-04-17",
                lastAirDate = "2019-05-19",
                voteAverage = 8.3,
                voteCount = 20000,
                popularity = 88.7,
                adult = false,
                originalLanguage = "en",
                originalName = "Game of Thrones",
                originCountry = listOf("US"),
                genreIds = listOf(18, 14, 80),
                genres = listOf(fakeGenres[6], fakeGenres[8], fakeGenres[4]),
                episodeRunTime = listOf(60),
                numberOfEpisodes = 73,
                numberOfSeasons = 8,
                status = "Ended",
                tagline = "Winter Is Coming",
                type = "Scripted",
                homepage = "https://www.hbo.com/game-of-thrones",
                inProduction = false,
                languages = listOf("en"),
            ),
        )

    val fakeActors =
        listOf(
            Actor(
                id = 1,
                name = "Leonardo DiCaprio",
                biography = "Leonardo Wilhelm DiCaprio is an American actor and film producer.",
                birthday = "1974-11-11",
                deathday = null,
                placeOfBirth = "Los Angeles, California, USA",
                profilePath = "/5Brc5dRvW7m0VW7T4fGqHl3p3y.jpg",
                adult = false,
                gender = 2,
                knownForDepartment = "Acting",
                originalName = "Leonardo DiCaprio",
                popularity = 85.2,
                alsoKnownAs = listOf("Leo", "Leonardo Wilhelm DiCaprio"),
            ),
            Actor(
                id = 2,
                name = "Tom Hanks",
                biography = "Thomas Jeffrey Hanks is an American actor and filmmaker.",
                birthday = "1956-07-09",
                deathday = null,
                placeOfBirth = "Concord, California, USA",
                profilePath = "/aAM3cQmM9vm6Xyhc0s7J3bTjzTk.jpg",
                adult = false,
                gender = 2,
                knownForDepartment = "Acting",
                originalName = "Tom Hanks",
                popularity = 78.9,
                alsoKnownAs = listOf("Thomas Jeffrey Hanks"),
            ),
        )

    val fakeUserCollections =
        listOf(
            UserCollection(
                id = "collection_1",
                name = "My Favorites",
                description = "My all-time favorite movies",
                isPublic = true,
                createdAt = BASE_TIME - ONE_DAY_MS,
                updatedAt = BASE_TIME,
                itemCount = 3,
                items =
                    listOf(
                        CollectionItem(
                            id = "item_1",
                            collectionId = "collection_1",
                            contentId = 1,
                            contentType = ContentType.MOVIE,
                            addedAt = BASE_TIME - ONE_DAY_MS,
                            userRating = 5,
                            userNotes = "Amazing movie!",
                            isWatched = true,
                            watchedAt = BASE_TIME - HALF_DAY_MS,
                        )
                    ),
            )
        )

    val fakeWatchlistItems =
        listOf(
            WatchlistItem(
                id = "watchlist_1",
                contentId = 2,
                contentType = ContentType.MOVIE,
                addedAt = BASE_TIME - ONE_HOUR_MS,
                priority = 1,
            )
        )

    val fakeFavoriteItems =
        listOf(
            FavoriteItem(
                id = "favorite_1",
                contentId = 1,
                contentType = ContentType.MOVIE,
                addedAt = BASE_TIME - TWO_HOURS_MS,
            )
        )

    val fakeUserRatings =
        listOf(
            UserRating(
                id = "rating_1",
                contentId = 1,
                contentType = ContentType.MOVIE,
                rating = 5,
                ratedAt = BASE_TIME - HALF_HOUR_MS,
                notes = "Perfect movie!",
            )
        )

    val fakeSearchResults =
        listOf(
            MultiSearchItem(
                id = 1,
                mediaType = "movie",
                title = "The Dark Knight",
                overview = "Batman raises the stakes in his war on crime.",
                posterPath = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                backdropPath = "/hqkIcbrOHL86UncnHIsHVcVmzue.jpg",
                releaseDate = "2008-07-18",
                voteAverage = 8.5,
                voteCount = 25000,
                popularity = 85.5,
                adult = false,
                originalLanguage = "en",
                originalTitle = "The Dark Knight",
                genreIds = listOf(28, 80, 18),
            ),
            MultiSearchItem(
                id = 1,
                mediaType = "person",
                name = "Leonardo DiCaprio",
                overview = "Leonardo Wilhelm DiCaprio is an American actor and film producer.",
                posterPath = "/5Brc5dRvW7m0VW7T4fGqHl3p3y.jpg",
                backdropPath = null,
                voteAverage = 8.5,
                voteCount = 1000,
                popularity = 85.2,
                adult = false,
                originalLanguage = "en",
                originalName = "Leonardo DiCaprio",
                knownForDepartment = "Acting",
                gender = 2,
            ),
        )
}
