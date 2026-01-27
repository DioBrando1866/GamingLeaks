package com.example.gamingleaks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/api/noticias")
    suspend fun obtenerTodas(): List<Noticia>

    @GET("/api/noticias/filtrar")
    suspend fun filtrar(@Query("tipo") categoria: String): List<Noticia>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}