package com.example.data.local

import com.example.data.model.CopyType
import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.data.model.SpecDetails
import com.example.data.model.StoreOffer
import com.example.data.model.SystemRequirements

object EntityMappers {

    fun GameDetail.toCachedEntity(): CachedGameEntity {
        return CachedGameEntity(
            gameId = this.id,
            title = this.title,
            bannerUrl = this.bannerUrl,
            posterUrl = this.posterUrl,
            platforms = this.platforms.joinToString(",") { it.name },
            developer = this.developer,
            publisher = this.publisher,
            releaseDate = this.releaseDate,
            metacriticScore = this.metacriticScore,
            userRating = this.userRating,
            esrbRating = this.esrbRating,
            genres = this.genres.joinToString(","),
            description = this.description,
            trailerYoutubeId = this.trailerYoutubeId,
            trailerTitle = this.trailerTitle,
            screenshots = this.screenshots.joinToString("|"),
            basePriceUsd = this.basePriceUsd,
            lowestHistoricalPriceUsd = this.lowestHistoricalPriceUsd,
            hasSystemRequirements = this.systemRequirements != null,
            minOs = this.systemRequirements?.minimum?.os ?: "",
            minProcessor = this.systemRequirements?.minimum?.processor ?: "",
            minMemory = this.systemRequirements?.minimum?.memory ?: "",
            minGraphics = this.systemRequirements?.minimum?.graphics ?: "",
            minStorage = this.systemRequirements?.minimum?.storage ?: "",
            recOs = this.systemRequirements?.recommended?.os ?: "",
            recProcessor = this.systemRequirements?.recommended?.processor ?: "",
            recMemory = this.systemRequirements?.recommended?.memory ?: "",
            recGraphics = this.systemRequirements?.recommended?.graphics ?: "",
            recStorage = this.systemRequirements?.recommended?.storage ?: "",
            rawgSlug = this.rawgSlug,
            rawgUrl = this.rawgUrl,
            websiteUrl = this.websiteUrl,
            redditUrl = this.redditUrl,
            averagePlaytimeHours = this.averagePlaytimeHours,
            tags = this.tags.joinToString(","),
            ratingsCount = this.ratingsCount,
            popularityScore = this.popularityScore,
            cachedAt = System.currentTimeMillis()
        )
    }

    fun CachedGameEntity.toDomainModel(
        digitalOffers: List<StoreOffer> = emptyList(),
        physicalOffers: List<StoreOffer> = emptyList()
    ): GameDetail {
        val platformList = this.platforms.split(",")
            .mapNotNull { name ->
                try {
                    PlatformType.valueOf(name.trim())
                } catch (e: Exception) {
                    null
                }
            }

        val genreList = this.genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val screenshotList = this.screenshots.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        val tagList = this.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val sysReq = if (this.hasSystemRequirements) {
            SystemRequirements(
                minimum = SpecDetails(
                    os = this.minOs,
                    processor = this.minProcessor,
                    memory = this.minMemory,
                    graphics = this.minGraphics,
                    storage = this.minStorage
                ),
                recommended = SpecDetails(
                    os = this.recOs,
                    processor = this.recProcessor,
                    memory = this.recMemory,
                    graphics = this.recGraphics,
                    storage = this.recStorage
                )
            )
        } else {
            null
        }

        val computedRawgUrl = when {
            this.rawgUrl.isNotBlank() -> this.rawgUrl
            this.rawgSlug.isNotBlank() -> "https://rawg.io/games/${this.rawgSlug}"
            else -> "https://rawg.io/games/${this.title.lowercase().replace(" ", "-").replace(":", "").replace("'", "")}"
        }

        val computedRatingBreakdown = if (this.userRating >= 4.5f) {
            listOf(
                com.example.data.model.RatingBreakdownItem("Exceptional", 72f, (this.ratingsCount * 0.72).toInt().coerceAtLeast(10), "#00FF66"),
                com.example.data.model.RatingBreakdownItem("Recommended", 22f, (this.ratingsCount * 0.22).toInt().coerceAtLeast(3), "#00F0FF"),
                com.example.data.model.RatingBreakdownItem("Meh", 4f, (this.ratingsCount * 0.04).toInt().coerceAtLeast(1), "#FFB800"),
                com.example.data.model.RatingBreakdownItem("Skip", 2f, (this.ratingsCount * 0.02).toInt().coerceAtLeast(1), "#FF0055")
            )
        } else if (this.userRating >= 3.8f) {
            listOf(
                com.example.data.model.RatingBreakdownItem("Exceptional", 48f, (this.ratingsCount * 0.48).toInt().coerceAtLeast(10), "#00FF66"),
                com.example.data.model.RatingBreakdownItem("Recommended", 38f, (this.ratingsCount * 0.38).toInt().coerceAtLeast(5), "#00F0FF"),
                com.example.data.model.RatingBreakdownItem("Meh", 10f, (this.ratingsCount * 0.10).toInt().coerceAtLeast(2), "#FFB800"),
                com.example.data.model.RatingBreakdownItem("Skip", 4f, (this.ratingsCount * 0.04).toInt().coerceAtLeast(1), "#FF0055")
            )
        } else {
            listOf(
                com.example.data.model.RatingBreakdownItem("Exceptional", 25f, (this.ratingsCount * 0.25).toInt().coerceAtLeast(5), "#00FF66"),
                com.example.data.model.RatingBreakdownItem("Recommended", 40f, (this.ratingsCount * 0.40).toInt().coerceAtLeast(8), "#00F0FF"),
                com.example.data.model.RatingBreakdownItem("Meh", 25f, (this.ratingsCount * 0.25).toInt().coerceAtLeast(5), "#FFB800"),
                com.example.data.model.RatingBreakdownItem("Skip", 10f, (this.ratingsCount * 0.10).toInt().coerceAtLeast(2), "#FF0055")
            )
        }

        return GameDetail(
            id = this.gameId,
            title = this.title,
            bannerUrl = this.bannerUrl,
            posterUrl = this.posterUrl,
            platforms = if (platformList.isEmpty()) listOf(PlatformType.PC) else platformList,
            developer = this.developer,
            publisher = this.publisher,
            releaseDate = this.releaseDate,
            metacriticScore = this.metacriticScore,
            userRating = this.userRating,
            esrbRating = this.esrbRating,
            genres = genreList,
            description = this.description,
            trailerYoutubeId = this.trailerYoutubeId,
            trailerTitle = this.trailerTitle,
            screenshots = screenshotList,
            basePriceUsd = this.basePriceUsd,
            lowestHistoricalPriceUsd = this.lowestHistoricalPriceUsd,
            systemRequirements = sysReq,
            digitalOffers = digitalOffers,
            physicalOffers = physicalOffers,
            rawgSlug = this.rawgSlug,
            rawgUrl = computedRawgUrl,
            websiteUrl = this.websiteUrl,
            redditUrl = this.redditUrl,
            averagePlaytimeHours = this.averagePlaytimeHours,
            tags = tagList,
            ratingsCount = this.ratingsCount,
            popularityScore = this.popularityScore,
            ratingBreakdown = computedRatingBreakdown
        )
    }

    fun StoreOffer.toCachedEntity(gameId: String): CachedStoreOfferEntity {
        return CachedStoreOfferEntity(
            id = this.id,
            gameId = gameId,
            storeName = this.storeName,
            copyType = this.copyType.name,
            platform = this.platform.name,
            price = this.price,
            originalPrice = this.originalPrice,
            discountPercent = this.discountPercent,
            inStock = this.inStock,
            edition = this.edition,
            condition = this.condition,
            purchaseUrl = this.purchaseUrl,
            countryCode = this.countryCode,
            cachedAt = System.currentTimeMillis()
        )
    }

    fun CachedStoreOfferEntity.toDomainModel(): StoreOffer {
        val copyEnum = try {
            CopyType.valueOf(this.copyType)
        } catch (e: Exception) {
            CopyType.DIGITAL
        }

        val platformEnum = try {
            PlatformType.valueOf(this.platform)
        } catch (e: Exception) {
            PlatformType.PC
        }

        return StoreOffer(
            id = this.id,
            storeName = this.storeName,
            copyType = copyEnum,
            platform = platformEnum,
            price = this.price,
            originalPrice = this.originalPrice,
            discountPercent = this.discountPercent,
            inStock = this.inStock,
            edition = this.edition,
            condition = this.condition,
            purchaseUrl = this.purchaseUrl,
            countryCode = this.countryCode
        )
    }
}
