package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.utils.copy
import com.wechantloup.gamelistoptimization.utils.getImagePath
import com.wechantloup.gamelistoptimization.utils.getPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DownloadUseCase(private val provider: GameListProvider) {

    suspend fun downloadGame(source: Source, platform: Platform, game: Game, dest: File): Boolean {
        val gamePath = game.getPath(platform)
        return download(source, gamePath, dest)
    }

    suspend fun downloadImage(source: Source, platform: Platform, game: Game, dest: File): Boolean {
        if (game.image == null) return false
        val imagePath = game.getImagePath(platform)
        return download(source, imagePath, dest)
    }

    private suspend fun download(source: Source, path: String, dest: File): Boolean = withContext(Dispatchers.IO) {
        try {
            provider.open(source)
            if (!dest.exists()) {
                dest.createNewFile()
            }
            val outputStream = dest.outputStream()
            val src = provider.getFileForReading(path)
            val inputStream = src.inputStream
            src.use {
                inputStream.use {
                    outputStream.use {
                        copy(inputStream, outputStream)
                    }
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Unable to download $path", e)
            return@withContext false
        }
    }

    companion object {
        private const val TAG = "DownloadUseCase"
    }
}
