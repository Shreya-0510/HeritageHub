package com.example.heritagehub.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat

/**
 * Creates a shimmer loading effect (skeleton loading)
 * Used to show loading state while data is being fetched
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    width: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        ),
        label = "shimmerX"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    val xShimmer = (shimmerX.value * 1000).toInt()
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(xShimmer.toFloat() - 200, 0f),
        end = Offset(xShimmer.toFloat() + 200, 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth(width)
            .background(brush, shape = RoundedCornerShape(8.dp))
    )
}

/**
 * Shimmer card skeleton for artwork loading
 */
@Composable
fun ShimmerArtworkCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(0.dp)
    ) {
        // Image placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        )

        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Title placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Artist name placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
            )
        }
    }
}

/**
 * Shimmer list loading state for multiple cards
 */
@Composable
fun ShimmerArtworkList(modifier: Modifier = Modifier, count: Int = 6) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            if (it % 2 == 0) {
                // Horizontal layout for featured section
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            } else {
                // Grid layout for explore section
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    ShimmerEffect(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

/**
 * Shimmer loading for customization request items
 */
@Composable
fun ShimmerRequestCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        // Client name placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Budget and buttons placeholder
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(12.dp)
            )
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }
    }
}

/**
 * Shimmer loading for request list
 */
@Composable
fun ShimmerRequestList(modifier: Modifier = Modifier, count: Int = 3) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            ShimmerRequestCard()
        }
    }
}







