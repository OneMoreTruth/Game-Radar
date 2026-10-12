package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items ORDER BY addedAtTimestamp DESC")
    fun getAllWishlist(): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE priority = :priority ORDER BY addedAtTimestamp DESC")
    fun getWishlistByPriority(priority: String): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' ORDER BY addedAtTimestamp DESC")
    fun searchWishlist(query: String): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE gameId = :gameId LIMIT 1")
    suspend fun getWishlistItem(gameId: String): WishlistItemEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE gameId = :gameId)")
    fun isInWishlist(gameId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM wishlist_items")
    fun getWishlistCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: WishlistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistItemEntity) = insertOrUpdate(item)

    @Query("DELETE FROM wishlist_items WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: String)

    @Delete
    suspend fun deleteWishlist(item: WishlistItemEntity)

    @Update
    suspend fun update(item: WishlistItemEntity)

    @Query("UPDATE wishlist_items SET notes = :notes WHERE gameId = :gameId")
    suspend fun updateNotes(gameId: String, notes: String)

    @Query("UPDATE wishlist_items SET priority = :priority WHERE gameId = :gameId")
    suspend fun updatePriority(gameId: String, priority: String)

    @Query("UPDATE wishlist_items SET targetPrice = :targetPrice WHERE gameId = :gameId")
    suspend fun updateTargetPrice(gameId: String, targetPrice: Double)
}

@Dao
interface GameCacheDao {
    @Query("SELECT * FROM cached_games ORDER BY cachedAt DESC")
    fun getAllCachedGames(): Flow<List<CachedGameEntity>>

    @Query("SELECT * FROM cached_games WHERE gameId = :gameId LIMIT 1")
    suspend fun getCachedGameById(gameId: String): CachedGameEntity?

    @Query("SELECT * FROM cached_games WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' OR LOWER(genres) LIKE '%' || LOWER(:query) || '%' ORDER BY cachedAt DESC")
    fun searchCachedGames(query: String): Flow<List<CachedGameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<CachedGameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: CachedGameEntity)

    @Query("DELETE FROM cached_games WHERE gameId = :gameId")
    suspend fun deleteGameById(gameId: String)

    @Query("DELETE FROM cached_games WHERE cachedAt < :cutoff")
    suspend fun deleteOldCache(cutoff: Long)

    @Query("SELECT * FROM cached_store_offers WHERE gameId = :gameId AND countryCode = :countryCode")
    fun getCachedOffersForGame(gameId: String, countryCode: String): Flow<List<CachedStoreOfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<CachedStoreOfferEntity>)

    @Query("DELETE FROM cached_store_offers WHERE gameId = :gameId AND countryCode = :countryCode")
    suspend fun deleteOffersForGame(gameId: String, countryCode: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM game_reviews WHERE gameId = :gameId ORDER BY createdAtTimestamp DESC")
    fun getReviewsForGame(gameId: String): Flow<List<GameReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: GameReviewEntity)

    @Query("SELECT COUNT(*) FROM game_reviews WHERE gameId = :gameId")
    suspend fun getReviewCount(gameId: String): Int
}

@Dao
interface CollectionDao {
    @Query("SELECT * FROM user_collections ORDER BY addedAtTimestamp DESC")
    fun getAllCollections(): Flow<List<UserCollectionEntity>>

    @Query("SELECT * FROM user_collections WHERE collectionType = :type ORDER BY addedAtTimestamp DESC")
    fun getCollectionsByType(type: String): Flow<List<UserCollectionEntity>>

    @Query("SELECT * FROM user_collections WHERE gameId = :gameId")
    fun getCollectionsForGame(gameId: String): Flow<List<UserCollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: UserCollectionEntity)

    @Delete
    suspend fun delete(item: UserCollectionEntity)

    @Query("DELETE FROM user_collections WHERE gameId = :gameId AND collectionType = :type")
    suspend fun removeFromCollection(gameId: String, type: String)
}

@Dao
interface AlertHistoryDao {
    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertHistoryEntity)

    @Query("DELETE FROM alert_history")
    suspend fun clearAll()
}
