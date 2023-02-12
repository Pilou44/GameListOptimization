package com.wechantloup.gamelistoptimization.model

import com.wechantloup.gamelistoptimization.ui.compose.DropdownComparable

data class Platform(
    val name: String,
    val games: List<Game>,
    val gamesBackup: List<Game>?,
    val path: String,
    val system: String,
    val extensions: List<String>,
): DropdownComparable {

    override fun isSameAs(other: DropdownComparable): Boolean {
        return (other as? Platform)?.path == path
    }

    override fun toString(): String {
        return name
    }

    fun hasBackup() = gamesBackup != null
}
