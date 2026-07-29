package com.lasante.tvkiosk.data

class MockCatalogRepository : CatalogRepository {
    private val units = listOf(
        BusinessUnit(
            unidadId = "genericos",
            nombre = "Genéricos",
            descripcion = "Genéricos y medicamentos",
            orden = 1,
            media = UnitMedia(icono = "💊")
        ),
        BusinessUnit(
            unidadId = "primary",
            nombre = "Primary Care",
            descripcion = "Atención primaria",
            orden = 2,
            media = UnitMedia(icono = "🩺")
        ),
        BusinessUnit(
            unidadId = "specialty",
            nombre = "Specialty Care",
            descripcion = "Cuidado especializado",
            orden = 3,
            media = UnitMedia(icono = "🧬")
        ),
        BusinessUnit(
            unidadId = "dermo",
            nombre = "Dermocosmética",
            descripcion = "Cuidado de la piel",
            orden = 4,
            media = UnitMedia(icono = "🧴")
        ),
        BusinessUnit(
            unidadId = "cardio",
            nombre = "Cardiometabólica",
            descripcion = "Salud cardiovascular",
            orden = 5,
            media = UnitMedia(icono = "❤️")
        ),
    )

    private val featuredProducts = listOf(
        // Usamos los modelos reales encontrados en assets
        Product(productoId = "p1", unidadId = "genericos", tratamientoId = "acne", nombre = "Acetaminofén", descripcion = "Analgésico", media = ProductMedia(modelo3d = Model3D(glb = "frasco.glb"))),
        Product(productoId = "p2", unidadId = "genericos", tratamientoId = "acne", nombre = "Frasco Genérico", descripcion = "Medicamento líquido", media = ProductMedia(modelo3d = Model3D(glb = "frasco.glb"))),
        Product(productoId = "p3", unidadId = "genericos", tratamientoId = "acne", nombre = "Caja y Frasco", descripcion = "Combo completo", media = ProductMedia(modelo3d = Model3D(glb = "cajayfrasco.glb"))),
        Product(productoId = "p4", unidadId = "genericos", tratamientoId = "acne", nombre = "Caja Estándar", descripcion = "Presentación tabletas", media = ProductMedia(modelo3d = Model3D(glb = "cajayfrasco.glb"))),

        Product(productoId = "p5", unidadId = "primary", tratamientoId = "migrana", nombre = "Líquido Primary", descripcion = "Alivio rápido", media = ProductMedia(modelo3d = Model3D(glb = "frasco.glb"))),
        Product(productoId = "p6", unidadId = "primary", tratamientoId = "migrana", nombre = "Caja Primary", descripcion = "Tratamiento diario", media = ProductMedia(modelo3d = Model3D(glb = "cajayfrasco.glb"))),
        Product(productoId = "p7", unidadId = "primary", tratamientoId = "migrana", nombre = "Frasco Vitrina", descripcion = "Uso hospitalario", media = ProductMedia(modelo3d = Model3D(glb = "frasco.glb"))),
        Product(productoId = "p8", unidadId = "primary", tratamientoId = "migrana", nombre = "Combo A", descripcion = "Pack ahorro", media = ProductMedia(modelo3d = Model3D(glb = "cajayfrasco.glb"))),
    )
    
    private val allProducts = featuredProducts.distinctBy { it.productoId }

    override suspend fun getIntroCatalogData(): IntroCatalogData =
        IntroCatalogData(
            businessUnits = units,
            vitrinaUnits = getVitrinaUnits(),
            vitrinaConfig = getVitrinaConfig(),
            screenSaverVideos = getScreenSaverVideos(),
            institutionalVideoUrl = getInstitutionalVideoUrl(),
        )

    override suspend fun getUnits(): List<BusinessUnit> = units
    override suspend fun getTreatments(unitId: String): List<Treatment> = emptyList()
    override suspend fun getProducts(treatmentId: String): List<Product> = allProducts
    override suspend fun getProduct(productId: String): Product? = allProducts.firstOrNull { it.productoId == productId }
    override suspend fun getFeaturedProducts(): List<Product> = featuredProducts

    override suspend fun getVitrinaUnits(): List<VitrinaUnit> =
        units.take(5).mapIndexed { index, unit ->
            VitrinaUnit(
                unit = unit,
                products = featuredProducts.filter { it.unidadId == unit.id }.ifEmpty { featuredProducts.take(4) },
                rotationDegrees = 72f * index
            )
        }

    override suspend fun getVitrinaConfig(): VitrinaConfig = VitrinaConfig()
    override suspend fun getScreenSaverVideos(): List<ScreenSaverVideo> = emptyList()
    override suspend fun getInstitutionalVideoUrl(): String? = null
    override suspend fun getAllProducts(): List<Product> = allProducts
    override suspend fun search(query: String, type: String?): SearchResult = SearchResult(query, 0, emptyList())
}
