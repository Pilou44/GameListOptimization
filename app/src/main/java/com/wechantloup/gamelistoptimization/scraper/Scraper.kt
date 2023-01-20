package com.wechantloup.gamelistoptimization.scraper

import com.wechantloup.gamelistoptimization.scraper.model.ScraperSystem
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.OkHttpClientFactory
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.ScraperGame
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.SystemListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toHexString

class Scraper {

    private val scraper = OkHttpClientFactory().createScreenScraperFr()
    private var systems: List<ScraperSystem>? = null

    suspend fun scrapGame(
        romName: String,
        system: String,
        fileSize: Long,
        crc: String,
    ): ScraperGame = withContext(Dispatchers.IO) {
        val systemId = getSystem(system).id
        val info = scraper.getGameInfo(
            crcHexa = crc,
            romName = romName,
            romSize = fileSize,
            systemId = systemId,
        )
        return@withContext info.response.game
    }

    suspend fun getSystem(systemName: String): ScraperSystem {
        val systems = getSystems()
        return systems.first { it.systemNames.contains(systemName) }
    }

    private suspend fun getSystems(): List<ScraperSystem> = withContext(Dispatchers.IO) {
        val currentSystems = systems
        if (currentSystems != null) return@withContext currentSystems

        val screenScraperSystems = scraper.getSystemList()
        val newSystems = screenScraperSystems.response.systems.map { it.toSystem() }
        systems = newSystems

        return@withContext newSystems
    }

    companion object {

        private const val TAG = "Scraper"

        private fun SystemListResponse.System.toSystem(): ScraperSystem {
            return ScraperSystem(
                id = id,
                euName = names?.euName,
                usName = names?.usName,
                systemNames = names?.retropieName?.split(',') ?: emptyList(),
                extensions = extensions?.split(',') ?: emptyList(),
            )
        }
    }
}
