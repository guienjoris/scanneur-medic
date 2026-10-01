package com.example.scanneurdemdicament.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MedicamentApiService {

    @GET("v1/medicaments")
    suspend fun getMedicamentByCip(
        @Query("cip") cip: String
    ): Response<MedicamentDto>

    @GET("v1/medicaments")
    suspend fun searchMedicaments(
        @Query("search") query: String
    ): Response<List<MedicamentDto>>

    companion object {
        private const val BASE_URL = "https://medicaments-api.giygas.dev/"

        fun create(): MedicamentApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MedicamentApiService::class.java)
        }
    }
}