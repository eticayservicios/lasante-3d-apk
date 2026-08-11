package com.lasante.tvkiosk.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Cuando [HikvisionLayoutDebug] está ON, monta la UI en canvas lógico **1280×720 dp**
 * (mismas constraints que el panel Hikvision) y la **escala** para llenar el host.
 *
 * En la TV física 1280×720 ocupa toda la pantalla; en Damasco (≈1333×800) sin escala
 * se veía un rectángulo más chico con bandas. Con scale≈1.04 llena el ancho como Hikvision.
 */
@Composable
fun HikvisionPreviewFrame(content: @Composable () -> Unit) {
    if (!HikvisionLayoutDebug.isForced()) {
        content()
        return
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        contentAlignment = Alignment.Center,
    ) {
        val scale = minOf(
            maxWidth / Tv66Reference.Width,
            maxHeight / Tv66Reference.Height,
        )
        LaunchedEffect(maxWidth, maxHeight, scale) {
            HikvisionLayoutDebug.updatePreviewHost(
                hostWidthDp = maxWidth.value,
                hostHeightDp = maxHeight.value,
                appliedScale = scale,
            )
        }

        // Slot visual = ref × scale (llena el host en el eje limitante).
        Box(
            modifier = Modifier
                .width(Tv66Reference.Width * scale)
                .height(Tv66Reference.Height * scale)
                .clipToBounds(),
        ) {
            // Canvas lógico siempre 1280×720; el scale es solo visual.
            Box(
                modifier = Modifier
                    .requiredWidth(Tv66Reference.Width)
                    .requiredHeight(Tv66Reference.Height)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .clipToBounds(),
            ) {
                content()
            }
        }
    }
}
