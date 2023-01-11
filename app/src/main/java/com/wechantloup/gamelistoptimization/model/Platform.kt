package com.wechantloup.gamelistoptimization.model

import com.wechantloup.gamelistoptimization.compose.DropdownComparable

data class Platform(
    val gameList: GameList,
    val gameListBackup: GameList?,
    val path: String,
): DropdownComparable {

    override fun isSameAs(other: DropdownComparable): Boolean {
        return (other as? Platform)?.path == path
    }

    override fun toString(): String {
        return gameList.platform ?: gameList.provider?.system ?: path
    }

    fun hasBackup() = gameListBackup != null
}
