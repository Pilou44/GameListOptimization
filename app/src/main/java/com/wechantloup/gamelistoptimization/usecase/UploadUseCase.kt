package com.wechantloup.gamelistoptimization.usecase

import android.util.Log
import com.wechantloup.gamelistoptimization.cacheprovider.CacheProvider
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.utils.copy
import com.wechantloup.gamelistoptimization.utils.getImagePath
import com.wechantloup.gamelistoptimization.utils.getPath
import com.wechantloup.gamelistoptimization.webdownloader.WebDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class UploadUseCase(
    private val provider: GameListProvider,
    private val webDownloader: WebDownloader,
    private val cacheProvider: CacheProvider,
) {

    suspend fun uploadGame(destSource: Source, srcPlatform: Platform, game: Game, src: File): Boolean {
        provider.open(destSource)

        val destPlatform =
            provider.getPlatforms().firstOrNull { it.isSameAs(srcPlatform) } ?: return false // ToDo Create platform

        val gamePath = game.getPath(destPlatform)

        val result = upload(src, gamePath)

        if (!result) return false

        val destGames = destPlatform.games.toMutableList()
            .apply { add(game) }
        val newPlatform = destPlatform.copy(games = destGames)
        provider.savePlatform(newPlatform)

        return true
    }

    suspend fun uploadImage(destSource: Source, srcPlatform: Platform, game: Game, src: File): Boolean {
        if (game.image == null) return false
        provider.open(destSource)
        val destPlatform =
            provider.getPlatforms().firstOrNull { it.isSameAs(srcPlatform) } ?: return false // ToDo Create platform
        val imagePath = game.getImagePath(destPlatform)
        return upload(src, imagePath)
    }

    suspend fun uploadImage(destSource: Source, srcPlatform: Platform, game: Game, url: String): Boolean {
        if (game.image == null) return false

        val parent = cacheProvider.getTempDir()
        if (!parent.exists()) parent.mkdirs()
        val cachedImage = File(parent, "tmp")

        withContext(Dispatchers.IO) {
            if (!cachedImage.exists()) cachedImage.createNewFile()
            cachedImage.deleteOnExit()
            webDownloader.download(url, cachedImage)
        }

        return uploadImage(destSource, srcPlatform, game, cachedImage)
    }

    suspend fun upload(
        src: File,
        destPath: String,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val dest = provider.getFileForWriting(destPath)
            val outputStream = dest.outputStream
            val inputStream = src.inputStream()
            dest.use {
                inputStream.use {
                    outputStream.use {
                        copy(inputStream, outputStream)
                    }
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Unable to upload to $destPath", e)
            return@withContext false
        }
    }

    companion object {

        private const val TAG = "UploadUseCase"
    }
}
