package com.example.yungman3_2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiService {
    private const val BASE_URL = "https://www.omdbapi.com"
    const val API_KEY = "b3184168" // Твой ключ

    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: OmdbApi by lazy {
        retrofit.create(OmdbApi::class.java)
    }
}

interface OmdbApi {
    @GET("/")
    suspend fun searchMovies(
        @Query("s") search: String,
        @Query("apikey") apiKey: String = ApiService.API_KEY,
        @Query("type") type: String = "movie"
    ): MovieSearchResponse

    @GET("/")
    suspend fun getMovieDetails(
        @Query("i") imdbId: String,
        @Query("apikey") apiKey: String = ApiService.API_KEY
    ): MovieDetailResponse
}