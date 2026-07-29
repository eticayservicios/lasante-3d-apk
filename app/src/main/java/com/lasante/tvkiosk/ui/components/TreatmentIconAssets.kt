package com.lasante.tvkiosk.ui.components

import java.text.Normalizer
import java.util.Locale

object TreatmentIconAssets {
    private const val BASE_PATH = "file:///android_asset/svg/clases-terapeuticas"
    private const val GENERIC_SLUG = "generico"

    private val aliases = linkedMapOf(
        "alergorespiratoria" to "alergo-respiratorio",
        "alergorespiratorio" to "alergo-respiratorio",
        "respiratoria" to "alergo-respiratorio",
        "respiratorio" to "alergo-respiratorio",
        "antiinfeccioso" to "antiinfeccioso",
        "antiinfecciosa" to "antiinfeccioso",
        "infeccioso" to "antiinfeccioso",
        "cardiovascular" to "cardiovascular",
        "dermatologico" to "dermatologico",
        "dermatologica" to "dermatologico",
        "digestivometabolico" to "digestivo-metabolico",
        "digestivometabolica" to "digestivo-metabolico",
        "metabolico" to "digestivo-metabolico",
        "musculoesqueletico" to "musculoesqueletico",
        "musculoesqueletica" to "musculoesqueletico",
        "sistemanerviosocentral" to "sistema-nervioso-central",
        "nerviosocentral" to "sistema-nervioso-central",
    )

    fun resolve(id: String? = null, name: String? = null, iconUrl: String? = null): String {
        val trimmedIconUrl = iconUrl?.trim()?.takeIf { it.isNotBlank() }
        if (trimmedIconUrl != null) {
            return normalizeCdnIconUrl(trimmedIconUrl)
        }

        val localSlug = sequenceOf(id, name)
            .filterNotNull()
            .map(::normalize)
            .firstNotNullOfOrNull { normalized ->
                aliases.entries.firstOrNull { (alias, _) -> normalized.contains(alias) }?.value
            }

        return localSlug?.let { "$BASE_PATH/$it.png" }
            ?: "$BASE_PATH/$GENERIC_SLUG.png"
    }

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

    private fun normalize(value: String): String {
        val withoutAccents = Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return withoutAccents
            .lowercase(Locale.ROOT)
            .replace("[^a-z0-9]+".toRegex(), "")
    }
}
