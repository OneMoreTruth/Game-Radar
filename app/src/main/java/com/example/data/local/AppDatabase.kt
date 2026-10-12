package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

typealias GameEntity = CachedGameEntity
typealias WishlistEntity = WishlistItemEntity
typealias GameDao = GameCacheDao

@Database(
    entities = [
        WishlistItemEntity::class,
        CachedGameEntity::class,
        CachedStoreOfferEntity::class,
        GameReviewEntity::class,
        UserCollectionEntity::class,
        AlertHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
    abstract fun gameCacheDao(): GameCacheDao
    abstract fun reviewDao(): ReviewDao
    abstract fun collectionDao(): CollectionDao
    abstract fun alertHistoryDao(): AlertHistoryDao

    // Convenience accessor matching GameDao naming
    fun gameDao(): GameCacheDao = gameCacheDao()

    companion object {
        const val DATABASE_NAME = "gameradar_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Dedicated Database Builder helper for explicit injection or direct retrieval
 */
object DatabaseBuilder {
    fun getInstance(context: Context): AppDatabase = AppDatabase.getInstance(context)
    fun getGameDao(context: Context): GameCacheDao = getInstance(context).gameCacheDao()
    fun getWishlistDao(context: Context): WishlistDao = getInstance(context).wishlistDao()
}
