package com.wechantloup.gamelistoptimization

import android.content.Context
import android.content.SharedPreferences

class PreferencesRepository(context: Context) {
    private var settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)

    fun saveString(key: String, value: String) {
        settings.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String = ""): String {
        return settings.getString(key, default) ?: default
    }

    companion object {
        private const val PREFS_NAME = "game_list_settings"
    }
}
