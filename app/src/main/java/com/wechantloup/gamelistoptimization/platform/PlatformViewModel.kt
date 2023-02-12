package com.wechantloup.gamelistoptimization.platform

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wechantloup.gamelistoptimization.cacheprovider.CacheProvider
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.Scraper
import com.wechantloup.gamelistoptimization.usecase.CacheImageUseCase
import com.wechantloup.gamelistoptimization.usecase.CleanGameListUseCase
import com.wechantloup.gamelistoptimization.usecase.SavePlatformUseCase
import com.wechantloup.gamelistoptimization.usecase.ScrapGameUseCase
import com.wechantloup.gamelistoptimization.usecase.ScrapPlatformUseCase
import com.wechantloup.gamelistoptimization.usecase.UploadUseCase
import com.wechantloup.gamelistoptimization.utils.deserialize
import com.wechantloup.gamelistoptimization.webdownloader.WebDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlatformViewModelFactory(
    private val activity: Activity,
    private val provider: GameListProvider,
    private val scraper: Scraper,
    private val webDownloader: WebDownloader,
    private val cacheProvider: CacheProvider,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val savePlatformUseCase = SavePlatformUseCase(provider)
        val cleanGameListUseCase = CleanGameListUseCase(provider)
        val scrapGameUseCase = ScrapGameUseCase(scraper, provider)
        val scrapPlatformUseCase = ScrapPlatformUseCase(scraper)
        val cacheImageUseCase = CacheImageUseCase(cacheProvider)
        val uploadUseCase = UploadUseCase(provider, webDownloader, cacheProvider)
        @Suppress("UNCHECKED_CAST")
        return PlatformViewModel(
            activity.application,
            cleanGameListUseCase,
            savePlatformUseCase,
            scrapGameUseCase,
            scrapPlatformUseCase,
            cacheImageUseCase,
            uploadUseCase,
        ) as T
    }
}

class PlatformViewModel(
    application: Application,
    private val cleanGameListUseCase: CleanGameListUseCase,
    private val savePlatformUseCase: SavePlatformUseCase,
    private val scrapGameUseCase: ScrapGameUseCase,
    private val srapPlatformUseCase: ScrapPlatformUseCase,
    private val cacheImageUseCase: CacheImageUseCase,
    private val uploadUseCase: UploadUseCase,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    fun setPlatform(source: Source, platform: Platform) {
        _stateFlow.value = stateFlow.value.copy(source = source, platform = platform)
    }

    fun savePlatform(callback: () -> Unit) {
        showLoader(true)
        viewModelScope.launch {
            downloadAllImages()
            val platform = getCurrentPlatform() ?: return@launch
            savePlatform(platform)
            showLoader(false)
            callback()
        }
    }

    fun updateName(name: String) {
        val platform = getCurrentPlatform() ?: return
        val newPlatform = platform.copy(name = name)

        _stateFlow.value = stateFlow.value.copy(platform = newPlatform)
    }

    fun cleanPlatform() {
        val source = getCurrentSource() ?: return
        val platform = getCurrentPlatform() ?: return
        showLoader(true)
        viewModelScope.launch {
            val updatedPlatform = srapPlatformUseCase.scrapPlatform(platform)
            val cleanedPlatform = cleanGameListUseCase.cleanGameList(source, updatedPlatform)
            _stateFlow.value = stateFlow.value.copy(platform = cleanedPlatform)
            showLoader(false)
        }
    }

    fun scrapAllGames() {
        val platform = getCurrentPlatform() ?: return
        showLoader(true)
        viewModelScope.launch {
            val scrapedGames = mutableListOf<Game>()
            val errors = mutableListOf<ScrapGameUseCase.Result>()
            platform.games.forEach { game ->
                val result = scrapGameUseCase.scrapGame(game, platform)
                val newGame = result.game
                scrapedGames.add(newGame)
                if (result.status != ScrapGameUseCase.Result.Status.SUCCESS) {
                    errors.add(result)
                }
            }
            val scrapedPlatform = platform.copy(games = scrapedGames)
            _stateFlow.value = stateFlow.value.copy(platform = scrapedPlatform)
            showLoader(false)
            // ToDo display errors
        }
    }

    private suspend fun downloadAllImages() {
        val source = getCurrentSource() ?: return
        val platform = getCurrentPlatform() ?: return
        val newGames = mutableListOf<Game>()
        platform.games.forEach { game ->
            val newGame = try {
                cacheImageUseCase.removeImageFile(source, platform, game)
                val imageUrl: ScrapGameUseCase.ImageUrl = requireNotNull(game.image).deserialize()
                imageUrl.upload(source, platform, game)
            } catch (e: Exception) {
                Log.e("TOTO", "Error reading scraped image", e)
                // No scraped image
                game
            }
            newGames.add(newGame)
        }
        val newPlatform = platform.copy(games = newGames)
        _stateFlow.value = stateFlow.value.copy(platform = newPlatform)
    }

    // ToDo duplicated
    private suspend fun ScrapGameUseCase.ImageUrl.upload(
        source: Source,
        platform: Platform,
        game: Game,
    ): Game {
        val romName = game.getRomName()
        val imageName = "${romName.substring(0, romName.lastIndexOf("."))}.$format"
        val imagePath = "./media/images/$imageName"
        val newGame = game.copy(image = imagePath)
        val result = uploadUseCase.uploadImage(source, platform, newGame, url)
        Log.d("TOTO", "Upload image for ${game.name} success = $result")
        return newGame
    }

    private suspend fun savePlatform(platform: Platform) {
        val source = requireNotNull(getCurrentSource())
        savePlatformUseCase.savePlatform(source, platform)
        _stateFlow.value = stateFlow.value.copy(platform = platform)
    }

    private fun getCurrentSource(): Source? = stateFlow.value.source
    private fun getCurrentPlatform(): Platform? = stateFlow.value.platform

    private fun showLoader(show: Boolean) {
        _stateFlow.value = stateFlow.value.copy(showLoader = show)
    }

    data class State(
        val source: Source? = null,
        val platform: Platform? = null,
        val showLoader: Boolean = false,
    )

    companion object {
        private const val TAG = "PlatformViewModel"
    }
}
