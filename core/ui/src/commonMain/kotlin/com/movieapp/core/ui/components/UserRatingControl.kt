package com.movieapp.core.ui.components

import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.user_rating_label
import com.movieapp.core.ui.resources.user_rating_star
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.movieapp.core.ui.designsystem.MovieAppSpacing

@Composable
fun UserRatingControl(
    ratingOutOfFive: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.user_rating_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.extraSmall)) {
            for (star in 1..5) {
                val selected = star <= ratingOutOfFive
                val starCd = stringResource(Res.string.user_rating_star, star)
                IconButton(
                    onClick = { if (enabled) onRatingSelected(star) },
                    modifier =
                        Modifier.size(MovieAppSpacing.minTouchTarget).semantics {
                            contentDescription = starCd
                        },
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector =
                            if (selected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint =
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                    )
                }
            }
        }
    }
}
