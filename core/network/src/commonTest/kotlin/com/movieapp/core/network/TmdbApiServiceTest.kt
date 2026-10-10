package com.movieapp.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class TmdbApiServiceTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val requests = mutableListOf<HttpRequestData>()

    private fun service(respond: MockRequestHandler): TmdbApiService {
        val config = NetworkConfig(apiKey = "test-key", baseUrl = "https://api.themoviedb.org/3/", debugLogging = false)
        val engine = MockEngine { request ->
            requests += request
            respond(request)
        }
        return TmdbApiService(createTmdbHttpClient(engine, tmdbTestJson(), config))
    }

    private fun fixture(name: String): MockRequestHandler = {
        respond(TmdbFixtures.read(name), HttpStatusCode.OK, jsonHeaders)
    }

    @Test
    fun `getPopularMovies - default args - decodes and sends expected request`() = runTest {
        val api = service(fixture("movie_popular.json"))

        val response = api.getPopularMovies()

        assertEquals(278, response.results.single().id)
        val request = requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/3/movie/popular", request.url.encodedPath)
        assertEquals("1", request.url.parameters["page"])
        assertEquals("en-US", request.url.parameters["language"])
        assertNull(request.url.parameters["region"])
        assertEquals("Bearer test-key", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun `getMovieDetails - with append - decodes nested videos and builds path`() = runTest {
        val api = service(fixture("movie_detail_with_videos.json"))

        val movie = api.getMovieDetails(movieId = 278, appendToResponse = "videos")

        assertEquals("PLl99DlL6b4", movie.videos?.results?.first()?.key)
        val request = requests.single()
        assertEquals("/3/movie/278", request.url.encodedPath)
        assertEquals("videos", request.url.parameters["append_to_response"])
    }

    @Test
    fun `deleteSession - body - sends DELETE with JSON body`() = runTest {
        val api = service { respond("""{"success":true}""", HttpStatusCode.OK, jsonHeaders) }

        val result = api.deleteSession(DeleteSessionBody(sessionId = "abc"))

        assertTrue(result.success)
        val request = requests.single()
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/3/authentication/session", request.url.encodedPath)
        val body = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
        assertEquals("""{"session_id":"abc"}""", body)
    }

    @Test
    fun `getAccount - 401 response - throws ClientRequestException`() = runTest {
        val api = service {
            respond("""{"status_code":7}""", HttpStatusCode.Unauthorized, jsonHeaders)
        }

        val error = assertFailsWith<ClientRequestException> { api.getAccount(sessionId = "bad") }

        assertEquals(401, error.response.status.value)
    }
}
