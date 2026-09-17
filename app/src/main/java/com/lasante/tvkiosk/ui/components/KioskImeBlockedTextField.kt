package com.lasante.tvkiosk.ui.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.theme.LaSanteTextSecondary

/**
 * Campo de texto kiosco: bloquea el IME de Android y abre el teclado virtual propio.
 * El texto lo escribe [KioskQwertyKeyboard] vía estado del padre (este campo es readOnly).
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
    LaunchedEffect(Unit) {
        // Una sola vez al montar; hide() puede ser costoso en algunos OEM.
        runCatching { keyboard?.hide() }
    }

    @Suppress("DEPRECATION")
    CompositionLocalProvider(LocalTextInputService provides null) {
        BasicTextField(
            value = value,
            onValueChange = {},
            enabled = enabled,
            singleLine = true,
            readOnly = true,
            textStyle = TextStyle(
                color = LaSanteText,
                fontSize = fontSize,
            ),
            cursorBrush = SolidColor(LaSanteGreen),
            modifier = modifier
                .then(
                    if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier,
                )
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        runCatching { keyboard?.hide() }
                        onOpenCustomKeyboard()
                    }
                },
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = LaSanteTextSecondary.copy(alpha = 0.40f),
                        fontSize = fontSize,
                    )
                }
                innerTextField()
            },
        )
    }
}

fun appendKioskChar(current: String, char: String, maxLen: Int = 80): String =
    (current + char).take(maxLen)

fun backspaceKioskText(current: String): String =
    if (current.isEmpty()) current else current.dropLast(1)
