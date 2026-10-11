package com.movieapp.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinksTest {
    @Test
    fun `parseDeepLink - supported destinations - return typed links`() {
        assertEquals(DeepLink.Movie(603), parseDeepLink("movieapp://movie/603"))
        assertEquals(DeepLink.Series(1399), parseDeepLink("movieapp://series/1399"))
        assertEquals(DeepLink.Actor(31), parseDeepLink("movieapp://actor/31"))
        assertEquals(DeepLink.Collection("favorites"), parseDeepLink("movieapp://collection/favorites"))
        assertEquals(DeepLink.Search, parseDeepLink("movieapp://search"))
        assertEquals(DeepLink.Library, parseDeepLink("movieapp://collections"))
        assertEquals(DeepLink.Upcoming, parseDeepLink("movieapp://upcoming"))
    }

    @Test
    fun `parseDeepLink - auth callback - preserves approval token and state`() {
        assertEquals(
            DeepLink.AuthCallback(true, "token", "state"),
            parseDeepLink(
                "movieapp://auth/callback?approved=true&request_token=token&state=state"
            ),
        )
        assertEquals(
            DeepLink.AuthCallback(false, null, "state"),
            parseDeepLink("movieapp://auth/callback?approved=false&state=state"),
        )
    }

    @Test
    fun `parseDeepLink - malformed urls - return null`() {
        assertNull(parseDeepLink("https://movie/603"))
        assertNull(parseDeepLink("movieapp://movie"))
        assertNull(parseDeepLink("movieapp://movie/not-a-number"))
        assertNull(parseDeepLink("movieapp://unknown/1"))
    }
}
