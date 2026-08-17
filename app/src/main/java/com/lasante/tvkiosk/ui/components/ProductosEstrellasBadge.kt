package com.lasante.tvkiosk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.ui.utils.UiSound
import com.lasante.tvkiosk.ui.utils.clickableWithSound

private val BadgeGreenStart = Color(0xFF6FA320)
private val BadgeGreenEnd = Color(0xFFA4D23A)

/** Badge verde “PRODUCTOS ESTRELLAS” (Intro y pantalla de estrellas). */
@Composable
fun ProductosEstrellasBadge(
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val fontSize = when {
        height >= 60.dp -> 18.sp
        height >= 50.dp -> 16.sp
        height >= 42.dp -> 15.sp
        height >= 32.dp -> 12.sp
        height >= 26.dp -> 10.sp
        else -> 9.sp
    }
    val clickModifier = if (onClick != null) {
        Modifier.clickableWithSound(sound = UiSound.Product, onClick = onClick)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .height(height)
            .wrapContentWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BadgeGreenStart, BadgeGreenEnd),
                ),
            )
            .then(clickModifier)
            .padding(horizontal = height * 0.45f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "PRODUCTOS ESTRELLAS",
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
    }
}
