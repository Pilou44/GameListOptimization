package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.data.scraper.Scraper
import com.wechantloup.gamelistoptimization.data.scraper.model.ScraperSystem
import com.wechantloup.gamelistoptimization.utils.isEuRegion
import com.wechantloup.gamelistoptimization.utils.isJpRegion
import com.wechantloup.gamelistoptimization.utils.isUsRegion
import java.util.Locale

class ScrapPlatformUseCase(private val scraper: Scraper) {

    suspend fun scrapPlatform(platform: Platform): Platform {
        return try {
            val system = platform.system
            scraper
                .getSystem(system)
                .toPlatform(system)
                .copy(
                    path = platform.path,
                    games = platform.games,
                    gamesBackup = platform.gamesBackup,
                )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to scrap $platform", e)
            platform
        }
    }

    private fun ScraperSystem.toPlatform(system: String): Platform {
        val userCountry = Locale.getDefault().country.lowercase()
        val name = if (userCountry.isEuRegion() && euName != null) {
            euName
        } else if (userCountry.isUsRegion() && usName != null) {
            usName
        } else if (userCountry.isJpRegion() && jpName != null) {
            jpName
        } else {
            euName ?: usName ?: jpName
        }
        return Platform(
            name = name ?: system,
            games = emptyList(),
            gamesBackup = null,
            path = "",
            system = system,
            extensions = extensions + "zip",
        )
    }

    companion object {
        private const val TAG = "ScrapPlatformUseCase"
    }
}
