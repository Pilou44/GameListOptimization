package com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model

import com.google.gson.annotations.SerializedName

class SystemListResponse(
    @SerializedName("response") val response: Response,
) {
    class Response(
        @SerializedName("systemes") val systems: List<System>,
    )

    class System(
        @SerializedName("id") val id: Int,
        @SerializedName("noms") val names: Names?,
        @SerializedName("extensions") val extensions: String?,
    )

    class Names(
        @SerializedName("nom_eu") val euName: String?,
        @SerializedName("nom_us") val usName: String?,
        @SerializedName("nom_jp") val jpName: String?,
        @SerializedName("nom_retropie") val retropieName: String?,
    )
}
