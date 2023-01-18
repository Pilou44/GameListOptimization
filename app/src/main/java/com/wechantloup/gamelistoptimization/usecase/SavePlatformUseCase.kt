package com.wechantloup.gamelistoptimization.usecase

import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider

class SavePlatformUseCase(private val provider: GameListProvider) {

    suspend fun savePlatform(source: Source, platform: Platform) {
        provider.open(source)
        provider.savePlatform(platform)
    }
}
