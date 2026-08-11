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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Cuando [HikvisionLayoutDebug] está ON, monta la UI dentro de un marco fijo
 * **1280×720 dp** (canvas Hikvision) centrado con letterbox.
 *
 * Si el dispositivo (p. ej. Damasco con barras de sistema) no alcanza 1280×720,
 * se escala uniformemente para caber — sin aplastar el aspect — para que métricas
 * y clipping coincidan con el panel físico.
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
        ).coerceAtMost(1f)

        // Slot visual del letterbox (tamaño escalado).
        Box(
            modifier = Modifier
                .width(Tv66Reference.Width * scale)
                .height(Tv66Reference.Height * scale)
                .clipToBounds(),
        ) {
            // Canvas lógico siempre 1280×720; scale solo visual.
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
