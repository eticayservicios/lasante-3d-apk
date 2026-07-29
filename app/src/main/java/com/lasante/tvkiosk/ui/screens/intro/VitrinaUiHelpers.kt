package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.data.VitrinaUnit

/** Posición en pantalla (0–1) de un slot o ancla de unidad. */
data class FeaturedSlotScreenPoint(
    val xFraction: Float,
    val yFraction: Float,
)

@Composable
fun rememberVitrinaInteractionAlpha(showProducts: Boolean): Float {
    val alpha by animateFloatAsState(
        targetValue = if (showProducts) 1f else 0f,
        animationSpec = VitrinaConstants.fadeAnimationSpec,
        label = "VitrinaInteractionAlpha",
    )
    return alpha
}

fun resolveActiveVitrinaUnit(
    vitrinaUnits: List<VitrinaUnit>,
    activeIndex: Int,
): Pair<Int, VitrinaUnit>? {
    if (vitrinaUnits.isEmpty()) return null
    val glbIndex = VitrinaGlbMapping.glbIndexFor(activeIndex)
    return resolveVitrinaUnitForGlbIndex(vitrinaUnits, glbIndex)
}

/**
 * Unidad de vitrina que corresponde a la cara [glbIndex] del cilindro,
 * independiente del orden en que venga el array del API.
 */
fun resolveVitrinaUnitForGlbIndex(
    vitrinaUnits: List<VitrinaUnit>,
    glbIndex: Int,
): Pair<Int, VitrinaUnit>? {
    if (vitrinaUnits.isEmpty()) return null
    val safeGlbIndex = VitrinaGlbMapping.glbIndexFor(glbIndex)
    val targetUnitId = VitrinaGlbMapping.navigationUnitIdFor(safeGlbIndex)
    val byId = vitrinaUnits.indexOfFirst { it.unit.id == targetUnitId }
    if (byId >= 0) return safeGlbIndex to vitrinaUnits[byId]
    val fallbackIndex = safeGlbIndex.coerceIn(0, vitrinaUnits.lastIndex)
    return safeGlbIndex to vitrinaUnits[fallbackIndex]
}

/** Productos destacados de la unidad activa — un producto por slot del API (sin colapsar por id). */
fun featuredProductsForUnit(activeUnit: VitrinaUnit): List<Product> =
    activeUnit.products.take(VitrinaConstants.SLOTS_PER_UNIT)

/** Clave única por slot de vitrina (mismo producto puede repetirse en varios slots). */
private fun Product.vitrinaSlotKey(): String =
    "${productoId}@${atributos["slot"] ?: orden}"

/** Productos indexados por slot visual 0–3 (backend slot 1–4). */
fun productsByVisualSlot(products: List<Product>): Array<Product?> {
    val result = arrayOfNulls<Product>(VitrinaConstants.SLOTS_PER_UNIT)
    products
        .distinctBy { it.vitrinaSlotKey() }
        .sortedBy { product ->
            product.atributos["slot"]?.toIntOrNull() ?: Int.MAX_VALUE
        }
        .forEachIndexed { listIndex, product ->
            val preferred = visualSlotIndex(product, listIndex)
            val targetIndex = when {
                preferred in result.indices && result[preferred] == null -> preferred
                else -> result.indexOfFirst { it == null }.takeIf { it >= 0 } ?: preferred
            }
            if (targetIndex in result.indices) {
                result[targetIndex] = product
            } else {
                VitrinaDebugLog.w(
                    "VitrinaBubbles",
                    "slotAssign SKIP product=${product.productoId} preferred=$preferred target=$targetIndex",
                )
            }
        }
    return result
}

/** Índice visual 0–3 según slot de vitrina (1–4). */
fun visualSlotIndex(product: Product, listIndex: Int): Int {
    val slotNum = product.atributos["slot"]?.toIntOrNull()
    val oneBased = when {
        slotNum == null || slotNum <= 0 -> listIndex + 1
        else -> slotNum
    }
    return (oneBased - 1).coerceIn(0, VitrinaConstants.SLOTS_PER_UNIT - 1)
}

/** Respeta el flag `modalEnabled` del slot en Admin > Vitrina. */
fun Product.isVitrinaModalEnabled(): Boolean =
    atributos["modalEnabled"]?.equals("false", ignoreCase = true) != true
