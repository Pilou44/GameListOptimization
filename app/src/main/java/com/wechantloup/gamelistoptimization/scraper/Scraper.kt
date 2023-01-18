package com.wechantloup.gamelistoptimization.scraper

import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.scraper.model.ScraperSystem
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.OkHttpClientFactory
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.GameInfoResponse
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.LanguageString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.RegionString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.SystemListResponse
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
            val systemId = getSystem(system).id
            val info = scraper.getGameInfo(
                crcHexa = crc.toHexString(),
                romName = romName,
                romSize = fileSize,
                systemId = systemId,
            )
            return@withContext info.toGame(romName)
    }

    suspend fun getPlatform(system: String): Platform {
        return getSystem(system).toPlatform(system)
    }

    private fun ScraperSystem.toPlatform(system: String): Platform {
        val userCountry = Locale.getDefault().country.lowercase()
        val name = if (userCountry.isEuCountry() && euName != null) euName else usName
        return Platform(
            name = name ?: system,
            games = emptyList(),
            gamesBackup = null,
            path = "",
            system = system,
            extensions = extensions + "zip",
        )
    }

    private fun GameInfoResponse.toGame(romName: String): Game = with(response.game) {
        if (unknownGame) {
            throw UnknownGameException("Unknown game $romName")
        }

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
            genreid = genres?.first()?.id,
        )
    }

    private fun List<RegionString>.extractFromRegion(): String? {
        val userCountry = Locale.getDefault().country.lowercase()
        return firstOrNull { it.region == userCountry }?.text
            ?: firstOrNull { userCountry.isEuCountry() && it.region == "eu" }?.text
            ?: firstOrNull { it.region == "wor" }?.text
            ?: firstOrNull()?.text
    }

    private fun List<LanguageString>.extractFromLanguage(): String? {
        val userLanguage = Locale.getDefault().language
        return firstOrNull { it.language == userLanguage }?.text
            ?: firstOrNull { it.language == DEFAULT_LANGUAGE }?.text
            ?: firstOrNull()?.text
    }

    private fun String.isEuCountry(): Boolean {
        val euCountries = listOf(
            "BE", "EL", "LT", "PT", "BG", "ES", "LU", "RO", "CZ", "FR", "HU", "SI", "DK", "HR",
            "MT", "SK", "DE", "IT", "NL", "FI", "EE", "CY", "AT", "SE", "IE", "LV", "PL", "UK",
            "CH", "NO", "IS", "LI"
        )
        return euCountries.contains(this.uppercase())
    }

    private suspend fun getSystem(systemName: String): ScraperSystem {
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

        private val DEFAULT_LANGUAGE = Locale.ENGLISH.language

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
