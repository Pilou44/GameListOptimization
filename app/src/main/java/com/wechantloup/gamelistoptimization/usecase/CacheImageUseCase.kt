package com.wechantloup.gamelistoptimization.usecase

import com.wechantloup.gamelistoptimization.cacheprovider.CacheProvider
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import java.io.File
import java.net.URLEncoder

class CacheImageUseCase(private val cacheProvider: CacheProvider) {

    fun getImageFile(source: Source, platform: Platform, game: Game): File {
        val name = getImageFileName(source, platform, game)
        val parent = cacheProvider.getImagesDir()
        return File(parent, name)
    }

    fun removeImageFile(source: Source, platform: Platform, game: Game): Boolean {
        val file = getImageFile(source, platform, game)
        return if (file.exists()) {
            file.delete()
        } else {
            true
        }
    }

    private fun getImageFileName(source: Source, platform: Platform, game: Game): String {
        return URLEncoder
            .encode("${source.ip}${source.path}${platform.system}${game.getRomName()}", Charsets.UTF_8.name())
            .replace("/", "")
            .replace("\\", "")
    }
}
