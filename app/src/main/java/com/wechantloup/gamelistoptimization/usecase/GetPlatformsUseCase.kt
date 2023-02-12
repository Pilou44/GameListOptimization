package com.wechantloup.gamelistoptimization.usecase

import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.data.sambaprovider.GameListProvider

class GetPlatformsUseCase(private val provider: GameListProvider) {

    suspend fun getPlatforms(source: Source): List<Platform> {
        provider.open(source)
        return provider.getPlatforms()
    }
}
