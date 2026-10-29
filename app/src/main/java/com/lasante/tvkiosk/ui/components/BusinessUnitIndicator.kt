package com.lasante.tvkiosk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.data.DisplayTitles
import java.text.Normalizer
import java.util.Locale

/** Identidad visual canónica de una unidad de negocio. */
enum class BusinessUnitBrand(
    val color: Color,
    internal val aliases: Set<String>,
) {
    GENERIC(
        color = Color(0xFF21B629),
        aliases = setOf(
            "genericos-la-sante", "genericos-lasante", "medicina-general",
            "genericos", "generico", "la-sante-generico",
        ),
    ),
    PHARMETIQUE(
        color = Color(0xFFE90055),
        aliases = setOf("primary-care", "primary", "pharmetique", "pharmatique"),
    ),
    OTC(
        color = Color(0xFF195AB0),
        aliases = setOf("phq-consumo", "phq", "consumo", "otc", "super-marcas"),
    ),
    HOSPITAL(
        color = Color(0xFF00A979),
        aliases = setOf("hospital-care", "hospital", "hospitalaria", "hospitalario"),
    ),
    ONCOLOGY(
        color = Color(0xFF89B3EB),
        aliases = setOf("specialty-care", "specialty", "oncologicas", "oncologico"),
    ),
    ;

    companion object {
        fun resolve(unitId: String?, unitName: String?): BusinessUnitBrand? {
            val candidates = sequenceOf(unitId, unitName)
                .filterNotNull()
                .map(::normalizeBusinessUnitKey)
                .filter { it.isNotBlank() }
                .toList()

            return entries.firstOrNull { brand ->
                brand.aliases.any { alias ->
                    val normalizedAlias = normalizeBusinessUnitKey(alias)
                    candidates.any { candidate ->
                        candidate == normalizedAlias ||
                            candidate.contains(normalizedAlias) ||
                            normalizedAlias.contains(candidate)
                    }
                }
            }
        }
    }
}

/** Punto de color y nombre de unidad usado debajo del título de una CT. */
@Composable
fun BusinessUnitIndicator(
    unitId: String,
    unitName: String?,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    fontSize: TextUnit = 13.sp,
) {
    val brand = BusinessUnitBrand.resolve(unitId, unitName) ?: return
    val label = DisplayTitles.resolve(unitName, unitId)
    if (label.isBlank()) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(brand.color),
        )
        Text(
            text = label,
            color = Color(0xFF777777),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = fontSize),
            maxLines = 1,
        )
    }
}

private fun normalizeBusinessUnitKey(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim("-".single())
