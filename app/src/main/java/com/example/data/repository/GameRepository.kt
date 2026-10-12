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

        // 1. Fetch from local Room cache first (Offline support)
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
            .toMutableList()

        // 2. If online and searching, query RAWG.io API and CheapShark API to update cache
        if (trimmed.isNotEmpty()) {
            val rawgApiKey = com.example.BuildConfig.RAWG_API_KEY
            val hasRawgApiKey = !rawgApiKey.isNullOrBlank() &&
                    !rawgApiKey.equals("YOUR_RAWG_API_KEY", ignoreCase = true)

            // 2a. Query RAWG.io Video Games Database for high-res posters, descriptions, and metadata
            if (hasRawgApiKey) {
                try {
                    val rawgResults = com.example.data.remote.RawgClient.searchRawgGames(
                        query = trimmed,
                        apiKey = rawgApiKey,
                        limit = 15
                    )
                    for (rawgGame in rawgResults) {
                        cacheDao.insertGame(rawgGame.toCachedEntity())
                        val exists = localMatches.any { it.title.equals(rawgGame.title, ignoreCase = true) }
                        if (!exists) {
                            if (selectedPlatform == PlatformType.ALL || rawgGame.platforms.contains(selectedPlatform)) {
                                localMatches.add(0, rawgGame) // Prioritize rich RAWG.io results
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fall back cleanly to CheapShark API and cached database
                }
            }

            // 2b. Also query CheapShark for real-time deals and pricing
            try {
                val remoteResults = api.searchGames(trimmed, limit = 15)
                for (item in remoteResults) {
                    val existing = localMatches.firstOrNull { it.title.equals(item.title, ignoreCase = true) }
                    if (existing == null) {
                        val cheapestPrice = item.cheapestPrice?.toDoubleOrNull() ?: 19.99
                        val generatedId = "cs-${item.gameId}"
                        val thumb = item.thumbUrl ?: "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600&auto=format&fit=crop"

                        val remoteGame = GameDetail(
                            id = generatedId,
                            title = item.title,
                            bannerUrl = thumb,
                            posterUrl = thumb,
                            platforms = listOf(PlatformType.PC, PlatformType.PS5, PlatformType.XBOX),
                            developer = "Featured Game Studios",
                            publisher = "Global Publisher",
                            releaseDate = "Recent Release",
                            metacriticScore = 85,
                            userRating = 4.5f,
                            esrbRating = "T (Teen)",
                            genres = listOf("Action", "Adventure", "Deals"),
                            description = "Real-time deal and price tracking across PC digital stores and verified physical retailers worldwide for ${item.title}.",
                            trailerYoutubeId = "oXZbEa1yqR8",
                            trailerTitle = "Official Trailer",
                            screenshots = listOf(thumb),
                            basePriceUsd = if (cheapestPrice > 0) cheapestPrice * 1.5 else 49.99,
                            lowestHistoricalPriceUsd = cheapestPrice,
                            systemRequirements = SystemRequirements(
                                minimum = SpecDetails(
                                    os = "Windows 10 64-bit",
                                    processor = "Quad-Core Intel or AMD CPU",
                                    memory = "8 GB RAM",
                                    graphics = "NVIDIA GTX 1050 / AMD RX 560",
                                    storage = "45 GB available space"
                                ),
                                recommended = SpecDetails(
                                    os = "Windows 11 64-bit",
                                    processor = "Six-Core Intel i5 / AMD Ryzen 5",
                                    memory = "16 GB RAM",
                                    graphics = "NVIDIA RTX 3060 / AMD RX 6600",
                                    storage = "50 GB SSD"
                                )
                            )
                        )

                        // Save into Room Cache for offline reuse!
                        cacheDao.insertGame(remoteGame.toCachedEntity())

                        if (selectedPlatform == PlatformType.ALL || remoteGame.platforms.contains(selectedPlatform)) {
                            localMatches.add(remoteGame)
                        }
                    }
                }
            } catch (e: Exception) {
                // Offline fallback: returns localMatches from Room cleanly!
            }
        }

        localMatches
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
                } catch (e: Exception) {
                    // ignore
                }
            }
        }

        // 2. Read cached offers for this country
        val cachedOffers = cacheDao.getCachedOffersForGame(gameId, country.code).firstOrNull() ?: emptyList()

        if (cachedOffers.isNotEmpty()) {
            val digital = cachedOffers.filter { it.copyType == CopyType.DIGITAL.name }.map { it.toDomainModel() }
            val physical = cachedOffers.filter { it.copyType == CopyType.PHYSICAL.name }.map { it.toDomainModel() }
            return@withContext baseGame.copy(digitalOffers = digital, physicalOffers = physical)
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

    // Offline Wishlist operations
    fun getWishlistFlow(): Flow<List<WishlistItemEntity>> = wishlistDao.getAllWishlist()

    fun getWishlistByPriorityFlow(priority: String): Flow<List<WishlistItemEntity>> =
        wishlistDao.getWishlistByPriority(priority)

    fun searchWishlistFlow(query: String): Flow<List<WishlistItemEntity>> =
        wishlistDao.searchWishlist(query)

    fun isInWishlistFlow(gameId: String): Flow<Boolean> = wishlistDao.isInWishlist(gameId)

    suspend fun addToWishlist(
        game: GameDetail,
        targetPrice: Double,
        country: CountryInfo,
        notes: String = "",
        priority: String = "MEDIUM"
    ) = withContext(Dispatchers.IO) {
        val localCurrentPrice = game.lowestHistoricalPriceUsd * country.exchangeRateToUsd
        val entity = WishlistItemEntity(
            gameId = game.id,
            title = game.title,
            posterUrl = game.posterUrl,
            bannerUrl = game.bannerUrl,
            currentPrice = localCurrentPrice,
            targetPrice = targetPrice,
            originalPrice = game.basePriceUsd * country.exchangeRateToUsd,
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

    suspend fun getInitialReviews(gameId: String): List<UserReview> {
        return GameCatalog.INITIAL_REVIEWS.filter { it.gameId == gameId }
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
        val oldLocalPrice = currentLowestUsd * country.exchangeRateToUsd
        val newLocalPrice = newLowestUsd * country.exchangeRateToUsd

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
