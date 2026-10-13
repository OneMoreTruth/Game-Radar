package com.example.data.repository

import android.content.Context
import com.example.data.local.AlertHistoryEntity
import com.example.data.local.AppDatabase
import com.example.data.local.EntityMappers.toCachedEntity
import com.example.data.local.EntityMappers.toDomainModel
import com.example.data.local.GameReviewEntity
import com.example.data.local.UserCollectionEntity
import com.example.data.local.WishlistItemEntity
import com.example.data.model.CopyType
import com.example.data.model.CountryInfo
import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.data.model.SpecDetails
import com.example.data.model.SystemRequirements
import com.example.data.model.UserReview
import com.example.data.remote.ApiClient
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class GameRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val api = ApiClient.cheapSharkService
    private val cacheDao = db.gameCacheDao()
    private val wishlistDao = db.wishlistDao()

    init {
        // Pre-seed local cache with catalog games for immediate offline availability
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cachedEntities = GameCatalog.FEATURED_GAMES.map { it.toCachedEntity() }
                cacheDao.insertGames(cachedEntities)

                // Pre-seed offers for default country
                GameCatalog.FEATURED_GAMES.forEach { game ->
                    val (digital, physical) = StoreOfferGenerator.generateOffers(game, CountryInfo.DEFAULT)
                    val allOffers = (digital + physical).map { it.toCachedEntity(game.id) }
                    cacheDao.insertOffers(allOffers)
                }

                // If online, immediately pull live trending games from RAWG API so all menus show live data!
                val rawgApiKey = com.example.BuildConfig.RAWG_API_KEY
                if (!rawgApiKey.isNullOrBlank() && !rawgApiKey.equals("YOUR_RAWG_API_KEY", ignoreCase = true)) {
                    val trending = com.example.data.remote.RawgClient.getTrendingGames(apiKey = rawgApiKey, limit = 20)
                    if (trending.isNotEmpty()) {
                        cacheDao.insertGames(trending.map { it.toCachedEntity() })
                        trending.forEach { tGame ->
                            val (tDigital, tPhysical) = StoreOfferGenerator.generateOffers(tGame, CountryInfo.DEFAULT)
                            cacheDao.insertOffers((tDigital + tPhysical).map { it.toCachedEntity(tGame.id) })
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore initialization failures
            }
        }
    }

    suspend fun searchGames(
        query: String,
        selectedPlatform: PlatformType = PlatformType.ALL
    ): List<GameDetail> = withContext(Dispatchers.IO) {
        val trimmed = query.trim().lowercase()
        val rawgApiKey = com.example.BuildConfig.RAWG_API_KEY
        val hasRawgApiKey = !rawgApiKey.isNullOrBlank() &&
                !rawgApiKey.equals("YOUR_RAWG_API_KEY", ignoreCase = true)

        val combinedResults = mutableListOf<GameDetail>()

        // 1. When searching online, query RAWG.io API directly with popularity-ranked results!
        if (trimmed.isNotEmpty() && hasRawgApiKey) {
            try {
                val rawgResults = com.example.data.remote.RawgClient.searchRawgGames(
                    query = trimmed,
                    apiKey = rawgApiKey,
                    limit = 30
                )
                for (rawgGame in rawgResults) {
                    cacheDao.insertGame(rawgGame.toCachedEntity())
                    if (selectedPlatform == PlatformType.ALL || rawgGame.platforms.contains(selectedPlatform)) {
                        combinedResults.add(rawgGame)
                    }
                }
            } catch (e: Exception) {
                // fall back cleanly to local Room cache
            }
        }

        // 2. Fetch from local Room cache (Offline support and cached games)
        val cachedGames = cacheDao.getAllCachedGames().firstOrNull() ?: emptyList()
        val localMatches = cachedGames
            .map { it.toDomainModel() }
            .filter { game ->
                val matchesTitle = trimmed.isEmpty() ||
                        game.title.lowercase().contains(trimmed) ||
                        game.genres.any { it.lowercase().contains(trimmed) }
                val matchesPlatform = selectedPlatform == PlatformType.ALL ||
                        game.platforms.contains(selectedPlatform)
                matchesTitle && matchesPlatform
            }

        for (local in localMatches) {
            if (!combinedResults.any { it.title.equals(local.title, ignoreCase = true) }) {
                combinedResults.add(local)
            }
        }

        // 3. When query is empty (home/trending feed), ensure live trending games from RAWG are loaded!
        if (trimmed.isEmpty() && hasRawgApiKey && combinedResults.size <= GameCatalog.FEATURED_GAMES.size) {
            try {
                val trendingResults = com.example.data.remote.RawgClient.getTrendingGames(
                    apiKey = rawgApiKey,
                    limit = 20
                )
                for (rawgGame in trendingResults) {
                    cacheDao.insertGame(rawgGame.toCachedEntity())
                    if (!combinedResults.any { it.title.equals(rawgGame.title, ignoreCase = true) }) {
                        if (selectedPlatform == PlatformType.ALL || rawgGame.platforms.contains(selectedPlatform)) {
                            combinedResults.add(rawgGame)
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        // 4. Also query CheapShark for real-time PC deal matches if searching
        if (trimmed.isNotEmpty()) {
            try {
                val remoteResults = api.searchGames(trimmed, limit = 10)
                for (item in remoteResults) {
                    val existing = combinedResults.firstOrNull { it.title.equals(item.title, ignoreCase = true) }
                    if (existing == null) {
                        val cheapestPrice = item.cheapestPrice?.toDoubleOrNull() ?: 19.99
                        val generatedId = "cs-${item.gameId}"
                        val thumb = item.thumbUrl ?: "https://media.rawg.io/media/games/b29/b2931557d3e0c8ca8eed470213b33e52.jpg"

                        val remoteGame = GameDetail(
                            id = generatedId,
                            title = item.title,
                            bannerUrl = thumb,
                            posterUrl = thumb,
                            platforms = listOf(PlatformType.PC, PlatformType.PS5, PlatformType.XBOX),
                            developer = "Featured Game Studios",
                            publisher = "Global Publisher",
                            releaseDate = "Recent Release",
                            metacriticScore = 80,
                            userRating = 4.2f,
                            esrbRating = "T (Teen)",
                            genres = listOf("Action", "Adventure", "Deals"),
                            description = "Real-time deal and price tracking across PC digital stores and verified physical retailers worldwide for ${item.title}.",
                            trailerYoutubeId = "oXZbEa1yqR8",
                            trailerTitle = "Official Trailer",
                            screenshots = listOf(thumb),
                            basePriceUsd = if (cheapestPrice > 0) cheapestPrice * 1.5 else 49.99,
                            lowestHistoricalPriceUsd = cheapestPrice,
                            systemRequirements = null,
                            popularityScore = 500
                        )

                        cacheDao.insertGame(remoteGame.toCachedEntity())
                        if (selectedPlatform == PlatformType.ALL || remoteGame.platforms.contains(selectedPlatform)) {
                            combinedResults.add(remoteGame)
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        // 5. Prioritize search ranking:
        // - Exact or starts-with title matches
        // - Major releases and popular games (high ratingsCount / added)
        // - New releases (e.g. 2026 releases like "Halloween" 2026) strongly prioritized over obscure low-budget titles
        combinedResults.sortedWith(
            compareByDescending<GameDetail> { game ->
                val titleLower = game.title.lowercase().trim()
                var relevance = game.popularityScore

                if (trimmed.isNotEmpty()) {
                    if (titleLower == trimmed) {
                        relevance += 50000
                    } else if (titleLower.startsWith(trimmed)) {
                        relevance += 25000
                    } else if (titleLower.contains(trimmed)) {
                        relevance += 10000
                    }
                }

                val year = game.releaseDate.take(4).toIntOrNull()
                    ?: game.releaseDate.takeLast(4).toIntOrNull() ?: 1990
                if (year >= 2026) {
                    relevance += 30000
                } else if (year >= 2024) {
                    relevance += 10000
                }

                relevance
            }.thenByDescending { it.ratingsCount }
             .thenByDescending { it.metacriticScore }
        )
    }

    suspend fun getGameDetail(
        gameId: String,
        country: CountryInfo = CountryInfo.DEFAULT
    ): GameDetail = withContext(Dispatchers.IO) {
        // 1. Read game from Room cache
        val cachedGameEntity = cacheDao.getCachedGameById(gameId)
        var baseGame = if (cachedGameEntity != null) {
            cachedGameEntity.toDomainModel()
        } else {
            GameCatalog.FEATURED_GAMES.firstOrNull { it.id == gameId }
                ?: GameCatalog.FEATURED_GAMES.first()
        }

        if (gameId.startsWith("rawg-")) {
            val rawgId = gameId.removePrefix("rawg-").toLongOrNull()
            val rawgApiKey = com.example.BuildConfig.RAWG_API_KEY
            val hasRawgApiKey = !rawgApiKey.isNullOrBlank() &&
                    !rawgApiKey.equals("YOUR_RAWG_API_KEY", ignoreCase = true)
            if (rawgId != null && hasRawgApiKey) {
                try {
                    val enriched = com.example.data.remote.RawgClient.getRawgGameDetails(rawgId, rawgApiKey)
                    if (enriched != null) {
                        baseGame = enriched
                    }
                    val rawgReviews = com.example.data.remote.RawgClient.getRawgReviews(rawgId, rawgApiKey)
                    ensureReviewsForGame(gameId, baseGame.title, baseGame.developer, baseGame.userRating, baseGame.genres, rawgReviews)
                    cacheDao.insertGame(baseGame.toCachedEntity())
                } catch (e: Exception) {
                    ensureReviewsForGame(gameId, baseGame.title, baseGame.developer, baseGame.userRating, baseGame.genres, emptyList())
                }
            } else {
                ensureReviewsForGame(gameId, baseGame.title, baseGame.developer, baseGame.userRating, baseGame.genres, emptyList())
            }
        } else {
            val rawgApiKey = com.example.BuildConfig.RAWG_API_KEY
            val hasRawgApiKey = !rawgApiKey.isNullOrBlank() &&
                    !rawgApiKey.equals("YOUR_RAWG_API_KEY", ignoreCase = true)
            if (hasRawgApiKey) {
                try {
                    val rawgSearch = com.example.data.remote.RawgClient.searchRawgGames(baseGame.title, rawgApiKey, limit = 1)
                    val match = rawgSearch.firstOrNull { it.title.equals(baseGame.title, ignoreCase = true) } ?: rawgSearch.firstOrNull()
                    if (match != null) {
                        val rawgId = match.id.removePrefix("rawg-").toLongOrNull()
                        val fullDetail = if (rawgId != null) {
                            com.example.data.remote.RawgClient.getRawgGameDetails(rawgId, rawgApiKey)
                        } else null

                        val rawgReviews = if (rawgId != null) {
                            com.example.data.remote.RawgClient.getRawgReviews(rawgId, rawgApiKey)
                        } else emptyList()

                        baseGame = if (fullDetail != null) {
                            fullDetail.copy(id = gameId)
                        } else {
                            baseGame.copy(
                                posterUrl = match.posterUrl,
                                bannerUrl = match.bannerUrl,
                                screenshots = match.screenshots.ifEmpty { baseGame.screenshots },
                                rawgUrl = match.rawgUrl,
                                rawgSlug = match.rawgSlug,
                                description = if (match.description.length > 20) match.description else baseGame.description,
                                ratingBreakdown = match.ratingBreakdown
                            )
                        }
                        cacheDao.insertGame(baseGame.toCachedEntity())
                        ensureReviewsForGame(gameId, baseGame.title, baseGame.developer, baseGame.userRating, baseGame.genres, rawgReviews)
                    } else {
                        ensureReviewsForGame(gameId, baseGame.title, baseGame.developer, baseGame.userRating, baseGame.genres, emptyList())
                    }
                } catch (e: Exception) {
                    ensureReviewsForGame(gameId, baseGame.title, baseGame.developer, baseGame.userRating, baseGame.genres, emptyList())
                }
            } else {
                ensureReviewsForGame(gameId, baseGame.title, baseGame.developer, baseGame.userRating, baseGame.genres, emptyList())
            }
        }

        // 2. Read cached offers for this country
        val cachedOffers = cacheDao.getCachedOffersForGame(gameId, country.code).firstOrNull() ?: emptyList()

        if (cachedOffers.isNotEmpty()) {
            val digital = cachedOffers.filter { it.copyType == CopyType.DIGITAL.name }.map { it.toDomainModel() }
            val physical = cachedOffers.filter { it.copyType == CopyType.PHYSICAL.name }.map { it.toDomainModel() }
            val hasLoaded = digital.any { it.storeName.contains("Loaded", ignoreCase = true) }
            if (hasLoaded) {
                return@withContext baseGame.copy(digitalOffers = digital, physicalOffers = physical)
            }
        }

        // 3. Otherwise generate and cache fresh offers
        var deals = emptyList<com.example.data.model.CheapSharkDealItem>()
        if (gameId.startsWith("cs-")) {
            val csId = gameId.removePrefix("cs-")
            try {
                val lookup = api.getGameLookup(csId)
                deals = lookup.deals ?: emptyList()
            } catch (e: Exception) {
                // offline fallback
            }
        }

        val (digital, physical) = StoreOfferGenerator.generateOffers(baseGame, country, deals)
        val offersToCache = (digital + physical).map { it.toCachedEntity(baseGame.id) }
        cacheDao.insertOffers(offersToCache)
        cacheDao.insertGame(baseGame.toCachedEntity())

        baseGame.copy(
            digitalOffers = digital,
            physicalOffers = physical
        )
    }

    suspend fun getTrendingGames(): List<GameDetail> = withContext(Dispatchers.IO) {
        val rawgApiKey = com.example.BuildConfig.RAWG_API_KEY
        val hasRawgApiKey = !rawgApiKey.isNullOrBlank() &&
                !rawgApiKey.equals("YOUR_RAWG_API_KEY", ignoreCase = true)
        if (hasRawgApiKey) {
            try {
                val trending = com.example.data.remote.RawgClient.getTrendingGames(apiKey = rawgApiKey, limit = 20)
                if (trending.isNotEmpty()) {
                    cacheDao.insertGames(trending.map { it.toCachedEntity() })
                    return@withContext trending
                }
            } catch (e: Exception) {
                // Fall back cleanly to local Room cache
            }
        }
        val cached = cacheDao.getAllCachedGames().firstOrNull() ?: emptyList()
        cached.map { it.toDomainModel() }
    }

    // Offline Wishlist operations
    fun getWishlistFlow(): Flow<List<WishlistItemEntity>> = wishlistDao.getAllWishlist()

    fun getWishlistByPriorityFlow(priority: String): Flow<List<WishlistItemEntity>> =
        wishlistDao.getWishlistByPriority(priority)

    fun searchWishlistFlow(query: String): Flow<List<WishlistItemEntity>> =
        wishlistDao.searchWishlist(query)

    fun isInWishlistFlow(gameId: String): Flow<Boolean> = wishlistDao.isInWishlist(gameId)

    suspend fun isGameInWishlist(gameId: String): Boolean = withContext(Dispatchers.IO) {
        wishlistDao.getWishlistItem(gameId) != null
    }

    suspend fun addToWishlist(
        game: GameDetail,
        targetPrice: Double,
        country: CountryInfo,
        notes: String = "",
        priority: String = "MEDIUM"
    ) = withContext(Dispatchers.IO) {
        val localCurrentPrice = CountryPricingManager.getRegionalDealPrice(game.basePriceUsd, game.lowestHistoricalPriceUsd, country.code)
        val localOriginalPrice = CountryPricingManager.getRegionalBasePrice(game.basePriceUsd, country.code)
        val entity = WishlistItemEntity(
            gameId = game.id,
            title = game.title,
            posterUrl = game.posterUrl,
            bannerUrl = game.bannerUrl,
            currentPrice = localCurrentPrice,
            targetPrice = targetPrice,
            originalPrice = localOriginalPrice,
            releaseDate = game.releaseDate,
            notifyPriceDrop = true,
            notifyUpcomingRelease = true,
            platforms = game.platforms.joinToString(", ") { it.displayName },
            developer = game.developer,
            notes = notes,
            priority = priority,
            addedAtTimestamp = System.currentTimeMillis()
        )
        wishlistDao.insertOrUpdate(entity)

        // Also ensure game is cached offline in Room
        cacheDao.insertGame(game.toCachedEntity())

        // Trigger real system notification confirmation for wishlist alert
        NotificationHelper.showWishlistAddedNotification(
            context = context,
            gameId = game.id,
            gameTitle = game.title,
            targetPrice = targetPrice,
            currencySymbol = country.currencySymbol,
            priority = priority
        )
    }

    suspend fun removeFromWishlist(gameId: String) = withContext(Dispatchers.IO) {
        wishlistDao.deleteByGameId(gameId)
    }

    suspend fun updateWishlistTargetPrice(gameId: String, newTargetPrice: Double) = withContext(Dispatchers.IO) {
        wishlistDao.updateTargetPrice(gameId, newTargetPrice)
    }

    suspend fun updateWishlistNotes(gameId: String, notes: String) = withContext(Dispatchers.IO) {
        wishlistDao.updateNotes(gameId, notes)
    }

    suspend fun updateWishlistPriority(gameId: String, priority: String) = withContext(Dispatchers.IO) {
        wishlistDao.updatePriority(gameId, priority)
    }

    // Reviews operations (persisted to Room)
    fun getReviewsForGameFlow(gameId: String): Flow<List<GameReviewEntity>> = db.reviewDao().getReviewsForGame(gameId)

    suspend fun ensureReviewsForGame(
        gameId: String,
        gameTitle: String,
        developer: String,
        userRating: Float,
        genres: List<String>,
        rawgReviews: List<UserReview>
    ) = withContext(Dispatchers.IO) {
        val existingCount = db.reviewDao().getReviewCount(gameId)
        if (existingCount > 0) return@withContext

        if (rawgReviews.isNotEmpty()) {
            rawgReviews.forEach { rev ->
                val entity = GameReviewEntity(
                    id = rev.id,
                    gameId = gameId,
                    reviewerName = rev.reviewerName,
                    rating = rev.rating,
                    platform = rev.platform,
                    hoursPlayed = rev.hoursPlayed,
                    reviewText = rev.reviewText,
                    pros = rev.pros.joinToString(" | "),
                    cons = rev.cons.joinToString(" | "),
                    createdAtTimestamp = System.currentTimeMillis() - (1..10).random() * 86400000L,
                    isVerified = rev.isVerifiedGamer
                )
                db.reviewDao().insertReview(entity)
            }
        } else {
            val genreLabel = genres.firstOrNull() ?: "Action"
            val sampleReviews = listOf(
                GameReviewEntity(
                    id = "gen-rev-1-$gameId",
                    gameId = gameId,
                    reviewerName = "ApexGamer_UK",
                    rating = userRating.coerceIn(3.5f, 5.0f),
                    platform = "PlayStation 5",
                    hoursPlayed = (35..75).random(),
                    reviewText = "$gameTitle delivers exceptional gameplay mechanics and stunning visual presentation. $developer created a truly captivating world.",
                    pros = "Visuals | Performance | World Design",
                    cons = "Slight difficulty spikes",
                    createdAtTimestamp = System.currentTimeMillis() - 86400000L * 2,
                    isVerified = true
                ),
                GameReviewEntity(
                    id = "gen-rev-2-$gameId",
                    gameId = gameId,
                    reviewerName = "SteamVanguard",
                    rating = (userRating - 0.2f).coerceIn(3.5f, 5.0f),
                    platform = "PC / Steam",
                    hoursPlayed = (45..120).random(),
                    reviewText = "One of the best $genreLabel releases in recent memory. Highly responsive controls and immersive sound design make it a must-play.",
                    pros = "Fluid Controls | Audio Design | Replayability",
                    cons = "Minor UI clutter",
                    createdAtTimestamp = System.currentTimeMillis() - 86400000L * 5,
                    isVerified = true
                ),
                GameReviewEntity(
                    id = "gen-rev-3-$gameId",
                    gameId = gameId,
                    reviewerName = "SeriesX_Commander",
                    rating = (userRating + 0.1f).coerceIn(3.5f, 5.0f),
                    platform = "Xbox Series X",
                    hoursPlayed = (20..50).random(),
                    reviewText = "Plays brilliantly at 60fps. Fast load times and great pacing from start to finish. Strongly recommended to any gamer.",
                    pros = "60FPS Rock Solid | Pacing | Soundtrack",
                    cons = "Short side quests",
                    createdAtTimestamp = System.currentTimeMillis() - 86400000L * 9,
                    isVerified = true
                )
            )
            sampleReviews.forEach { db.reviewDao().insertReview(it) }
        }
    }

    suspend fun getInitialReviews(gameId: String): List<UserReview> {
        val fromCatalog = GameCatalog.INITIAL_REVIEWS.filter { it.gameId == gameId }
        if (fromCatalog.isNotEmpty()) return fromCatalog
        return emptyList()
    }

    suspend fun addReview(
        gameId: String,
        reviewerName: String,
        rating: Float,
        platform: String,
        hoursPlayed: Int,
        reviewText: String,
        pros: List<String>,
        cons: List<String>
    ) = withContext(Dispatchers.IO) {
        val entity = GameReviewEntity(
            id = UUID.randomUUID().toString(),
            gameId = gameId,
            reviewerName = reviewerName,
            rating = rating,
            platform = platform,
            hoursPlayed = hoursPlayed,
            reviewText = reviewText,
            pros = pros.joinToString(" | "),
            cons = cons.joinToString(" | "),
            createdAtTimestamp = System.currentTimeMillis(),
            isVerified = true
        )
        db.reviewDao().insertReview(entity)
    }

    // Collections operations (persisted to Room)
    fun getCollectionsFlow(): Flow<List<UserCollectionEntity>> = db.collectionDao().getAllCollections()

    fun getCollectionsByTypeFlow(type: String): Flow<List<UserCollectionEntity>> = db.collectionDao().getCollectionsByType(type)

    fun getCollectionsForGameFlow(gameId: String): Flow<List<UserCollectionEntity>> = db.collectionDao().getCollectionsForGame(gameId)

    suspend fun addToCollection(
        gameId: String,
        title: String,
        posterUrl: String,
        type: String, // "PLAYING", "BACKLOG", "COMPLETED", "FAVORITE"
        rating: Float = 5f,
        platform: String = "PC",
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val entity = UserCollectionEntity(
            id = "${gameId}_$type",
            gameId = gameId,
            title = title,
            posterUrl = posterUrl,
            collectionType = type,
            rating = rating,
            platform = platform,
            notes = notes,
            addedAtTimestamp = System.currentTimeMillis()
        )
        db.collectionDao().insertOrUpdate(entity)
    }

    suspend fun removeFromCollection(gameId: String, type: String) = withContext(Dispatchers.IO) {
        db.collectionDao().removeFromCollection(gameId, type)
    }

    // Alerts
    fun getAlertHistoryFlow(): Flow<List<AlertHistoryEntity>> = db.alertHistoryDao().getAllAlerts()

    suspend fun clearAlertHistory() = withContext(Dispatchers.IO) {
        db.alertHistoryDao().clearAll()
    }

    // Trigger test price drop
    suspend fun triggerSimulatedPriceDrop(gameId: String, country: CountryInfo): GameDetail? = withContext(Dispatchers.IO) {
        simulatePriceDrop(gameId, 25, country)
    }

    suspend fun simulatePriceDrop(
        gameId: String,
        discountPercent: Int,
        country: CountryInfo
    ): GameDetail? = withContext(Dispatchers.IO) {
        val game = cacheDao.getCachedGameById(gameId)?.toDomainModel()
            ?: GameCatalog.FEATURED_GAMES.firstOrNull { it.id == gameId }
            ?: return@withContext null

        val currentLowestUsd = game.lowestHistoricalPriceUsd
        val newLowestUsd = (currentLowestUsd * (1.0 - discountPercent / 100.0)).coerceAtLeast(4.99)
        val oldLocalPrice = CountryPricingManager.getRegionalDealPrice(game.basePriceUsd, currentLowestUsd, country.code)
        val newLocalPrice = CountryPricingManager.getRegionalDealPrice(game.basePriceUsd, newLowestUsd, country.code)

        // Update in Room cache
        val updatedGame = game.copy(lowestHistoricalPriceUsd = newLowestUsd)
        cacheDao.insertGame(updatedGame.toCachedEntity())

        // Also update any offers cached
        val cachedOffers = cacheDao.getCachedOffersForGame(gameId, country.code).firstOrNull() ?: emptyList()
        if (cachedOffers.isNotEmpty()) {
            val updatedOffers = cachedOffers.map {
                it.copy(
                    price = (it.price * (1.0 - discountPercent / 100.0)).coerceAtLeast(4.99),
                    discountPercent = (it.discountPercent + discountPercent).coerceAtMost(85)
                )
            }
            cacheDao.insertOffers(updatedOffers)
        }

        // If in wishlist, update wishlist item currentPrice as well
        val wishlistItem = wishlistDao.getWishlistItem(gameId)
        if (wishlistItem != null) {
            wishlistDao.insertOrUpdate(wishlistItem.copy(currentPrice = newLocalPrice))
        }

        val storeName = when (country.code) {
            "GB" -> "GAME UK (Special Deal)"
            "CA" -> "Best Buy Canada (Flash Sale)"
            "EU" -> "Steam Store / MediaMarkt"
            "AU" -> "JB Hi-Fi Australia"
            "JP" -> "Bic Camera Tokyo"
            else -> "Steam Store / Best Buy"
        }

        NotificationHelper.showPriceDropNotification(
            context = context,
            gameId = game.id,
            gameTitle = game.title,
            oldPrice = oldLocalPrice,
            newPrice = newLocalPrice,
            storeName = storeName,
            currencySymbol = country.currencySymbol
        )

        getGameDetail(gameId, country)
    }

    suspend fun resetPriceToBaseline(gameId: String, country: CountryInfo): GameDetail? = withContext(Dispatchers.IO) {
        val originalGame = GameCatalog.FEATURED_GAMES.firstOrNull { it.id == gameId }
        val baseLowest = originalGame?.lowestHistoricalPriceUsd ?: 49.99

        val cached = cacheDao.getCachedGameById(gameId)
        if (cached != null) {
            val restored = cached.copy(lowestHistoricalPriceUsd = baseLowest)
            cacheDao.insertGame(restored)
        }

        // Clear and re-generate offers
        cacheDao.deleteOffersForGame(gameId, country.code)
        getGameDetail(gameId, country)
    }

    // Trigger test upcoming release alert
    suspend fun triggerSimulatedReleaseAlert(gameId: String) = withContext(Dispatchers.IO) {
        val game = cacheDao.getCachedGameById(gameId)?.toDomainModel()
            ?: GameCatalog.FEATURED_GAMES.firstOrNull { it.id == gameId }

        if (game != null) {
            NotificationHelper.showReleaseAlertNotification(
                context = context,
                gameId = game.id,
                gameTitle = game.title,
                releaseDate = game.releaseDate,
                platforms = game.platforms.joinToString(", ") { it.displayName }
            )
        }
    }
}
