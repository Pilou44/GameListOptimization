package com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model

import com.google.gson.annotations.SerializedName

class GameInfoResponse(
    @SerializedName("response") val response: Response,
)

class Response(
    @SerializedName("jeu") val game: Game,
)

class Game(
    @SerializedName("id") val id: String?,
    @SerializedName("noms") val names: List<RegionString>?,
    @SerializedName("synopsis") val synopsis: List<LanguageString>?,
    @SerializedName("note") val rating: TextString?,
    @SerializedName("dates") val dates: List<RegionString>?,
    @SerializedName("developpeur") val developer: IdString?,
    @SerializedName("editeur") val publisher: IdString?,
    @SerializedName("genres") val genres: List<Genre>?,
    @SerializedName("joueurs") val players: TextString?,
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
    @SerializedName("langue") val region: String,
    @SerializedName("text") val text: String,
)
