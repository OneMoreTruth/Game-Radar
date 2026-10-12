package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawgGameListResponse(
    val count: Int? = null,
    val next: String? = null,
    val previous: String? = null,
    val results: List<RawgGameItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RawgGameItem(
    val id: Long,
    val slug: String? = null,
    val name: String,
    val released: String? = null,
    @Json(name = "background_image") val backgroundImage: String? = null,
    val rating: Double? = null,
    @Json(name = "rating_top") val ratingTop: Int? = null,
    @Json(name = "ratings_count") val ratingsCount: Int? = null,
    val metacritic: Int? = null,
    val playtime: Int? = null,
    val platforms: List<RawgPlatformWrapper>? = null,
    val genres: List<RawgNamedEntity>? = null,
    val tags: List<RawgNamedEntity>? = null,
    @Json(name = "short_screenshots") val shortScreenshots: List<RawgScreenshot>? = null,
    @Json(name = "esrb_rating") val esrbRating: RawgNamedEntity? = null
)

@JsonClass(generateAdapter = true)
data class RawgGameDetailResponse(
    val id: Long,
    val slug: String? = null,
    val name: String,
    @Json(name = "description_raw") val descriptionRaw: String? = null,
    val description: String? = null,
    val released: String? = null,
    @Json(name = "background_image") val backgroundImage: String? = null,
    @Json(name = "background_image_additional") val backgroundImageAdditional: String? = null,
    val website: String? = null,
    @Json(name = "reddit_url") val redditUrl: String? = null,
    @Json(name = "reddit_name") val redditName: String? = null,
    val rating: Double? = null,
    @Json(name = "ratings_count") val ratingsCount: Int? = null,
    val metacritic: Int? = null,
    val playtime: Int? = null,
    val platforms: List<RawgPlatformWrapper>? = null,
    val genres: List<RawgNamedEntity>? = null,
    val tags: List<RawgNamedEntity>? = null,
    val developers: List<RawgNamedEntity>? = null,
    val publishers: List<RawgNamedEntity>? = null,
    @Json(name = "esrb_rating") val esrbRating: RawgNamedEntity? = null,
    val stores: List<RawgStoreWrapper>? = null
)

@JsonClass(generateAdapter = true)
data class RawgStoreWrapper(
    val id: Long? = null,
    val url: String? = null,
    val store: RawgNamedEntity? = null
)

@JsonClass(generateAdapter = true)
data class RawgPlatformWrapper(
    val platform: RawgNamedEntity,
    @Json(name = "requirements_en") val requirementsEn: RawgRequirements? = null,
    val requirements: RawgRequirements? = null
)

@JsonClass(generateAdapter = true)
data class RawgRequirements(
    val minimum: String? = null,
    val recommended: String? = null
)

@JsonClass(generateAdapter = true)
data class RawgNamedEntity(
    val id: Long? = null,
    val name: String,
    val slug: String? = null
)

@JsonClass(generateAdapter = true)
data class RawgScreenshot(
    val id: Long? = null,
    val image: String
)

@JsonClass(generateAdapter = true)
data class RawgScreenshotListResponse(
    val count: Int? = null,
    val results: List<RawgScreenshot>? = null
)

@JsonClass(generateAdapter = true)
data class RawgTrailerListResponse(
    val count: Int? = null,
    val results: List<RawgTrailerItem>? = null
)

@JsonClass(generateAdapter = true)
data class RawgTrailerItem(
    val id: Long,
    val name: String? = null,
    val preview: String? = null,
    val data: RawgTrailerData? = null
)

@JsonClass(generateAdapter = true)
data class RawgTrailerData(
    val max: String? = null,
    @Json(name = "480") val p480: String? = null
)
