package com.wechantloup.gamelistoptimization.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.utils.getImagePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageUseCase(private val provider: GameListProvider) {

    suspend fun getImageBitmapt(source: Source, platform: Platform, game: Game): Bitmap? = withContext(Dispatchers.IO) {
        if (game.image == null) return@withContext null
        val imagePath = game.getImagePath(platform)
        try {
            provider.open(source)
            val src = provider.getFileForReading(imagePath)
            val inputStream = src.inputStream
            return@withContext BitmapFactory.decodeStream(inputStream, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get bitmap from $imagePath", e)
            return@withContext null
        }
    }

    companion object {
        private const val TAG = "ImageUseCase"
    }
}
