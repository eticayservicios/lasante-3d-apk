package com.lasante.tvkiosk.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color

/**
 * Cuando [HikvisionLayoutDebug] está ON, monta la UI dentro de un marco fijo
 * **1280×720 dp** (canvas Hikvision) centrado con letterbox.
 *
 * Así Intro / CT / Productos reciben las mismas constraints que el panel físico,
 * aunque el dispositivo real sea Damasco u otro tamaño.
 */
@Composable
fun HikvisionPreviewFrame(content: @Composable () -> Unit) {
    if (!HikvisionLayoutDebug.isForced()) {
        content()
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(Tv66Reference.Width)
                .height(Tv66Reference.Height)
                .clipToBounds(),
        ) {
            content()
        }
    }
}
