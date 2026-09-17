package com.lasante.tvkiosk.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface CatalogApiService {

    /**
     * Bootstrap kiosco. Default backend = slim (unidades+tratamientos+vitrina, sin productos anidados).
     * view=full solo para diagnóstico / legado.
     */
    @GET("home")
    suspend fun getHome(
        @Query("view") view: String = "slim",
    ): HomeDto

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String? = null,
    ): SearchResultDto

    /** Índice liviano para buscador (todas las unidades). */
    @GET("catalog")
    suspend fun getProductosIndex(
        @Query("entity") entity: String = "productos-index",
    ): CatalogPageDto

    @GET("catalog")
    suspend fun getTratamientosPage(
        @Query("entity") entity: String = "tratamientos",
        @Query("unidadId") unidadId: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): TreatmentPageDto

    @GET("catalog")
    suspend fun getProductosByTratamiento(
        @Query("entity") entity: String = "productos",
        @Query("tratamientoId") tratamientoId: String,
        @Query("unidadId") unidadId: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): CatalogPageDto

    @GET("catalog")
    suspend fun getProductosByUnidad(
        @Query("entity") entity: String = "productos",
        @Query("unidadId") unidadId: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): CatalogPageDto

    /** Catálogo global (todas las unidades) — VER TODOS desde Intro. */
    @GET("catalog")
    suspend fun getAllProductos(
        @Query("entity") entity: String = "productos",
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): CatalogPageDto
}
