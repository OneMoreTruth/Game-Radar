package com.example.data.repository

import kotlin.math.roundToInt

/**
 * Regional Video Game Pricing Manager.
 * Video game prices in different countries follow standardized regional retail MSRP tiers
 * rather than simple raw currency exchange rates.
 *
 * For example:
 * - A $69.99 USD next-gen AAA game in the UK retails at £69.99 (or £64.99/£59.99 on PC/Steam), NOT £55.29!
 * - In the EU, it retails at €79.99 (Console) or €69.99 (PC).
 * - In Canada, it retails at $89.99 CAD.
 * - In Australia, it retails at $109.95 AUD.
 * - In Japan, it retails at ¥9,680 JPY.
 *
 * Furthermore, discount pricing must be calculated against the authentic regional base tier
 * rather than converting a discounted USD price to foreign currency.
 */
object CountryPricingManager {

    /**
     * Standard MSRP mapping tiers for video games in major gaming regions.
     */
    private val REGIONAL_TIERS_UK = listOf(
        79.99 to 74.99, // Ultimate / Deluxe
        69.99 to 69.99, // Standard Next-Gen AAA (PS5, XSX)
        59.99 to 49.99, // Standard AAA (Last-Gen / PC)
        49.99 to 44.99, // Mid-tier / AA
        39.99 to 34.99,
        29.99 to 24.99,
        19.99 to 16.99,
        14.99 to 12.99,
        9.99 to 8.49,
        4.99 to 4.29
    )

    private val REGIONAL_TIERS_EU = listOf(
        79.99 to 89.99,
        69.99 to 79.99, // Console next-gen
        59.99 to 69.99,
        49.99 to 49.99,
        39.99 to 39.99,
        29.99 to 29.99,
        19.99 to 19.99,
        14.99 to 14.99,
        9.99 to 9.99,
        4.99 to 4.99
    )

    private val REGIONAL_TIERS_CA = listOf(
        79.99 to 99.99,
        69.99 to 89.99,
        59.99 to 79.99,
        49.99 to 64.99,
        39.99 to 49.99,
        29.99 to 39.99,
        19.99 to 26.99,
        14.99 to 19.99,
        9.99 to 13.49,
        4.99 to 6.99
    )

    private val REGIONAL_TIERS_AU = listOf(
        79.99 to 124.95,
        69.99 to 109.95,
        59.99 to 89.95,
        49.99 to 74.95,
        39.99 to 59.95,
        29.99 to 44.95,
        19.99 to 29.95,
        14.99 to 22.95,
        9.99 to 14.95,
        4.99 to 7.95
    )

    private val REGIONAL_TIERS_JP = listOf(
        79.99 to 11000.0,
        69.99 to 9680.0,
        59.99 to 8580.0,
        49.99 to 6980.0,
        39.99 to 5280.0,
        29.99 to 3980.0,
        19.99 to 2480.0,
        14.99 to 1980.0,
        9.99 to 1280.0,
        4.99 to 680.0
    )

    /**
     * Get the authentic MSRP base price for a game in the user's selected country.
     */
    fun getRegionalBasePrice(basePriceUsd: Double, countryCode: String): Double {
        if (basePriceUsd <= 0.0) return 0.0

        val mappedPrice = when (countryCode) {
            "GB" -> matchRegionalTier(basePriceUsd, REGIONAL_TIERS_UK, fallbackRate = 0.85)
            "EU" -> matchRegionalTier(basePriceUsd, REGIONAL_TIERS_EU, fallbackRate = 1.05)
            "CA" -> matchRegionalTier(basePriceUsd, REGIONAL_TIERS_CA, fallbackRate = 1.35)
            "AU" -> matchRegionalTier(basePriceUsd, REGIONAL_TIERS_AU, fallbackRate = 1.55)
            "JP" -> matchRegionalTier(basePriceUsd, REGIONAL_TIERS_JP, fallbackRate = 150.0)
            else -> basePriceUsd // US / Default
        }

        return formatRegionalPrice(mappedPrice, countryCode)
    }

    /**
     * Calculate regional deal/discount price.
     * Preserves the authentic percentage discount against the regional base MSRP.
     */
    fun getRegionalDealPrice(basePriceUsd: Double, dealPriceUsd: Double, countryCode: String): Double {
        if (dealPriceUsd <= 0.0) return 0.0
        val regionalBase = getRegionalBasePrice(basePriceUsd, countryCode)

        // If there's no discount, return regional base
        if (dealPriceUsd >= basePriceUsd || basePriceUsd <= 0.0) {
            return regionalBase
        }

        // Calculate discount ratio (e.g. 50% off)
        val discountRatio = (dealPriceUsd / basePriceUsd).coerceIn(0.05, 1.0)
        val rawDealPrice = regionalBase * discountRatio

        return formatRegionalPrice(rawDealPrice, countryCode)
    }

    /**
     * Auto-imported price calculation for Loaded.com (formerly CDKeys.com).
     * Loaded.com specializes in deep key discounts:
     * - Brand new 2026/upcoming releases: ~18% - 28% off regional MSRP
     * - 2024-2025 hits: ~30% - 48% off regional MSRP
     * - Classic / evergreen titles: ~50% - 75% off regional MSRP
     * - Games already on sale: Loaded beats lowest retail by 8-18% extra!
     */
    fun getLoadedKeyPrice(
        basePriceUsd: Double,
        dealPriceUsd: Double,
        countryCode: String,
        releaseYear: Int = 2025
    ): Double {
        val regionalBase = getRegionalBasePrice(basePriceUsd, countryCode)
        val regionalDeal = getRegionalDealPrice(basePriceUsd, dealPriceUsd, countryCode)

        val loadedPriceRaw = if (regionalDeal < regionalBase) {
            // If already on discount at Steam/Retail, Loaded.com typically undercuts it by 10-15%
            (regionalDeal * 0.88).coerceAtLeast(regionalBase * 0.12)
        } else {
            // Base price discount tier based on release age
            val discountFactor = when {
                releaseYear >= 2026 -> 0.78 // ~22% off for brand new / pre-orders
                releaseYear == 2025 -> 0.68 // ~32% off
                releaseYear == 2024 -> 0.58 // ~42% off
                releaseYear in 2020..2023 -> 0.42 // ~58% off
                else -> 0.32 // ~68% off for older classics
            }
            regionalBase * discountFactor
        }

        return formatRegionalPrice(loadedPriceRaw, countryCode)
    }

    /**
     * Format price to clean regional retail standard (e.g., .99 or .95 or round Yen).
     */
    fun formatRegionalPrice(price: Double, countryCode: String): Double {
        if (countryCode == "JP") {
            // Japan prices are whole integers (Yen)
            return (price / 10.0).roundToInt() * 10.0
        }

        if (countryCode == "AU") {
            // Australia often uses .95 or .00
            val whole = price.toInt()
            val decimal = price - whole
            return if (decimal in 0.85..0.99) {
                whole + 0.95
            } else {
                ((price * 100).roundToInt()) / 100.0
            }
        }

        // Standard 2 decimal places round
        return ((price * 100).roundToInt()) / 100.0
    }

    private fun matchRegionalTier(
        baseUsd: Double,
        tiers: List<Pair<Double, Double>>,
        fallbackRate: Double
    ): Double {
        // Direct matching if close to standard MSRP tier (+/- 1.50)
        for ((usdTier, localTier) in tiers) {
            if (kotlin.math.abs(baseUsd - usdTier) <= 1.50) {
                return localTier
            }
        }

        // Closest tier interpolation
        val lowerTier = tiers.lastOrNull { it.first <= baseUsd }
        val upperTier = tiers.firstOrNull { it.first >= baseUsd }

        if (lowerTier != null && upperTier != null && lowerTier.first != upperTier.first) {
            val progress = (baseUsd - lowerTier.first) / (upperTier.first - lowerTier.first)
            return lowerTier.second + progress * (upperTier.second - lowerTier.second)
        }

        return lowerTier?.second ?: upperTier?.second ?: (baseUsd * fallbackRate)
    }
}
