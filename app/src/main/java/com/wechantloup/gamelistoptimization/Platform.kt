package com.wechantloup.gamelistoptimization

class Platform(
    val gameList: GameList,
    val path: String,
) {

    override fun toString(): String {
        return gameList.provider.system
    }
}