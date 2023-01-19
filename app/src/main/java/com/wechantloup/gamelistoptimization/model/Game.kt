package com.wechantloup.gamelistoptimization.model

data class Game(
    val id: String? = null,
    val source: String? = null,
    val path: String,
    val name: String? = null,
    val desc: String? = null,
    val rating: String? = null,
    val releaseDate: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val genre: String? = null,
    val players: String? = null,
    val image: String? = null,
    val marquee: String? = null,
    val video: String? = null,
    val genreId: String? = null,
    var favorite: Boolean? = null,
    var kidgame: Boolean? = null,
    var hidden: Boolean? = null,
) {
    fun getRomName() = path.substring(path.lastIndexOf("/") + 1)
}
