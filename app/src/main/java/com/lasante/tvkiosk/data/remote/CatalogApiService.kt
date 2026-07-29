package com.lasante.tvkiosk.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogApiService {

    @GET("home")
    suspend fun getHome(): HomeDto

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String? = null
    ): SearchResultDto
}
