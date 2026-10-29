package com.lasante.tvkiosk.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class BusinessUnitBrandTest {

    @Test
    fun `resolves current and legacy business unit identifiers`() {
        assertEquals(BusinessUnitBrand.GENERIC, BusinessUnitBrand.resolve("genericos-la-sante", null))
        assertEquals(BusinessUnitBrand.GENERIC, BusinessUnitBrand.resolve("medicina-general", null))
        assertEquals(BusinessUnitBrand.PHARMETIQUE, BusinessUnitBrand.resolve("primary-care", null))
        assertEquals(BusinessUnitBrand.OTC, BusinessUnitBrand.resolve("phq-consumo", null))
        assertEquals(BusinessUnitBrand.HOSPITAL, BusinessUnitBrand.resolve("hospital-care", null))
        assertEquals(BusinessUnitBrand.ONCOLOGY, BusinessUnitBrand.resolve("specialty-care", null))
    }

    @Test
    fun `resolves accented display names when id is unknown`() {
        assertEquals(BusinessUnitBrand.GENERIC, BusinessUnitBrand.resolve("unknown", "Genéricos La Santé"))
        assertEquals(BusinessUnitBrand.ONCOLOGY, BusinessUnitBrand.resolve("unknown", "Oncológicas"))
    }
}
