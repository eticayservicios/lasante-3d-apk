package com.lasante.tvkiosk.ui.components

/**
 * Resuelve iconos de clase terapéutica.
 * Fuente canónica: URL remota CloudFront (`media.icono`). Sin assets locales.
 */
object TreatmentIconAssets {
    fun resolve(iconUrl: String?): String? {
        val trimmed = iconUrl?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return normalizeCdnIconUrl(trimmed)
    }

    /** Compat: ignora id/name; solo CloudFront. */
    fun resolve(id: String? = null, name: String? = null, iconUrl: String? = null): String? =
        resolve(iconUrl)

    private fun normalizeCdnIconUrl(url: String): String {
        var normalized = url
        if (normalized.contains("/svg/tratamientos/", ignoreCase = true)) {
            normalized = normalized.replace("/svg/tratamientos/", "/png/tratamientos/", ignoreCase = true)
        }
        if (normalized.endsWith(".svg", ignoreCase = true)) {
            normalized = normalized.replace(Regex("\\.svg$", RegexOption.IGNORE_CASE), ".png")
        }
        return normalized
    }
}
