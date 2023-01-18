package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider

class CleanGameListUseCase(private val provider: GameListProvider) {

    suspend fun cleanGameList(source: Source, platform: Platform): Platform {
        provider.open(source)
        val gameNames = provider.getGameNamesFromPlatform(platform)
        Log.d(TAG, "Directory contains ${gameNames.size} games")

        val platformGames = platform.games.toMutableList()
        Log.d(TAG, "Platform contains ${platformGames.size} games")

        platform.games.forEach {
            val fileName = it.path.substring(it.path.lastIndexOf("/") + 1)
            if (!gameNames.contains(fileName)) {
                platformGames.remove(it)
            }
        }
        Log.d(TAG, "Platform contains ${platformGames.size} games after removing missing files")

        val platformGameNames = platformGames.map { it.path.substring(it.path.lastIndexOf("/") + 1) }
        gameNames.forEach {
            if (!platformGameNames.contains(it)) {
                platformGames.add(Game(
                    path = "./$it",
                ))
            }
        }
        Log.d(TAG, "Platform contains ${platformGames.size} games after adding remaining files")

        return platform.copy(games = platformGames)
    }

    companion object {
        private const val TAG = "CleanGameListUseCase"
    }
}
