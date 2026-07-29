package com.lasante.tvkiosk.ui.components

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.SharedModalMetrics
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.screens.intro.VitrinaAssets
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.widgets.ModelViewerStub
import kotlinx.coroutines.delay

private const val MODAL_3D_DEFER_MS = 180L

private data class ProductModalMetrics(
    val modalWidthFraction: Float,
    val modalHeightFraction: Float,
    val modelWeight: Float,
    val descriptionWeight: Float,
    val descriptionHeightFraction: Float,
    val columnSpacing: Dp,
    val rowOffsetX: Dp,
    val rowOffsetY: Dp,
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
    descriptionOffsetY = descriptionOffsetY,
    descriptionHorizontalPadding = descriptionHorizontalPadding,
    descriptionTextAlign = TextAlign.Center,
    modelScaleToUnits = modelScaleToUnits,
    descriptionAlignTop = descriptionAlignTop,
    alignRowTop = alignRowTop,
)

@Composable
private fun rememberProductModalMetrics(widthClass: WindowWidthSizeClass?): ProductModalMetrics {
    val configuration = LocalConfiguration.current
    val maxWidth = configuration.screenWidthDp.dp
    val maxHeight = configuration.screenHeightDp.dp
    val profile = remember(maxWidth, maxHeight, widthClass) {
        DeviceProfileResolver.resolve(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            widthClass = widthClass ?: WindowWidthSizeClass.Compact,
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

    LaunchedEffect(product.productoId, modelUrl) {
        Log.d(
            "ProductModal3D",
            "product=${product.productoId} name=${product.nombre} " +
                "glb=${product.media.modelo3d.glb} " +
                "glbAbrircaja=${product.media.modelo3d.glbAbrircaja} " +
                "glbFrasco=${product.media.modelo3d.glbFrasco} selected=$modelUrl",
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
                .fillMaxHeight(if (isCompact) 0.82f else layout.modalHeightFraction),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(if (isCompact) 18.dp else 22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.25f),
                        )
                        ProductDescriptionPanel(
                            product = product,
                            businessUnitName = businessUnitName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            compact = true,
                        )
                    }
                } else {
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
                                modifier = Modifier
                                    .weight(layout.modelWeight)
                                    .fillMaxHeight(),
                                scaleToUnits = layout.modelScaleToUnits,
                            )
                            ProductDescriptionPanel(
                                product = product,
                                businessUnitName = businessUnitName,
                                modifier = Modifier
                                    .weight(layout.descriptionWeight)
                                    .fillMaxHeight(layout.descriptionHeightFraction)
                                    .offset(y = layout.descriptionOffsetY),
                                horizontalPadding = layout.descriptionHorizontalPadding,
                                descriptionTextAlign = layout.descriptionTextAlign,
                                alignContentTop = layout.descriptionAlignTop,
                            )
                        }
                    }
                }
            }
            ProductModalCloseButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = if (isCompact) 6.dp else 10.dp,
                        y = if (isCompact) (-12).dp else (-16).dp,
                    )
                    .zIndex(10f),
                size = if (isCompact) 38.dp else 40.dp,
            )
        }
    }
}

@Composable
fun ProductPresentationContent(
    product: Product,
    businessUnitName: String? = null,
    modifier: Modifier = Modifier,
) {
    val modelUrl = remember(product) {
        product.resolvePresentationGlb()
    }

    Row(
        modifier = modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProductModelStage(
            modelUrl = modelUrl,
            modifier = Modifier
                .weight(1.42f)
                .fillMaxHeight(),
        )
        ProductDescriptionPanel(
            product = product,
            businessUnitName = businessUnitName,
            modifier = Modifier
                .weight(0.88f)
                .fillMaxHeight(0.88f),
        )
    }
}

private fun Product.resolvePresentationGlb(): String? {
    val model = media.modelo3d
    return model.glb?.trim()?.takeIf { it.isNotBlank() }
        ?: model.glbAbrircaja?.trim()?.takeIf { it.isNotBlank() }
        ?: model.glbFrasco?.trim()?.takeIf { it.isNotBlank() }
        ?: VitrinaAssets.resolveProductGlb(this, 0)
}

@Composable
private fun ProductModelStage(
    modelUrl: String?,
    modifier: Modifier = Modifier,
    scaleToUnits: Float = 1.38f,
) {
    var renderModel by remember(modelUrl) { mutableStateOf(false) }
    LaunchedEffect(modelUrl) {
        renderModel = false
        delay(MODAL_3D_DEFER_MS)
        renderModel = true
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (renderModel) {
            ModelViewerStub(
                modifier = Modifier.fillMaxSize(),
                modelUrl = modelUrl,
                scaleToUnits = scaleToUnits,
            )
        } else {
            CircularProgressIndicator(color = LaSanteGreen)
        }
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

@Composable
private fun ProductDescriptionPanel(
    product: Product,
    businessUnitName: String?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    horizontalPadding: Dp? = null,
    descriptionTextAlign: TextAlign? = null,
    alignContentTop: Boolean = false,
    onClose: (() -> Unit)? = null,
) {
    val panelHorizontalPadding = horizontalPadding ?: if (compact) 18.dp else 30.dp
    val panelDescriptionAlign = descriptionTextAlign ?: if (compact) TextAlign.Start else TextAlign.Center
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 18.dp else 20.dp))
            .background(Color.White.copy(alpha = if (compact) 0.42f else 0.34f))
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = panelHorizontalPadding,
                vertical = if (compact) 16.dp else 24.dp,
            ),
        verticalArrangement = if (alignContentTop || compact) Arrangement.Top else Arrangement.Center,
    ) {
        ProductModalHeader(
            product = product,
            onClose = onClose,
            centered = false,
            showClose = false,
        )

        if (!businessUnitName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = businessUnitName,
                color = LaSanteText.copy(alpha = 0.58f),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = if (compact) 12.sp else 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

        Text(
            text = product.description.ifBlank { "Sin descripción disponible." },
            modifier = Modifier.fillMaxWidth(),
            color = LaSanteText.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (compact) 14.sp else 17.sp,
            lineHeight = if (compact) 19.sp else 24.sp,
            textAlign = panelDescriptionAlign,
        )

        val visibleAttributes = product.atributos.filterKeys { it.shouldShowInProductModal() }

        if (visibleAttributes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(if (compact) 12.dp else 18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                visibleAttributes.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        color = LaSanteText.copy(alpha = 0.82f),
                        fontSize = if (compact) 12.sp else 14.sp,
                        lineHeight = if (compact) 16.sp else 20.sp,
                    )
                }
            }
        }
        if (alignContentTop) {
            Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))
        }
    }
}

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
private fun ProductModalHeader(
    product: Product,
    onClose: (() -> Unit)?,
    centered: Boolean,
    showClose: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(min = 0.dp),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            Text(
                text = product.name,
                color = LaSanteText,
                style = MaterialTheme.typography.titleLarge,
                fontSize = if (centered) 22.sp else 26.sp,
                lineHeight = if (centered) 26.sp else 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(if (centered) 8.dp else 10.dp))
            Box(
                modifier = Modifier
                    .width(if (centered) 72.dp else 92.dp)
                    .height(if (centered) 5.dp else 6.dp)
                    .background(LaSanteGreen, CircleShape),
            )
        }
        if (showClose && onClose != null) {
            ProductModalCloseButton(onClick = onClose)
        }
    }
}

@Composable
private fun ProductModalCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.5.dp, LaSanteGreen.copy(alpha = 0.35f), CircleShape),
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = LaSanteGreen,
            modifier = Modifier.size(size * 0.48f),
        )
    }
}
