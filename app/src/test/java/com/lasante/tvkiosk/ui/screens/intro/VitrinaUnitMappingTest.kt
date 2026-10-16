package com.lasante.tvkiosk.ui.screens.intro

import com.lasante.tvkiosk.data.DisplayTitles
import org.junit.Assert.assertEquals
import org.junit.Test

class VitrinaUnitMappingTest {

    @Test
    fun `legacy unit ids resolve to current visible names`() {
        assertEquals("La Santé Genérico", DisplayTitles.resolve("genericos-la-sante"))
        assertEquals("Pharmetique", DisplayTitles.resolve("Primary Care"))
        assertEquals("OTC", DisplayTitles.resolve("PHQ Consumo"))
        assertEquals("Hospitalaria", DisplayTitles.resolve("Hospital Care"))
        assertEquals("Oncológicas", DisplayTitles.resolve("Specialty Care"))
    }

    @Test
    fun `cintillo order maps each glb face to the intended business unit`() {
        assertEquals(
            listOf(
                "genericos-la-sante",
                "primary-care",
                "phq-consumo",
                "hospital-care",
                "specialty-care",
            ),
            VitrinaGlbMapping.orderedNavigationUnitIds,
        )
        assertEquals("specialty_care", VitrinaGlbMapping.glbNodeNameForUnitId("phq-consumo"))
        assertEquals("phq_consumo", VitrinaGlbMapping.glbNodeNameForUnitId("hospital-care"))
        assertEquals("hospital_care", VitrinaGlbMapping.glbNodeNameForUnitId("specialty-care"))
    }

    @Test
    fun `legacy names are replaced inside unit descriptions`() {
        assertEquals(
            "Pharmetique y Oncológicas",
            DisplayTitles.replaceLegacyUnitNames("Primary Care y Specialty Care"),
        )
    }
}
