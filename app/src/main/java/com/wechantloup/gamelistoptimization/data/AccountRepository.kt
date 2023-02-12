package com.wechantloup.gamelistoptimization.data

import android.content.Context
import android.content.SharedPreferences

class AccountRepository(context: Context) {
    private var settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)

    fun getAccount(): Pair<String, String> {
        val login = getString(PREF_LOGIN)
        val password = getString(PREF_PASSWORD)
        return login to password
    }

    fun saveAccount(login: String, password: String) {
        saveString(PREF_LOGIN, login)
        saveString(PREF_PASSWORD, password)
    }

    private fun saveString(key: String, value: String) {
        settings.edit().putString(key, value).apply()
    }

    private fun getString(key: String, default: String = ""): String {
        return settings.getString(key, default) ?: default
    }

    companion object {
        private const val PREFS_NAME = "game_list_settings"
        private const val PREF_LOGIN = "pref_login"
        private const val PREF_PASSWORD = "pref_password"
    }
}
