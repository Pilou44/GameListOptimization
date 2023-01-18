package com.wechantloup.gamelistoptimization.utils

import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform

fun Game.getImagePath(platform: Platform): String {
    return "${platform.system}\\$image".cleanPath()
}

fun Game.getPath(platform: Platform): String {
    return "${platform.system}\\$path".cleanPath()
}

private fun String.cleanPath(): String = replace("/", "\\").replace("\\.\\", "\\")
