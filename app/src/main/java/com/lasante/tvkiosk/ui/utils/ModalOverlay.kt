package com.lasante.tvkiosk.ui.utils

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

private val BackdropBlurRadius = 20.dp
private val BackdropFrostColor = Color.White.copy(alpha = 0.14f)

/** Desenfoque Compose (phone/Infinix — funciona con TextureView en esos GPUs). */
fun Modifier.modalBackdropBlur(active: Boolean, radius: Dp = BackdropBlurRadius): Modifier {
    if (!active) return this
    return composed {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                val radiusPx = radius.toPx()
                renderEffect = BlurEffect(
                    radiusX = radiusPx,
                    radiusY = radiusPx,
                    edgeTreatment = TileMode.Clamp,
                )
            }
        } else {
            blur(radius)
        }
    }
}

@Composable
fun ModalFrostScrim(
    visible: Boolean,
    modifier: Modifier = Modifier,
    color: Color = BackdropFrostColor,
    strong: Boolean = false,
) {
    if (!visible) return
    if (strong) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f)),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.26f)),
        )
        return
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color),
    )
}

/** API 30 (p. ej. Fire HD): sin blur nativo ni BlurEffect — usar frost más marcado. */
fun needsLegacyModalBlurFallback(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

@Composable
fun ModalOverlayDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setDimAmount(0f)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
