package com.wechantloup.gamelistoptimization.ui.main

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.model.Sources
import com.wechantloup.gamelistoptimization.data.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.usecase.GetPlatformsUseCase
import com.wechantloup.gamelistoptimization.usecase.SavePlatformUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModelFactory(
    private val activity: Activity,
    private val provider: GameListProvider,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val getPlatformsUseCase = GetPlatformsUseCase(provider)
        val savePlatformUseCase = SavePlatformUseCase(provider)
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(activity.application, getPlatformsUseCase, savePlatformUseCase) as T
    }
}

class MainViewModel(
    application: Application,
    private val getPlatformsUseCase: GetPlatformsUseCase,
    private val savePlatformUseCase: SavePlatformUseCase,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    private val gameSources = Sources.values().map { it.source }.toList()

    init {
        _stateFlow.value = stateFlow.value.copy(sources = gameSources)
    }

    fun refresh() {
        viewModelScope.launch {
            val currentPlatform = getCurrentPlatform()
            val source = requireNotNull(getCurrentSource())
            val platforms = getPlatformsUseCase.getPlatforms(source)
            val currentPlatformIndex = if (currentPlatform == null) {
                -1
            } else {
                platforms.indexOfFirst { it.isSameAs(currentPlatform) }
            }
            _stateFlow.value = stateFlow.value.copy(
                platforms = platforms,
                currentPlatformIndex = currentPlatformIndex,
            )
        }
    }

    fun setSource(source: Source) {
        Log.d(TAG, "Set source ${source.name}")
        _stateFlow.value = stateFlow.value.copy(
            currentSourceIndex = getSources().indexOf(source),
            platforms = emptyList(),
            currentPlatformIndex = -1,
        )
        viewModelScope.launch {
            _stateFlow.value = stateFlow.value.copy(
                platforms = getPlatformsUseCase.getPlatforms(source),
                currentPlatformIndex = -1,
            )
        }
    }

    fun setPlatform(selectedPlatform: Platform) {
        _stateFlow.value = stateFlow.value.copy(
            currentPlatformIndex = getPlatforms().getPlatformIndex(selectedPlatform),
        )
    }

    fun onGameSetForKids(gamePath: String, value: Boolean) {
        showLoader(true)
        val platform = getCurrentPlatform() ?: return
        platform.games.first { it.path == gamePath }.kidgame = value
        viewModelScope.launch {
            savePlatform(platform)
            showLoader(false)
        }
    }

    fun onGameSetFavorite(gamePath: String, value: Boolean) {
        showLoader(true)
        val platform = getCurrentPlatform() ?: return
        platform.games.first { it.path == gamePath }.favorite = value
        viewModelScope.launch {
            savePlatform(platform)
            showLoader(false)
        }
    }

    fun copyBackupValues() {
        showLoader(true)
        val platform = getCurrentPlatform() ?: return
        val gameListBackup = platform.gamesBackup ?: return
        platform.games.forEach { game ->
            val backup = gameListBackup.firstOrNull { it.id == game.id }
            backup?.let {
                game.kidgame = backup.kidgame
                game.favorite = backup.favorite
            }
        }
        viewModelScope.launch {
            savePlatform(platform)
            showLoader(false)
        }
    }

    fun setAllFavorite() {
        showLoader(true)
        val platform = getCurrentPlatform() ?: return
        val allFavorite = platform.games.all { it.favorite == true }
        platform.games.forEach {
            it.favorite = !allFavorite
        }
        viewModelScope.launch {
            savePlatform(platform)
            showLoader(false)
        }
    }

    fun setAllForKids() {
        showLoader(true)
        val platform = getCurrentPlatform() ?: return
        val allForKids = platform.games.all { it.kidgame == true }
        platform.games.forEach {
            it.kidgame = !allForKids
        }
        viewModelScope.launch {
            savePlatform(platform)
            showLoader(false)
        }
    }

    private fun showLoader(show: Boolean) {
        _stateFlow.value = stateFlow.value.copy(showLoader = show)
    }

    private suspend fun savePlatform(platform: Platform) {
        val source = getCurrentSource() ?: return
        savePlatformUseCase.savePlatform(source, platform)

        val platforms = getPlatformsUseCase.getPlatforms(source)
        val platformIndex = platforms.getPlatformIndex(platform)

        _stateFlow.value = stateFlow.value.copy(
            platforms = platforms,
            currentPlatformIndex = platformIndex,
        )
    }

    private fun List<Platform>.getPlatformIndex(platform: Platform) = indexOfFirst { it.isSameAs(platform) }

    private fun getPlatforms(): List<Platform> = stateFlow.value.platforms
    private fun getSources(): List<Source> = stateFlow.value.sources
    private fun getCurrentPlatformIndex(): Int = stateFlow.value.currentPlatformIndex
    private fun getCurrentSourceIndex(): Int = stateFlow.value.currentSourceIndex

    private fun getCurrentSource(): Source? {
        val index = getCurrentSourceIndex()

        if (index == -1) return null

        return getSources()[index]
    }

    private fun getCurrentPlatform(): Platform? {
        val index = getCurrentPlatformIndex()

        if (index == -1) return null

        return getPlatforms()[index]
    }

    data class State(
        val sources: List<Source> = emptyList(),
        val platforms: List<Platform> = emptyList(),
        val currentSourceIndex: Int = -1,
        val currentPlatformIndex: Int = -1,
        val showLoader: Boolean = false,
    )

    companion object {

        private const val TAG = "MainViewModel"
    }
}
