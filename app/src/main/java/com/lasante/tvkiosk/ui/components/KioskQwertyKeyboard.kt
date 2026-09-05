package com.lasante.tvkiosk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.ui.theme.LaSanteGreenDark
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.theme.LaSanteWhite
import com.lasante.tvkiosk.ui.utils.UiSound
import com.lasante.tvkiosk.ui.utils.clickableWithSound

private val Row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
private val Row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "Ñ")
private val Row3 = listOf("Z", "X", "C", "V", "B", "N", "M")

/**
 * Teclado QWERTY en pantalla para kiosco/TV (Hikvision).
 * No usa el IME de Android: el padre debe bloquear soft input.
 */
@Composable
fun KioskQwertyKeyboard(
    onChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    layoutScale: Float = 1f,
) {
    val keyHeight = (34.dp * layoutScale).coerceIn(28.dp, 44.dp)
    val keyGap = (4.dp * layoutScale).coerceIn(3.dp, 6.dp)
    val pad = (6.dp * layoutScale).coerceIn(5.dp, 10.dp)
    val corner = (8.dp * layoutScale).coerceIn(6.dp, 12.dp)
    val labelSp = (13f * layoutScale).coerceIn(11f, 16f).sp
    val shape = RoundedCornerShape(corner)

    Column(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .background(Color(0xFFF4F4F4))
            .border(1.dp, Color(0x14000000), shape)
            // Consume toques para que el scrim de fuera no cierre al teclear.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .padding(pad),
        verticalArrangement = Arrangement.spacedBy(keyGap),
    ) {
        KeyboardKeyRow(
            labels = Row1,
            keyHeight = keyHeight,
            keyGap = keyGap,
            labelSp = labelSp,
            corner = corner,
            onChar = onChar,
        )
        KeyboardKeyRow(
            labels = Row2,
            keyHeight = keyHeight,
            keyGap = keyGap,
            labelSp = labelSp,
            corner = corner,
            onChar = onChar,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keyGap),
        ) {
            Row3.forEach { label ->
                KeyboardKey(
                    label = label,
                    modifier = Modifier
                        .weight(1f)
                        .height(keyHeight),
                    labelSp = labelSp,
                    corner = corner,
                    onClick = { onChar(label.lowercase()) },
                )
            }
            KeyboardKey(
                label = "⌫",
                modifier = Modifier
                    .weight(1.35f)
                    .height(keyHeight),
                labelSp = labelSp,
                corner = corner,
                containerColor = Color(0xFFE8E8E8),
                onClick = onBackspace,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keyGap),
        ) {
            KeyboardKey(
                label = "Espacio",
                modifier = Modifier
                    .weight(1f)
                    .height(keyHeight),
                labelSp = labelSp,
                corner = corner,
                onClick = { onChar(" ") },
            )
            KeyboardKey(
                label = "Listo",
                modifier = Modifier
                    .width((72.dp * layoutScale).coerceIn(64.dp, 96.dp))
                    .height(keyHeight),
                labelSp = labelSp,
                corner = corner,
                containerColor = LaSanteGreenDark,
                contentColor = LaSanteWhite,
                onClick = onDone,
            )
        }
    }
}

@Composable
private fun KeyboardKeyRow(
    labels: List<String>,
    keyHeight: Dp,
    keyGap: Dp,
    labelSp: TextUnit,
    corner: Dp,
    onChar: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(keyGap),
    ) {
        labels.forEach { label ->
            KeyboardKey(
                label = label,
                modifier = Modifier
                    .weight(1f)
                    .height(keyHeight),
                labelSp = labelSp,
                corner = corner,
                onClick = { onChar(label.lowercase()) },
            )
        }
    }
}

@Composable
private fun KeyboardKey(
    label: String,
    modifier: Modifier = Modifier,
    labelSp: TextUnit,
    corner: Dp,
    containerColor: Color = LaSanteWhite,
    contentColor: Color = LaSanteText,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = shape)
            .clip(shape)
            .background(containerColor)
            .clickableWithSound(sound = UiSound.Click, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = labelSp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
