package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Métricas responsivas derivadas del espacio real (ancho + alto).
 * Punto único para layout Compose y escena 3D.
 */
@Immutable
data class IntroLayoutMetrics(
    val maxWidth: Dp,
    val maxHeight: Dp,
    val widthClass: WindowWidthSizeClass,
) {
    val isCompactWidth: Boolean
        get() = widthClass == WindowWidthSizeClass.Compact

    val isExpandedWidth: Boolean
        get() = widthClass == WindowWidthSizeClass.Expanded

    /** Tablet en landscape (p. ej. 1280×720): no usar layout phone — recorta slots externos. */
    val isTabletLandscape: Boolean
        get() = maxWidth > maxHeight && maxWidth >= 640.dp && maxHeight >= 400.dp

    val isPhoneLandscape: Boolean
        get() = maxWidth > maxHeight && maxHeight < 520.dp && !isTabletLandscape

    val isPhonePortrait: Boolean
        get() = maxHeight > maxWidth && maxWidth < 420.dp

    val isShortHeight: Boolean
        get() = maxHeight < 440.dp

    val isTv: Boolean
        get() = maxWidth >= 880.dp && maxHeight >= 480.dp && !isPhoneLandscape

    /** TV 32" ~720p (853×480 dp). */
    val isTv32: Boolean
        get() = isTv && maxWidth < 900.dp

    /** TV 42" ~1080p (960×540 dp). El tablet del proyecto (~961×529) usa este mismo perfil. */
    val isTv42: Boolean
        get() = isTv && maxWidth in 900.dp..1400.dp

    /** TV 66" ~4K (1920×1080 dp). */
    val isTv66: Boolean
        get() = isTv && maxWidth > 1400.dp

    /** @deprecated Usar isTv42 — alias para compatibilidad en logs. */
    val isTv1080: Boolean
        get() = isTv42

    /** Perfil estable — TV antes que phone/tablet (961×529 = tv_42). */
    val deviceProfile: String
        get() = vitrinaProfileKey

    val vitrinaProfileKey: String
        get() = when {
            isTv32 -> "tv_32"
            isTv42 -> "tv_42"
            isTv66 -> "tv_66"
            isPhoneLandscape -> "phone_landscape"
            isTabletLandscape -> "tablet_landscape"
            isPhonePortrait -> "phone_portrait"
            isShortHeight -> "short_height"
            isExpandedWidth -> "expanded"
            isTv -> "tv_unknown"
            else -> "default"
        }

    /** Evita escena 3D con constraints en 0 al primer frame (salto de escala). */
    val hasStableLayout: Boolean
        get() = maxWidth >= 200.dp && maxHeight >= 150.dp

    /**
     * Todas las métricas de layout deben usar [vitrinaProfileKey].
     * No usar isTabletLandscape antes que isTv*: el tablet del proyecto (~961×529)
     * es isTabletLandscape=true e isTv42=true a la vez; el perfil canónico es tv_42.
     */
    val dragHandleWidth: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 22.dp
            "tv_32" -> 32.dp
            "tv_42" -> 36.dp
            "tv_66" -> 44.dp
            "tablet_landscape" -> 36.dp
            "tv_unknown" -> 40.dp
            else -> 28.dp
        }

    /** Umbral de snap al soltar drag, en píxeles según densidad del dispositivo. */
    fun dragSnapThresholdPx(density: androidx.compose.ui.unit.Density): Float = with(density) {
        when (vitrinaProfileKey) {
            "phone_landscape" -> 22.dp.toPx()
            "tv_32", "tv_42", "tv_66", "tv_unknown" -> 30.dp.toPx()
            "tablet_landscape" -> 30.dp.toPx()
            else -> 26.dp.toPx()
        }
    }

    /** Slop mínimo antes de iniciar drag horizontal (menor = más sensible). */
    fun dragPointerSlopPx(density: androidx.compose.ui.unit.Density): Float = with(density) {
        when (vitrinaProfileKey) {
            "phone_landscape" -> 8.dp.toPx()
            "tv_32", "tv_42", "tv_66", "tv_unknown" -> 10.dp.toPx()
            "tablet_landscape" -> 10.dp.toPx()
            else -> 7.dp.toPx()
        }
    }

    val titleFontSize: TextUnit
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 18.sp
            "tv_32", "tv_42", "tv_66", "tv_unknown" -> 44.sp
            "tablet_landscape" -> 44.sp
            "expanded" -> 48.sp
            else -> if (isCompactWidth) 20.sp else 28.sp
        }

    val featuredHintFontSize: TextUnit
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 20.sp
            "tv_32" -> 22.sp
            "tv_42", "tv_66", "tablet_landscape" -> 26.sp
            "expanded" -> 32.sp
            else -> if (isCompactWidth) 16.sp else 22.sp
        }

    val featuredHintTopPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 2.dp
            "tv_32" -> 33.dp
            "tv_42", "tv_66", "tablet_landscape" -> 44.dp
            "expanded" -> 41.dp
            "short_height" -> 27.dp
            else -> 31.dp
        }

    /** Ajuste fino vertical del hint (+ = abajo, − = subir). */
    val featuredHintVerticalOffset: Dp
        get() = 0.dp

    /** Fracción del alto de escena — alineado al estante de destacados (detrás del 3D). */
    val featuredHintTopFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.07f
            "tv_42", "tablet_landscape" -> 0.08f
            else -> 0f
        }

    val featuredHintWidthFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.88f
            "tv_32" -> 0.68f
            "tv_42", "tv_66", "tablet_landscape" -> 0.66f
            "expanded" -> 0.64f
            else -> 0.72f
        }

    /** Badge verde "PRODUCTOS ESTRELLAS". */
    val bubblesBadgeHeight: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 46.dp
            "phone_portrait" -> 44.dp
            // tablet del proyecto = tv_42
            "tv_42", "tablet_landscape" -> 96.dp
            "tv_32" -> 52.dp
            "tv_66" -> 60.dp
            "short_height" -> 44.dp
            "expanded" -> 54.dp
            "tv_unknown" -> 54.dp
            else -> 46.dp
        }

    val bubblesBadgeWidthFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.371f
            "phone_portrait" -> 0.42f
            "tv_42", "tablet_landscape" -> 0.52f
            "tv_32" -> 0.25f
            "tv_66" -> 0.22f
            "short_height" -> 0.38f
            "expanded" -> 0.28f
            "tv_unknown" -> 0.25f
            else -> 0.30f
        }

    /** Sube el badge hacia el borde superior. Valores más altos = más arriba. */
    val bubblesBadgeTopPullUp: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 4.dp
            "phone_portrait" -> 4.dp
            // 1% más abajo que el calibrado previo (32.dp).
            "tv_42", "tablet_landscape" -> 32.dp - maxHeight * 0.01f
            "tv_32", "tv_66", "tv_unknown" -> 0.dp
            "short_height" -> 4.dp
            "expanded" -> 2.dp
            else -> 2.dp
        }

    /**
     * Ancho de la fila relativo a la columna 3D.
     */
    val bubblesRowWidthFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.56f
            "phone_portrait" -> 0.60f
            "tv_42", "tablet_landscape" -> 0.72f
            "tv_32" -> 0.60f
            "tv_66" -> 0.57f
            "short_height" -> 0.58f
            "expanded" -> 0.60f
            "tv_unknown" -> 0.58f
            else -> 0.60f
        }

    /**
     * Padding desde el top del área 3D hasta las burbujas.
     * Mayor = burbujas más abajo (cerca del estante).
     */
    val bubblesRowTopInScene: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 2.dp + maxHeight * 0.05f
            "phone_portrait" -> 8.dp
            "tv_42", "tablet_landscape" -> 30.dp + maxHeight * 0.06f
            "tv_32" -> 10.dp
            "tv_66" -> 14.dp
            "short_height" -> 6.dp
            "expanded" -> 10.dp
            "tv_unknown" -> 12.dp
            else -> 8.dp
        }

    /** Diámetro máximo por burbuja. */
    val bubbleSize: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 54.dp
            "phone_portrait" -> 81.dp
            "tv_42", "tablet_landscape" -> 89.dp
            "tv_32" -> 81.dp
            "tv_66" -> 102.dp
            "short_height" -> 78.dp
            "expanded" -> 84.dp
            "tv_unknown" -> 87.dp
            else -> 81.dp
        }

    val bubbleSpacing: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 9.dp
            "phone_portrait" -> 5.dp
            "tv_42", "tablet_landscape" -> 28.dp
            "tv_32" -> 7.dp
            "tv_66" -> 10.dp
            "short_height" -> 4.dp
            "expanded" -> 7.dp
            "tv_unknown" -> 8.dp
            else -> 5.dp
        }

    /** Miniatura ~78% del diámetro: producto grande con poco margen (referencia). */
    val bubbleProductSizeFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape", "tv_42", "tablet_landscape" -> 0.78f
            else -> 0.75f
        }

    val sceneHeightFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.94f
            "short_height" -> 0.92f
            "tv_32", "tv_42", "tv_66", "tv_unknown", "tablet_landscape" -> 1.0f
            "expanded" -> 0.96f
            else -> 0.94f
        }

    val sceneWidthFraction: Float
        get() = 1.0f

    val sideIconSize: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 58.dp
            "short_height" -> 64.dp
            "tv_32", "tv_42", "tv_66", "tv_unknown", "tablet_landscape" -> 92.dp
            "expanded" -> 96.dp
            else -> 76.dp
        }

    val sideIconSpacing: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 14.dp
            "short_height" -> 18.dp
            "expanded" -> 52.dp
            else -> 32.dp
        }

    val horizontalPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.dp
            "expanded" -> 16.dp
            else -> if (isCompactWidth) 4.dp else 8.dp
        }

    val verticalPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.dp
            "short_height" -> 2.dp
            "expanded" -> 10.dp
            else -> 6.dp
        }

    val logoHeight: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 22.dp
            "short_height" -> 28.dp
            "tv_32" -> 24.dp
            "tv_42", "tv_66", "tablet_landscape" -> 25.dp
            "expanded" -> 29.dp
            else -> 36.dp
        }

    /** Padding inferior del logo — más inset = logo más arriba. */
    val logoBottomPadding: Dp
        get() = maxHeight * vitrinaInsetBottomFraction * 0.70f + verticalPadding + when (vitrinaProfileKey) {
            "phone_landscape" -> 10.dp
            "tv_42", "tv_66", "tablet_landscape" -> 14.dp
            else -> 8.dp
        }

    /** Alineado con gridHorizontalPadding de productos/clases (24 landscape / 16 portrait). */
    val logoEndPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape", "tv_32", "tv_42", "tv_66", "tablet_landscape", "expanded" -> 26.dp
            else -> 18.dp
        }

    val socialIconSize: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 24.dp
            "tv_42", "tablet_landscape" -> 39.dp
            "tv_32", "tv_66" -> 46.dp
            else -> if (isCompactWidth) 30.dp else 37.dp
        }

    /** Espacio horizontal entre iconos de redes sociales. */
    val socialIconSpacing: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 10.dp
            else -> if (isCompactWidth) 6.dp else 8.dp
        }

    /** Inset izquierdo de redes — alineado con gridHorizontalPadding + respiro extra en phone. */
    val socialStartPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 42.dp
            "tv_32", "tv_42", "tv_66", "tablet_landscape" -> 29.dp
            else -> 21.dp
        }

    val vitrinaBlockWidthFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 1.0f
            "tv_32" -> 0.94f
            "tv_42", "tv_66", "tablet_landscape" -> 0.96f
            "short_height" -> 0.92f
            "expanded" -> 0.92f
            else -> 0.94f
        }

    val vitrinaBlockHeightFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 1.0f
            "tv_32", "tv_42", "tv_66", "tablet_landscape" -> 0.98f
            "short_height" -> 0.94f
            else -> 0.97f
        }

    /** Ajuste fino en dp del bloque layout (negativo = subir, positivo = bajar). No mueve la cámara. */
    val vitrinaVerticalOffsetAdjustment: Dp
        get() = when (vitrinaProfileKey) {
            "tv_42", "tablet_landscape" -> (-42).dp
            "tv_66" -> (-50).dp
            "phone_landscape" -> 0.dp
            else -> 0.dp
        }

    /**
     * Baja solo el cilindro 3D (+ botón Gira) sin mover las burbujas.
     * Usa el espacio vacío inferior; no pegar al bottom.
     */
    val vitrinaCylinderNudgeDown: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 10.dp
            "tv_42", "tablet_landscape" -> maxHeight * 0.10f
            else -> 0.dp
        }

    val vitrinaVerticalBias: Float
        get() = when (vitrinaProfileKey) {
            "tv_32" -> 0.08f
            "tv_42", "tv_66", "tablet_landscape" -> 0.05f
            "phone_landscape" -> 0.06f
            "phone_portrait" -> 0.05f
            "expanded" -> 0.06f
            "short_height" -> 0.04f
            else -> 0.06f
        }

    val vitrinaInsetStartFraction: Float
        get() = when (vitrinaProfileKey) {
            "tv_32" -> 0.12f
            "tv_42", "tv_66", "tablet_landscape" -> 0.14f
            "phone_landscape" -> 0.08f
            "phone_portrait" -> 0.05f
            "expanded" -> 0.10f
            else -> 0.08f
        }

    val vitrinaInsetEndFraction: Float
        get() = when (vitrinaProfileKey) {
            "tv_32" -> 0.12f
            "tv_42", "tv_66", "tablet_landscape" -> 0.14f
            "phone_landscape" -> 0.08f
            "phone_portrait" -> 0.05f
            "expanded" -> 0.10f
            else -> 0.08f
        }

    val vitrinaInsetTopFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape", "phone_portrait", "tablet_landscape",
            "tv_32", "tv_42", "tv_66", "tv_unknown",
            -> 0f
            "short_height", "expanded" -> 0.02f
            else -> 0.02f
        }

    val vitrinaInsetBottomFraction: Float
        get() = when (vitrinaProfileKey) {
            "tv_32", "tv_42", "tv_66", "tablet_landscape", "phone_landscape" -> 0.06f
            "phone_portrait" -> 0.06f
            else -> 0.04f
        }

    /** Mismo tamaño que [rotateButtonSize] en todos los perfiles. */
    val historyButtonSize: Dp
        get() = rotateButtonSize

    val rotateButtonSize: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 54.dp
            "short_height" -> 64.dp
            "tv_32", "tv_42", "tv_66", "tablet_landscape" -> 78.dp
            "expanded" -> 88.dp
            "tv_unknown" -> 80.dp
            else -> 70.dp
        }

    /** Borde derecho del cilindro 3D — botón Nuestra Historia (padding ≥ 0). */
    val vitrinaOverlayEndPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape", "tv_32" -> 0.dp
            "tv_42", "tablet_landscape" -> 2.dp
            "tv_66" -> 4.dp
            else -> 4.dp
        }

    /** Ajuste fino horizontal del botón Nuestra Historia (+ = hacia la derecha). */
    val vitrinaOverlayEndOffset: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 2.dp
            else -> 0.dp
        }

    /**
     * Inset desde el borde derecho de la escena hasta el botón Gira.
     * Mayor = más hacia el cilindro; menor = más afuera.
     */
    val rotateButtonEndPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.dp
            "tv_32" -> 8.dp
            // Pegado al cilindro con un poco de aire (~estante medio).
            "tv_42", "tablet_landscape" -> 40.dp
            "tv_66" -> 12.dp
            else -> 6.dp
        }

    /**
     * Ajuste fino horizontal del botón girar (+ = derecha/afuera, − = izquierda/hacia cilindro).
     * Anclado al borde de la escena completa (no a la columna 3D).
     */
    val rotateButtonProtrudeOffset: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> {
                // Anclado al borde de escena: compensar el handle derecho para no mover Infinix.
                val pullTowardCylinder = maxWidth * 0.12f
                val pull = if (pullTowardCylinder > 56.dp) pullTowardCylinder else 56.dp
                -(pull + dragHandleWidth)
            }
            "tv_42", "tablet_landscape" -> 0.dp
            "tv_32" -> (-6).dp
            "tv_66" -> (-10).dp
            else -> 4.dp
        }

    /** Desplazamiento vertical del botón girar hacia el estante medio (+ = abajo). */
    val rotateButtonCenterYOffset: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> maxHeight * sceneHeightFraction * 0.01f
            "tv_32" -> maxHeight * sceneHeightFraction * 0.02f
            "tv_42", "tablet_landscape" -> maxHeight * sceneHeightFraction * 0.035f - 5.dp
            "tv_66" -> maxHeight * sceneHeightFraction * 0.025f
            "expanded" -> maxHeight * sceneHeightFraction * 0.04f
            else -> maxHeight * sceneHeightFraction * 0.04f
        }
}

@Composable
fun BoxWithConstraintsScope.introLayoutMetrics(
    widthClass: WindowWidthSizeClass,
): IntroLayoutMetrics = IntroLayoutMetrics(
    maxWidth = maxWidth,
    maxHeight = maxHeight,
    widthClass = widthClass,
)
