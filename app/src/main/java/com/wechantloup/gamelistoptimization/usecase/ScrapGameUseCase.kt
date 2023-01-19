package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.Scraper
import com.wechantloup.gamelistoptimization.scraper.UnknownGameException
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.LanguageString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.RegionString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.ScraperGame
import com.wechantloup.gamelistoptimization.utils.getPath
import com.wechantloup.gamelistoptimization.utils.isEuCountry
import com.wechantloup.gamelistoptimization.utils.serialize
import java.util.Locale

class ScrapGameUseCase(private val scraper: Scraper, private val provider: GameListProvider) {

    suspend fun scrapGame(game: Game, platform: Platform): Game {
        val romName = game.getRomName()
        val gamePath = game.getPath(platform)

        val scraperGame = try {
            scraper.scrapGame(
                romName = romName,
                system = platform.system,
                fileSize = provider.getFileSize(gamePath),
                crc = provider.getFileCrc(gamePath),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to scrap ${game.getRomName()}", e)
            if (game.name.isNullOrBlank() || game.name.contains(romName)) {
                return unknownGame(romName)
            } else {
                return game
            }
        }

        val scrapedGame = scraperGame.toGame(romName)

        return Game(
            id = scrapedGame.id ?: game.id,
            source = scrapedGame.source ?: game.source,
            path = game.path,
            name = scrapedGame.name ?: game.name,
            desc = scrapedGame.desc ?: game.desc,
            rating = scrapedGame.rating ?: game.rating,
            releaseDate = scrapedGame.releaseDate ?: game.releaseDate,
            developer = scrapedGame.developer ?: game.developer,
            publisher = scrapedGame.publisher ?: game.publisher,
            genre = scrapedGame.genre ?: game.genre,
            players = scrapedGame.players ?: game.players,
            image = scrapedGame.image ?: game.image,
            marquee = scrapedGame.marquee ?: game.marquee,
            video = scrapedGame.video ?: game.video,
            genreId = scrapedGame.genreId ?: game.genreId,
            favorite = game.favorite,
            kidgame = game.kidgame,
            hidden = game.hidden,
        )
    }

    private fun unknownGame(romName: String): Game {
        return Game(
            path = "./$romName",
            name = romName.substring(0, romName.lastIndexOf(".")),
        )
    }

    private fun ScraperGame.toGame(romName: String): Game {
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
            releaseDate = dates?.extractFromRegion(),
            developer = developer?.text,
            publisher = publisher?.text,
            genre = genres?.map { it.names.extractFromLanguage() }?.joinToString { ", " },
            players = players?.text,
            genreId = genres?.first()?.id,
            image = extractImage().serialize()
        )
    }

    private fun ScraperGame.extractImage(): ImageUrl? {
        val images = medias.filter { it.type == MEDIA_BOX_2D }

        if (images.isEmpty()) {
            return null
        } else if (images.size == 1) {
            return ImageUrl(format = images[0].format, url = images[0].url)
        }

        // ToDo Region should come from ROM
        val userCountry = Locale.getDefault().country.lowercase()
        return images.firstOrNull { it.region == userCountry }?.let { ImageUrl(format = it.format, url = it.url) }
            ?: images.firstOrNull { userCountry.isEuCountry() && it.region == "eu" }?.let { ImageUrl(format = it.format, url = it.url) }
            ?: images.firstOrNull { it.region == "wor" }?.let { ImageUrl(format = it.format, url = it.url) }
            ?: images.firstOrNull()?.let { ImageUrl(format = it.format, url = it.url) }
    }

    private fun List<RegionString>.extractFromRegion(): String? {
        // ToDo Region should come from ROM
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

    data class ImageUrl(
        val url: String,
        val format: String,
    )

    companion object {
        private const val TAG = "ScrapGameUseCase"

        private val DEFAULT_LANGUAGE = Locale.ENGLISH.language

        private const val MEDIA_BOX_2D = "box-2D"
    }
}
