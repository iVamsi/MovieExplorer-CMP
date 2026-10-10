package com.movieapp.core.network

internal object TmdbFixtures {
    fun read(name: String): String = checkNotNull(fixtures[name]) { "Missing fixture tmdb/$name" }

    private val fixtures = mapOf(
        "actor_detail.json" to """
{
  "id": 3223,
  "name": "Robert Downey Jr.",
  "biography": "American actor.",
  "birthday": "1965-04-04",
  "profile_path": "/blah.jpg",
  "popularity": 50.2,
  "adult": false,
  "gender": 2,
  "also_known_as": ["RDJ"]
}
        """.trimIndent(),
        "actor_movie_credits.json" to """
{
  "cast": [
    {
      "id": 99861,
      "title": "Avengers: Age of Ultron",
      "character": "Tony Stark / Iron Man",
      "order": 0,
      "poster_path": "/poster.jpg",
      "overview": "Earth's mightiest heroes.",
      "popularity": 80.0,
      "release_date": "2015-05-01",
      "vote_average": 7.3,
      "vote_count": 15000,
      "credit_id": "52fe4ad2c3a36847f825d8bd",
      "adult": false,
      "genre_ids": [28, 12],
      "original_language": "en",
      "original_title": "Avengers: Age of Ultron",
      "video": false
    }
  ],
  "crew": []
}
        """.trimIndent(),
        "actor_tv_credits.json" to """
{
  "cast": [
    {
      "id": 1396,
      "name": "Breaking Bad",
      "character": "Walter White",
      "order": 0,
      "poster_path": "/bb.jpg",
      "overview": "A chemistry teacher.",
      "popularity": 200.0,
      "first_air_date": "2008-01-20",
      "vote_average": 8.9,
      "vote_count": 5000,
      "credit_id": "52542282760ee313280017f9",
      "adult": false,
      "genre_ids": [18],
      "original_language": "en",
      "original_name": "Breaking Bad",
      "origin_country": ["US"]
    }
  ],
  "crew": []
}
        """.trimIndent(),
        "discover_movie.json" to """
{
  "page": 1,
  "results": [
    {
      "id": 27205,
      "title": "Inception",
      "overview": "A thief who steals secrets.",
      "release_date": "2010-07-16",
      "vote_average": 8.4,
      "vote_count": 35000,
      "popularity": 100.0,
      "adult": false,
      "original_language": "en",
      "original_title": "Inception",
      "video": false,
      "genre_ids": [28, 878]
    }
  ],
  "total_pages": 100,
  "total_results": 2000
}
        """.trimIndent(),
        "discover_tv.json" to """
{
  "page": 1,
  "results": [
    {
      "id": 66732,
      "name": "Stranger Things",
      "overview": "In 1980s Indiana, a boy vanishes.",
      "first_air_date": "2016-07-15",
      "vote_average": 8.6,
      "vote_count": 16000,
      "popularity": 300.0,
      "adult": false,
      "original_language": "en",
      "original_name": "Stranger Things",
      "origin_country": ["US"],
      "genre_ids": [9648, 18]
    }
  ],
  "total_pages": 50,
  "total_results": 800
}
        """.trimIndent(),
        "movie_detail_sparse_video.json" to """
{
  "id": 100,
  "title": "Sparse Video Test",
  "videos": {
    "results": [
      {
        "id": "1",
        "key": "abc123xyz",
        "site": "YouTube",
        "type": "Trailer"
      }
    ]
  }
}
        """.trimIndent(),
        "movie_detail_with_videos.json" to """
{
  "id": 278,
  "title": "The Shawshank Redemption",
  "overview": "Two imprisoned men bond over years.",
  "runtime": 142,
  "release_date": "1994-09-23",
  "vote_average": 8.7,
  "vote_count": 26000,
  "videos": {
    "results": [
      {
        "id": "533533",
        "key": "PLl99DlL6b4",
        "name": "Trailer",
        "site": "YouTube",
        "size": 1080,
        "type": "Trailer",
        "official": true,
        "published_at": "2015-01-01T12:00:00.000Z"
      }
    ]
  }
}
        """.trimIndent(),
        "movie_popular.json" to """
{
  "page": 1,
  "results": [
    {
      "id": 278,
      "title": "The Shawshank Redemption",
      "overview": "Two imprisoned men bond over years.",
      "poster_path": "/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg",
      "backdrop_path": "/kXfqcdQKsVR0zGl3NQ2LMWcjmwu.jpg",
      "release_date": "1994-09-23",
      "vote_average": 8.7,
      "vote_count": 26000,
      "popularity": 120.5,
      "adult": false,
      "original_language": "en",
      "original_title": "The Shawshank Redemption",
      "video": false,
      "genre_ids": [18, 80]
    }
  ],
  "total_pages": 500,
  "total_results": 10000
}
        """.trimIndent(),
        "paged_actors.json" to """
{
  "page": 1,
  "results": [
    {
      "id": 287,
      "name": "Brad Pitt",
      "popularity": 45.2,
      "adult": false,
      "gender": 2,
      "known_for_department": "Acting",
      "profile_path": "/kU3B75tyR5k6wd1zXTC7MCi6BGe.jpg",
      "known_for": []
    }
  ],
  "total_pages": 500,
  "total_results": 10000
}
        """.trimIndent(),
        "paged_movies_empty.json" to """
{
  "page": 1,
  "results": [],
  "total_pages": 0,
  "total_results": 0
}
        """.trimIndent(),
        "person_tagged_images.json" to """
{
  "page": 1,
  "results": [
    {
      "aspect_ratio": 1.778,
      "file_path": "/tagged_still.jpg",
      "height": 720,
      "width": 1280,
      "vote_average": 5.0,
      "vote_count": 1,
      "iso_639_1": null,
      "image_type": "backdrop",
      "media": {
        "id": 278,
        "title": "Test Movie",
        "name": null,
        "poster_path": "/p.jpg",
        "backdrop_path": "/b.jpg",
        "vote_average": 8.0,
        "vote_count": 100,
        "media_type": "movie"
      },
      "media_type": "movie"
    }
  ],
  "total_pages": 1,
  "total_results": 1
}
        """.trimIndent(),
        "search_movie.json" to """
{
  "page": 1,
  "results": [
    {
      "id": 550,
      "title": "Fight Club",
      "overview": "An insomniac office worker.",
      "release_date": "1999-10-15",
      "vote_average": 8.4,
      "vote_count": 28000,
      "popularity": 90.0,
      "adult": false,
      "original_language": "en",
      "original_title": "Fight Club",
      "video": false,
      "genre_ids": [18]
    }
  ],
  "total_pages": 10,
  "total_results": 200
}
        """.trimIndent(),
        "tv_detail_with_videos.json" to """
{
  "id": 1396,
  "name": "Breaking Bad",
  "overview": "A chemistry teacher turned meth producer.",
  "first_air_date": "2008-01-20",
  "vote_average": 8.9,
  "number_of_seasons": 5,
  "videos": {
    "results": [
      {
        "id": "999",
        "key": "HhesaRleEyA",
        "name": "Official Trailer",
        "site": "YouTube",
        "size": 720,
        "type": "Trailer",
        "official": true,
        "published_at": "2013-08-01T00:00:00.000Z"
      }
    ]
  }
}
        """.trimIndent(),
    )
}
