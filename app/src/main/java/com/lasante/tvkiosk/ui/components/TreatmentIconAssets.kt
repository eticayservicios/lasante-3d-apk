package com.lasante.tvkiosk.ui.components

import java.text.Normalizer
import java.util.Locale

/**
 * Resuelve iconos de clase terapéutica.
 * Fuente canónica: URL remota CloudFront (`media.icono`). Sin assets locales.
 */
object TreatmentIconAssets {
    fun resolve(iconUrl: String?): String? {
        val trimmed = iconUrl?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return normalizeCdnIconUrl(trimmed)
    }

    /**
     * Hospitalaria aún publica estas clases sin media.icono. Mientras el
     * catálogo se completa, usa los iconos terapéuticos equivalentes.
     */
    fun resolve(id: String? = null, name: String? = null, iconUrl: String? = null): String? =
        resolve(iconUrl) ?: fallbackIconUrl(id, name)

    private fun fallbackIconUrl(id: String?, name: String?): String? {
        val key = normalizeKey(listOfNotNull(id, name).joinToString(" "))
        val asset = when {
            key.contains("anestesia") -> "sistema-nervioso-central"
            key.contains("cateter") -> "cardiovascular"
            key.contains("cuidado-de-piel") -> "7%20(1)"
            key.contains("soluciones-y-nutricion") -> "digestivo-metabolico"
            key.contains("suturas") -> "musculoesqueletico"
            else -> return null
        }
        return "https://catalog-assets.gurusaws.com/png/tratamientos/" + asset + ".png"
    }

    private fun normalizeKey(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim(45.toChar())

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
