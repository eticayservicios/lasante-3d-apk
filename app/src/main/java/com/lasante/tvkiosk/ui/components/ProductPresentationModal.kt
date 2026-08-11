package com.lasante.tvkiosk.ui.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.FireTv42Spacing
import com.lasante.tvkiosk.ui.layout.SharedModalMetrics
import com.lasante.tvkiosk.ui.layout.TvProfileDetector
import com.lasante.tvkiosk.ui.screens.intro.VitrinaAssets
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteGreenDark
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.widgets.ModelViewerStub
import kotlinx.coroutines.delay

private const val MODAL_3D_DEFER_MS = 180L
private const val CLOSE_MODAL_ASSET = "file:///android_asset/vitrina/ui/close_modal.png"
private const val MODAL_DESCRIPTION_MAX_CHARS = 280
private const val MODAL_BULLETS_MAX = 5
/** Escala de la card blanca dentro de su columna. */
private const val DESCRIPTION_CARD_WIDTH_SCALE = 1.0f
private const val DESCRIPTION_CARD_HEIGHT_SCALE = 0.80f
/** Ampliar GLB ~3% respecto a la escala base del perfil. */
private const val MODEL_SCALE_BOOST = 1.03f
/** Reducción de ancho del card sólido (espacios teclado Poppins). */
private val DESCRIPTION_CARD_WIDTH_TRIM = FireTv42Spacing.spaces(8)

private data class ProductModalMetrics(
    val modalWidthFraction: Float,
    val modalHeightFraction: Float,
    val modelWeight: Float,
    val descriptionWeight: Float,
    val descriptionHeightFraction: Float,
    val columnSpacing: Dp,
    val rowOffsetX: Dp,
    val rowOffsetY: Dp,
    val descriptionOffsetX: Dp,
    val descriptionOffsetY: Dp,
    val descriptionHorizontalPadding: Dp,
    val descriptionTextAlign: TextAlign,
    val modelScaleToUnits: Float,
    val descriptionAlignTop: Boolean,
    val alignRowTop: Boolean,
)

private fun SharedModalMetrics.toProductModalMetrics() = ProductModalMetrics(
    modalWidthFraction = modalWidthFraction,
    modalHeightFraction = modalHeightFraction,
    modelWeight = modelWeight,
    descriptionWeight = descriptionWeight,
    descriptionHeightFraction = descriptionHeightFraction,
    columnSpacing = columnSpacing,
    rowOffsetX = rowOffsetX,
    rowOffsetY = rowOffsetY,
    descriptionOffsetX = descriptionOffsetX,
    descriptionOffsetY = descriptionOffsetY,
    descriptionHorizontalPadding = descriptionHorizontalPadding,
    descriptionTextAlign = TextAlign.Start,
    modelScaleToUnits = modelScaleToUnits,
    descriptionAlignTop = descriptionAlignTop,
    alignRowTop = alignRowTop,
)

@Composable
private fun rememberProductModalMetrics(widthClass: WindowWidthSizeClass?): ProductModalMetrics {
    val configuration = LocalConfiguration.current
    val maxWidth = configuration.screenWidthDp.dp
    val maxHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val context = LocalContext.current
    val preferTv66 = remember(maxWidth, maxHeight, density, context) {
        TvProfileDetector.isTv66Candidate(maxWidth, maxHeight, density, context)
    }
    val profile = remember(maxWidth, maxHeight, widthClass, preferTv66) {
        DeviceProfileResolver.resolve(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            widthClass = widthClass ?: WindowWidthSizeClass.Compact,
            preferTv66 = preferTv66,
        )
    }
    return remember(profile) {
        DeviceProfileResolver.modalMetrics(profile).toProductModalMetrics()
    }
}

@Composable
fun ProductPresentationModal(
    product: Product,
    businessUnitName: String? = null,
    widthClass: WindowWidthSizeClass? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)

    val isCompact = widthClass == WindowWidthSizeClass.Compact
    val layout = rememberProductModalMetrics(widthClass)
    val modelUrl = remember(product) {
        product.resolvePresentationGlb()
    }
    val fallbackImageUrl = remember(product) {
        product.resolvePresentationImage()
    }
    val closeSize = (if (isCompact) 38.dp else 40.dp) * 0.95f
    // Franja a la derecha del card para el close (top al lado, no arriba).
    val closeSideSlot = closeSize + FireTv42Spacing.spaces(2)

    LaunchedEffect(product.productoId, modelUrl, fallbackImageUrl) {
        Log.d(
            "ProductModal3D",
            "product=${product.productoId} name=${product.nombre} " +
                "glb=${product.media.modelo3d.glb} " +
                "glbAbrircaja=${product.media.modelo3d.glbAbrircaja} " +
                "glbFrasco=${product.media.modelo3d.glbFrasco} selected=$modelUrl " +
                "fallbackImage=$fallbackImageUrl",
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            )
            .zIndex(220f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isCompact) 0.94f else layout.modalWidthFraction)
                .fillMaxHeight(if (isCompact) 0.82f else layout.modalHeightFraction)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            if (isCompact) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProductModelStage(
                        modelUrl = modelUrl,
                        fallbackImageUrl = fallbackImageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.25f),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(DESCRIPTION_CARD_WIDTH_SCALE)
                                .padding(end = DESCRIPTION_CARD_WIDTH_TRIM)
                                .fillMaxHeight(DESCRIPTION_CARD_HEIGHT_SCALE),
                            verticalAlignment = Alignment.Top,
                        ) {
                            ProductDescriptionPanel(
                                product = product,
                                businessUnitName = businessUnitName,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                compact = true,
                            )
                            Box(
                                modifier = Modifier
                                    .width(closeSideSlot)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                ProductModalCloseButton(
                                    onClick = onClose,
                                    size = closeSize,
                                    modifier = Modifier.zIndex(12f),
                                )
                            }
                        }
                    }
                }
            } else {
                // Dos columnas independientes: GLB/miniatura | card (sin solape).
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = layout.rowOffsetX, y = layout.rowOffsetY),
                        verticalAlignment = if (layout.alignRowTop) {
                            Alignment.Top
                        } else {
                            Alignment.CenterVertically
                        },
                        horizontalArrangement = Arrangement.spacedBy(layout.columnSpacing),
                    ) {
                        ProductModelStage(
                            modelUrl = modelUrl,
                            fallbackImageUrl = fallbackImageUrl,
                            modifier = Modifier
                                .weight(layout.modelWeight)
                                .fillMaxHeight()
                                .clipToBounds(),
                            scaleToUnits = layout.modelScaleToUnits * MODEL_SCALE_BOOST,
                        )
                        Box(
                            modifier = Modifier
                                .weight(layout.descriptionWeight)
                                .fillMaxHeight(layout.descriptionHeightFraction)
                                .offset(
                                    x = layout.descriptionOffsetX,
                                    y = layout.descriptionOffsetY,
                                ),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(DESCRIPTION_CARD_WIDTH_SCALE)
                                    .padding(end = DESCRIPTION_CARD_WIDTH_TRIM)
                                    .fillMaxHeight(DESCRIPTION_CARD_HEIGHT_SCALE),
                                verticalAlignment = Alignment.Top,
                            ) {
                                ProductDescriptionPanel(
                                    product = product,
                                    businessUnitName = businessUnitName,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    horizontalPadding = layout.descriptionHorizontalPadding,
                                    descriptionTextAlign = layout.descriptionTextAlign,
                                    alignContentTop = layout.descriptionAlignTop,
                                )
                                Box(
                                    modifier = Modifier
                                        .width(closeSideSlot)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.TopCenter,
                                ) {
                                    ProductModalCloseButton(
                                        onClick = onClose,
                                        size = closeSize,
                                        modifier = Modifier.zIndex(12f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Product.resolvePresentationGlb(): String? {
    val model = media.modelo3d
    return model.glb?.trim()?.takeIf { it.isNotBlank() }
        ?: model.glbAbrircaja?.trim()?.takeIf { it.isNotBlank() }
        ?: model.glbFrasco?.trim()?.takeIf { it.isNotBlank() }
        ?: VitrinaAssets.resolveProductGlb(this, 0)
}

private fun Product.resolvePresentationImage(): String? {
    return media.imagenes2d.miniatura?.trim()?.takeIf { it.isNotBlank() }
        ?: media.imagenes2d.principal?.trim()?.takeIf { it.isNotBlank() }
        ?: media.modelo3d.vistaPrevia?.trim()?.takeIf { it.isNotBlank() }
}

@Composable
private fun ProductModelStage(
    modelUrl: String?,
    fallbackImageUrl: String? = null,
    modifier: Modifier = Modifier,
    scaleToUnits: Float = 1.38f,
) {
    val hasGlb = !modelUrl.isNullOrBlank()
    val context = LocalContext.current
    var renderModel by remember(modelUrl) { mutableStateOf(false) }
    LaunchedEffect(modelUrl, hasGlb) {
        if (!hasGlb) {
            renderModel = false
            return@LaunchedEffect
        }
        renderModel = false
        delay(MODAL_3D_DEFER_MS)
        renderModel = true
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            hasGlb && renderModel -> {
                ModelViewerStub(
                    modifier = Modifier.fillMaxSize(),
                    modelUrl = modelUrl,
                    scaleToUnits = scaleToUnits,
                )
            }
            hasGlb -> {
                CircularProgressIndicator(color = LaSanteGreen)
            }
            !fallbackImageUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(fallbackImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        if (hasGlb) {
            Text(
                text = "ARRASTRA PARA GIRAR EL PRODUCTO",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = LaSanteText.copy(alpha = 0.35f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProductDescriptionPanel(
    product: Product,
    businessUnitName: String?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    horizontalPadding: Dp? = null,
    descriptionTextAlign: TextAlign? = null,
    alignContentTop: Boolean = true,
) {
    val panelHorizontalPadding = horizontalPadding ?: if (compact) 18.dp else 28.dp
    val (titlePart, strengthPart) = remember(product.name, product.dosisDisplay) {
        val apiDosis = product.dosisDisplay
        if (!apiDosis.isNullOrBlank()) {
            product.name.trim() to apiDosis
        } else {
            // Fallback legado: dosis embutida en el nombre.
            splitModalTitleAndStrength(product.name)
        }
    }
    val bullets = remember(product.description, product.atributos) {
        product.modalBulletLines()
    }
    val corner = if (compact) 18.dp else 20.dp
    val cornerShape = RoundedCornerShape(corner)
    val titleBrush = Brush.horizontalGradient(
        listOf(LaSanteGreenDark, LaSanteGreen, Color(0xFFA8C829)),
    )
    val titleFontFamily = MaterialTheme.typography.bodyLarge.fontFamily
    // Sombra suave (mock): elevación difusa + leve sesgo arriba/derecha.
    val shadowElevation = if (compact) 12.dp else 20.dp

    Box(modifier = modifier) {
        // Capa de sombra desplazada (arriba + derecha), sin bloque sólido.
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = if (compact) 3.dp else 5.dp,
                    y = if (compact) (-2).dp else (-3).dp,
                )
                .shadow(
                    elevation = shadowElevation,
                    shape = cornerShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.16f),
                    spotColor = Color.Black.copy(alpha = 0.22f),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (compact) 8.dp else 14.dp,
                    shape = cornerShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.10f),
                    spotColor = Color.Black.copy(alpha = 0.18f),
                )
                .background(Color.White, cornerShape)
                .clip(cornerShape)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = panelHorizontalPadding,
                    end = panelHorizontalPadding,
                    top = if (compact) 20.dp else 28.dp,
                    bottom = if (compact) 18.dp else 24.dp,
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            // Título Poppins + verde degradado (mock / listado productos).
            Text(
                text = titlePart,
                style = TextStyle(
                    brush = titleBrush,
                    fontFamily = titleFontFamily,
                    fontSize = if (compact) 18.sp else 21.6.sp,
                    lineHeight = if (compact) 21.6.sp else 25.2.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = Color.Unspecified,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!strengthPart.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strengthPart,
                    color = LaSanteText,
                    fontSize = if (compact) 14.sp else 16.sp,
                    lineHeight = if (compact) 18.sp else 20.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!businessUnitName.isNullOrBlank() && compact) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = businessUnitName,
                    color = LaSanteText.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 14.dp else 18.dp))

            if (bullets.isEmpty()) {
                Text(
                    text = "Sin descripción disponible.",
                    color = LaSanteText.copy(alpha = 0.75f),
                    fontSize = if (compact) 13.sp else 14.sp,
                    lineHeight = if (compact) 18.sp else 20.sp,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)) {
                    bullets.forEachIndexed { index, line ->
                        ModalBulletLine(
                            text = line,
                            emphasizeLabel = index == 0 && line.contains(':'),
                            compact = compact,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalBulletLine(
    text: String,
    emphasizeLabel: Boolean,
    compact: Boolean,
) {
    val bodySize = if (compact) 13.sp else 14.sp
    val bodyLine = if (compact) 18.sp else 20.sp
    val annotated = remember(text, emphasizeLabel) {
        buildAnnotatedString {
            append("• ")
            val colon = text.indexOf(':')
            if (emphasizeLabel && colon in 1 until text.lastIndex) {
                val label = text.take(colon + 1)
                val rest = text.substring(colon + 1).trimStart()
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = LaSanteText,
                    ),
                ) {
                    append(label)
                }
                if (rest.isNotEmpty()) {
                    append(' ')
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = LaSanteText)) {
                        append(rest)
                    }
                }
            } else {
                withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = LaSanteText)) {
                    append(text)
                }
            }
        }
    }
    Text(
        text = annotated,
        fontSize = bodySize,
        lineHeight = bodyLine,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Bullets del modal: características (atributos) si hay; si no, frases de la descripción.
 */
private fun Product.modalBulletLines(): List<String> {
    val fromAttrs = atributos
        .filterKeys { it.shouldShowInProductModal() }
        .entries
        .take(MODAL_BULLETS_MAX)
        .map { (k, v) ->
            val value = v.trim()
            if (value.isEmpty()) k.trim() else "${k.trim()}: $value"
        }
        .filter { it.isNotBlank() }
    if (fromAttrs.isNotEmpty()) return fromAttrs

    val raw = description.trim()
    if (raw.isEmpty()) return emptyList()

    val byLines = raw
        .split('\n', '•', '●', '·')
        .map { it.trim().trimStart('-', '*', '–').trim() }
        .filter { it.isNotBlank() }
    if (byLines.size >= 2) {
        return byLines.take(MODAL_BULLETS_MAX).map { shortenModalText(it, 140) }
    }

    // Una sola descripción → partir por oraciones.
    val sentences = raw
        .split(Regex("(?<=[.!?])\\s+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
    return when {
        sentences.size >= 2 -> sentences.take(MODAL_BULLETS_MAX).map { shortenModalText(it, 160) }
        else -> listOf(shortenModalText(raw, MODAL_DESCRIPTION_MAX_CHARS))
    }
}

private fun shortenModalText(text: String, maxChars: Int): String {
    if (text.length <= maxChars) return text
    val cut = text.take(maxChars)
    val lastStop = maxOf(cut.lastIndexOf('.'), cut.lastIndexOf('!'), cut.lastIndexOf('?'))
    if (lastStop >= maxChars / 3) return cut.take(lastStop + 1).trim()
    val lastSpace = cut.lastIndexOf(' ')
    val base = if (lastSpace > maxChars / 4) cut.take(lastSpace) else cut
    return base.trimEnd(',', ';', ':', ' ') + "…"
}

private fun splitModalTitleAndStrength(rawName: String): Pair<String, String?> {
    val name = rawName.trim()
    if (name.isEmpty()) return "" to null

    val dosageMatch = MODAL_DOSAGE_SUFFIX.find(name)
    if (dosageMatch != null) {
        val title = dosageMatch.groupValues[1].trim().trimEnd('-', '–', '—').trim()
        val strength = dosageMatch.groupValues[2].trim()
        if (title.isNotEmpty()) return title to strength
    }

    val dashIndex = name.lastIndexOf(" - ")
    if (dashIndex > 0) {
        val title = name.substring(0, dashIndex).trim()
        val strength = name.substring(dashIndex + 3).trim()
        if (title.isNotEmpty() && strength.isNotEmpty()) return title to strength
    }

    return name to null
}

private val MODAL_DOSAGE_SUFFIX = Regex(
    """^(.+?)\s+(\d+[.,]?\d*(?:\s*[-–/]\s*\d+[.,]?\d*)?\s*(?:mg|g|ml|mcg|µg|ui|%))\s*$""",
    RegexOption.IGNORE_CASE,
)

private fun String.shouldShowInProductModal(): Boolean {
    val normalized = lowercase().replace("[^a-z0-9]+".toRegex(), "")
    return normalized !in setOf(
        "slot",
        "featuredslot",
        "featuredslotindex",
        "vitrinaslot",
        "slotindex",
        "destacadoslot",
        "modalenabled",
    )
}

@Composable
private fun ProductModalCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp * 0.95f,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = CLOSE_MODAL_ASSET,
            contentDescription = "Cerrar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}
