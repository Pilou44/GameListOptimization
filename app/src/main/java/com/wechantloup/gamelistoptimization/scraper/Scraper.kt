package com.wechantloup.gamelistoptimization.scraper

import android.util.Log
import com.wechantloup.gamelistoptimization.scraper.model.ScraperSystem
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.OkHttpClientFactory
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.SystemListResponse

class Scraper {

    private val scraper = OkHttpClientFactory().createScreenScraperFr()
    private var systems: List<ScraperSystem>? = null

    suspend fun scrapGame(name: String, system: String, fileSize: Long) {
        val systems = getSystems()
        val systemId = systems.first { it.systemNames.contains(system) }
        Log.d(TAG, "System ID=$systemId")
    }

    suspend fun getSystemId(systemName: String): Int {
        val systems = getSystems()
        val system = systems.first { it.systemNames.contains(systemName) }
        return system.id
    }

    private suspend fun getSystems(): List<ScraperSystem> {
        val currentSystems = systems
        if (currentSystems != null) return currentSystems

        val screenScraperSystems = scraper.getSystemList()
        val newSystems = screenScraperSystems.response.systems.map { it.toSystem() }
        systems = newSystems

        return newSystems
    }

    companion object {
        private const val TAG = "Scraper"
        private fun SystemListResponse.System.toSystem(): ScraperSystem {
            return ScraperSystem(
                id = id,
                euName = names.euName,
                usName = names.usName,
                systemNames = names.retropieName.split(','),
                extensions = extensions.split(','),
            )
        }

    }
}
