package com.wechantloup.gamelistoptimization.game

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider.Companion.GAMELIST_FILE
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.model.Sources
import com.wechantloup.gamelistoptimization.scraper.Scraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLEncoder

class GameViewModelFactory(
    private val activity: Activity,
    private val provider: GameListProvider,
    private val scraper: Scraper,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GameViewModel(activity.application, provider, scraper) as T
    }
}

class GameViewModel(
    application: Application,
    private val provider: GameListProvider,
    private val scraper: Scraper,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    private val gameSources = Sources.values().map { it.source }.toList()

    override fun onCleared() {
        viewModelScope.launch {
            provider.close()
            super.onCleared()
        }
    }

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

    fun saveGame(game: Game) {
        _stateFlow.value = stateFlow.value.copy(game = game)

        val currentPlatform = requireNotNull(getCurrentPlatform())
        val mutableGameList = currentPlatform.games.toMutableList()
        val gameIndex = mutableGameList.indexOfFirst { it.path == game.path }
        mutableGameList.removeAt(gameIndex)
        mutableGameList.add(gameIndex, game)
        val platform = currentPlatform.copy(games = mutableGameList)

        viewModelScope.launch {
            provider.savePlatform(platform)
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
            val scrapedGame = scraper.scrapGame(
                romName = game.getRomName(),
                system = platform.system,
                fileSize = provider.getGameSize(game, platform),
                crc = provider.getGameCrc(game, platform),
            )
            val newGame = Game(
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
            Log.i(TAG, "Set new scraper info for ${newGame.name}")
            _stateFlow.value = stateFlow.value.copy(scrapedGame = newGame)
            showLoader(false)
        }
    }

    private fun Game.getRomName() = path.substring(path.lastIndexOf("/") + 1)

    private suspend fun copyGameToCache(): File = withContext(Dispatchers.IO) {
        val game = requireNotNull(getCurrentGame())
        val platform = requireNotNull(getCurrentPlatform())
        val cacheFile = getGameFile(game)
        val result = provider.downloadGame(game, platform, cacheFile)
        Log.i(TAG, "File downloaded to cache success = $result")
        return@withContext cacheFile
    }

    private suspend fun copyGameToDest(file: File, destination: Source) {
        val platform = requireNotNull(getCurrentPlatform())
        val game = requireNotNull(getCurrentGame())
        val imageFile = getImageFile(
            requireNotNull(getCurrentSource()),
            platform,
            game,
        )
        val result = provider.uploadGame(
            game = game,
            gameFile = file,
            imageFile = if (imageFile.exists()) imageFile else null,
            srcPlatform = platform,
            destination = destination,
        )
        Log.i(TAG, "File upload to cache success = $result")
    }

    private suspend fun Game.retrieveImage(source: Source, platform: Platform) = withContext(Dispatchers.IO) {
        if (image == null) return@withContext

        val cachedImage: File = getImageFile(source, platform, this@retrieveImage)
        if (cachedImage.exists()) {
            _stateFlow.value = stateFlow.value.copy(image = cachedImage.path)
            return@withContext
        }

        if (provider.downloadGameImage(this@retrieveImage, platform, cachedImage)) {
            _stateFlow.value = stateFlow.value.copy(image = cachedImage.path)
        }
    }

    private fun showLoader(show: Boolean) {
        _stateFlow.value = stateFlow.value.copy(showLoader = show)
    }

    private fun getImageFile(source: Source, platform: Platform, game: Game): File {
        val platformPath = platform.path.substring(0, platform.path.indexOf(GAMELIST_FILE))
        val name = URLEncoder
            .encode("${source.ip}${source.path}$platformPath${game.path}", Charsets.UTF_8.name())
            .replace("/", "")
            .replace("\\", "")
        val parent = File(getApplication<Application>().cacheDir, "images")
        if (!parent.exists()) parent.mkdirs()
        return File(parent, name)
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
        val scrapedGame: Game? = null,
        val image: String? = null,
        val showLoader: Boolean = false,
        val copyDestinations: List<Source> = emptyList(),
    )

    companion object {

        private const val TAG = "GameViewModel"
    }
}
