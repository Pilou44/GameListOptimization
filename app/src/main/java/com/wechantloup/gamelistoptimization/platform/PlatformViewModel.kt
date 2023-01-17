package com.wechantloup.gamelistoptimization.platform

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.Scraper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlatformViewModelFactory(
    private val activity: Activity,
    private val provider: GameListProvider,
    private val scraper: Scraper,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PlatformViewModel(activity.application, provider, scraper) as T
    }
}

class PlatformViewModel(
    application: Application,
    private val provider: GameListProvider,
    private val scraper: Scraper,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    fun setPlatform(platform: Platform) {
        _stateFlow.value = stateFlow.value.copy(platform = platform)
    }

    fun savePlatform(callback: () -> Unit) {
        val platform = getCurrentPlatform() ?: return
        showLoader(true)
        viewModelScope.launch {
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
        val platform = getCurrentPlatform() ?: return
        showLoader(true)
        viewModelScope.launch {
            val updatedPlatform = scraper
                .getPlatform(platform.system)
                .copy(
                    path = platform.path,
                    games = platform.games,
                    gamesBackup = platform.gamesBackup,
                )

            val cleanedPlatform = provider.cleanGameList(updatedPlatform)
            _stateFlow.value = stateFlow.value.copy(platform = cleanedPlatform)
            showLoader(false)
        }
    }

    private suspend fun savePlatform(platform: Platform) {
        provider.savePlatform(platform)
        _stateFlow.value = stateFlow.value.copy(platform = platform)
    }

    private fun getCurrentPlatform(): Platform? = stateFlow.value.platform

    private fun showLoader(show: Boolean) {
        _stateFlow.value = stateFlow.value.copy(showLoader = show)
    }

    data class State(
        val platform: Platform? = null,
        val showLoader: Boolean = false,
    )
}
