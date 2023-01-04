package com.wechantloup.gamelistoptimization

class Platform(
    val name: String,
    val gameList: GameList,
    val gameListBackup: GameList?,
    val path: String,
) {

    override fun toString(): String {
        return gameList.provider?.system ?: name
    }
}
