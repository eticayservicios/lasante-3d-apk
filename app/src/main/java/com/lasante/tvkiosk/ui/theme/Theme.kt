package com.lasante.tvkiosk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LaSanteGreen,
    onPrimary = Color.White,
    secondary = LaSanteGreenDark,
    onSecondary = Color.White,
    background = LaSanteBackground,
    surface = LaSanteWhite,
    onBackground = LaSanteText,
    onSurface = LaSanteText,
)

@Composable
fun LaSanteTheme(
    content: @Composable () -> Unit
) {
    val (titleFamily, bodyFamily) = laSanteFontFamilies()
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = laSanteTypography(titleFamily, bodyFamily),
        content = content,
    )
}
