package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreWishlistItem(
    @DocumentId val gameId: String = "",
    val userId: String = "",
    val title: String = "",
    val posterUrl: String = "",
    val bannerUrl: String = "",
    val currentPrice: Double = 0.0,
    val targetPrice: Double = 0.0,
    val originalPrice: Double = 59.99,
    val notes: String = "",
    val priority: String = "MEDIUM",
    val platforms: String = "All",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
