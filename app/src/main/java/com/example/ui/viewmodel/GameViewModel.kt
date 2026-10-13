package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlertHistoryEntity
import com.example.data.local.UserCollectionEntity
import com.example.data.local.WishlistItemEntity
import com.example.data.model.CopyType
import com.example.data.model.CountryInfo
import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.data.model.UserReview
import com.example.data.repository.CountryPricingManager
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    TRENDING("🔥 Trending"),
    DISCOUNT("💰 Top Discounts"),
    METACRITIC("⭐ Highest Rated"),
    PRICE_LOW("🏷️ Price: Low to High")
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    private val prefs = application.getSharedPreferences("gameradar_prefs", Context.MODE_PRIVATE)

    private val _selectedCountry = MutableStateFlow(CountryInfo.DEFAULT)
    val selectedCountry: StateFlow<CountryInfo> = _selectedCountry.asStateFlow()

    private val _preferredPlatform = MutableStateFlow(
        try {
            val saved = prefs.getString("preferred_platform", PlatformType.ALL.name) ?: PlatformType.ALL.name
            PlatformType.valueOf(saved)
        } catch (e: Exception) {
            PlatformType.ALL
        }
    )
    val preferredPlatform: StateFlow<PlatformType> = _preferredPlatform.asStateFlow()

    private val _selectedPlatform = MutableStateFlow(_preferredPlatform.value)
    val selectedPlatform: StateFlow<PlatformType> = _selectedPlatform.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.TRENDING)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GameDetail>>(emptyList())
    val searchResults: StateFlow<List<GameDetail>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedGame = MutableStateFlow<GameDetail?>(null)
    val selectedGame: StateFlow<GameDetail?> = _selectedGame.asStateFlow()

    private val _selectedGameReviews = MutableStateFlow<List<UserReview>>(emptyList())
    val selectedGameReviews: StateFlow<List<UserReview>> = _selectedGameReviews.asStateFlow()

    private val _isInWishlist = MutableStateFlow(false)
    val isInWishlist: StateFlow<Boolean> = _isInWishlist.asStateFlow()

    private val _gameCollectionTypes = MutableStateFlow<Set<String>>(emptySet())
    val gameCollectionTypes: StateFlow<Set<String>> = _gameCollectionTypes.asStateFlow()

    private val _activePurchaseTab = MutableStateFlow(CopyType.DIGITAL)
    val activePurchaseTab: StateFlow<CopyType> = _activePurchaseTab.asStateFlow()

    private val _selectedCollectionFilter = MutableStateFlow("ALL")
    val selectedCollectionFilter: StateFlow<String> = _selectedCollectionFilter.asStateFlow()

    val wishlistItems: StateFlow<List<WishlistItemEntity>> = repository.getWishlistFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCollections: StateFlow<List<UserCollectionEntity>> = repository.getCollectionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertHistory: StateFlow<List<AlertHistoryEntity>> = repository.getAlertHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        performSearch()
    }

    fun setCountry(country: CountryInfo) {
        _selectedCountry.value = country
        // Refresh selected game if currently viewed
        _selectedGame.value?.let { current ->
            loadGameDetail(current.id)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch()
    }

    fun setPlatform(platform: PlatformType) {
        _selectedPlatform.value = platform
        performSearch()
    }

    fun setPreferredPlatform(platform: PlatformType, updateActiveFilter: Boolean = true) {
        _preferredPlatform.value = platform
        prefs.edit().putString("preferred_platform", platform.name).apply()
        if (updateActiveFilter) {
            setPlatform(platform)
        }
    }

    fun getEffectivePlatform(): PlatformType {
        return if (_selectedPlatform.value != PlatformType.ALL) {
            _selectedPlatform.value
        } else {
            _preferredPlatform.value
        }
    }

    fun setSort(sort: SortOption) {
        _selectedSort.value = sort
        applySorting()
    }

    fun setActivePurchaseTab(type: CopyType) {
        _activePurchaseTab.value = type
    }

    fun setCollectionFilter(filter: String) {
        _selectedCollectionFilter.value = filter
    }

    private fun performSearch() {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = repository.searchGames(_searchQuery.value, _selectedPlatform.value)
                _searchResults.value = results
                applySorting()
            } finally {
                _isSearching.value = false
            }
        }
    }

    private fun applySorting() {
        val current = _searchResults.value.toMutableList()
        val sorted = when (_selectedSort.value) {
            SortOption.TRENDING -> {
                val trimmed = _searchQuery.value.trim().lowercase()
                current.sortedWith(
                    compareByDescending<GameDetail> { game ->
                        val titleLower = game.title.lowercase().trim()
                        var score = game.popularityScore

                        if (trimmed.isNotEmpty()) {
                            if (titleLower == trimmed) score += 50000
                            else if (titleLower.startsWith(trimmed)) score += 25000
                            else if (titleLower.contains(trimmed)) score += 10000
                        }

                        val year = game.releaseDate.take(4).toIntOrNull()
                            ?: game.releaseDate.takeLast(4).toIntOrNull() ?: 1990
                        if (year >= 2026) score += 30000
                        else if (year >= 2024) score += 10000

                        score
                    }.thenByDescending { it.ratingsCount }
                     .thenByDescending { it.metacriticScore }
                     .thenByDescending { it.userRating }
                )
            }
            SortOption.DISCOUNT -> current.sortedByDescending {
                val discount = ((it.basePriceUsd - it.lowestHistoricalPriceUsd) / it.basePriceUsd) * 100
                discount
            }
            SortOption.METACRITIC -> current.sortedByDescending { it.metacriticScore }
            SortOption.PRICE_LOW -> current.sortedBy { it.lowestHistoricalPriceUsd }
        }
        _searchResults.value = sorted
    }

    fun loadGameDetail(gameId: String) {
        viewModelScope.launch {
            val game = repository.getGameDetail(gameId, _selectedCountry.value)
            _selectedGame.value = game

            // Observe wishlist status
            repository.isInWishlistFlow(gameId).collectLatest { inList ->
                _isInWishlist.value = inList
            }
        }

        viewModelScope.launch {
            // Load reviews
            val initial = repository.getInitialReviews(gameId)
            repository.getReviewsForGameFlow(gameId).collectLatest { dbReviews ->
                val convertedDb = dbReviews.map {
                    UserReview(
                        id = it.id,
                        gameId = it.gameId,
                        reviewerName = it.reviewerName,
                        rating = it.rating,
                        platform = it.platform,
                        hoursPlayed = it.hoursPlayed,
                        reviewText = it.reviewText,
                        pros = if (it.pros.isNotBlank()) it.pros.split(" | ") else emptyList(),
                        cons = if (it.cons.isNotBlank()) it.cons.split(" | ") else emptyList(),
                        date = "Recently",
                        isVerifiedGamer = it.isVerified
                    )
                }
                _selectedGameReviews.value = convertedDb + initial
            }
        }

        viewModelScope.launch {
            // Check collections
            repository.getCollectionsForGameFlow(gameId).collectLatest { list ->
                _gameCollectionTypes.value = list.map { it.collectionType }.toSet()
            }
        }
    }

    fun toggleWishlist(
        game: GameDetail,
        targetPrice: Double? = null,
        notes: String = "",
        priority: String = "MEDIUM"
    ) {
        viewModelScope.launch {
            val currentlyInWishlist = repository.isGameInWishlist(game.id)
            if (currentlyInWishlist) {
                repository.removeFromWishlist(game.id)
            } else {
                val regionalDeal = game.getDisplayLowestPrice(_selectedCountry.value)
                val defaultTarget = targetPrice ?: (regionalDeal * 0.85)
                repository.addToWishlist(game, defaultTarget, _selectedCountry.value, notes, priority)
            }
        }
    }

    fun removeFromWishlist(gameId: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(gameId)
        }
    }

    fun updateWishlistTargetPrice(gameId: String, newPrice: Double) {
        viewModelScope.launch {
            repository.updateWishlistTargetPrice(gameId, newPrice)
        }
    }

    fun updateWishlistNotes(gameId: String, notes: String) {
        viewModelScope.launch {
            repository.updateWishlistNotes(gameId, notes)
        }
    }

    fun updateWishlistPriority(gameId: String, priority: String) {
        viewModelScope.launch {
            repository.updateWishlistPriority(gameId, priority)
        }
    }

    fun toggleGameCollection(game: GameDetail, collectionType: String) {
        viewModelScope.launch {
            if (_gameCollectionTypes.value.contains(collectionType)) {
                repository.removeFromCollection(game.id, collectionType)
            } else {
                repository.addToCollection(
                    gameId = game.id,
                    title = game.title,
                    posterUrl = game.posterUrl,
                    type = collectionType,
                    rating = game.userRating,
                    platform = game.platforms.firstOrNull()?.displayName ?: "All"
                )
            }
        }
    }

    fun submitUserReview(
        gameId: String,
        reviewerName: String,
        rating: Float,
        platform: String,
        hours: Int,
        reviewText: String,
        pros: List<String>,
        cons: List<String>
    ) {
        viewModelScope.launch {
            repository.addReview(gameId, reviewerName, rating, platform, hours, reviewText, pros, cons)
        }
    }

    fun triggerPriceDropTest(gameId: String, onPriceDropped: ((Double, Double) -> Unit)? = null) {
        viewModelScope.launch {
            val oldGame = _selectedGame.value
            val country = _selectedCountry.value
            val oldPrice = oldGame?.getDisplayLowestPrice(country) ?: CountryPricingManager.getRegionalBasePrice(59.99, country.code)
            val updated = repository.triggerSimulatedPriceDrop(gameId, country)
            if (updated != null) {
                _selectedGame.value = updated
                val newPrice = updated.getDisplayLowestPrice(country)
                onPriceDropped?.invoke(oldPrice, newPrice)
                // Also refresh search results list so discounted price reflects across app
                performSearch()
            }
        }
    }

    fun resetPriceTest(gameId: String) {
        viewModelScope.launch {
            val restored = repository.resetPriceToBaseline(gameId, _selectedCountry.value)
            if (restored != null) {
                _selectedGame.value = restored
                performSearch()
            }
        }
    }

    fun triggerReleaseAlertTest(gameId: String) {
        viewModelScope.launch {
            repository.triggerSimulatedReleaseAlert(gameId)
        }
    }

    fun clearAlerts() {
        viewModelScope.launch {
            repository.clearAlertHistory()
        }
    }

    fun buildShareCollectionText(filterType: String): String {
        val filtered = if (filterType == "ALL") {
            allCollections.value
        } else {
            allCollections.value.filter { it.collectionType == filterType }
        }

        val typeLabel = when (filterType) {
            "PLAYING" -> "🎮 Currently Playing"
            "BACKLOG" -> "📚 Gaming Backlog"
            "COMPLETED" -> "🏆 Completed Hall of Fame"
            "FAVORITE" -> "⭐ All-Time Favorites"
            else -> "🎮 My Game Collection"
        }

        val sb = StringBuilder()
        sb.appendLine("═══════════════════════════")
        sb.appendLine("🕹️ GameRadar Collection: $typeLabel")
        sb.appendLine("Total Games: ${filtered.size}")
        sb.appendLine("═══════════════════════════\n")

        if (filtered.isEmpty()) {
            sb.appendLine("No games currently in this collection.")
        } else {
            filtered.forEachIndexed { i, item ->
                sb.appendLine("${i + 1}. ${item.title}")
                sb.appendLine("   Platform: ${item.platform} | Rating: ★ ${item.rating}/5")
                if (item.notes.isNotBlank()) {
                    sb.appendLine("   Note: ${item.notes}")
                }
            }
        }
        sb.appendLine("\nTrack games, deals, & price drops on GameRadar!")
        return sb.toString()
    }

    fun buildShareGameText(game: GameDetail): String {
        val country = _selectedCountry.value
        val localBestPrice = "${country.currencySymbol}${"%.2f".format(game.getDisplayLowestPrice(country))}"
        val origPrice = "${country.currencySymbol}${"%.2f".format(game.getDisplayBasePrice(country))}"

        return """
            🎮 Check out ${game.title} on GameRadar!
            ⭐ Metacritic: ${game.metacriticScore}/100 | Gamer Rating: ${game.userRating}/5
            🏷️ Best Deal: $localBestPrice (Retail $origPrice) in ${country.name} ${country.flagEmoji}
            🎯 Platforms: ${game.platforms.joinToString(", ") { it.displayName }}
            📺 Watch Trailer: https://www.youtube.com/watch?v=${game.trailerYoutubeId}
            
            Compare Digital vs Physical copies on GameRadar!
        """.trimIndent()
    }
}
