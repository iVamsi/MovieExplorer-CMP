package com.movieapp.core.ui.components

import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.cd_actor_no_profile_photo
import com.movieapp.core.ui.resources.cd_actor_photo
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.movieapp.core.common.Constants
import com.movieapp.core.model.Actor
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.cinematicPressFeedback
import com.movieapp.core.ui.theme.CinematicTheme
import com.movieapp.core.ui.utils.toImageUrl

@Composable
fun ActorCard(actor: Actor, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactions = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = modifier.width(MovieAppSpacing.personWidth).cinematicPressFeedback(interactions),
        interactionSource = interactions,
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
        ) {
            val imageUrl = actor.profilePath?.toImageUrl(Constants.ImageSize.PROFILE_MEDIUM)
            val context = LocalPlatformContext.current
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .aspectRatio(MovieAppSpacing.posterAspectRatio)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model =
                            ImageRequest.Builder(context)
                                .data(imageUrl)
                                .size(Size(240, 360))
                                .crossfade(false)
                                .build(),
                        contentDescription = stringResource(Res.string.cd_actor_photo, actor.name),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription =
                            stringResource(Res.string.cd_actor_no_profile_photo, actor.name),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(MovieAppSpacing.spacious),
                    )
                }
            }

            Text(
                text = actor.name,
                modifier = Modifier.fillMaxWidth().padding(horizontal = MovieAppSpacing.small),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2,
            )

            Text(
                text = actor.knownForDepartment,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = MovieAppSpacing.small)
                        .padding(bottom = MovieAppSpacing.small),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                minLines = 1,
            )
        }
    }
}

@Preview(name = "Actor light", widthDp = 144, showBackground = true)
@Preview(
    name = "Actor narrow and large text",
    widthDp = 120,
    fontScale = 2.0f,
    showBackground = true,
)
@Composable
private fun ActorCardPreview() {
    CinematicTheme {
        ActorCard(
            actor = Actor(id = 3, name = "Lupita Nyong’o", knownForDepartment = "Acting"),
            onClick = {},
        )
    }
}

@Preview(name = "Actor dark", widthDp = 144, showBackground = true)
@Composable
private fun ActorCardDarkPreview() {
    CinematicTheme(darkTheme = true) {
        ActorCard(
            actor = Actor(id = 3, name = "Lupita Nyong’o", knownForDepartment = "Acting"),
            onClick = {},
        )
    }
}
