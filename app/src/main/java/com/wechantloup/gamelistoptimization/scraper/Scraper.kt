package com.wechantloup.gamelistoptimization.scraper

import com.wechantloup.gamelistoptimization.PreferencesRepository
import com.wechantloup.gamelistoptimization.scraper.model.ScraperSystem
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.OkHttpClientFactory
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.ScraperGame
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.SystemListResponse
import com.wechantloup.gamelistoptimization.usecase.AccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class Scraper(preferencesRepository: PreferencesRepository) {

    private val accountUseCase = AccountUseCase(preferencesRepository)
    private val scraper = OkHttpClientFactory().createScreenScraperFr(accountUseCase)
    private var systems: List<ScraperSystem>? = null

    suspend fun scrapGame(
        romName: String,
        system: String,
        fileSize: Long,
        crc: String,
    ): ScraperGame = withContext(Dispatchers.IO) {
        try {
            val systemId = getSystem(system).id
            val info = scraper.getGameInfo(
                crcHexa = crc,
                romName = romName,
                romSize = fileSize,
                systemId = systemId,
            )
            return@withContext info.response.game
        } catch (e: HttpException) {
            throw parseException(e, romName, crc)
        }
    }

    suspend fun getSystem(systemName: String): ScraperSystem {
        val systems = getSystems()
        return systems.first { it.systemNames.contains(systemName) }
    }

    private fun parseException(exception: HttpException, romName:String, crc: String): Exception {
        val errorCode = exception.code()
        val msg = "Error scraping $romName witch crc $crc"
        return when (errorCode) {
            400 -> BadCrcException(msg, exception)
            404 -> UnknownGameException(msg, exception)
            429 -> TooManyRequestsException(msg, exception)
            else -> exception
        }
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
                jpName = names?.jpName,
                systemNames = names?.retropieName?.split(',') ?: emptyList(),
                extensions = extensions?.split(',') ?: emptyList(),
            )
        }
    }
}
