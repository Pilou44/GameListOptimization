package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.Scraper

class ScrapGameUseCase(private val scraper: Scraper, private val provider: GameListProvider) {

    suspend fun scrapGame(game: Game, platform: Platform): Game {
        val romName = game.getRomName()
        val scrapedGame = try {
            scraper.scrapGame(
                romName = romName,
                system = platform.system,
                fileSize = provider.getGameSize(game, platform),
                crc = provider.getGameCrc(game, platform),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to scrap ${game.getRomName()}", e)
            if (game.name.isNullOrBlank() || game.name.contains(romName)) {
                unknownGame(romName)
            } else {
                game
            }
        }

        return Game(
            id = scrapedGame.id ?: game.id,
            source = scrapedGame.source ?: game.source,
            path = game.path,
            name = scrapedGame.name ?: game.name,
            desc = scrapedGame.desc ?: game.desc,
            rating = scrapedGame.rating ?: game.rating,
            releasedate = scrapedGame.releasedate ?: game.releasedate,
            developer = scrapedGame.developer ?: game.developer,
            publisher = scrapedGame.publisher ?: game.publisher,
            genre = scrapedGame.genre ?: game.genre,
            players = scrapedGame.players ?: game.players,
            image = scrapedGame.image ?: game.image,
            marquee = scrapedGame.marquee ?: game.marquee,
            video = scrapedGame.video ?: game.video,
            genreid = scrapedGame.genreid ?: game.genreid,
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

    companion object {
        private const val TAG = "ScrapGameUseCase"
    }
}
