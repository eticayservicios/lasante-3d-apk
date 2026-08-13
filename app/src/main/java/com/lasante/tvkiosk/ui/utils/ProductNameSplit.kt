package com.lasante.tvkiosk.ui.utils

/**
 * Separa nombre de concentración/presentación.
 * Ej: "Cetirizina 10 mg" → ("Cetirizina", "10 mg");
 *     "Fexofenadina - Suspensión" → ("Fexofenadina", "Suspensión").
 */
fun splitProductTitleAndStrength(rawName: String): Pair<String, String?> {
    val name = rawName.trim()
    if (name.isEmpty()) return "" to null

    val dosageMatch = DOSAGE_SUFFIX.find(name)
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

private val DOSAGE_SUFFIX = Regex(
    """^(.+?)\s+(\d+[.,]?\d*(?:\s*[-–/]\s*\d+[.,]?\d*)?\s*(?:mg|g|ml|mcg|µg|ui|%))\s*$""",
    RegexOption.IGNORE_CASE,
)
