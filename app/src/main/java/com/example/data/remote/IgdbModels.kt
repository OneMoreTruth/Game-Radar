package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwitchTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "token_type") val tokenType: String
)

@JsonClass(generateAdapter = true)
data class IgdbGame(
    val id: Long,
    val name: String,
    val summary: String? = null,
    val storyline: String? = null,
    val cover: IgdbImageResource? = null,
    val artworks: List<IgdbImageResource>? = null,
    val screenshots: List<IgdbImageResource>? = null,
    @Json(name = "first_release_date") val firstReleaseDate: Long? = null,
    val rating: Double? = null,
    @Json(name = "total_rating") val totalRating: Double? = null,
    val genres: List<IgdbNamedResource>? = null,
    val platforms: List<IgdbNamedResource>? = null,
    @Json(name = "involved_companies") val involvedCompanies: List<IgdbInvolvedCompany>? = null,
    val videos: List<IgdbVideoResource>? = null
)

@JsonClass(generateAdapter = true)
data class IgdbImageResource(
    val id: Long? = null,
    @Json(name = "image_id") val imageId: String? = null,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class IgdbNamedResource(
    val id: Long? = null,
    val name: String
)

@JsonClass(generateAdapter = true)
data class IgdbInvolvedCompany(
    val id: Long? = null,
    val company: IgdbNamedResource? = null,
    val developer: Boolean? = null,
    val publisher: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class IgdbVideoResource(
    val id: Long? = null,
    val name: String? = null,
    @Json(name = "video_id") val videoId: String? = null
)
