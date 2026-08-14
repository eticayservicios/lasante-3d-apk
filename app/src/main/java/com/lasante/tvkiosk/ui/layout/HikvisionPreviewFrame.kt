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
import androidx.compose.ui.unit.Dp

/**
 * Viewport lógico fijo + scale-to-fit del host.
 * Usado por [HikvisionLayoutDebug] (1280×720) y [Tv1080LayoutDebug] (1137×711).
 */
@Composable
fun DebugLayoutPreviewFrame(
    forced: Boolean,
    refWidth: Dp,
    refHeight: Dp,
    onHostUpdated: (hostW: Float, hostH: Float, scale: Float) -> Unit,
    content: @Composable () -> Unit,
) {
    if (!forced) {
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
            maxWidth / refWidth,
            maxHeight / refHeight,
        )
        LaunchedEffect(maxWidth, maxHeight, scale) {
            onHostUpdated(maxWidth.value, maxHeight.value, scale)
        }

        Box(
            modifier = Modifier
                .width(refWidth * scale)
                .height(refHeight * scale)
                .clipToBounds(),
        ) {
            Box(
                modifier = Modifier
                    .requiredWidth(refWidth)
                    .requiredHeight(refHeight)
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

/**
 * Cuando [HikvisionLayoutDebug] está ON, monta la UI en canvas lógico **1280×720 dp**
 * y la escala para llenar el host.
 */
@Composable
fun HikvisionPreviewFrame(content: @Composable () -> Unit) {
    DebugLayoutPreviewFrame(
        forced = HikvisionLayoutDebug.isForced(),
        refWidth = Tv66Reference.Width,
        refHeight = Tv66Reference.Height,
        onHostUpdated = { w, h, scale ->
            HikvisionLayoutDebug.updatePreviewHost(w, h, scale)
        },
        content = content,
    )
}

/**
 * Cuando [Tv1080LayoutDebug] está ON, monta la UI en canvas lógico **1137×711 dp**
 * (TV1080 / Television_1080) y la escala para llenar el host.
 */
@Composable
fun Tv1080PreviewFrame(content: @Composable () -> Unit) {
    DebugLayoutPreviewFrame(
        forced = Tv1080LayoutDebug.isForced(),
        refWidth = Tv1080Reference.Width,
        refHeight = Tv1080Reference.Height,
        onHostUpdated = { w, h, scale ->
            Tv1080LayoutDebug.updatePreviewHost(w, h, scale)
        },
        content = content,
    )
}
