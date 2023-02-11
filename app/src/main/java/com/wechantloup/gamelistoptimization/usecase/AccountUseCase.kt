package com.wechantloup.gamelistoptimization.usecase

import com.wechantloup.gamelistoptimization.PreferencesRepository

class AccountUseCase(private val repo: PreferencesRepository) {

    fun getAccount(): Pair<String, String> {
        val login = repo.getString(PREF_LOGIN)
        val password = repo.getString(PREF_PASSWORD)
        return login to password
    }

    fun saveAccount(login: String, password: String) {
        repo.saveString(PREF_LOGIN, login)
        repo.saveString(PREF_PASSWORD, password)
    }

    companion object {
        private const val PREF_LOGIN = "pref_login"
        private const val PREF_PASSWORD = "pref_password"
    }
}
