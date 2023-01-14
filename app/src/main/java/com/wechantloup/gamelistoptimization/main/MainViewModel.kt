package com.wechantloup.gamelistoptimization.main

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.model.Sources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModelFactory(
    private val activity: Activity,
    private val provider: GameListProvider,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(activity.application, provider) as T
    }
}

class MainViewModel(
    application: Application,
    private val provider: GameListProvider,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    private val gameSources = Sources.values().map { it.source }.toList()

    init {
        _stateFlow.value = stateFlow.value.copy(sources = gameSources)
    }

    override fun onCleared() {
        viewModelScope.launch {
            provider.close()
            super.onCleared()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val source = requireNotNull(getCurrentSource())
            if (provider.open(source)) {
                _stateFlow.value = stateFlow.value.copy(
                    platforms = provider.getPlatforms(),
                )
            }
        }
    }

    fun setSource(source: Source) {
        Log.d(TAG, "Set source ${source.name}")
        viewModelScope.launch {
            if (provider.open(source)) {
                _stateFlow.value = stateFlow.value.copy(
                    currentSourceIndex = getSources().indexOf(source),
                    platforms = provider.getPlatforms(),
                    currentPlatformIndex = -1,
                )
            }
        }
    }

    fun setPlatform(selectedPlatform: Platform) {
        _stateFlow.value = stateFlow.value.copy(
            currentPlatformIndex = getPlatforms().getPlatformIndex(selectedPlatform),
        )
    }

    fun onGameSetForKids(gamePath: String, value: Boolean) {
        val platform = getCurrentPlatform() ?: return
        platform.gameList.games.first { it.path == gamePath }.kidgame = value
        viewModelScope.launch { savePlatform(platform) }
    }

    fun onGameSetFavorite(gamePath: String, value: Boolean) {
        val platform = getCurrentPlatform() ?: return
        platform.gameList.games.first { it.path == gamePath }.favorite = value
        viewModelScope.launch { savePlatform(platform) }
    }

    fun copyBackupValues() {
        val platform = getCurrentPlatform() ?: return
        val gameListBackup = platform.gameListBackup ?: return
        platform.gameList.games.forEach { game ->
            val backup = gameListBackup.games.firstOrNull { it.id == game.id }
            backup?.let {
                game.kidgame = backup.kidgame
                game.favorite = backup.favorite
            }
        }
        viewModelScope.launch { savePlatform(platform) }
    }

    fun setAllFavorite() {
        val platform = getCurrentPlatform() ?: return
        val allFavorite = platform.gameList.games.all { it.favorite == true }
        platform.gameList.games.forEach {
            it.favorite = !allFavorite
        }
        viewModelScope.launch { savePlatform(platform) }
    }

    fun setAllForKids() {
        val platform = getCurrentPlatform() ?: return
        val allForKids = platform.gameList.games.all { it.kidgame == true }
        platform.gameList.games.forEach {
            it.kidgame = !allForKids
        }
        viewModelScope.launch { savePlatform(platform) }
    }

    fun savePlatformName(name: String) {
        val platform = getCurrentPlatform() ?: return
        if (name.isEmpty() || name == platform.gameList.platform) return

        val newGameList = platform.gameList.copy(platform = name)
        val newPlatform = platform.copy(gameList = newGameList)
        viewModelScope.launch { savePlatform(newPlatform) }
    }

    private suspend fun savePlatform(platform: Platform) {
        provider.savePlatform(platform)

        val platforms = provider.getPlatforms()
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
    )

    companion object {
        private const val TAG = "MainViewModel"
    }
}
