package com.example.data.remote

import com.example.data.model.CheapSharkGameLookup
import com.example.data.model.CheapSharkGameSearch
import com.example.data.model.CheapSharkStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CheapSharkApiService {

    @GET("games")
    suspend fun searchGames(
        @Query("title") title: String,
        @Query("limit") limit: Int = 30
    ): List<CheapSharkGameSearch>

    @GET("games")
    suspend fun getGameLookup(
        @Query("id") gameId: String
    ): CheapSharkGameLookup

    @GET("stores")
    suspend fun getStores(): List<CheapSharkStore>
}

object ApiClient {
    private const val BASE_URL = "https://www.cheapshark.com/api/1.0/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val cheapSharkService: CheapSharkApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CheapSharkApiService::class.java)
    }
}
