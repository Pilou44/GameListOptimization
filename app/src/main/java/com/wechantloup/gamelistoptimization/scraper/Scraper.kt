package com.wechantloup.gamelistoptimization.scraper

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.scraper.model.ScraperSystem
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.OkHttpClientFactory
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.GameInfoResponse
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.LanguageString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.RegionString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.SystemListResponse
import com.wechantloup.gamelistoptimization.utils.serialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toHexString
import java.util.Locale

class Scraper {

    private val scraper = OkHttpClientFactory().createScreenScraperFr()
    private var systems: List<ScraperSystem>? = null

    suspend fun scrapGame(
        romName: String,
        system: String,
        fileSize: Long,
        crc: Long,
    ): Game = withContext(Dispatchers.IO) {
        val systemId = getSystemId(system)
        val info = scraper.getGameInfo(
            crcHexa = crc.toHexString(),
            romName = romName,
            romSize = fileSize,
            systemId = systemId,
        )
        Log.d(TAG, info.response.game.serialize())
        return@withContext info.toGame(romName)
    }

    private fun GameInfoResponse.toGame(romName: String): Game = with(response.game) {
        return Game(
            id = id,
            source = "ScreenScraper.fr",
            path = "./$romName",
            name = names?.extractFromRegion(),
            desc = synopsis?.extractFromLanguage(),
            rating = rating?.text, // ToDo
            releasedate = dates?.extractFromRegion(),
            developer = developer?.text,
            publisher = publisher?.text,
            genre = genres?.map { it.names.extractFromLanguage() }?.joinToString { ", " },
            players = players?.text,
            image = null,
            marquee = null,
            video = null,
            genreid = genres?.first()?.id,
            favorite = false,
            kidgame = false,
            hidden = false,
        )
    }

    private fun List<RegionString>.extractFromRegion(): String? {
        val userCountry = Locale.getDefault().country.lowercase()
        val value = firstOrNull { it.region == userCountry }?.text
            ?: firstOrNull { userCountry.isEuCountry() && it.region == "eu" }?.text
            ?: firstOrNull { it.region == "wor" }?.text
            ?: firstOrNull()?.text
        Log.d("TOTO", "$value")
        return value
    }

    private fun List<LanguageString>.extractFromLanguage(): String? {
        val userLanguage = Locale.getDefault().language
        return firstOrNull { it.language == userLanguage }?.text
            ?: firstOrNull { it.language == DEFAULT_LANGUAGE }?.text
            ?: firstOrNull()?.text
    }

    fun String.isEuCountry(): Boolean {
        val euCountries = listOf(
            "BE", "EL", "LT", "PT", "BG", "ES", "LU", "RO", "CZ", "FR", "HU", "SI", "DK", "HR",
            "MT", "SK", "DE", "IT", "NL", "FI", "EE", "CY", "AT", "SE", "IE", "LV", "PL", "UK",
            "CH", "NO", "IS", "LI"
        )
        return euCountries.contains(this.uppercase())
    }

    private suspend fun getSystemId(systemName: String): Int {
        val systems = getSystems()
        val system = systems.first { it.systemNames.contains(systemName) }
        return system.id
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

        private val DEFAULT_LANGUAGE = Locale.ENGLISH.language

        private fun SystemListResponse.System.toSystem(): ScraperSystem {
            Log.d(TAG, this.serialize())
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
