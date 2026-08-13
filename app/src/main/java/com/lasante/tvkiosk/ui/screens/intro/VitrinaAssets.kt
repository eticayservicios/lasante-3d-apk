package com.lasante.tvkiosk.ui.screens.intro

import com.lasante.tvkiosk.data.Product

/** Resolución de rutas GLB del producto (modal de detalle). */
object VitrinaAssets {
    fun resolveProductGlb(product: Product?, slotIndex: Int = 0): String? {
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
