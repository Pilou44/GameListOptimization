package com.wechantloup.gamelistoptimization.settings

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wechantloup.gamelistoptimization.PreferencesRepository
import com.wechantloup.gamelistoptimization.usecase.AccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModelFactory(
    private val activity: Activity,
    private val repo: PreferencesRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val accountUseCase = AccountUseCase(repo)
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(
            activity.application,
            accountUseCase,
        ) as T
    }
}

class SettingsViewModel(
    application: Application,
    private val accountUseCase: AccountUseCase,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    init {
        val (login, password) = accountUseCase.getAccount()
        _stateFlow.value = stateFlow.value.copy(login = login, password = password)
    }

    fun updateAccount(login: String, password: String) {
        accountUseCase.saveAccount(login, password)
    }

    data class State(
        val login: String = "",
        val password: String = "",
    )
}
