package com.wechantloup.gamelistoptimization.game

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
import com.wechantloup.gamelistoptimization.model.Sources
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.Scraper
import com.wechantloup.gamelistoptimization.usecase.CacheImageUseCase
import com.wechantloup.gamelistoptimization.usecase.DownloadUseCase
import com.wechantloup.gamelistoptimization.usecase.SavePlatformUseCase
import com.wechantloup.gamelistoptimization.usecase.ScrapGameUseCase
import com.wechantloup.gamelistoptimization.usecase.UploadUseCase
import com.wechantloup.gamelistoptimization.utils.deserialize
import com.wechantloup.gamelistoptimization.webdownloader.WebDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GameViewModelFactory(
    private val activity: Activity,
    private val provider: GameListProvider,
    private val scraper: Scraper,
    private val webDownloader: WebDownloader,
    private val cacheProvider: CacheProvider,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val savePlatformUseCase = SavePlatformUseCase(provider)
        val downloadUseCase = DownloadUseCase(provider)
        val uploadUseCase = UploadUseCase(provider, webDownloader, cacheProvider)
        val scrapGameUseCase = ScrapGameUseCase(scraper, provider)
        val cacheImageUseCase = CacheImageUseCase(cacheProvider)
        @Suppress("UNCHECKED_CAST")
        return GameViewModel(
            activity.application,
            savePlatformUseCase,
            downloadUseCase,
            uploadUseCase,
            scrapGameUseCase,
            cacheImageUseCase,
        ) as T
    }
}

class GameViewModel(
    application: Application,
    private val savePlatformUseCase: SavePlatformUseCase,
    private val downloadUseCase: DownloadUseCase,
    private val uploadUseCase: UploadUseCase,
    private val scrapGameUseCase: ScrapGameUseCase,
    private val cacheImageUseCase: CacheImageUseCase,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    private val gameSources = Sources.values().map { it.source }.toList()

    fun openGame(source: Source, platform: Platform, game: Game) {
        val currentSource = getCurrentSource()
        val currentPlatform = getCurrentPlatform()
        val currentGame = getCurrentGame()

        val isSameSource = currentSource == source
        val isSamePlatform = currentPlatform == platform
        val isSameGame = currentGame == game

        if (isSameSource && isSamePlatform && isSameGame) return

        Log.d(TAG,
            "Clear data: isSameSource='$isSameSource', isSamePlatform='$isSamePlatform', isSameGame='$isSameGame'")
        _stateFlow.value = stateFlow.value.copy(source = null, platform = null, game = null, image = null)

        Log.d(TAG, "Open game")
        _stateFlow.value = stateFlow.value.copy(source = source,
            platform = platform,
            game = game,
            copyDestinations = gameSources - source)
        viewModelScope.launch {
            game.retrieveImage(source, platform)
        }
    }

    fun onGameChanged(game: Game) {
        _stateFlow.value = stateFlow.value.copy(game = game)
    }

    fun saveGame(callBack: () -> Unit) {
        showLoader(true)
        val source = requireNotNull(getCurrentSource())
        val game = requireNotNull(getCurrentGame())
        val currentPlatform = requireNotNull(getCurrentPlatform())
        val mutableGameList = currentPlatform.games.toMutableList()
        val gameIndex = mutableGameList.indexOfFirst { it.path == game.path }

        viewModelScope.launch {
            val newGame = try {
                cacheImageUseCase.removeImageFile(source, currentPlatform, game)
                val imageUrl: ScrapGameUseCase.ImageUrl = requireNotNull(game.image).deserialize()
                imageUrl.upload(source, currentPlatform, game)
            } catch (e: Exception) {
                Log.e("TOTO", "Error reading scraped image", e)
                // No scraped image
                game
            }

            mutableGameList.removeAt(gameIndex)
            mutableGameList.add(gameIndex, newGame)
            val platform = currentPlatform.copy(games = mutableGameList)

            Log.d("TOTO", "Save game with image ${game.image}")
            savePlatformUseCase.savePlatform(source, platform)
            showLoader(false)
            callBack()
        }
    }

    fun copyGame(destination: Source) {
        viewModelScope.launch {
            val cacheFile: File = copyGameToCache()
            copyGameToDest(cacheFile, destination)
            if (!cacheFile.delete()) {
                cacheFile.deleteOnExit()
            }
        }
    }

    fun scrapGame() {
        showLoader(true)
        viewModelScope.launch {
            val game = requireNotNull(getCurrentGame())
            val platform = requireNotNull(getCurrentPlatform())
            val result = scrapGameUseCase.scrapGame(game, platform)

            if (result.status == ScrapGameUseCase.Result.Status.SUCCESS) {
                val newGame = result.game

                try {
                    val imageUrl: ScrapGameUseCase.ImageUrl? = newGame.image?.deserialize()

                    imageUrl?.let {
                        _stateFlow.value = stateFlow.value.copy(image = it.url)
                    }
                } catch (e: Exception) {
                    // No scraped image
                }

                Log.i(TAG, "Set new scraper info for ${newGame.name}")
                _stateFlow.value = stateFlow.value.copy(game = newGame)
            } else {
                // ToDo display error
            }
            showLoader(false)
        }
    }

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

    private suspend fun copyGameToCache(): File = withContext(Dispatchers.IO) {
        val source = requireNotNull(getCurrentSource())
        val game = requireNotNull(getCurrentGame())
        val platform = requireNotNull(getCurrentPlatform())
        val cacheFile = getGameFile(game)
        val result = downloadUseCase.downloadGame(source, platform, game, cacheFile)
        Log.i(TAG, "Download of ${game.path} success = $result")
        return@withContext cacheFile
    }

    private suspend fun copyGameToDest(file: File, destination: Source) {
        val source = requireNotNull(getCurrentSource())
        val platform = requireNotNull(getCurrentPlatform())
        val game = requireNotNull(getCurrentGame())

        val imageFile = cacheImageUseCase.getImageFile(source, platform, game)
        if (imageFile.exists()) uploadUseCase.uploadImage(destination, platform, game, imageFile)

        val result = uploadUseCase.uploadGame(
            destSource = destination,
            srcPlatform = platform,
            game = game,
            src = file,
        )
        Log.i(TAG, "File upload to cache success = $result")
    }

    private suspend fun Game.retrieveImage(source: Source, platform: Platform) = withContext(Dispatchers.IO) {
        Log.d("TOTO", "Retrieve image $image")
        if (image == null) return@withContext

        val cachedImage = cacheImageUseCase.getImageFile(source, platform, this@retrieveImage)
        if (cachedImage.exists() && cachedImage.length() > 0) {
            Log.d("TOTO", "Cached image exists: ${cachedImage.path}")
            _stateFlow.value = stateFlow.value.copy(image = cachedImage.path)
            return@withContext
        }

        if (downloadUseCase.downloadImage(source, platform, this@retrieveImage, cachedImage)) {
            Log.d("TOTO", "Download image to cache")
            _stateFlow.value = stateFlow.value.copy(image = cachedImage.path)
        }
    }

    private fun showLoader(show: Boolean) {
        _stateFlow.value = stateFlow.value.copy(showLoader = show)
    }

    private fun getGameFile(game: Game): File {
        val gamePath = game.path
        val name = if (gamePath.contains("/")) {
            gamePath.substring(gamePath.lastIndexOf("/") + 1)
        } else if (gamePath.contains("\\")) {
            gamePath.substring(gamePath.lastIndexOf("\\") + 1)
        } else {
            gamePath
        }
        val parent = File(getApplication<Application>().cacheDir, "games")
        if (!parent.exists()) parent.mkdirs()
        return File(parent, name)
    }

    private fun getCurrentSource(): Source? = stateFlow.value.source
    private fun getCurrentPlatform(): Platform? = stateFlow.value.platform
    private fun getCurrentGame(): Game? = stateFlow.value.game

    data class State(
        val source: Source? = null,
        val platform: Platform? = null,
        val game: Game? = null,
        val image: String? = null,
        val showLoader: Boolean = false,
        val copyDestinations: List<Source> = emptyList(),
    )

    companion object {

        private const val TAG = "GameViewModel"
    }
}
