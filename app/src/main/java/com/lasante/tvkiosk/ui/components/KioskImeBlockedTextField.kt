package com.lasante.tvkiosk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.theme.LaSanteTextSecondary

/**
 * Campo de texto kiosco: solo muestra el query; no usa [BasicTextField] para evitar
 * selección/copiar del sistema. El texto lo escribe [KioskQwertyKeyboard] vía el padre.
 */
@Composable
fun KioskImeBlockedTextField(
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Buscar Producto",
    fontSize: TextUnit,
    focusRequester: FocusRequester? = null,
    onOpenCustomKeyboard: () -> Unit = {},
) {
    val keyboard = LocalSoftwareKeyboardController.current
    // focusRequester se conserva en la firma por compat; el campo ya no toma foco.
    @Suppress("UNUSED_PARAMETER")
    val unusedFocus = focusRequester

    LaunchedEffect(Unit) {
        runCatching { keyboard?.hide() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = {
                    runCatching { keyboard?.hide() }
                    onOpenCustomKeyboard()
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = LaSanteTextSecondary.copy(alpha = 0.40f),
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = value,
                style = TextStyle(
                    color = LaSanteText,
                    fontSize = fontSize,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

fun appendKioskChar(current: String, char: String, maxLen: Int = 80): String =
    (current + char).take(maxLen)

fun backspaceKioskText(current: String): String =
    if (current.isEmpty()) current else current.dropLast(1)
