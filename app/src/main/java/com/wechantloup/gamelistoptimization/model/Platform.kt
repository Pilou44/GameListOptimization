package com.wechantloup.gamelistoptimization.model

data class Platform(
    val gameList: GameList,
    val gameListBackup: GameList?,
    val path: String,
) {

    override fun toString(): String {
        return gameList.platform ?: gameList.provider?.system ?: path
    }
}
