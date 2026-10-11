package com.movieapp.feature.profile.screens.profile

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.domain.repository.AuthState
import org.junit.Test

class ProfileAccountPresentationTest {

    @Test
    fun `accountPresentation - signed out - keeps local library available`() {
        val presentation = accountPresentation(AuthState.SignedOut)

        assertThat(presentation.status).isEqualTo(AccountStatus.SignedOut)
        assertThat(presentation.localLibraryAvailable).isTrue()
    }

    @Test
    fun `accountPresentation - signed in - shows TMDB identity without claiming sync`() {
        val presentation =
            accountPresentation(
                AuthState.SignedIn(
                    accountId = 1,
                    username = "cinephile",
                    name = null,
                    avatarPath = null,
                )
            )

        assertThat(presentation.status).isEqualTo(AccountStatus.SignedIn)
        assertThat(presentation.displayName).isEqualTo("cinephile")
        assertThat(presentation.localLibraryAvailable).isTrue()
    }
}
