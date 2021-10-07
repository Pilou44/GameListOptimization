package com.wechantloup.gamelistoptimization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GameListHolder(
    @SerializedName("gameList") val gameList: GameList,
)

@Keep
data class GameList(
    @SerializedName("provider") val provider: Provider,
    @SerializedName("game") val game: List<Game>
)

@Keep
data class Provider(
    @SerializedName("System") val system: String,
    @SerializedName("software") val software: String,
    @SerializedName("database") val database: String,
    @SerializedName("web") val web: String,
)

@Keep
data class Game(
    @SerializedName("id") val id: String,
    @SerializedName("source") val source: String,
    @SerializedName("path") val path: String,
    @SerializedName("name") val name: String,
    @SerializedName("desc") val desc: String,
    @SerializedName("rating") val rating: String,
    @SerializedName("releasedate") val releasedate: String,
    @SerializedName("developer") val developer: String,
    @SerializedName("publisher") val publisher: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("players") val players: String,
    @SerializedName("image") val image: String,
    @SerializedName("marquee") val marquee: String,
    @SerializedName("video") val video: String,
    @SerializedName("genreid") val genreid: String,
    @SerializedName("favorite") val favorite: Boolean = false,
    @SerializedName("hidden") val kidgame: Boolean = false,
    @SerializedName("kidgame") val hidden: Boolean = true,
)