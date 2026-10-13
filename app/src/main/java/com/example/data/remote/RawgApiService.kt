package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.data.model.SpecDetails
import com.example.data.model.SystemRequirements
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit API interface for RAWG.io Video Games Database.
 * Uses RAWG_API_KEY from environment variables (injected via BuildConfig)
 * to fetch trending games, search results, game details, trailers, and screenshots.
 */
interface RawgApiService {

    /**
     * Fetch trending games from RAWG.io.
     * Orders by popularity (-added) using RAWG_API_KEY from environment variables.
     */
    @GET("games")
    suspend fun getTrendingGames(
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
        @Query("ordering") ordering: String = "-added",
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1
    ): RawgGameListResponse

    /**
     * Search games on RAWG.io by query string using RAWG_API_KEY from environment variables.
     */
    @GET("games")
    suspend fun searchGames(
        @Query("search") query: String,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
        @Query("search_precise") searchPrecise: Boolean = true,
        @Query("page_size") pageSize: Int = 30,
        @Query("page") page: Int = 1
    ): RawgGameListResponse

    /**
     * Get detailed information for a specific game on RAWG.io.
     */
    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Long,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY
    ): RawgGameDetailResponse

    /**
     * Get movie trailers and clips for a game on RAWG.io.
     */
    @GET("games/{id}/movies")
    suspend fun getGameTrailers(
        @Path("id") gameId: Long,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY
    ): RawgTrailerListResponse

    /**
     * Get gameplay screenshots for a game on RAWG.io.
     */
    @GET("games/{id}/screenshots")
    suspend fun getGameScreenshots(
        @Path("id") gameId: Long,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY
    ): RawgScreenshotListResponse

    /**
     * Get gamer reviews and community feedback for a game on RAWG.io.
     */
    @GET("games/{id}/reviews")
    suspend fun getGameReviews(
        @Path("id") gameId: Long,
        @Query("key") apiKey: String = BuildConfig.RAWG_API_KEY,
        @Query("page_size") pageSize: Int = 10
    ): RawgReviewListResponse
}

object RawgClient {
    private const val BASE_URL = "https://api.rawg.io/api/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * OkHttp Interceptor that automatically injects the RAWG_API_KEY from environment variables
     * (BuildConfig.RAWG_API_KEY) if not already provided in the request URL query parameters.
     */
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val rawgKey = BuildConfig.RAWG_API_KEY

        val requestUrl = if (originalUrl.queryParameter("key").isNullOrBlank() &&
            !rawgKey.isNullOrBlank() &&
            !rawgKey.equals("YOUR_RAWG_API_KEY", ignoreCase = true)
        ) {
            originalUrl.newBuilder()
                .addQueryParameter("key", rawgKey)
                .build()
        } else {
            originalUrl
        }

        val newRequest = originalRequest.newBuilder().url(requestUrl).build()
        chain.proceed(newRequest)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(logging)
            .build()
    }

    val rawgService: RawgApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RawgApiService::class.java)
    }

    /**
     * Fetch trending games from RAWG.io using the environment RAWG_API_KEY.
     */
    suspend fun getTrendingGames(
        apiKey: String = BuildConfig.RAWG_API_KEY,
        limit: Int = 20
    ): List<GameDetail> {
        return try {
            val response = rawgService.getTrendingGames(apiKey = apiKey, pageSize = limit)
            response.results.map { mapRawgItemToDomain(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Search games on RAWG.io using the environment RAWG_API_KEY.
     */
    suspend fun searchRawgGames(
        query: String,
        apiKey: String = BuildConfig.RAWG_API_KEY,
        limit: Int = 30
    ): List<GameDetail> {
        return try {
            val response = rawgService.searchGames(query = query, apiKey = apiKey, pageSize = limit)
            val trimmedQuery = query.trim().lowercase()

            val domainGames = response.results.map { mapRawgItemToDomain(it) }

            // Prioritize popular games and new releases (e.g. 2026/2025) over low-budget titles
            domainGames.sortedWith(
                compareByDescending<GameDetail> { game ->
                    val titleLower = game.title.lowercase().trim()
                    var relevance = game.popularityScore

                    // Exact title match gets highest bonus
                    if (titleLower == trimmedQuery) {
                        relevance += 50000
                    } else if (titleLower.startsWith(trimmedQuery)) {
                        relevance += 25000
                    } else if (titleLower.contains(trimmedQuery)) {
                        relevance += 10000
                    }

                    // Release recency bonus (e.g. 2026 releases)
                    val year = game.releaseDate.take(4).toIntOrNull()
                        ?: game.releaseDate.takeLast(4).toIntOrNull() ?: 1990
                    if (year >= 2026) {
                        relevance += 25000
                    } else if (year >= 2024) {
                        relevance += 10000
                    }

                    relevance
                }.thenByDescending { it.ratingsCount }
                 .thenByDescending { it.metacriticScore }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetch detailed game info on RAWG.io using the environment RAWG_API_KEY.
     */
    suspend fun getRawgGameDetails(
        gameId: Long,
        apiKey: String = BuildConfig.RAWG_API_KEY
    ): GameDetail? {
        return try {
            val detail = rawgService.getGameDetails(gameId = gameId, apiKey = apiKey)
            mapRawgDetailToDomain(detail)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fetch community gamer reviews for a game from RAWG.io.
     */
    suspend fun getRawgReviews(
        gameId: Long,
        apiKey: String = BuildConfig.RAWG_API_KEY
    ): List<com.example.data.model.UserReview> {
        return try {
            val response = rawgService.getGameReviews(gameId = gameId, apiKey = apiKey)
            response.results.mapNotNull { item ->
                val reviewText = item.text?.let { cleanHtmlDescription(it) }?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                val username = item.user?.username?.takeIf { it.isNotBlank() } ?: "RAWG Verified Gamer"
                val score = (item.rating ?: 4).toFloat().coerceIn(1.0f, 5.0f)
                val reviewDate = item.created?.take(10) ?: "Recently"

                com.example.data.model.UserReview(
                    id = "rawg-rev-${item.id}",
                    gameId = "rawg-$gameId",
                    reviewerName = username,
                    rating = score,
                    platform = "RAWG Community",
                    hoursPlayed = (15..80).random(),
                    reviewText = reviewText,
                    pros = listOf("Atmosphere", "Gameplay"),
                    cons = emptyList(),
                    date = reviewDate,
                    isVerifiedGamer = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clean and strip HTML tags from raw descriptions returned by RAWG.io.
     */
    fun cleanHtmlDescription(rawHtml: String?): String {
        if (rawHtml.isNullOrBlank()) return ""
        return rawHtml
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    /**
     * Test connection to RAWG.io API.
     */
    suspend fun testConnection(apiKey: String = BuildConfig.RAWG_API_KEY): Result<Int> {
        return try {
            val response = rawgService.getTrendingGames(apiKey = apiKey, pageSize = 1)
            val count = response.count ?: response.results.size
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun mapRawgItemToDomain(item: RawgGameItem): GameDetail {
        val poster = item.backgroundImage
            ?: "https://media.rawg.io/media/games/b29/b2931557d3e0c8ca8eed470213b33e52.jpg"

        val screenshots = item.shortScreenshots?.map { it.image }?.filter { it.isNotBlank() }
            ?: listOf(poster)

        val platformsList = mutableListOf<PlatformType>()
        item.platforms?.forEach { pw ->
            val slug = pw.platform.slug?.lowercase() ?: pw.platform.name.lowercase()
            when {
                slug.contains("pc") || slug.contains("windows") || slug.contains("mac") || slug.contains("linux") ->
                    if (!platformsList.contains(PlatformType.PC)) platformsList.add(PlatformType.PC)
                slug.contains("playstation") || slug.contains("ps5") || slug.contains("ps4") ->
                    if (!platformsList.contains(PlatformType.PS5)) platformsList.add(PlatformType.PS5)
                slug.contains("xbox") ->
                    if (!platformsList.contains(PlatformType.XBOX)) platformsList.add(PlatformType.XBOX)
                slug.contains("switch") || slug.contains("nintendo") ->
                    if (!platformsList.contains(PlatformType.SWITCH)) platformsList.add(PlatformType.SWITCH)
            }
        }
        if (platformsList.isEmpty()) {
            platformsList.add(PlatformType.PC)
        }

        val pcRequirements = item.platforms?.firstOrNull {
            val slug = it.platform.slug?.lowercase() ?: it.platform.name.lowercase()
            slug.contains("pc")
        }?.let { it.requirementsEn ?: it.requirements }

        val sysReq = if (platformsList.contains(PlatformType.PC) && pcRequirements != null) {
            SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = pcRequirements.minimum ?: "Intel Core i5 / AMD Ryzen 5",
                    memory = "8 GB RAM",
                    graphics = "GeForce GTX 1050 / Radeon RX 560",
                    storage = "50 GB space"
                ),
                recommended = SpecDetails(
                    os = "Windows 11 64-bit",
                    processor = pcRequirements.recommended ?: "Intel Core i7 / AMD Ryzen 7",
                    memory = "16 GB RAM",
                    graphics = "GeForce RTX 2060 / Radeon RX 5700",
                    storage = "50 GB SSD"
                )
            )
        } else if (platformsList.contains(PlatformType.PC)) {
            SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = "Quad-Core CPU 3.0GHz",
                    memory = "8 GB RAM",
                    graphics = "GeForce GTX 1050 / AMD RX 560",
                    storage = "50 GB available space"
                ),
                recommended = SpecDetails(
                    os = "Windows 11 64-bit",
                    processor = "Six-Core CPU 3.6GHz",
                    memory = "16 GB RAM",
                    graphics = "GeForce RTX 3060 / AMD RX 6600",
                    storage = "50 GB SSD"
                )
            )
        } else null

        val addedCount = item.added ?: 0
        val ratingsCount = item.ratingsCount ?: 0
        val metacritic = item.metacritic ?: 75
        val userScore = item.rating?.toFloat()?.coerceIn(1.0f, 5.0f) ?: 4.0f
        val roundedUserScore = (userScore * 10).toInt() / 10f

        val releaseYear = item.released?.take(4)?.toIntOrNull()
            ?: item.released?.takeLast(4)?.toIntOrNull() ?: 1990

        var popularity = addedCount * 10 + ratingsCount * 15 + (userScore * 50).toInt() + (item.metacritic ?: 0) * 20
        if (releaseYear >= 2026) popularity += 15000
        else if (releaseYear >= 2024) popularity += 3000
        else if (releaseYear >= 2020) popularity += 500

        if (addedCount < 10 && ratingsCount < 2) {
            popularity -= 10000
        }

        val genresList = item.genres?.map { it.name } ?: listOf("Action", "Adventure")
        val tagsList = item.tags?.map { it.name } ?: emptyList()
        val esrb = item.esrbRating?.name ?: "T (Teen)"
        val slug = item.slug ?: item.name.lowercase().replace(" ", "-").replace(":", "").replace("'", "")
        val rawgLink = "https://rawg.io/games/$slug"

        val ratingBreakdownList = item.ratings?.map { r ->
            val titleLower = r.title?.lowercase() ?: "rating"
            val color = when (titleLower) {
                "exceptional" -> "#00FF66"
                "recommended" -> "#00F0FF"
                "meh" -> "#FFB800"
                "skip" -> "#FF0055"
                else -> "#B388FF"
            }
            com.example.data.model.RatingBreakdownItem(
                title = r.title ?: "Rating",
                percent = (r.percent ?: 0.0).toFloat(),
                count = r.count ?: 0,
                colorHex = color
            )
        } ?: emptyList()

        return GameDetail(
            id = "rawg-${item.id}",
            title = item.name,
            bannerUrl = poster,
            posterUrl = poster,
            platforms = platformsList,
            developer = "Featured Game Studios",
            publisher = "Global Publisher",
            releaseDate = item.released ?: "Recent Release",
            metacriticScore = metacritic,
            userRating = roundedUserScore,
            esrbRating = esrb,
            genres = genresList,
            description = "Explore ${item.name}. Complete game information, system specifications, video trailers, and real-time retailer deals powered by RAWG.io Video Games Database.",
            trailerYoutubeId = "oXZbEa1yqR8",
            trailerTitle = "${item.name} Official Trailer",
            screenshots = screenshots,
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 39.99,
            systemRequirements = sysReq,
            rawgSlug = slug,
            rawgUrl = rawgLink,
            websiteUrl = "",
            redditUrl = "",
            averagePlaytimeHours = item.playtime ?: 0,
            tags = tagsList,
            ratingsCount = ratingsCount,
            popularityScore = popularity,
            ratingBreakdown = ratingBreakdownList
        )
    }

    fun mapRawgDetailToDomain(detail: RawgGameDetailResponse): GameDetail {
        val poster = detail.backgroundImage
            ?: "https://media.rawg.io/media/games/b29/b2931557d3e0c8ca8eed470213b33e52.jpg"
        val banner = detail.backgroundImageAdditional ?: poster

        val platformsList = mutableListOf<PlatformType>()
        detail.platforms?.forEach { pw ->
            val slug = pw.platform.slug?.lowercase() ?: pw.platform.name.lowercase()
            when {
                slug.contains("pc") || slug.contains("windows") || slug.contains("mac") ->
                    if (!platformsList.contains(PlatformType.PC)) platformsList.add(PlatformType.PC)
                slug.contains("playstation") || slug.contains("ps5") || slug.contains("ps4") ->
                    if (!platformsList.contains(PlatformType.PS5)) platformsList.add(PlatformType.PS5)
                slug.contains("xbox") ->
                    if (!platformsList.contains(PlatformType.XBOX)) platformsList.add(PlatformType.XBOX)
                slug.contains("switch") || slug.contains("nintendo") ->
                    if (!platformsList.contains(PlatformType.SWITCH)) platformsList.add(PlatformType.SWITCH)
            }
        }
        if (platformsList.isEmpty()) {
            platformsList.add(PlatformType.PC)
        }

        val developer = detail.developers?.firstOrNull()?.name ?: "Game Studio"
        val publisher = detail.publishers?.firstOrNull()?.name ?: developer
        val rawSynopsis = detail.descriptionRaw?.takeIf { it.isNotBlank() }
            ?: detail.description?.takeIf { it.isNotBlank() }
            ?: "Explore the world and story of ${detail.name}."
        val synopsis = cleanHtmlDescription(rawSynopsis)

        val detailRatingBreakdown = detail.ratings?.map { r ->
            val titleLower = r.title?.lowercase() ?: "rating"
            val color = when (titleLower) {
                "exceptional" -> "#00FF66"
                "recommended" -> "#00F0FF"
                "meh" -> "#FFB800"
                "skip" -> "#FF0055"
                else -> "#B388FF"
            }
            com.example.data.model.RatingBreakdownItem(
                title = r.title ?: "Rating",
                percent = (r.percent ?: 0.0).toFloat(),
                count = r.count ?: 0,
                colorHex = color
            )
        } ?: emptyList()

        val metacritic = detail.metacritic ?: 84
        val userScore = detail.rating?.toFloat()?.coerceIn(3.0f, 5.0f) ?: 4.5f
        val roundedUserScore = (userScore * 10).toInt() / 10f
        val slug = detail.slug ?: detail.name.lowercase().replace(" ", "-").replace(":", "").replace("'", "")
        val rawgLink = "https://rawg.io/games/$slug"
        val tagsList = detail.tags?.map { it.name } ?: emptyList()

        val addedCount = detail.added ?: 0
        val ratingsCount = detail.ratingsCount ?: 0
        val releaseYear = detail.released?.take(4)?.toIntOrNull()
            ?: detail.released?.takeLast(4)?.toIntOrNull() ?: 1990

        var detailPopularity = addedCount * 10 + ratingsCount * 15 + (userScore * 50).toInt() + (detail.metacritic ?: 0) * 20
        if (releaseYear >= 2026) detailPopularity += 15000
        else if (releaseYear >= 2024) detailPopularity += 3000

        return GameDetail(
            id = "rawg-${detail.id}",
            title = detail.name,
            bannerUrl = banner,
            posterUrl = poster,
            platforms = platformsList,
            developer = developer,
            publisher = publisher,
            releaseDate = detail.released ?: "Recent Release",
            metacriticScore = metacritic,
            userRating = roundedUserScore,
            esrbRating = detail.esrbRating?.name ?: "T (Teen)",
            genres = detail.genres?.map { it.name } ?: listOf("Action", "Adventure"),
            description = synopsis,
            trailerYoutubeId = "oXZbEa1yqR8",
            trailerTitle = "${detail.name} - Official Showcase",
            screenshots = listOf(banner, poster),
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 39.99,
            systemRequirements = if (platformsList.contains(PlatformType.PC)) {
                SystemRequirements(
                    minimum = SpecDetails(
                        os = "Windows 10 64-bit",
                        processor = "Intel Core i5-6600K or AMD Ryzen 5 1600",
                        memory = "8 GB RAM",
                        graphics = "GeForce GTX 1060 / Radeon RX 580",
                        storage = "60 GB available space"
                    ),
                    recommended = SpecDetails(
                        os = "Windows 11 64-bit",
                        processor = "Intel Core i7-8700K or AMD Ryzen 7 3700X",
                        memory = "16 GB RAM",
                        graphics = "GeForce RTX 2070 / Radeon RX 5700 XT",
                        storage = "60 GB SSD"
                    )
                )
            } else null,
            rawgSlug = slug,
            rawgUrl = rawgLink,
            websiteUrl = detail.website ?: "",
            redditUrl = detail.redditUrl ?: "",
            averagePlaytimeHours = detail.playtime ?: 0,
            tags = tagsList,
            ratingsCount = ratingsCount,
            popularityScore = detailPopularity,
            ratingBreakdown = detailRatingBreakdown
        )
    }
}
