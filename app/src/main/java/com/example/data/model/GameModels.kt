package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class PlatformType(val displayName: String, val iconName: String) {
    ALL("All Platforms", "sports_esports"),
    PC("PC / Steam", "desktop_windows"),
    PS5("PlayStation 5", "gamepad"),
    XBOX("Xbox Series X|S", "videogame_asset"),
    SWITCH("Nintendo Switch", "tablet_android")
}

data class CountryInfo(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val currencySymbol: String,
    val currencyCode: String,
    val exchangeRateToUsd: Double // 1 USD = exchangeRate in local currency
) {
    companion object {
        val ALL_COUNTRIES = listOf(
            CountryInfo("US", "United States", "🇺🇸", "$", "USD", 1.0),
            CountryInfo("GB", "United Kingdom", "🇬🇧", "£", "GBP", 0.79),
            CountryInfo("CA", "Canada", "🇨🇦", "CA$", "CAD", 1.38),
            CountryInfo("EU", "European Union / Germany", "🇪🇺", "€", "EUR", 0.92),
            CountryInfo("AU", "Australia", "🇦🇺", "A$", "AUD", 1.54),
            CountryInfo("JP", "Japan", "🇯🇵", "¥", "JPY", 152.0)
        )
        val DEFAULT = ALL_COUNTRIES[0]
    }
}

enum class CopyType {
    DIGITAL,
    PHYSICAL
}

data class StoreOffer(
    val id: String,
    val storeName: String,
    val copyType: CopyType,
    val platform: PlatformType,
    val price: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val inStock: Boolean = true,
    val edition: String = "Standard Edition",
    val condition: String = "New", // New, Pre-Owned, Digital Key
    val purchaseUrl: String,
    val storeLogoUrl: String? = null,
    val countryCode: String = "US"
)

data class SystemRequirements(
    val minimum: SpecDetails,
    val recommended: SpecDetails
)

data class SpecDetails(
    val os: String,
    val processor: String,
    val memory: String,
    val graphics: String,
    val storage: String,
    val directX: String = "Version 12",
    val soundCard: String = "DirectX compatible"
)

data class UserReview(
    val id: String,
    val gameId: String,
    val reviewerName: String,
    val rating: Float, // 1.0 to 5.0
    val platform: String,
    val hoursPlayed: Int,
    val reviewText: String,
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    val date: String,
    val isVerifiedGamer: Boolean = true
)

data class RatingBreakdownItem(
    val title: String, // exceptional, recommended, meh, skip
    val percent: Float, // e.g. 65.5%
    val count: Int,
    val colorHex: String = "#00F0FF"
)

data class GameDetail(
    val id: String,
    val title: String,
    val bannerUrl: String,
    val posterUrl: String,
    val platforms: List<PlatformType>,
    val developer: String,
    val publisher: String,
    val releaseDate: String,
    val metacriticScore: Int,
    val userRating: Float,
    val esrbRating: String, // E, T, M, etc.
    val genres: List<String>,
    val description: String,
    val trailerYoutubeId: String, // e.g. "dQw4w9WgXcQ"
    val trailerTitle: String,
    val screenshots: List<String>,
    val basePriceUsd: Double,
    val lowestHistoricalPriceUsd: Double,
    val systemRequirements: SystemRequirements?,
    val physicalOffers: List<StoreOffer> = emptyList(),
    val digitalOffers: List<StoreOffer> = emptyList(),
    val rawgSlug: String = "",
    val rawgUrl: String = "",
    val websiteUrl: String = "",
    val redditUrl: String = "",
    val averagePlaytimeHours: Int = 0,
    val tags: List<String> = emptyList(),
    val ratingsCount: Int = 0,
    val popularityScore: Int = 0,
    val ratingBreakdown: List<RatingBreakdownItem> = emptyList()
) {
    fun getDisplayBasePrice(country: CountryInfo, platform: PlatformType = PlatformType.ALL): Double {
        val base = com.example.data.repository.CountryPricingManager.getRegionalBasePrice(basePriceUsd, country.code)
        return if (platform == PlatformType.PS5 || platform == PlatformType.XBOX) {
            if (country.code == "GB" && base < 69.99 && basePriceUsd >= 59.99) 69.99
            else if (country.code == "EU" && base < 79.99 && basePriceUsd >= 59.99) 79.99
            else base
        } else base
    }

    fun getLoadedPrice(country: CountryInfo, platform: PlatformType = PlatformType.ALL): Double {
        val year = releaseDate.take(4).toIntOrNull()
            ?: releaseDate.takeLast(4).toIntOrNull() ?: 2025
        val baseLoaded = com.example.data.repository.CountryPricingManager.getLoadedKeyPrice(
            basePriceUsd = basePriceUsd,
            dealPriceUsd = lowestHistoricalPriceUsd,
            countryCode = country.code,
            releaseYear = year
        )
        return when (platform) {
            PlatformType.PC -> com.example.data.repository.CountryPricingManager.formatRegionalPrice(baseLoaded * 0.96, country.code)
            PlatformType.PS5 -> com.example.data.repository.CountryPricingManager.formatRegionalPrice(baseLoaded * 1.04, country.code)
            PlatformType.XBOX -> com.example.data.repository.CountryPricingManager.formatRegionalPrice(baseLoaded * 1.02, country.code)
            PlatformType.SWITCH -> com.example.data.repository.CountryPricingManager.formatRegionalPrice(baseLoaded * 1.06, country.code)
            PlatformType.ALL -> baseLoaded
        }
    }

    fun getLoadedDiscountPercent(country: CountryInfo, platform: PlatformType = PlatformType.ALL): Int {
        val base = getDisplayBasePrice(country, platform)
        val loaded = getLoadedPrice(country, platform)
        return if (base > 0) {
            (((base - loaded) / base) * 100).toInt().coerceIn(15, 85)
        } else 30
    }

    fun getDisplayLowestPrice(country: CountryInfo, platform: PlatformType = PlatformType.ALL): Double {
        val targetOffers = if (platform == PlatformType.ALL) {
            digitalOffers + physicalOffers
        } else {
            (digitalOffers + physicalOffers).filter { it.platform == platform }
        }

        if (targetOffers.isNotEmpty()) {
            val minOffer = targetOffers.minOf { it.price }
            val loaded = getLoadedPrice(country, platform)
            return if (loaded > 0.0) minOf(minOffer, loaded) else minOffer
        }

        val standardDeal = com.example.data.repository.CountryPricingManager.getRegionalDealPrice(basePriceUsd, lowestHistoricalPriceUsd, country.code)
        val loadedKeyPrice = getLoadedPrice(country, platform)
        return when {
            loadedKeyPrice > 0.0 && standardDeal > 0.0 -> minOf(standardDeal, loadedKeyPrice)
            loadedKeyPrice > 0.0 -> loadedKeyPrice
            else -> standardDeal
        }
    }
}

// CheapShark JSON Models
@JsonClass(generateAdapter = true)
data class CheapSharkGameSearch(
    @Json(name = "gameID") val gameId: String,
    @Json(name = "steamAppID") val steamAppId: String?,
    @Json(name = "cheapest") val cheapestPrice: String?,
    @Json(name = "cheapestDealID") val cheapestDealId: String?,
    @Json(name = "external") val title: String,
    @Json(name = "thumb") val thumbUrl: String?
)

@JsonClass(generateAdapter = true)
data class CheapSharkGameLookup(
    @Json(name = "info") val info: CheapSharkGameInfo?,
    @Json(name = "cheapestPriceEver") val cheapestPriceEver: CheapSharkCheapestEver?,
    @Json(name = "deals") val deals: List<CheapSharkDealItem>?
)

@JsonClass(generateAdapter = true)
data class CheapSharkGameInfo(
    @Json(name = "title") val title: String,
    @Json(name = "steamAppID") val steamAppId: String?,
    @Json(name = "thumb") val thumb: String?
)

@JsonClass(generateAdapter = true)
data class CheapSharkCheapestEver(
    @Json(name = "price") val price: String?,
    @Json(name = "date") val date: Long?
)

@JsonClass(generateAdapter = true)
data class CheapSharkDealItem(
    @Json(name = "storeID") val storeId: String,
    @Json(name = "dealID") val dealId: String,
    @Json(name = "price") val price: String,
    @Json(name = "retailPrice") val retailPrice: String,
    @Json(name = "savings") val savings: String?
)

@JsonClass(generateAdapter = true)
data class CheapSharkStore(
    @Json(name = "storeID") val storeId: String,
    @Json(name = "storeName") val storeName: String,
    @Json(name = "isActive") val isActive: Int
)
