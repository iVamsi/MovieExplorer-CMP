package com.movieapp.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movieapp.app.resources.Res
import com.movieapp.app.resources.cd_gallery_still
import com.movieapp.app.resources.gallery_gesture_hint
import com.movieapp.app.resources.gallery_loading_artwork
import com.movieapp.app.resources.gallery_missing_artwork
import com.movieapp.app.resources.gallery_no_stills
import com.movieapp.app.resources.gallery_position
import com.movieapp.app.resources.gallery_title
import com.movieapp.app.resources.loading
import com.movieapp.core.common.Constants
import com.movieapp.core.model.Image
import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.cd_back
import com.movieapp.core.ui.components.AppErrorState
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.utils.toImageUrl
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageGalleryScreen(
    kind: String,
    id: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImageGalleryViewModel = koinViewModel { parametersOf(kind, id) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ImageGalleryContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = { viewModel.onEvent(ImageGalleryUiEvent.Retry) },
        modifier = modifier,
    )
}

@Composable
internal fun ImageGalleryContent(
    uiState: ImageGalleryUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        when {
            uiState.isLoading && uiState.images.isEmpty() -> GalleryLoadingState()
            uiState.error != null && uiState.images.isEmpty() ->
                GalleryErrorState(message = uiState.error.orEmpty(), onRetry = onRetry)
            uiState.images.isEmpty() -> GalleryEmptyState()
            else -> GalleryPager(images = uiState.images, onBackClick = onBackClick)
        }
        if (uiState.images.isEmpty()) {
            GalleryTopControls(imageCount = 0, currentPage = null, onBackClick = onBackClick)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun GalleryLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(MovieAppSpacing.small, Alignment.CenterVertically),
    ) {
        LoadingIndicator()
        Text(
            text = stringResource(Res.string.loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GalleryErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AppErrorState(message = message, onRetry = onRetry)
    }
}

@Composable
private fun GalleryEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.gallery_no_stills),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(MovieAppSpacing.large),
        )
    }
}

@Composable
private fun GalleryPager(images: List<Image>, onBackClick: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            ZoomableArtwork(image = images[page])
        }
        GalleryTopControls(
            imageCount = images.size,
            currentPage = pagerState.currentPage,
            onBackClick = onBackClick,
        )
        Text(
            text = stringResource(Res.string.gallery_gesture_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(MovieAppSpacing.medium),
        )
    }
}

@Composable
private fun ZoomableArtwork(image: Image) {
    var transform by remember(image.filePath) { mutableStateOf(GalleryTransform()) }
    var viewportSize by remember(image.filePath) { mutableStateOf(IntSize.Zero) }
    var artworkLoadState by
        remember(image.filePath) {
            mutableStateOf(
                if (image.filePath.isBlank()) ArtworkLoadState.Unavailable
                else ArtworkLoadState.Loading
            )
        }
    val transformableState = rememberTransformableState { centroid, zoomChange, panChange, _ ->
        transform =
            calculateGalleryTransform(
                currentScale = transform.scale,
                currentOffset = transform.offset,
                zoomChange = zoomChange,
                panChange = panChange,
                centroid = centroid,
                viewportSize = viewportSize,
                artworkAspectRatio = image.aspectRatio.toFloat(),
            )
    }
    LaunchedEffect(viewportSize) {
        transform =
            calculateGalleryTransform(
                currentScale = transform.scale,
                currentOffset = transform.offset,
                zoomChange = 1f,
                panChange = Offset.Zero,
                centroid = Offset.Unspecified,
                viewportSize = viewportSize,
                artworkAspectRatio = image.aspectRatio.toFloat(),
            )
    }
    val imageSize =
        if (image.aspectRatio > 0 && image.aspectRatio < 1.0) {
            Constants.ImageSize.PROFILE_LARGE
        } else {
            Constants.ImageSize.BACKDROP_MEDIUM
        }

    if (artworkLoadState == ArtworkLoadState.Unavailable) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.gallery_missing_artwork),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(MovieAppSpacing.large),
            )
        }
    } else {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .onSizeChanged { viewportSize = it }
                    .graphicsLayer {
                        scaleX = transform.scale
                        scaleY = transform.scale
                        translationX = transform.offset.x
                        translationY = transform.offset.y
                    }
                    .transformable(
                        state = transformableState,
                        canPan = { transform.scale > MinimumGalleryScale },
                    )
        ) {
            if (artworkLoadState == ArtworkLoadState.Loading) {
                GalleryArtworkLoadingState()
            }
            AsyncImage(
                model = image.filePath.toImageUrl(imageSize),
                contentDescription = stringResource(Res.string.cd_gallery_still),
                contentScale = ContentScale.Fit,
                onLoading = { artworkLoadState = ArtworkLoadState.Loading },
                onSuccess = { artworkLoadState = ArtworkLoadState.Available },
                onError = { artworkLoadState = ArtworkLoadState.Unavailable },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

internal data class GalleryTransform(
    val scale: Float = MinimumGalleryScale,
    val offset: Offset = Offset.Zero,
)

internal fun calculateGalleryTransform(
    currentScale: Float,
    currentOffset: Offset,
    zoomChange: Float,
    panChange: Offset,
    centroid: Offset,
    viewportSize: IntSize,
    artworkAspectRatio: Float,
): GalleryTransform {
    val newScale = (currentScale * zoomChange).coerceIn(MinimumGalleryScale, MaximumGalleryScale)
    if (newScale == MinimumGalleryScale || viewportSize == IntSize.Zero) {
        return GalleryTransform()
    }

    val viewportCenter = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
    val effectiveCentroid = centroid.takeIf { it.isSpecified } ?: viewportCenter
    val scaleRatio = newScale / currentScale
    val requestedOffset =
        currentOffset * scaleRatio +
            (effectiveCentroid - viewportCenter) * (1f - scaleRatio) +
            panChange
    val viewportAspectRatio = viewportSize.width.toFloat() / viewportSize.height
    val validArtworkAspectRatio = artworkAspectRatio.takeIf { it > 0f } ?: viewportAspectRatio
    val artworkWidth: Float
    val artworkHeight: Float
    if (validArtworkAspectRatio > viewportAspectRatio) {
        artworkWidth = viewportSize.width.toFloat()
        artworkHeight = artworkWidth / validArtworkAspectRatio
    } else {
        artworkHeight = viewportSize.height.toFloat()
        artworkWidth = artworkHeight * validArtworkAspectRatio
    }
    val maximumOffsetX = ((artworkWidth * newScale - viewportSize.width) / 2f).coerceAtLeast(0f)
    val maximumOffsetY = ((artworkHeight * newScale - viewportSize.height) / 2f).coerceAtLeast(0f)

    return GalleryTransform(
        scale = newScale,
        offset =
            Offset(
                x = requestedOffset.x.coerceIn(-maximumOffsetX, maximumOffsetX),
                y = requestedOffset.y.coerceIn(-maximumOffsetY, maximumOffsetY),
            ),
    )
}

private const val MinimumGalleryScale = 1f
private const val MaximumGalleryScale = 4f

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun GalleryArtworkLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(MovieAppSpacing.small, Alignment.CenterVertically),
    ) {
        LoadingIndicator()
        Text(
            text = stringResource(Res.string.gallery_loading_artwork),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class ArtworkLoadState {
    Loading,
    Available,
    Unavailable,
}

@Composable
private fun GalleryTopControls(
    imageCount: Int,
    currentPage: Int?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(MovieAppSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(CoreRes.string.cd_back),
            )
        }
        Text(
            text =
                currentPage?.let { page ->
                    stringResource(Res.string.gallery_position, page + 1, imageCount)
                } ?: stringResource(Res.string.gallery_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(
                        horizontal = MovieAppSpacing.medium,
                        vertical = MovieAppSpacing.small,
                    ),
        )
    }
}

