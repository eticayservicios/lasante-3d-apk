package com.lasante.tvkiosk.data

import java.text.Normalizer
import java.util.Locale

/**
 * Convierte ids/slugs o nombres crudos del API en títulos legibles
 * (p. ej. `genericos-la-sante` → `La Santé Genérico`).
 */
object DisplayTitles {

    private val knownUnits = mapOf(
        "genericos-la-sante" to "La Santé Genérico",
        "genericos_lasante" to "La Santé Genérico",
        "genericos-lasante" to "La Santé Genérico",
        "medicina-general" to "La Santé Genérico",
        "genericos" to "La Santé Genérico",
        "la-sante-generico" to "La Santé Genérico",
        "primary-care" to "Pharmetique",
        "primary" to "Pharmetique",
        "pharmetique" to "Pharmetique",
        "pharmatique" to "Pharmetique",
        "specialty-care" to "Oncológicas",
        "specialty" to "Oncológicas",
        "oncologicas" to "Oncológicas",
        "phq-consumo" to "OTC",
        "phq" to "OTC",
        "otc" to "OTC",
        "hospital-care" to "Hospitalaria",
        "hospital" to "Hospitalaria",
        "hospitalaria" to "Hospitalaria",
    )

    private val wordOverrides = mapOf(
        "genericos" to "Genéricos",
        "generico" to "Genérico",
        "sante" to "Santé",
        "la" to "La",
        "phq" to "PHQ",
        "primary" to "Primary",
        "specialty" to "Specialty",
        "hospital" to "Hospital",
        "care" to "Care",
        "consumo" to "Consumo",
        "medicina" to "Medicina",
        "general" to "General",
    )

    fun resolve(raw: String?, fallbackId: String? = null): String {
        val primary = raw?.trim().orEmpty()
        val fallback = fallbackId?.trim().orEmpty()
        knownUnits[normalizeKey(primary)]?.let { return it }
        knownUnits[normalizeKey(fallback)]?.let { return it }
        if (primary.isNotBlank() && !looksLikeSlug(primary)) {
            return titleCaseWords(primary)
        }
        val source = primary.ifBlank { fallback }
        if (source.isBlank()) return ""
        return formatSlug(source)
    }

    /** Reemplaza nombres legacy dentro de descripciones sin modificar IDs ni contenido restante. */
    fun replaceLegacyUnitNames(value: String): String =
        legacyUnitNameReplacements.fold(value) { result, (legacy, current) ->
            result.replace(legacy, current, ignoreCase = true)
        }

    private val legacyUnitNameReplacements = listOf(
        "Genéricos La Santé" to "La Santé Genérico",
        "Genericos La Sante" to "La Santé Genérico",
        "Primary Care" to "Pharmetique",
        "Specialty Care" to "Oncológicas",
        "PHQ Consumo" to "OTC",
        "Hospital Care" to "Hospitalaria",
    )

    private fun looksLikeSlug(value: String): Boolean {
        if (value.contains('-') || value.contains('_')) return true
        val letters = value.filter { it.isLetter() }
        return letters.isNotEmpty() && letters.all { it.isLowerCase() || it.isDigit() } &&
            !value.any { it.isWhitespace() }
    }

    private fun formatSlug(value: String): String {
        knownUnits[normalizeKey(value)]?.let { return it }
        return value
            .replace('_', '-')
            .split('-', ' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                val key = normalizeKey(token)
                wordOverrides[key] ?: token.lowercase(Locale.getDefault()).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
    }

    private fun titleCaseWords(value: String): String =
        value
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                val key = normalizeKey(word)
                wordOverrides[key] ?: word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }

    private fun normalizeKey(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.getDefault())
            .replace('_', '-')
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
}
