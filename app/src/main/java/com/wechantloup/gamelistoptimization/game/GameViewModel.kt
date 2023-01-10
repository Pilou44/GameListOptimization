package com.wechantloup.gamelistoptimization.game

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wechantloup.gamelistoptimization.GameListProvider
import com.wechantloup.gamelistoptimization.GameListProvider.Companion.GAMELIST_FILE
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

class GameViewModelFactory(
    private val activity: Activity,
    private val provider: GameListProvider,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GameViewModel(activity.application, provider) as T
    }
}

class GameViewModel(
    application: Application,
    private val provider: GameListProvider,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    override fun onCleared() {
        viewModelScope.launch {
            provider.close()
            super.onCleared()
        }
    }

    fun openGame(source: Source, platformPath: String, gamePath: String) {
        val currentSource = getCurrentSource()
        val currentPlatformPath = getCurrentPlatform()?.path
        val currentGamePath = getCurrentGame()?.path
        val gameDecodedPath = URLDecoder.decode(gamePath, Charsets.UTF_8.name())
        val platformDecodedPath = URLDecoder.decode(platformPath, Charsets.UTF_8.name())

        val isSameSource = source == currentSource
        val isSamePlatform = currentPlatformPath == platformDecodedPath
        val isSameGame = currentGamePath == gameDecodedPath

        if (isSameSource && isSamePlatform && isSameGame) return

        Log.d(TAG, "Clear data: isSameSource='$isSameSource', isSamePlatform='$isSamePlatform', isSameGame='$isSameGame'")
        _stateFlow.value = stateFlow.value.copy(source = null, platform = null, game = null, image = null)

        Log.d(TAG, "Open game $gameDecodedPath")
        viewModelScope.launch {
            if (!provider.open(source)) throw IllegalStateException("Can't open source")

            val platform = provider
                .getPlatforms()
                .firstOrNull { it.path == platformDecodedPath }
                ?: throw IllegalStateException("Can't find platform")

            val game = platform.gameList.games
                .firstOrNull { it.path == gameDecodedPath }
                ?: throw IllegalStateException("Can't find game")

            _stateFlow.value = stateFlow.value.copy(source = source, platform = platform, game = game)

            game.retrieveImage(source, platform)
        }
    }

    private suspend fun Game.retrieveImage(source: Source, platform: Platform) = withContext(Dispatchers.IO) {
        if (image == null) return@withContext

        val cachedImage: File = getFile(source, platform, this@retrieveImage)
        if (cachedImage.exists()) {
            _stateFlow.value = stateFlow.value.copy(image = cachedImage.path)
            return@withContext
        }

        if (provider.downloadGameImage(this@retrieveImage, platform.path, cachedImage)) {
            _stateFlow.value = stateFlow.value.copy(image = cachedImage.path)
        }
    }

    private fun getFile(source: Source, platform: Platform, game: Game): File {
        val platformPath = platform.path.substring(0, platform.path.indexOf(GAMELIST_FILE))
        val name = URLEncoder
            .encode("${source.ip}${source.path}$platformPath${game.path}", Charsets.UTF_8.name())
            .replace("/", "")
            .replace("\\", "")
        val parent = File(getApplication<Application>().cacheDir, "images")
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
    )

    companion object {
        private const val TAG = "GameViewModel"
    }
}
