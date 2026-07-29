package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.utils.clickableWithSound

private const val BADGE_ASSET = "file:///android_asset/vitrina/ui/productos-estrellas.png"

@Composable
fun VitrinaProductosEstrellasBadge(
    visible: Boolean,
    metrics: IntroLayoutMetrics,
    modifier: Modifier = Modifier,
) {
    val alpha = rememberVitrinaInteractionAlpha(visible)
    if (alpha <= 0.01f) return

    val context = LocalContext.current
    val model = remember(context) {
        ImageRequest.Builder(context)
            .data(BADGE_ASSET)
            .build()
    }
    AsyncImage(
        model = model,
        contentDescription = "Productos estrellas",
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .fillMaxWidth(metrics.bubblesBadgeWidthFraction)
            .height(metrics.bubblesBadgeHeight),
        contentScale = ContentScale.FillWidth,
    )
}

@Composable
fun VitrinaBubblesRow(
    slotProducts: Array<Product?>,
    visible: Boolean,
    metrics: IntroLayoutMetrics,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = rememberVitrinaInteractionAlpha(visible)
    if (alpha <= 0.01f) return

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .fillMaxWidth(metrics.bubblesRowWidthFraction),
    ) {
        val slotCount = VitrinaConstants.SLOTS_PER_UNIT
        val totalSpacing = metrics.bubbleSpacing * (slotCount - 1)
        val sizeFromWidth = (maxWidth - totalSpacing) / slotCount
        val bubbleSize = minOf(metrics.bubbleSize, sizeFromWidth)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = metrics.bubbleSpacing,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slotProducts.forEachIndexed { _, product ->
                if (product != null) {
                    VitrinaProductBubble(
                        product = product,
                        bubbleSize = bubbleSize,
                        productSizeFraction = metrics.bubbleProductSizeFraction,
                        onClick = { onProductClick(product) },
                    )
                } else {
                    Spacer(modifier = Modifier.size(bubbleSize))
                }
            }
        }
    }
}

/**
 * Burbuja blanca con sombra interior grisácea (hacia dentro).
 * Centro claro → borde gris suave.
 */
@Composable
private fun VitrinaBubbleSphere(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFFFFFFFF),
                    0.55f to Color(0xFFF4F4F4),
                    0.82f to Color(0xFFE4E4E4),
                    0.94f to Color(0xFFCBCBCB),
                    1.00f to Color(0xFFB4B4B4),
                ),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}

@Composable
private fun VitrinaProductBubble(
    product: Product,
    bubbleSize: Dp,
    productSizeFraction: Float,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val imageUrl = product.bubbleImageUrl()
    val productModel = remember(imageUrl, context) {
        imageUrl?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(true)
                .build()
        }
    }

    Box(
        modifier = Modifier
            .size(bubbleSize)
            .clip(CircleShape)
            .clickableWithSound(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        VitrinaBubbleSphere(modifier = Modifier.fillMaxSize())
        if (productModel != null) {
            Box(
                modifier = Modifier.fillMaxSize(productSizeFraction),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = productModel,
                    contentDescription = product.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

/** Miniatura o imagen principal para la burbuja del producto destacado. */
fun Product.bubbleImageUrl(): String? {
    val miniatura = media.imagenes2d.miniatura?.trim()?.takeIf { it.isNotBlank() }
    val principal = media.imagenes2d.principal?.trim()?.takeIf { it.isNotBlank() }
    return miniatura ?: principal
}
