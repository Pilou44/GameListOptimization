package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.scraper.Scraper

class ScrapPlatformUseCase(private val scraper: Scraper) {

    suspend fun scrapPlatform(platform: Platform): Platform {
        return try {
            scraper
                .getPlatform(platform.system)
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

    companion object {
        private const val TAG = "ScrapPlatformUseCase"
    }
}
