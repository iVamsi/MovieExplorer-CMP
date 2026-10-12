package com.movieapp.core.domain.repository

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.testing.FakeMovieRepository
import com.movieapp.core.testing.FakeSeriesRepository
import org.junit.Test

class RepositoryDefaultsTest {

    @Test
    fun `discover movies - filters omitted - passes nulls`() {
        val repository = FakeMovieRepository()

        repository.discoverMovies()

        assertThat(repository.discoverCalls.single())
            .isEqualTo(listOf(null, null, null, null, null))
    }

    @Test
    fun `discover series - filters omitted - passes nulls`() {
        val repository = FakeSeriesRepository()

        repository.discoverSeries()

        assertThat(repository.discoverCalls.single())
            .isEqualTo(listOf(null, null, null, null, null))
    }
}
