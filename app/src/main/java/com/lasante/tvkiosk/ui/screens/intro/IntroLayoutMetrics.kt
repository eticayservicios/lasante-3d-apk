package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.ui.layout.CatalogHeaderMetrics
import com.lasante.tvkiosk.ui.layout.DeviceProfile
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.TvProfileDetector

/**
 * Métricas responsivas Intro / escena 3D.
 * Tier TV/phone/tablet viene de [DeviceProfile] (mismo resolver que CT/Productos).
 */
@Immutable
data class IntroLayoutMetrics(
    val maxWidth: Dp,
    val maxHeight: Dp,
    val widthClass: WindowWidthSizeClass,
    val profile: DeviceProfile,
    /** Forzar tv_66 en paneles 4K/Hikvision aunque reporten ~960 dp. */
    val preferTv66: Boolean = false,
) {
    val isCompactWidth: Boolean
        get() = widthClass == WindowWidthSizeClass.Compact

    val isExpandedWidth: Boolean
        get() = widthClass == WindowWidthSizeClass.Expanded

    val isTabletLandscape: Boolean
        get() = profile.tier == DeviceProfileTier.TABLET_LANDSCAPE ||
            (maxWidth > maxHeight && maxWidth >= 640.dp && maxHeight >= 400.dp &&
                profile.tier != DeviceProfileTier.TV_REGULAR &&
                profile.tier != DeviceProfileTier.TV_LARGE &&
                profile.tier != DeviceProfileTier.COMPACT_LANDSCAPE)

    val isPhoneLandscape: Boolean
        get() = profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE

    val isPhonePortrait: Boolean
        get() = profile.tier == DeviceProfileTier.COMPACT_PORTRAIT

    val isShortHeight: Boolean
        get() = maxHeight < 440.dp

    val isTv: Boolean
        get() = profile.tier == DeviceProfileTier.TV_REGULAR ||
            profile.tier == DeviceProfileTier.TV_LARGE ||
            (maxWidth >= 880.dp && maxHeight >= 480.dp && !isPhoneLandscape)

    /** TV 32" ~720p (853×480 dp) — raro; fallback por ancho si el tier es TV. */
    val isTv32: Boolean
        get() = isTv && maxWidth < 900.dp && profile.tier != DeviceProfileTier.TV_LARGE

    /** TV 42" / Fire / Television_1080 — [DeviceProfileTier.TV_REGULAR]. */
    val isTv42: Boolean
        get() = profile.tier == DeviceProfileTier.TV_REGULAR

    /**
     * Canvas LARGE del catálogo (Ariana / Damasco / todo TV_REGULAR).
     * Misma regla que CT/Productos vía [CatalogHeaderMetrics.isLargeCatalogCanvas].
     */
    val isTv42LargeCanvas: Boolean
        get() = CatalogHeaderMetrics.isLargeCatalogCanvas(profile, maxHeight)

    /** TV ~65–75" / 4K — [DeviceProfileTier.TV_LARGE]. */
    val isTv66: Boolean
        get() = profile.tier == DeviceProfileTier.TV_LARGE

    /** @deprecated Usar isTv42 — alias para compatibilidad en logs. */
    val isTv1080: Boolean
        get() = isTv42

    /** Perfil estable — TV antes que phone/tablet (961×529 = tv_42). */
    val deviceProfile: String
        get() = vitrinaProfileKey

    val vitrinaProfileKey: String
        get() = when {
            isTv32 -> "tv_32"
            profile.tier == DeviceProfileTier.TV_REGULAR -> "tv_42"
            profile.tier == DeviceProfileTier.TV_LARGE -> "tv_66"
            profile.tier == DeviceProfileTier.COMPACT_LANDSCAPE -> "phone_landscape"
            profile.tier == DeviceProfileTier.TABLET_LANDSCAPE -> "tablet_landscape"
            profile.tier == DeviceProfileTier.COMPACT_PORTRAIT -> "phone_portrait"
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
            "phone_landscape" -> 36.dp
            "tv_32" -> 40.dp
            "tv_42", "tablet_landscape" -> 44.dp
            "tv_66" -> 48.dp
            "tv_unknown" -> 44.dp
            else -> 36.dp
        }

    /** Slop mínimo antes de iniciar drag (igual en todos los perfiles). */
    fun dragPointerSlopPx(density: androidx.compose.ui.unit.Density): Float =
        with(density) { 3.dp.toPx() }

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

    /**
     * Top padding del botón Historia (TopEnd).
     * TV42: subir ~15%H vs baseline 44dp — justo bajo PRODUCTOS ESTRELLAS, a la derecha (como phone).
     */
    val historyButtonTopPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 2.dp
            "tv_42", "tablet_landscape" -> {
                val raised = 44.dp - maxHeight * 0.15f
                if (raised < 4.dp) 4.dp else raised
            }
            "tv_32" -> 33.dp
            "tv_66" -> 44.dp
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
            "tv_42", "tv_66", "tablet_landscape" -> 0.08f
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

    /** Badge verde inline "PRODUCTOS ESTRELLAS". */
    val bubblesBadgeHeight: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 26.dp
            "phone_portrait" -> 28.dp
            // Damasco: 43. Fire/TV1080: +10% vs 26 → 29. Otros tablet: 30.
            "tv_42", "tablet_landscape" -> when {
                isTv42LargeCanvas -> 43.dp
                isTv42 -> 29.dp
                else -> 30.dp
            }
            "tv_66" -> 64.dp
            "tv_32" -> 34.dp
            "short_height" -> 26.dp
            "expanded" -> 36.dp
            "tv_unknown" -> 36.dp
            else -> 28.dp
        }

    /** @deprecated El badge ya no usa fracción de ancho de pantalla (va inline). */
    val bubblesBadgeWidthFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.371f
            "phone_portrait" -> 0.42f
            "tv_42", "tv_66", "tablet_landscape" -> 0.52f
            "tv_32" -> 0.25f
            "short_height" -> 0.38f
            "expanded" -> 0.28f
            "tv_unknown" -> 0.25f
            else -> 0.30f
        }

    /** Sube el badge Historia (comparte anclaje tope con la barra de estrellas). */
    val bubblesBadgeTopPullUp: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 4.dp
            "phone_portrait" -> 4.dp
            // 1% más abajo que el calibrado previo (32.dp).
            "tv_42", "tv_66", "tablet_landscape" -> 32.dp - maxHeight * 0.01f
            "tv_32", "tv_unknown" -> 0.dp
            "short_height" -> 4.dp
            "expanded" -> 2.dp
            else -> 2.dp
        }

    /**
     * Offset horizontal del centro del badge "PRODUCTOS ESTRELLAS" dentro de la fila
     * de burbujas (coords del área 3D, origen = borde izquierdo de la fila).
     * El centro del badge se alinea con el centro de la columna de redes.
     * En [VitrinaBubblesRow] se resta la mitad del ancho medido del badge.
     */
    val bubblesBadgeCenterXInRow: Dp
        get() = socialStartPadding + socialIconSize / 2f -
            maxWidth * vitrinaInsetStartFraction - dragHandleWidth

    /** @deprecated Usar [bubblesBadgeCenterXInRow] + mitad del ancho medido. */
    val bubblesBadgeStartOffset: Dp
        get() = bubblesBadgeCenterXInRow

    /** Grosor de la línea verde que une badge → burbujas. */
    val bubblesConnectorStroke: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 2.dp
            "tv_42", "tablet_landscape" -> 3.dp
            "tv_66" -> 3.5.dp
            else -> 2.5.dp
        }

    /** Puntitos decorativos entre burbujas. */
    val bubblesDotSize: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 7.dp
            "tv_42", "tablet_landscape" -> 11.dp
            "tv_66" -> 14.dp
            else -> 9.dp
        }

    /**
     * Ancho del cluster de burbujas relativo a la columna 3D (como antes del rediseño).
     * El badge vive aparte a la izquierda; no empuja las burbujas.
     */
    val bubblesRowWidthFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.56f
            "phone_portrait" -> 0.60f
            "tv_42", "tablet_landscape" -> if (isTv42LargeCanvas) 0.78f else 0.72f
            "tv_66" -> 0.78f
            "tv_32" -> 0.60f
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
            // Infinix: −15.dp subir bloque PRODUCTOS ESTRELLAS + burbujas.
            "phone_landscape" -> (2.dp + maxHeight * 0.05f) - 15.dp
            "phone_portrait" -> 8.dp
            // −10.dp: subir un chilín el bloque PRODUCTOS ESTRELLAS + burbujas.
            "tv_42", "tablet_landscape" -> 20.dp + maxHeight * 0.06f
            // TV66: aire flotante vs cilindro (−3%H vs 0.11 previo); −10.dp igual que TV42.
            "tv_66" -> 20.dp + maxHeight * 0.08f
            "tv_32" -> 10.dp
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
            // Damasco/Ariana LARGE: 109 −5%. Fire/TV1080: 70 −5%. Otros tablet: 80.
            "tv_42", "tablet_landscape" -> when {
                isTv42LargeCanvas -> 109.dp * 0.95f
                isTv42 -> 70.dp * 0.95f
                else -> 80.dp
            }
            // TV66: +50% vs baseline 89 (+20% sobre el 1.25 previo, para verlo en emulador).
            "tv_66" -> 89.dp * 1.50f
            "tv_32" -> 81.dp
            "short_height" -> 78.dp
            "expanded" -> 84.dp
            "tv_unknown" -> 87.dp
            else -> 81.dp
        }

    val bubbleSpacing: Dp
        get() = when (vitrinaProfileKey) {
            // Infinix: más aire para que se vea la línea entre puntitos.
            "phone_landscape" -> 18.dp
            "phone_portrait" -> 5.dp
            "tv_42", "tablet_landscape" -> 28.dp
            // TV66: baseline 28 + 5% del diámetro a cada lado.
            "tv_66" -> 28.dp + (89.dp * 1.50f) * 0.05f * 2f
            "tv_32" -> 7.dp
            "short_height" -> 4.dp
            "expanded" -> 7.dp
            "tv_unknown" -> 8.dp
            else -> 5.dp
        }

    /** Miniatura ~78% del diámetro: producto grande con poco margen (referencia). */
    val bubbleProductSizeFraction: Float
        get() = when (vitrinaProfileKey) {
            "phone_landscape", "tv_42", "tv_66", "tablet_landscape" -> 0.78f
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

    /**
     * Inset derecho del rail BadgeHistoria + logo vertical.
     * Mayor = más hacia la izquierda (columna compartida más adentro).
     */
    val logoEndPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> maxWidth * 0.055f
            "tv_66" -> maxWidth * 0.048f
            // Fire/TV1080: 0.07. Damasco/otros tablet: 0.052.
            "tv_42", "tablet_landscape" -> when {
                isTv42 && !isTv42LargeCanvas -> maxWidth * 0.07f
                else -> maxWidth * 0.052f
            }
            "tv_32", "expanded" -> maxWidth * 0.052f
            else -> maxWidth * 0.05f
        }

    /**
     * Badge Historia (BadgeHistoria.png + Historia.gif), mismo patrón que clases terapéuticas.
     * Aspecto del PNG: 459×480.
     */
    val historiaBadgeHeight: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 52.dp
            "short_height" -> 58.dp
            // TV42/tablet: +28% baseline; Damasco canvas alto +40%.
            // Fire/Television_1080: −5% adicional sobre el badge (feedback pergamino).
            "tv_32" -> 72.dp * 1.20f
            "tv_42", "tablet_landscape" -> when {
                isTv42LargeCanvas -> 72.dp * 1.40f
                isTv42 -> 72.dp * 1.28f * 0.95f // Fire/TV1080 −5%
                else -> 72.dp * 1.28f
            }
            // TV66: mismo ancho que el logo vertical (dos bloques alineados).
            "tv_66" -> {
                val logoAspect = 229f / 1004f
                val badgeAspect = 459f / 480f
                maxHeight * logoAspect / (badgeAspect + logoAspect)
            }
            "expanded" -> 80.dp
            "tv_unknown" -> 74.dp
            else -> 64.dp
        }

    val historiaBadgeWidth: Dp
        get() = historiaBadgeHeight * (459f / 480f)

    val historiaBadgeIconSize: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 34.dp
            "short_height" -> 40.dp
            "tv_32" -> 48.dp * 1.20f
            "tv_42", "tablet_landscape" -> when {
                isTv42LargeCanvas -> 48.dp * 1.40f
                isTv42 -> 48.dp * 1.28f * 0.95f
                else -> 48.dp * 1.28f
            }
            "tv_66" -> historiaBadgeHeight * 0.58f
            "expanded" -> 54.dp
            "tv_unknown" -> 50.dp
            else -> 44.dp
        }

    /**
     * Inset superior del gif (pergamino) dentro del badge.
     * Solo Fire/TV1080: más bajo para centrarlo. Damasco/TV66/Infinix: sin cambio.
     */
    val historiaBadgeIconTop: Dp
        get() = when {
            isTv42 && !isTv42LargeCanvas -> historiaBadgeHeight * 0.22f
            else -> historiaBadgeHeight * 0.10f
        }

    /** Redes Intro — TV42/Fire/Damasco unificados (LARGE). */
    val socialIconSize: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 24.dp * 1.30f
            "tv_42", "tablet_landscape" -> 56.dp // 62 −10%
            "tv_66" -> rotateButtonSize
            "tv_32" -> 46.dp
            else -> if (isCompactWidth) 30.dp else 37.dp
        }

    /** Espacio vertical entre iconos de redes sociales. */
    val socialIconSpacing: Dp
        get() = when (vitrinaProfileKey) {
            "tv_66" -> 42.dp
            // TV42 unificado (Fire/Damasco LARGE): mismo spacing.
            "tv_42", "tablet_landscape" -> 34.dp
            else -> 30.dp
        }

    /**
     * Pull-up del rail BadgeHistoria + logo (mismo criterio que PRODUCTOS ESTRELLAS).
     * TV42/tablet: −3%H vs pull-up de estrellas (antes −5%; se subió 2% porque se bajó de más).
     * Phone: −1%H para que el icono del badge no se corte arriba.
     */
    val historiaRailTopPullUp: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> bubblesBadgeTopPullUp - maxHeight * 0.01f
            "tv_42", "tablet_landscape" -> {
                val lowered = bubblesBadgeTopPullUp - maxHeight * 0.03f
                if (lowered < 0.dp) 0.dp else lowered
            }
            else -> bubblesBadgeTopPullUp
        }

    /**
     * Inset izquierdo de redes.
     * Fire/TV1080: un poco más a la derecha para alinear centro con PRODUCTOS ESTRELLAS
     * (el badge sigue el mismo eje vía [bubblesBadgeCenterXInRow]).
     */
    val socialStartPadding: Dp
        get() = when {
            isTv42 && !isTv42LargeCanvas -> maxWidth * 0.095f
            else -> maxWidth * 0.10f
        }

    /**
     * Desplazamiento vertical de redes (CenterStart). En TV66 se alinea con el botón Gira.
     * (+ = abajo).
     */
    val socialCenterYOffset: Dp
        get() = when (vitrinaProfileKey) {
            // Infinix: bajar ~10%H para centrar respecto a la vitrina (antes quedaban muy arriba).
            "phone_landscape" -> maxHeight * 0.10f
            "tv_66" -> {
                val paddedCenterShift = -(maxHeight * vitrinaInsetBottomFraction * 0.5f)
                paddedCenterShift +
                    maxHeight * vitrinaVerticalBias +
                    vitrinaVerticalOffsetAdjustment +
                    vitrinaCylinderNudgeDown +
                    rotateButtonCenterYOffset
            }
            else -> 0.dp
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
            // Fire: subir un poco más (−54). Damasco canvas alto: −62.
            "tv_42", "tablet_landscape" -> if (isTv42LargeCanvas) (-62).dp else (-54).dp
            // TV66: baseline -42 + subir 75px @ dens 320 (= 37.5 dp).
            "tv_66" -> (-42).dp - 37.5.dp
            "phone_landscape" -> 0.dp
            else -> 0.dp
        }

    /**
     * Baja solo el cilindro 3D (+ botón Gira) sin mover las burbujas.
     * Usa el espacio vacío inferior; no pegar al bottom.
     * Negativo = subir el cilindro (las burbujas cancelan este nudge).
     */
    val vitrinaCylinderNudgeDown: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 10.dp
            // Fire: menos nudge = cilindro más arriba. Damasco ya en 0.07.
            "tv_42", "tablet_landscape" ->
                if (isTv42LargeCanvas) maxHeight * 0.07f else maxHeight * 0.08f
            // TV66: baseline 0.10 → subir 7% → bajar 4% ⇒ nudge 0.07H (burbujas no se mueven).
            "tv_66" -> maxHeight * 0.07f
            else -> 0.dp
        }

    /**
     * Altura visual de la vitrina (misma caja 3D que [IntroVitrina] × factor de framing
     * del cilindro). El logo La Santé debe medir esto para quedar al ras tope/base.
     */
    val vitrinaVisualHeight: Dp
        get() {
            val sceneH = maxHeight * sceneHeightFraction
            val fill = when (vitrinaProfileKey) {
                "phone_landscape" -> 0.82f
                // Fire: más corto. Damasco: un poco menos para que la é no se salga del tope.
                "tv_42", "tablet_landscape" -> if (isTv42LargeCanvas) 0.74f else 0.72f
                "tv_66" -> 0.76f
                else -> 0.78f
            }
            val trim = when {
                vitrinaProfileKey == "tv_42" || vitrinaProfileKey == "tablet_landscape" ->
                    if (isTv42LargeCanvas) 6.dp else 6.dp
                else -> 3.dp
            }
            return ((sceneH * fill) - trim).coerceAtLeast(48.dp)
        }

    /**
     * Offset Y del centro de la vitrina vs centro de pantalla
     * (misma fórmula que el cilindro en IntroVitrina).
     */
    val vitrinaCenterYOffset: Dp
        get() = maxHeight * vitrinaVerticalBias +
            vitrinaVerticalOffsetAdjustment +
            vitrinaCylinderNudgeDown

    /**
     * Resta altura al logo vertical dentro del rail (dp).
     * Phone: 10. Damasco: 45 (badge más grande + logo un poco más corto).
     */
    val verticalLogoHeightReduction: Dp
        get() = when {
            isPhoneLandscape -> 10.dp
            isTv42LargeCanvas -> 45.dp
            else -> 0.dp
        }

    /**
     * @deprecated Preferir [vitrinaVisualHeight] para el logo.
     */
    val verticalLogoMaxHeight: Dp
        get() = vitrinaVisualHeight

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
            // Infinix: gira = touch (mismo tamaño; antes gira se veía mucho más grande).
            "phone_landscape" -> 42.dp
            "short_height" -> 64.dp
            // Fire/TV1080: −5%. Damasco: 78. Otros tablet: 78.
            "tv_42", "tablet_landscape" -> when {
                isTv42LargeCanvas -> 78.dp
                isTv42 -> 78.dp * 0.95f
                else -> 78.dp
            }
            "tv_32" -> 78.dp
            // TV66: +30% vs baseline 78 (gira / historia / touch).
            "tv_66" -> 78.dp * 1.30f
            "expanded" -> 88.dp
            "tv_unknown" -> 80.dp
            else -> 70.dp
        }

    /** Touch y gira comparten tamaño en todos los perfiles. */
    val touchHintSize: Dp
        get() = rotateButtonSize

    /** Padding inferior del hint touch.gif (sobre el cintillo frontal). */
    val touchHintBottomPadding: Dp
        get() = when (vitrinaProfileKey) {
            // Infinix: −2.dp más abajo dentro del cintillo.
            "phone_landscape" -> maxHeight * 0.100f - 2.dp
            // Fire/TV1080: −7.dp (bajar touch ~2.dp más). Damasco/otros: sin ese offset.
            "tv_42", "tablet_landscape" ->
                if (isTv42 && !isTv42LargeCanvas) maxHeight * 0.055f - 7.dp
                else maxHeight * 0.055f
            "tv_32", "tv_66" -> maxHeight * 0.055f
            else -> maxHeight * 0.04f
        }

    /**
     * Offset horizontal del touch desde el centro del cintillo frontal.
     * Negativo = izquierda. Tablet/TV/Fire: −1%W respecto a la calibración anterior.
     */
    val touchHintCenterXOffset: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> -(maxWidth * 0.10f)
            "tv_66" -> -(maxWidth * 0.10f)
            "tv_32", "tv_42", "tablet_landscape" -> -(maxWidth * 0.11f)
            else -> -(maxWidth * 0.09f)
        }

    /** Borde derecho del cilindro 3D — botón Nuestra Historia (padding ≥ 0). */
    val vitrinaOverlayEndPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape", "tv_32", "tv_42", "tablet_landscape" -> 0.dp
            "tv_66" -> 4.dp
            else -> 4.dp
        }

    /** Ajuste fino horizontal del botón Nuestra Historia (+ = hacia la derecha). */
    val vitrinaOverlayEndOffset: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 2.dp
            // TV42/tablet: más a la derecha, estilo phone.
            "tv_42", "tablet_landscape" -> 8.dp
            else -> 0.dp
        }

    /**
     * Inset desde el borde derecho de la escena hasta el botón Gira.
     * Mayor = más hacia el cilindro (encima de la vitrina, de lado).
     */
    val rotateButtonEndPadding: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> 0.dp
            "tv_32" -> 36.dp
            "tv_42", "tablet_landscape" -> 88.dp
            // TV66: en captura quedaba en el aire a la derecha — más adentro.
            "tv_66" -> 168.dp
            else -> 24.dp
        }

    /**
     * Ajuste fino horizontal del botón girar (+ = derecha/afuera, − = izquierda/hacia cilindro).
     * Anclado al borde de la escena completa (no a la columna 3D).
     */
    val rotateButtonProtrudeOffset: Dp
        get() = when (vitrinaProfileKey) {
            "phone_landscape" -> {
                // Más adentro: encima del lado derecho de la vitrina.
                val pullTowardCylinder = maxWidth * 0.16f
                val pull = if (pullTowardCylinder > 72.dp) pullTowardCylinder else 72.dp
                -(pull + dragHandleWidth) - 8.dp
            }
            // Damasco: −18. Fire/TV1080: −6 (+2 vs −8). Otros tablet: +12 (baseline previo).
            "tv_42", "tablet_landscape" -> when {
                isTv42LargeCanvas -> (-18).dp
                isTv42 -> (-6).dp
                else -> 12.dp
            }
            "tv_66" -> (-36).dp
            "tv_32" -> (-14).dp
            else -> (-8).dp
        }

    /**
     * Ms para un giro idle completo (360°).
     * Mismos °/s se ven más rápidos en pantallas grandes (más px en el borde);
     * por eso tablet/TV usan duración mayor que phone.
     */
    val idleFullRotationMs: Int
        get() = when (vitrinaProfileKey) {
            "phone_landscape", "phone_portrait" -> 60_000
            "tv_32" -> 85_000
            "tv_42", "tablet_landscape" -> 95_000
            "tv_66" -> 110_000
            else -> 75_000
        }

    /** Desplazamiento vertical del botón girar hacia el estante medio (+ = abajo). */
    val rotateButtonCenterYOffset: Dp
        get() = when (vitrinaProfileKey) {
            // Infinix: +3.dp previo + 2.dp más.
            "phone_landscape" -> maxHeight * sceneHeightFraction * 0.01f + 5.dp
            "tv_32" -> maxHeight * sceneHeightFraction * 0.02f
            // Damasco: base. Fire/TV1080: +3.dp (+1 vs +2) para alinear con la línea del estante.
            "tv_42", "tablet_landscape" -> {
                val base = maxHeight * sceneHeightFraction * 0.035f - 5.dp
                if (isTv42 && !isTv42LargeCanvas) base + 3.dp else base
            }
            "tv_66" -> maxHeight * sceneHeightFraction * 0.035f - 5.dp
            "expanded" -> maxHeight * sceneHeightFraction * 0.04f
            else -> maxHeight * sceneHeightFraction * 0.04f
        }
}

@Composable
fun BoxWithConstraintsScope.introLayoutMetrics(
    widthClass: WindowWidthSizeClass,
): IntroLayoutMetrics {
    val density = LocalDensity.current
    val context = LocalContext.current
    val preferTv66 = TvProfileDetector.isTv66Candidate(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        density = density,
        context = context,
    )
    return remember(maxWidth, maxHeight, widthClass, preferTv66) {
        val profile = DeviceProfileResolver.resolve(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            widthClass = widthClass,
            preferTv66 = preferTv66,
        )
        IntroLayoutMetrics(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            widthClass = widthClass,
            profile = profile,
            preferTv66 = preferTv66,
        )
    }
}
