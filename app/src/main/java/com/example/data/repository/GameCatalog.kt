package com.example.data.repository

import com.example.data.model.CopyType
import com.example.data.model.GameDetail
import com.example.data.model.PlatformType
import com.example.data.model.SpecDetails
import com.example.data.model.StoreOffer
import com.example.data.model.SystemRequirements
import com.example.data.model.UserReview

object GameCatalog {

    val FEATURED_GAMES: List<GameDetail> = listOf(
        GameDetail(
            id = "elden-ring",
            title = "Elden Ring: Shadow of the Erdtree",
            bannerUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.PC, PlatformType.PS5, PlatformType.XBOX),
            developer = "FromSoftware Inc.",
            publisher = "Bandai Namco Entertainment",
            releaseDate = "June 21, 2024",
            metacriticScore = 95,
            userRating = 4.8f,
            esrbRating = "M (Mature 17+)",
            genres = listOf("Action RPG", "Open World", "Souls-like", "Dark Fantasy"),
            description = "Guided by Empyrean Miquella, players are beckoned to the Land of Shadow, a place obscured by the Erdtree where the goddess Marika first set foot. In these strange new lands, uncover dark secrets of the world alongside other characters with ulterior motives.",
            trailerYoutubeId = "qLZenOn7WUo",
            trailerTitle = "Official Gameplay Reveal Trailer",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?q=80&w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 34.99,
            systemRequirements = SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = "Intel Core i5-8400 or AMD Ryzen 3 3300X",
                    memory = "12 GB RAM",
                    graphics = "NVIDIA GeForce GTX 1060 3GB or AMD Radeon RX 580 4GB",
                    storage = "60 GB available space (SSD Recommended)"
                ),
                recommended = SpecDetails(
                    os = "Windows 11 / 10 64-bit",
                    processor = "Intel Core i7-8700K or AMD Ryzen 5 3600X",
                    memory = "16 GB RAM",
                    graphics = "NVIDIA GeForce RTX 2070 8GB or AMD Radeon RX 5700 XT 8GB",
                    storage = "60 GB SSD"
                )
            )
        ),
        GameDetail(
            id = "cyberpunk-2077",
            title = "Cyberpunk 2077: Ultimate Edition",
            bannerUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.PC, PlatformType.PS5, PlatformType.XBOX),
            developer = "CD Projekt Red",
            publisher = "CD Projekt",
            releaseDate = "December 5, 2023",
            metacriticScore = 90,
            userRating = 4.6f,
            esrbRating = "M (Mature 17+)",
            genres = listOf("RPG", "Cyberpunk", "Open World", "Sci-Fi", "FPS"),
            description = "Immerse yourself into every story the world of the dark future has to offer, featuring every gameplay update and all previously released content in one package — including the acclaimed spy-thriller expansion Phantom Liberty.",
            trailerYoutubeId = "UnA7tepsc7s",
            trailerTitle = "Phantom Liberty Official Cinematic Trailer",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1552824722-ddab1374e622?q=80&w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 29.99,
            systemRequirements = SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = "Intel Core i7-6700 or AMD Ryzen 5 1600",
                    memory = "12 GB RAM",
                    graphics = "NVIDIA GeForce GTX 1060 6GB or AMD Radeon RX 580 8GB",
                    storage = "70 GB SSD"
                ),
                recommended = SpecDetails(
                    os = "Windows 11 / 10 64-bit",
                    processor = "Intel Core i7-12700 or AMD Ryzen 7 7800X3D",
                    memory = "16 GB RAM",
                    graphics = "NVIDIA GeForce RTX 3070 8GB or AMD Radeon RX 6800 XT",
                    storage = "70 GB NVMe SSD"
                )
            )
        ),
        GameDetail(
            id = "zelda-totk",
            title = "The Legend of Zelda: Tears of the Kingdom",
            bannerUrl = "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.SWITCH),
            developer = "Nintendo EPD",
            publisher = "Nintendo",
            releaseDate = "May 12, 2023",
            metacriticScore = 96,
            userRating = 4.9f,
            esrbRating = "E10+ (Everyone 10+)",
            genres = listOf("Action-Adventure", "Open World", "Fantasy", "Puzzle"),
            description = "An epic adventure across the land and skies of Hyrule awaits in The Legend of Zelda: Tears of the Kingdom for Nintendo Switch. In this sequel, harness the power of Link's new abilities to craft wild vehicles, ascend through terrain, and unravel ancient mysteries.",
            trailerYoutubeId = "uHGShqcAHlQ",
            trailerTitle = "Official Launch Trailer",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 69.99,
            lowestHistoricalPriceUsd = 49.99,
            systemRequirements = null // Switch exclusive
        ),
        GameDetail(
            id = "baldurs-gate-3",
            title = "Baldur's Gate 3",
            bannerUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.PC, PlatformType.PS5, PlatformType.XBOX),
            developer = "Larian Studios",
            publisher = "Larian Studios",
            releaseDate = "August 3, 2023",
            metacriticScore = 96,
            userRating = 4.9f,
            esrbRating = "M (Mature 17+)",
            genres = listOf("CRPG", "Tactical Turn-Based", "Dungeons & Dragons", "Story Rich"),
            description = "Gather your party and return to the Forgotten Realms in a tale of fellowship and betrayal, sacrifice and survival, and the lure of absolute power. Mysterious abilities are awakening inside you, drawn from a mind flayer parasite planted in your brain.",
            trailerYoutubeId = "1T22wN1BIzU",
            trailerTitle = "Official Launch Trailer",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 47.99,
            systemRequirements = SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = "Intel I5 4690 / AMD FX 8350",
                    memory = "8 GB RAM",
                    graphics = "Nvidia GTX 970 / RX 480 (4GB+ VRAM)",
                    storage = "150 GB available space (SSD required)"
                ),
                recommended = SpecDetails(
                    os = "Windows 10 / 11 64-bit",
                    processor = "Intel i7 8700K / AMD r5 3600",
                    memory = "16 GB RAM",
                    graphics = "Nvidia 2060 Super / RX 5700 XT (8GB+ VRAM)",
                    storage = "150 GB SSD"
                )
            )
        ),
        GameDetail(
            id = "spiderman-2",
            title = "Marvel's Spider-Man 2",
            bannerUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.PS5, PlatformType.PC),
            developer = "Insomniac Games",
            publisher = "Sony Interactive Entertainment",
            releaseDate = "October 20, 2023",
            metacriticScore = 90,
            userRating = 4.7f,
            esrbRating = "T (Teen)",
            genres = listOf("Action", "Superhero", "Open World", "Beat 'em up"),
            description = "Spider-Men Peter Parker and Miles Morales return for an exciting new adventure in the critically acclaimed Marvel's Spider-Man franchise. Swing, jump and utilize the new Web Wings to travel across Marvel's New York, quickly switching between both Spider-Men.",
            trailerYoutubeId = "9fVYKs4amLE",
            trailerTitle = "Gameplay Reveal",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 69.99,
            lowestHistoricalPriceUsd = 49.99,
            systemRequirements = SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = "Intel Core i3-8100 or AMD Ryzen 3 1300X",
                    memory = "16 GB RAM",
                    graphics = "NVIDIA GeForce GTX 1650 or AMD Radeon RX 5500 XT",
                    storage = "85 GB SSD"
                ),
                recommended = SpecDetails(
                    os = "Windows 11 64-bit",
                    processor = "Intel Core i5-11400 or AMD Ryzen 5 5600",
                    memory = "16 GB RAM",
                    graphics = "NVIDIA GeForce RTX 3060 or AMD Radeon RX 6700 XT",
                    storage = "85 GB SSD"
                )
            )
        ),
        GameDetail(
            id = "black-myth-wukong",
            title = "Black Myth: Wukong",
            bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.PC, PlatformType.PS5),
            developer = "Game Science",
            publisher = "Game Science",
            releaseDate = "August 20, 2024",
            metacriticScore = 82,
            userRating = 4.8f,
            esrbRating = "M (Mature 17+)",
            genres = listOf("Action RPG", "Mythology", "Souls-like", "Fast-Paced"),
            description = "Black Myth: Wukong is an action RPG rooted in Chinese mythology. The story is based on Journey to the West, one of the Four Great Classical Novels of Chinese literature. You shall set out as the Destined One to venture into the challenges and marvels ahead.",
            trailerYoutubeId = "oXZbEa1yqR8",
            trailerTitle = "Official Launch Trailer",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 49.99,
            systemRequirements = SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = "Intel Core i5-8400 / AMD Ryzen 5 1600",
                    memory = "16 GB RAM",
                    graphics = "NVIDIA GeForce GTX 1060 6GB / AMD Radeon RX 580 8GB",
                    storage = "130 GB SSD"
                ),
                recommended = SpecDetails(
                    os = "Windows 10 / 11 64-bit",
                    processor = "Intel Core i7-9700 / AMD Ryzen 5 5500",
                    memory = "16 GB RAM",
                    graphics = "NVIDIA GeForce RTX 2060 / AMD Radeon RX 5700 XT",
                    storage = "130 GB SSD"
                )
            )
        ),
        GameDetail(
            id = "monster-hunter-wilds",
            title = "Monster Hunter Wilds",
            bannerUrl = "https://images.unsplash.com/photo-1552824722-ddab1374e622?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.PC, PlatformType.PS5, PlatformType.XBOX),
            developer = "Capcom",
            publisher = "Capcom",
            releaseDate = "February 28, 2025",
            metacriticScore = 89,
            userRating = 4.7f,
            esrbRating = "T (Teen)",
            genres = listOf("Action", "Hunting RPG", "Multiplayer Co-op", "Open World"),
            description = "The unbridled force of nature runs wild and relentless, with environments transforming drastically from one moment to the next. This is a story of monsters and humans and their struggles to live in harmony in a world of duality.",
            trailerYoutubeId = "e57Wj6G3Jp8",
            trailerTitle = "State of Play Official Trailer",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 69.99,
            lowestHistoricalPriceUsd = 59.99,
            systemRequirements = SystemRequirements(
                minimum = SpecDetails(
                    os = "Windows 10 64-bit",
                    processor = "Intel Core i5-10600 or AMD Ryzen 5 3600",
                    memory = "16 GB RAM",
                    graphics = "NVIDIA GeForce GTX 1660 Super or AMD Radeon RX 5600 XT",
                    storage = "140 GB SSD"
                ),
                recommended = SpecDetails(
                    os = "Windows 11 64-bit",
                    processor = "Intel Core i7-12700K or AMD Ryzen 7 7700X",
                    memory = "32 GB RAM",
                    graphics = "NVIDIA GeForce RTX 4070 or AMD Radeon RX 7800 XT",
                    storage = "140 GB NVMe SSD"
                )
            )
        ),
        GameDetail(
            id = "astro-bot",
            title = "Astro Bot",
            bannerUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=1200&auto=format&fit=crop",
            posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=600&auto=format&fit=crop",
            platforms = listOf(PlatformType.PS5),
            developer = "Team ASOBI",
            publisher = "Sony Interactive Entertainment",
            releaseDate = "September 6, 2024",
            metacriticScore = 94,
            userRating = 4.9f,
            esrbRating = "E10+ (Everyone 10+)",
            genres = listOf("3D Platformer", "Family", "Adventure"),
            description = "Join ASTRO on a supersized space adventure! The PS5 mothership has been attacked by ASTRO’s long-standing alien nemesis, leaving its crew scattered across multiple galaxies. Explore over 50 planets full of fun, danger, and surprises.",
            trailerYoutubeId = "o3V-GsvszvQ",
            trailerTitle = "Official Launch Trailer",
            screenshots = listOf(
                "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?q=80&w=800&auto=format&fit=crop"
            ),
            basePriceUsd = 59.99,
            lowestHistoricalPriceUsd = 49.99,
            systemRequirements = null
        )
    )

    val INITIAL_REVIEWS: List<UserReview> = listOf(
        UserReview(
            id = "rev-1",
            gameId = "elden-ring",
            reviewerName = "ValkyrieBlade",
            rating = 5.0f,
            platform = "PC / Steam",
            hoursPlayed = 165,
            reviewText = "An absolute masterpiece. The Land of Shadow expansion took everything great about the base game and perfected the boss encounters and environmental storytelling. Stunning weapon variety.",
            pros = listOf("Breathtaking world design", "Challenging boss battles", "Deep build customization"),
            cons = listOf("Performance drops in dense forests"),
            date = "3 days ago",
            isVerifiedGamer = true
        ),
        UserReview(
            id = "rev-2",
            gameId = "elden-ring",
            reviewerName = "DarkSoul_Vet",
            rating = 4.5f,
            platform = "PlayStation 5",
            hoursPlayed = 210,
            reviewText = "Messmer and the final boss tested every limit of patience I had. 10/10 level design. Scadutree fragment system is great once you understand the scaling.",
            pros = listOf("Huge new map", "Epic soundtrack", "Rewarding exploration"),
            cons = listOf("High difficulty spike"),
            date = "1 week ago",
            isVerifiedGamer = true
        ),
        UserReview(
            id = "rev-3",
            gameId = "cyberpunk-2077",
            reviewerName = "NightCityChoom",
            rating = 5.0f,
            platform = "PC / Steam",
            hoursPlayed = 140,
            reviewText = "Update 2.0 and Phantom Liberty completely redeemed this game. Dogtown is packed with nuance, Idris Elba's performance as Solomon Reed is unbelievable, and the ray tracing graphics set the gold standard.",
            pros = listOf("Incredible story & voice acting", "Overhauled perks & combat", "Sublime visuals"),
            cons = listOf("Demanding on older GPUs"),
            date = "5 days ago",
            isVerifiedGamer = true
        ),
        UserReview(
            id = "rev-4",
            gameId = "baldurs-gate-3",
            reviewerName = "Tav_Paladin",
            rating = 5.0f,
            platform = "PC / Steam",
            hoursPlayed = 280,
            reviewText = "The best RPG in the last decade. The sheer number of branching choices, reactive companion interactions, and tactical possibilities is unprecedented.",
            pros = listOf("Limitless choice & consequence", "Memorable companion stories", "Deep tactical depth"),
            cons = listOf("Act 3 has occasional slowdowns"),
            date = "2 weeks ago",
            isVerifiedGamer = true
        ),
        UserReview(
            id = "rev-5",
            gameId = "zelda-totk",
            reviewerName = "HyruleCrafter",
            rating = 5.0f,
            platform = "Nintendo Switch",
            hoursPlayed = 185,
            reviewText = "The physics engine with Ultrahand and Fuse is witchcraft running on the Switch hardware. The Depths and Sky islands expand the sense of wonder tenfold.",
            pros = listOf("Ultrahand creativity", "Massive underground depths", "Satisfying puzzles"),
            cons = listOf("30 FPS hardware limits"),
            date = "1 month ago",
            isVerifiedGamer = true
        )
    )
}
