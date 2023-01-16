package com.wechantloup.gamelistoptimization.model

data class Game(
    val id: String?,
    val source: String?,
    val path: String,
    val name: String?,
    val desc: String?,
    val rating: String?,
    val releasedate: String?,
    val developer: String?,
    val publisher: String?,
    val genre: String?,
    val players: String?,
    val image: String?,
    val marquee: String?,
    val video: String?,
    val genreid: String?,
    var favorite: Boolean?,
    var kidgame: Boolean?,
    var hidden: Boolean?,
)
