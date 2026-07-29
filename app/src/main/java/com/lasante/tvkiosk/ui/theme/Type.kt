package com.lasante.tvkiosk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.R

private val TitleFontFamily = FontFamily(
    Font(R.font.anton_regular, FontWeight.Normal),
)

private val BodyFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

fun laSanteFontFamilies(): Pair<FontFamily, FontFamily> = TitleFontFamily to BodyFontFamily

fun laSanteTypography(titleFamily: FontFamily, bodyFamily: FontFamily): Typography {
    val body = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    return Typography(
        displayLarge = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
        displayMedium = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
        displaySmall = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp),
        headlineLarge = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp),
        headlineMedium = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp),
        headlineSmall = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
        titleLarge = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp),
        titleSmall = TextStyle(fontFamily = titleFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = body,
        bodySmall = body.copy(fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        labelMedium = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
        labelSmall = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp),
    )
}
