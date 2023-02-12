package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.BadCrcException
import com.wechantloup.gamelistoptimization.scraper.Scraper
import com.wechantloup.gamelistoptimization.scraper.TooManyRequestsException
import com.wechantloup.gamelistoptimization.scraper.UnknownGameException
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.LanguageString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.RegionString
import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.ScraperGame
import com.wechantloup.gamelistoptimization.utils.getPath
import com.wechantloup.gamelistoptimization.utils.serialize
import okhttp3.internal.toHexString
import java.util.Locale

class ScrapGameUseCase(private val scraper: Scraper, private val provider: GameListProvider) {

    suspend fun scrapGame(game: Game, platform: Platform): Game {
        val gamePath = game.getPath(platform)
        val crc = provider.getFileCrc(gamePath).toHexString()
        return scrapGame(game, platform, crc, retry = true)
    }

    private suspend fun scrapGame(
        game: Game,
        platform: Platform,
        crc: String,
        retry: Boolean,
    ): Game {
        val romName = game.getRomName()
        val gamePath = game.getPath(platform)
        val scrapedGame = try {
            val scrap = scraper.scrapGame(
                romName = romName,
                system = platform.system,
                fileSize = provider.getFileSize(gamePath),
                crc = crc,
            )
            scrap.toGame(romName, crc)
        } catch (e: TooManyRequestsException) {
            if (retry) {
                return scrapGame(game, platform, crc, retry = false)
            } else {
                Log.w(TAG, "Scrap tried 2 times, ignoring")
                Log.e(TAG, "Too many scrap requests", e)
                return game
            }
        } catch (e: BadCrcException) {
            Log.e(TAG, "Bad crc $crc for $romName", e)
            if (game.name.isNullOrBlank() || game.name.contains(romName)) {
                return unknownGame(romName)
            } else {
                return game
            }
        } catch (e: UnknownGameException) {
            Log.e(TAG, "Unknown game $romName", e)
            if (game.name.isNullOrBlank() || game.name.contains(romName)) {
                return unknownGame(romName)
            } else {
                return game
            }
        }

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

    private fun ScraperGame.toGame(romName: String, crc: String): Game {
        if (unknownGame) {
            Log.w(TAG, "Unknown game detected at conversion")
            throw UnknownGameException("Unknown game $romName")
        }

        return Game(
            id = id,
            source = "ScreenScraper.fr",
            path = "./$romName",
            name = names?.extractFromRegion(this, romName, crc),
            desc = synopsis?.extractFromLanguage(),
            rating = rating?.text, // ToDo
            releaseDate = dates?.extractFromRegion(this, romName, crc),
            developer = developer?.text,
            publisher = publisher?.text,
            genre = genres?.map { it.names.extractFromLanguage() }?.joinToString { ", " },
            players = players?.text,
            genreId = genres?.first()?.id,
            image = extractImage(romName, crc).serialize()
        )
    }

    private fun ScraperGame.extractImage(romName: String, crc: String): ImageUrl? {
        val images = medias.filter { it.type == MEDIA_MIX_RBV_2 }

        if (images.isEmpty()) {
            return null
        } else if (images.size == 1) {
            return ImageUrl(format = images[0].format, url = images[0].url)
        }

        val regions = extractRegions(romName, crc) ?: return null
        val image = images.firstOrNull { it.region == regions.first() }
            ?: images.firstOrNull { regions.contains(it.region) }
            ?: images.firstOrNull().takeIf { regions.contains(REGION_WORLD) }
            ?: return null

        return ImageUrl(format = image.format, url = image.url)
    }

    private fun ScraperGame.extractRegions(romName: String, crc: String): List<String>? {
        val rom = roms.firstOrNull { it.crc?.uppercase() == crc.uppercase() } ?: roms.firstOrNull { it.fileName == romName } ?: return null
        return rom.regions.shortNames
    }

    private fun List<RegionString>.extractFromRegion(game: ScraperGame, romName: String, crc: String): String? {
        val regions = game.extractRegions(romName, crc) ?: return null
        val regionString = firstOrNull { it.region == regions.first() }
            ?: firstOrNull { regions.contains(it.region) }
            ?: firstOrNull().takeIf { regions.contains(REGION_WORLD) }
            ?: return null

        return regionString.text
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

        private const val MEDIA_MIX_RBV_2 = "mixrbv2"
        private const val REGION_WORLD = "wor"
    }
}
