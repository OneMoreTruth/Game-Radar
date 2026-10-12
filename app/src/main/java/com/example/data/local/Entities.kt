package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlist_items",
    indices = [Index(value = ["addedAtTimestamp"]), Index(value = ["priority"])]
)
data class WishlistItemEntity(
    @PrimaryKey val gameId: String,
    val title: String,
    val posterUrl: String,
    val bannerUrl: String = "",
    val currentPrice: Double,
    val targetPrice: Double,
    val originalPrice: Double = 59.99,
    val releaseDate: String,
    val notifyPriceDrop: Boolean = true,
    val notifyUpcomingRelease: Boolean = true,
    val platforms: String,
    val developer: String = "",
    val notes: String = "",
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val addedAtTimestamp: Long = System.currentTimeMillis(),
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cached_games",
    indices = [Index(value = ["title"]), Index(value = ["cachedAt"])]
)
data class CachedGameEntity(
    @PrimaryKey val gameId: String,
    val title: String,
    val bannerUrl: String,
    val posterUrl: String,
    val platforms: String, // comma separated, e.g. "PC,PS5,XBOX"
    val developer: String,
    val publisher: String,
    val releaseDate: String,
    val metacriticScore: Int,
    val userRating: Float,
    val esrbRating: String,
    val genres: String, // comma separated
    val description: String,
    val trailerYoutubeId: String,
    val trailerTitle: String,
    val screenshots: String, // pipe separated
    val basePriceUsd: Double,
    val lowestHistoricalPriceUsd: Double,
    // System Requirements
    val hasSystemRequirements: Boolean = false,
    val minOs: String = "",
    val minProcessor: String = "",
    val minMemory: String = "",
    val minGraphics: String = "",
    val minStorage: String = "",
    val recOs: String = "",
    val recProcessor: String = "",
    val recMemory: String = "",
    val recGraphics: String = "",
    val recStorage: String = "",
    val rawgSlug: String = "",
    val rawgUrl: String = "",
    val websiteUrl: String = "",
    val redditUrl: String = "",
    val averagePlaytimeHours: Int = 0,
    val tags: String = "",
    val ratingsCount: Int = 0,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cached_store_offers",
    indices = [Index(value = ["gameId", "countryCode"])]
)
data class CachedStoreOfferEntity(
    @PrimaryKey val id: String,
    val gameId: String,
    val storeName: String,
    val copyType: String, // "DIGITAL", "PHYSICAL"
    val platform: String,
    val price: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val inStock: Boolean = true,
    val edition: String = "Standard Edition",
    val condition: String = "New",
    val purchaseUrl: String,
    val countryCode: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_reviews")
data class GameReviewEntity(
    @PrimaryKey val id: String,
    val gameId: String,
    val reviewerName: String,
    val rating: Float,
    val platform: String,
    val hoursPlayed: Int,
    val reviewText: String,
    val pros: String, // comma or newline separated
    val cons: String,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true
)

@Entity(tableName = "user_collections")
data class UserCollectionEntity(
    @PrimaryKey val id: String, // e.g. gameId + "_" + collectionType
    val gameId: String,
    val title: String,
    val posterUrl: String,
    val collectionType: String, // "PLAYING", "BACKLOG", "COMPLETED", "FAVORITE"
    val rating: Float = 0f,
    val platform: String = "All",
    val notes: String = "",
    val addedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alert_history")
data class AlertHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val gameTitle: String,
    val alertType: String, // "PRICE_DROP", "UPCOMING_RELEASE"
    val oldPrice: Double = 0.0,
    val newPrice: Double = 0.0,
    val storeName: String = "",
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
