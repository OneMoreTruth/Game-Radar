package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CachedGameEntity
import com.example.data.local.EntityMappers.toCachedEntity
import com.example.data.local.WishlistItemEntity
import com.example.data.model.CountryInfo
import com.example.data.repository.GameCatalog
import com.example.data.repository.StoreOfferGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GameRadar", appName)
  }

  @Test
  fun `game catalog contains featured titles`() {
    val games = GameCatalog.FEATURED_GAMES
    assertTrue(games.isNotEmpty())
    val eldenRing = games.firstOrNull { it.id == "elden-ring" }
    assertTrue(eldenRing != null)
    assertTrue(eldenRing!!.systemRequirements != null)
  }

  @Test
  fun `store offer generator creates physical and digital offers for countries`() {
    val game = GameCatalog.FEATURED_GAMES.first()
    val uk = CountryInfo.ALL_COUNTRIES.first { it.code == "GB" }
    val (digital, physical) = StoreOfferGenerator.generateOffers(game, uk)

    assertTrue(digital.isNotEmpty())
    assertTrue(physical.isNotEmpty())
    assertTrue(physical.any { it.storeName.contains("GAME UK") || it.storeName.contains("Amazon UK") })
  }

  @Test
  fun `room database offline wishlist operations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val wishlistDao = db.wishlistDao()

    val item = WishlistItemEntity(
      gameId = "test-game-1",
      title = "Hollow Knight: Silksong",
      posterUrl = "https://example.com/poster.jpg",
      currentPrice = 29.99,
      targetPrice = 19.99,
      releaseDate = "TBA",
      platforms = "PC, Switch",
      notes = "Buy on launch day",
      priority = "HIGH"
    )

    wishlistDao.insertOrUpdate(item)

    val retrieved = wishlistDao.getWishlistItem("test-game-1")
    assertNotNull(retrieved)
    assertEquals("Hollow Knight: Silksong", retrieved?.title)
    assertEquals("HIGH", retrieved?.priority)
    assertEquals("Buy on launch day", retrieved?.notes)

    // Test updating notes and priority offline
    wishlistDao.updateNotes("test-game-1", "Wait for sale")
    wishlistDao.updatePriority("test-game-1", "MEDIUM")

    val updated = wishlistDao.getWishlistItem("test-game-1")
    assertEquals("Wait for sale", updated?.notes)
    assertEquals("MEDIUM", updated?.priority)

    // Test cleanup
    wishlistDao.deleteByGameId("test-game-1")
    val deleted = wishlistDao.getWishlistItem("test-game-1")
    assertEquals(null, deleted)
  }

  @Test
  fun `room database game caching operations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val cacheDao = db.gameCacheDao()

    val game = GameCatalog.FEATURED_GAMES.first()
    val cachedEntity = game.toCachedEntity()

    cacheDao.insertGame(cachedEntity)

    val cached = cacheDao.getCachedGameById(game.id)
    assertNotNull(cached)
    assertEquals(game.title, cached?.title)

    val searchResults = cacheDao.searchCachedGames(game.title.take(5)).first()
    assertTrue(searchResults.any { it.gameId == game.id })
  }

  @Test
  fun `database builder provides singleton and daos`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db1 = com.example.data.local.DatabaseBuilder.getInstance(context)
    val db2 = com.example.data.local.DatabaseBuilder.getInstance(context)
    assertEquals(db1, db2)

    val gameDao = com.example.data.local.DatabaseBuilder.getGameDao(context)
    val wishlistDao = com.example.data.local.DatabaseBuilder.getWishlistDao(context)
    assertNotNull(gameDao)
    assertNotNull(wishlistDao)
  }

  @Test
  fun `rawg model mapping produces valid domain game`() {
    val rawgItem = com.example.data.remote.RawgGameItem(
      id = 3498L,
      name = "Grand Theft Auto V",
      released = "2013-09-17",
      backgroundImage = "https://media.rawg.io/media/games/20a/20aa58de18776585ffab8a04b901864c.jpg",
      rating = 4.47,
      metacritic = 92,
      genres = listOf(com.example.data.remote.RawgNamedEntity(id = 4L, name = "Action")),
      platforms = listOf(
        com.example.data.remote.RawgPlatformWrapper(
          platform = com.example.data.remote.RawgNamedEntity(id = 4L, name = "PC", slug = "pc")
        )
      )
    )

    val domain = com.example.data.remote.RawgClient.mapRawgItemToDomain(rawgItem)
    assertEquals("Grand Theft Auto V", domain.title)
    assertEquals("https://media.rawg.io/media/games/20a/20aa58de18776585ffab8a04b901864c.jpg", domain.posterUrl)
    assertEquals(92, domain.metacriticScore)
    assertTrue(domain.platforms.contains(com.example.data.model.PlatformType.PC))
    assertTrue(domain.systemRequirements != null)
  }
}
