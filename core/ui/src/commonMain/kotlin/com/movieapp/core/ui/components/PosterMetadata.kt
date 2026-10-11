package com.movieapp.core.ui.components

import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.rating_not_rated
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.movieapp.core.ui.utils.formatRating
import com.movieapp.core.ui.utils.formatReleaseYear

@Composable
internal fun PosterMetadata(releaseDate: String?, rating: Double?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = releaseDate.formatReleaseYear(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text =
                if (rating != null && rating.isFinite() && rating > 0) {
                    "★ ${rating.formatRating()}"
                } else {
                    stringResource(Res.string.rating_not_rated)
                },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
