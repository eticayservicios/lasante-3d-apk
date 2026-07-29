package com.lasante.tvkiosk.data.remote

import com.lasante.tvkiosk.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // URL base de la API
    private const val BASE_URL = "https://api.gurusaws.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                )
            }
        }
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
            }.build()
            chain.proceed(request)
        }
        .build()

    val catalogApi: CatalogApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatalogApiService::class.java)
    }
}
