package com.example.data.remote

import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.data.model.SpecDetails
import com.example.data.model.SystemRequirements
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

interface TwitchAuthApi {
    @POST("oauth2/token")
    suspend fun getAccessToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("grant_type") grantType: String = "client_credentials"
    ): TwitchTokenResponse
}

interface IgdbApi {
    @POST("v4/games")
    suspend fun queryGames(
        @Header("Client-ID") clientId: String,
        @Header("Authorization") authorization: String,
        @Body query: RequestBody
    ): List<IgdbGame>
}

object IgdbClient {
    private const val TWITCH_OAUTH_URL = "https://id.twitch.tv/"
    private const val IGDB_BASE_URL = "https://api.igdb.com/"

    private var cachedToken: String? = null
    private var tokenExpiryTimestamp: Long = 0

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

    val twitchService: TwitchAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(TWITCH_OAUTH_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TwitchAuthApi::class.java)
    }

    val igdbService: IgdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(IGDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(IgdbApi::class.java)
    }

    suspend fun getValidBearerToken(clientId: String, clientSecret: String): String? {
        val now = System.currentTimeMillis()
        if (!cachedToken.isNullOrBlank() && now < tokenExpiryTimestamp) {
            return cachedToken
        }

        return try {
            val response = twitchService.getAccessToken(clientId, clientSecret)
            cachedToken = response.accessToken
            // Expire 60 seconds before actual duration for safety
            tokenExpiryTimestamp = now + (response.expiresIn * 1000) - 60000
            response.accessToken
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchIgdbGames(
        query: String,
        clientId: String,
        clientSecret: String,
        limit: Int = 15
    ): List<GameDetail> {
        val token = getValidBearerToken(clientId, clientSecret) ?: return emptyList()

        val sanitizedQuery = query.replace("\"", "\\\"").trim()
        val apicalypseQuery = """
            search "$sanitizedQuery";
            fields name,summary,storyline,cover.image_id,artworks.image_id,screenshots.image_id,first_release_date,rating,total_rating,genres.name,platforms.name,involved_companies.company.name,involved_companies.developer,involved_companies.publisher,videos.video_id,videos.name;
            limit $limit;
        """.trimIndent()

        val requestBody = apicalypseQuery.toRequestBody("text/plain".toMediaTypeOrNull())

        return try {
            val results = igdbService.queryGames(
                clientId = clientId,
                authorization = "Bearer $token",
                query = requestBody
            )
            results.map { mapIgdbToDomain(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun mapIgdbToDomain(game: IgdbGame): GameDetail {
        val poster = if (!game.cover?.imageId.isNullOrBlank()) {
            "https://images.igdb.com/igdb/image/upload/t_cover_big/${game.cover?.imageId}.jpg"
        } else {
            "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600&auto=format&fit=crop"
        }

        val banner = when {
            !game.artworks.isNullOrEmpty() && !game.artworks[0].imageId.isNullOrBlank() ->
                "https://images.igdb.com/igdb/image/upload/t_1080p/${game.artworks[0].imageId}.jpg"
            !game.screenshots.isNullOrEmpty() && !game.screenshots[0].imageId.isNullOrBlank() ->
                "https://images.igdb.com/igdb/image/upload/t_1080p/${game.screenshots[0].imageId}.jpg"
            else -> poster
        }

        val screenshots = game.screenshots?.mapNotNull {
            it.imageId?.let { id -> "https://images.igdb.com/igdb/image/upload/t_screenshot_big/$id.jpg" }
        } ?: listOf(poster)

        val platformsList = mutableListOf<PlatformType>()
        game.platforms?.forEach { p ->
            val lower = p.name.lowercase()
            when {
                lower.contains("pc") || lower.contains("windows") || lower.contains("linux") || lower.contains("mac") ->
                    if (!platformsList.contains(PlatformType.PC)) platformsList.add(PlatformType.PC)
                lower.contains("playstation 5") || lower.contains("ps5") || lower.contains("playstation 4") || lower.contains("ps4") ->
                    if (!platformsList.contains(PlatformType.PS5)) platformsList.add(PlatformType.PS5)
                lower.contains("xbox") ->
                    if (!platformsList.contains(PlatformType.XBOX)) platformsList.add(PlatformType.XBOX)
                lower.contains("switch") || lower.contains("nintendo") ->
                    if (!platformsList.contains(PlatformType.SWITCH)) platformsList.add(PlatformType.SWITCH)
            }
        }
        if (platformsList.isEmpty()) {
            platformsList.add(PlatformType.PC)
        }

        val developer = game.involvedCompanies
            ?.firstOrNull { it.developer == true }
            ?.company?.name ?: "Game Studio"

        val publisher = game.involvedCompanies
            ?.firstOrNull { it.publisher == true }
            ?.company?.name ?: developer

        val releaseFormatted = game.firstReleaseDate?.let { epochSec ->
            val date = Date(epochSec * 1000)
            SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date)
        } ?: "Coming Soon"

        val metacritic = game.totalRating?.toInt() ?: game.rating?.toInt() ?: 84
        val userRating = ((game.rating ?: 84.0) / 20.0).toFloat().coerceIn(3.0f, 5.0f)
        val roundedUserRating = (userRating * 10).toInt() / 10f

        val genres = game.genres?.map { it.name } ?: listOf("Action", "Adventure")
        val synopsis = game.summary?.takeIf { it.isNotBlank() }
            ?: game.storyline?.takeIf { it.isNotBlank() }
            ?: "Immerse yourself into ${game.name}. Complete gameplay information, system requirements, and pricing available across all platforms."

        val video = game.videos?.firstOrNull { !it.videoId.isNullOrBlank() }
        val youtubeId = video?.videoId ?: "oXZbEa1yqR8"
        val trailerTitle = video?.name ?: "${game.name} - Official Video"

        return GameDetail(
            id = "igdb-${game.id}",
            title = game.name,
            bannerUrl = banner,
            posterUrl = poster,
            platforms = platformsList,
            developer = developer,
            publisher = publisher,
            releaseDate = releaseFormatted,
            metacriticScore = metacritic,
            userRating = roundedUserRating,
            esrbRating = "T (Teen)",
            genres = genres,
            description = synopsis,
            trailerYoutubeId = youtubeId,
            trailerTitle = trailerTitle,
            screenshots = screenshots,
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 39.99,
            systemRequirements = if (platformsList.contains(PlatformType.PC)) {
                SystemRequirements(
                    minimum = SpecDetails(
                        os = "Windows 10 64-bit",
                        processor = "Intel Core i5-6600K / AMD Ryzen 5 1600",
                        memory = "8 GB RAM",
                        graphics = "NVIDIA GTX 1060 / AMD Radeon RX 580",
                        storage = "60 GB available space"
                    ),
                    recommended = SpecDetails(
                        os = "Windows 11 64-bit",
                        processor = "Intel Core i7-8700K / AMD Ryzen 7 3700X",
                        memory = "16 GB RAM",
                        graphics = "NVIDIA RTX 2070 / AMD Radeon RX 5700 XT",
                        storage = "60 GB SSD"
                    )
                )
            } else null
        )
    }
}
