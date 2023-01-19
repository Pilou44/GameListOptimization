package com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model

import com.google.gson.annotations.SerializedName

class GameInfoResponse(
    @SerializedName("response") val response: Response,
)

class Response(
    @SerializedName("jeu") val game: ScraperGame,
)

class ScraperGame(
    @SerializedName("id") val id: String?,
    @SerializedName("noms") val names: List<RegionString>?,
    @SerializedName("synopsis") val synopsis: List<LanguageString>?,
    @SerializedName("note") val rating: TextString?,
    @SerializedName("dates") val dates: List<RegionString>?,
    @SerializedName("developpeur") val developer: IdString?,
    @SerializedName("editeur") val publisher: IdString?,
    @SerializedName("genres") val genres: List<Genre>?,
    @SerializedName("joueurs") val players: TextString?,
    @SerializedName("notgame") val unknownGame: Boolean,
    @SerializedName("medias") val medias: List<Media>,
)

class TextString(
    @SerializedName("text") val text: String,
)

class IdString(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
)

class Genre(
    @SerializedName("id") val id: String,
    @SerializedName("noms") val names: List<LanguageString>,
)

class RegionString(
    @SerializedName("region") val region: String,
    @SerializedName("text") val text: String,
)

class LanguageString(
    @SerializedName("langue") val language: String,
    @SerializedName("text") val text: String,
)

class Media(
    @SerializedName("type") val type: String,
    @SerializedName("url") val url: String,
    @SerializedName("region") val region: String,
)
