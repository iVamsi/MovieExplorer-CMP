package com.movieapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movieapp.core.ui.components.shimmer.rememberShimmerBrush

@Preview(showBackground = true, name = "SeriesCardSkeleton – Light")
@Composable
private fun SeriesCardSkeletonPreview() {
    MaterialTheme { SeriesCardSkeleton(modifier = Modifier.width(150.dp)) }
}

@Composable
fun SeriesCardSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(brush)
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(14.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(brush)
                )
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(14.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(brush)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier =
                        Modifier.fillMaxWidth(0.6f)
                            .height(11.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(brush)
                )
            }
        }
    }
}
