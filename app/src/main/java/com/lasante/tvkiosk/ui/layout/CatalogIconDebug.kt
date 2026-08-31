package com.lasante.tvkiosk.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.BuildConfig

/** Medidas en px del layout real (1.er card / badge). Solo DEBUG. */
data class CatalogIconDebugSizes(
    val card: IntSize = IntSize.Zero,
    val slot: IntSize = IntSize.Zero,
    val image: IntSize = IntSize.Zero,
    val badge: IntSize = IntSize.Zero,
    val badgeIcon: IntSize = IntSize.Zero,
)

object CatalogIconDebugColors {
    val Card = Color(0xFF43A047)
    val Slot = Color(0xFF1E88E5)
    val Image = Color(0xFFE53935)
    val Badge = Color(0xFF8E24AA)
    val BadgeIcon = Color(0xFFFF6F00)
}

fun IntSize.toDebugLabel(density: Density): String {
    if (width == 0 && height == 0) return "—"
    return with(density) {
        "${width}×${height}px · ${width.toDp().value.toInt()}×${height.toDp().value.toInt()}dp"
    }
}

@Composable
fun CatalogIconDebugPanel(
    title: String,
    staticLines: List<String>,
    measuredLines: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    if (!BuildConfig.DEBUG) return
    val text = buildString {
        append(title)
        staticLines.forEach { append('\n').append(it) }
        if (measuredLines.isNotEmpty()) {
            append("\n— medido en pantalla —")
            measuredLines.forEach { append('\n').append(it) }
        }
    }
    Text(
        text = text,
        color = Color.White,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        modifier = modifier
            .background(Color(0xDD000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    )
}

@Composable
fun Modifier.catalogIconDebugBorder(
    enabled: Boolean,
    color: Color,
    onMeasured: (IntSize) -> Unit,
): Modifier {
    if (!enabled) return this
    return border(2.5.dp, color).onGloballyPositioned { coords ->
        onMeasured(coords.size)
    }
}
