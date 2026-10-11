package com.movieapp.core.ui

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class LocalizedLabelsTest {

    @Test
    fun `values strings - section labels - present in en and es`() {
        val en = File("src/commonMain/composeResources/values/strings.xml").readText()
        val es = File("src/commonMain/composeResources/values-es/strings.xml").readText()
        val keys =
            listOf(
                "label_biography",
                "label_movies_subsection",
                "label_tv_series_subsection",
                "settings_all",
                "cd_add_to_collection",
            )
        for (key in keys) {
            assertThat(en).contains("name=\"$key\"")
            assertThat(es).contains("name=\"$key\"")
        }
    }
}
