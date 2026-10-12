package com.example.data.repository

import com.example.data.model.CheapSharkDealItem
import com.example.data.model.CopyType
import com.example.data.model.CountryInfo
import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.data.model.StoreOffer

object StoreOfferGenerator {

    private val CHEAPSHARK_STORE_NAMES = mapOf(
        "1" to "Steam",
        "2" to "GamersGate",
        "3" to "GreenManGaming",
        "7" to "GOG.com",
        "8" to "Origin / EA App",
        "11" to "Humble Store",
        "13" to "Uplay / Ubisoft",
        "15" to "Fanatical",
        "21" to "WinGameStore",
        "23" to "GameBillet",
        "25" to "Epic Games Store",
        "27" to "Gamesplanet",
        "29" to "Blizzard / Battle.net",
        "31" to "AllYouPlay",
        "34" to "Noctre",
        "35" to "DreamGame"
    )

    fun generateOffers(
        game: GameDetail,
        country: CountryInfo,
        cheapSharkDeals: List<CheapSharkDealItem> = emptyList()
    ): Pair<List<StoreOffer>, List<StoreOffer>> {
        val rate = country.exchangeRateToUsd
        val baseUsd = game.basePriceUsd
        val localBasePrice = baseUsd * rate

        val digitalOffers = mutableListOf<StoreOffer>()
        val physicalOffers = mutableListOf<StoreOffer>()

        // 1. Digital Offers from CheapShark if available
        if (cheapSharkDeals.isNotEmpty()) {
            cheapSharkDeals.forEachIndexed { index, deal ->
                val storeName = CHEAPSHARK_STORE_NAMES[deal.storeId] ?: "PC Store #${deal.storeId}"
                val dealPriceUsd = deal.price.toDoubleOrNull() ?: baseUsd
                val retailPriceUsd = deal.retailPrice.toDoubleOrNull() ?: baseUsd
                val savings = deal.savings?.toDoubleOrNull()?.toInt() ?: 0

                val localPrice = dealPriceUsd * rate
                val localRetail = retailPriceUsd * rate

                val storeUrl = "https://www.cheapshark.com/redirect?dealID=${deal.dealId}"

                digitalOffers.add(
                    StoreOffer(
                        id = "cs-${deal.dealId}-$index",
                        storeName = storeName,
                        copyType = CopyType.DIGITAL,
                        platform = PlatformType.PC,
                        price = localPrice,
                        originalPrice = localRetail,
                        discountPercent = savings,
                        inStock = true,
                        edition = "Digital Edition",
                        condition = "Instant Key / DRM Code",
                        purchaseUrl = storeUrl,
                        countryCode = country.code
                    )
                )
            }
        }

        // 2. Official Platform Digital Stores (PlayStation Store, Xbox Store, Nintendo eShop, Steam, Epic)
        game.platforms.forEach { platform ->
            when (platform) {
                PlatformType.PS5 -> {
                    val psDiscount = if (game.lowestHistoricalPriceUsd < baseUsd) 20 else 0
                    val psPrice = localBasePrice * (1.0 - psDiscount / 100.0)
                    digitalOffers.add(
                        StoreOffer(
                            id = "ps-store-${game.id}",
                            storeName = "PlayStation Store (${country.code})",
                            copyType = CopyType.DIGITAL,
                            platform = PlatformType.PS5,
                            price = psPrice,
                            originalPrice = localBasePrice,
                            discountPercent = psDiscount,
                            inStock = true,
                            edition = "Standard Digital",
                            condition = "Instant PSN Account License",
                            purchaseUrl = "https://store.playstation.com/search/${game.title.replace(' ', '+')}",
                            countryCode = country.code
                        )
                    )
                }
                PlatformType.XBOX -> {
                    val xbDiscount = if (game.lowestHistoricalPriceUsd < baseUsd) 15 else 0
                    val xbPrice = localBasePrice * (1.0 - xbDiscount / 100.0)
                    digitalOffers.add(
                        StoreOffer(
                            id = "xbox-store-${game.id}",
                            storeName = "Xbox Games Store (${country.code})",
                            copyType = CopyType.DIGITAL,
                            platform = PlatformType.XBOX,
                            price = xbPrice,
                            originalPrice = localBasePrice,
                            discountPercent = xbDiscount,
                            inStock = true,
                            edition = "Xbox Play Anywhere / Digital",
                            condition = "Instant Microsoft Key",
                            purchaseUrl = "https://www.xbox.com/search?q=${game.title.replace(' ', '+')}",
                            countryCode = country.code
                        )
                    )
                }
                PlatformType.SWITCH -> {
                    val nintendoDiscount = if (game.lowestHistoricalPriceUsd < baseUsd) 10 else 0
                    val nintendoPrice = localBasePrice * (1.0 - nintendoDiscount / 100.0)
                    digitalOffers.add(
                        StoreOffer(
                            id = "nintendo-eshop-${game.id}",
                            storeName = "Nintendo eShop (${country.code})",
                            copyType = CopyType.DIGITAL,
                            platform = PlatformType.SWITCH,
                            price = nintendoPrice,
                            originalPrice = localBasePrice,
                            discountPercent = nintendoDiscount,
                            inStock = true,
                            edition = "Digital eShop Download",
                            condition = "Instant Switch License",
                            purchaseUrl = "https://www.nintendo.com/search/#q=${game.title.replace(' ', '+')}",
                            countryCode = country.code
                        )
                    )
                }
                PlatformType.PC -> {
                    if (cheapSharkDeals.isEmpty()) {
                        val steamDiscount = if (game.lowestHistoricalPriceUsd < baseUsd) 25 else 0
                        val steamPrice = localBasePrice * (1.0 - steamDiscount / 100.0)
                        digitalOffers.add(
                            StoreOffer(
                                id = "steam-${game.id}",
                                storeName = "Steam Store",
                                copyType = CopyType.DIGITAL,
                                platform = PlatformType.PC,
                                price = steamPrice,
                                originalPrice = localBasePrice,
                                discountPercent = steamDiscount,
                                inStock = true,
                                edition = "Steam Key",
                                condition = "Steam PC Download",
                                purchaseUrl = "https://store.steampowered.com/search/?term=${game.title.replace(' ', '+')}",
                                countryCode = country.code
                            )
                        )
                        digitalOffers.add(
                            StoreOffer(
                                id = "epic-${game.id}",
                                storeName = "Epic Games Store",
                                copyType = CopyType.DIGITAL,
                                platform = PlatformType.PC,
                                price = localBasePrice,
                                originalPrice = localBasePrice,
                                discountPercent = 0,
                                inStock = true,
                                edition = "Epic Launcher Key",
                                condition = "Digital PC",
                                purchaseUrl = "https://store.epicgames.com/en-US/browse?q=${game.title.replace(' ', '+')}",
                                countryCode = country.code
                            )
                        )
                    }
                }
                else -> {}
            }
        }

        // 3. Country-Specific Physical Retailers
        val primaryConsole = game.platforms.firstOrNull { it != PlatformType.PC } ?: PlatformType.PC
        val physicalRetailers = when (country.code) {
            "GB" -> listOf(
                PhysicalStoreTemplate("GAME UK", 0.08, "Boxed Disc (Standard)", "New in Box", "https://www.game.co.uk"),
                PhysicalStoreTemplate("Amazon UK", 0.12, "Physical Edition with Slipcase", "Brand New", "https://www.amazon.co.uk"),
                PhysicalStoreTemplate("Argos UK", 0.05, "Physical Copy", "In Stock (Click & Collect)", "https://www.argos.co.uk"),
                PhysicalStoreTemplate("Currys PC World", 0.00, "Collector's Steelbook Edition", "Brand New", "https://www.currys.co.uk")
            )
            "CA" -> listOf(
                PhysicalStoreTemplate("Best Buy Canada", 0.10, "Physical Disc Edition", "Brand New", "https://www.bestbuy.ca"),
                PhysicalStoreTemplate("GameStop Canada", 0.15, "Pre-Owned Boxed Disc", "Pre-Owned / Refurbished", "https://www.gamestop.ca"),
                PhysicalStoreTemplate("Amazon Canada", 0.05, "Retail Edition", "Brand New Sealed", "https://www.amazon.ca"),
                PhysicalStoreTemplate("Walmart Canada", 0.00, "Standard Case Edition", "In Stock", "https://www.walmart.ca")
            )
            "EU" -> listOf(
                PhysicalStoreTemplate("MediaMarkt", 0.10, "Boxed Disc Edition", "Neu & OVP", "https://www.mediamarkt.de"),
                PhysicalStoreTemplate("Amazon EU", 0.12, "Physical Edition", "Brand New", "https://www.amazon.de"),
                PhysicalStoreTemplate("Saturn", 0.08, "Standard Retail Box", "In Stock", "https://www.saturn.de"),
                PhysicalStoreTemplate("Micromania", 0.05, "Steelbook Edition", "Neuf Sous Blister", "https://www.micromania.fr")
            )
            "AU" -> listOf(
                PhysicalStoreTemplate("JB Hi-Fi Australia", 0.15, "Standard Retail Disc", "Brand New Sealed", "https://www.jbhifi.com.au"),
                PhysicalStoreTemplate("EB Games Australia", 0.05, "Pre-Owned / Trade-In", "EB Pre-Owned Inspected", "https://www.ebgames.com.au"),
                PhysicalStoreTemplate("Amazon Australia", 0.12, "Physical Edition", "Prime Next Day", "https://www.amazon.com.au"),
                PhysicalStoreTemplate("Big W", 0.10, "Boxed Case", "In Stock", "https://www.bigw.com.au")
            )
            "JP" -> listOf(
                PhysicalStoreTemplate("Bic Camera", 0.08, "Package Edition (パッケージ版)", "Brand New Sealed", "https://www.biccamera.com"),
                PhysicalStoreTemplate("Yodobashi Camera", 0.10, "Standard Pack (ヨドバシ)", "In Stock 10% Points", "https://www.yodobashi.com"),
                PhysicalStoreTemplate("Amazon Japan", 0.12, "Physical Package with bonus", "Amazon Limited", "https://www.amazon.co.jp"),
                PhysicalStoreTemplate("GEO (ゲオ)", 0.20, "Used / Pre-Owned (中古品)", "Inspected Grade A", "https://ec.geo-online.co.jp")
            )
            else -> listOf( // "US" default
                PhysicalStoreTemplate("Best Buy", 0.10, "Physical Disc Edition", "Brand New Sealed", "https://www.bestbuy.com"),
                PhysicalStoreTemplate("GameStop", 0.18, "Pre-Owned Disc in Case", "Guaranteed to Play", "https://www.gamestop.com"),
                PhysicalStoreTemplate("Amazon US", 0.08, "Standard Physical Edition", "Prime Delivery", "https://www.amazon.com"),
                PhysicalStoreTemplate("Target", 0.05, "Standard Case Edition", "In Store & Shipping", "https://www.target.com"),
                PhysicalStoreTemplate("Walmart", 0.07, "Boxed Edition", "Brand New", "https://www.walmart.com")
            )
        }

        physicalRetailers.forEachIndexed { idx, item ->
            val discPrice = localBasePrice * (1.0 - item.discountRate)
            val discountPercent = (item.discountRate * 100).toInt()
            val queryTitle = game.title.replace(' ', '+')

            physicalOffers.add(
                StoreOffer(
                    id = "phys-${country.code}-$idx-${game.id}",
                    storeName = item.name,
                    copyType = CopyType.PHYSICAL,
                    platform = primaryConsole,
                    price = discPrice,
                    originalPrice = localBasePrice,
                    discountPercent = discountPercent,
                    inStock = true,
                    edition = item.edition,
                    condition = item.condition,
                    purchaseUrl = "${item.baseUrl}/search?q=$queryTitle",
                    countryCode = country.code
                )
            )
        }

        return Pair(digitalOffers, physicalOffers)
    }

    private data class PhysicalStoreTemplate(
        val name: String,
        val discountRate: Double,
        val edition: String,
        val condition: String,
        val baseUrl: String
    )
}
