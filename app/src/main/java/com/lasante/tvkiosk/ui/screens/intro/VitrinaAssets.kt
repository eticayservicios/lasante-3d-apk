package com.lasante.tvkiosk.ui.screens.intro

import com.lasante.tvkiosk.data.Product

/** Resolución de rutas GLB del producto (modal de detalle). */
object VitrinaAssets {
    fun hasRenderableGlb(product: Product): Boolean =
        resolveProductGlb(product, 0) != null

    fun resolveProductGlb(product: Product?, slotIndex: Int): String? {
        return resolveProductGlb(product, slotIndex, normalizeForShelf = false)
    }

    fun resolveFeaturedShelfGlb(product: Product?, slotIndex: Int): String? {
        return resolveProductGlb(product, slotIndex, normalizeForShelf = true)
    }

    private fun resolveProductGlb(
        product: Product?,
        slotIndex: Int,
        normalizeForShelf: Boolean,
    ): String? {
        product ?: return null
        val shelfGlb = product.media.modelo3d.glbFrasco?.trim().orEmpty()
        val mainGlb = product.media.modelo3d.glb?.trim().orEmpty()
        val raw = when {
            shelfGlb.isNotEmpty() -> shelfGlb
            mainGlb.isNotEmpty() -> mainGlb
            else -> return null
        }
        return when {
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            raw.isNotEmpty() -> raw.removePrefix("file:///android_asset/").removePrefix("./")
            else -> null
        }
    }
}
