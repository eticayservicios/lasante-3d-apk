package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasante.tvkiosk.app.FilamentNativeSupport
import com.lasante.tvkiosk.data.BusinessUnit
import com.lasante.tvkiosk.data.DisplayTitles
import com.lasante.tvkiosk.ui.theme.LaSanteBackground
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.theme.LaSanteTextSecondary

/**
 * Arranque degradado: la app no se cierra si Filament/GLES falla.
 * Mantiene navegación a tratamientos por unidad.
 */
@Composable
fun FilamentUnavailableIntro(
    businessUnits: List<BusinessUnit>,
    onNavigateToTreatments: (String) -> Unit,
    onNavigateToViewAllProducts: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LaSanteBackground)
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Vitrina 3D no disponible en este equipo",
                color = LaSanteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = FilamentNativeSupport.userMessage(),
                color = LaSanteTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Elige una unidad de negocio para continuar",
                color = LaSanteText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                items(businessUnits, key = { it.id }) { unit ->
                    val label = DisplayTitles.resolve(unit.nombre, unit.id)
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LaSanteGreen)
                            .clickable {
                                onNavigateToTreatments(unit.id)
                            }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (businessUnits.isNotEmpty()) {
                Text(
                    text = "Ver todos los productos",
                    color = LaSanteGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            onNavigateToViewAllProducts(businessUnits.first().id)
                        }
                        .padding(8.dp),
                )
            }
        }
    }
}
