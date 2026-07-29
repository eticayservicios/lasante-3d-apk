package com.lasante.tvkiosk.data

import java.text.Normalizer
import java.util.Locale

/**
 * Convierte ids/slugs o nombres crudos del API en títulos legibles
 * (p. ej. `genericos-la-sante` → `Genéricos La Santé`).
 */
object DisplayTitles {

    private val knownUnits = mapOf(
        "genericos-la-sante" to "Genéricos La Santé",
        "genericos_lasante" to "Genéricos La Santé",
        "genericos-lasante" to "Genéricos La Santé",
        "medicina-general" to "Genéricos La Santé",
        "genericos" to "Genéricos La Santé",
        "primary-care" to "Primary Care",
        "primary" to "Primary Care",
        "specialty-care" to "Specialty Care",
        "specialty" to "Specialty Care",
        "phq-consumo" to "PHQ Consumo",
        "phq" to "PHQ Consumo",
        "hospital-care" to "Hospital Care",
        "hospital" to "Hospital Care",
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
