package com.wechantloup.gamelistoptimization.data.scraper.screenscraperfr

import com.wechantloup.gamelistoptimization.data.scraper.screenscraperfr.model.GameInfoResponse
import com.wechantloup.gamelistoptimization.data.scraper.screenscraperfr.model.SystemListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ScreenScraperFrService {

    @GET("systemesListe.php")
    suspend fun getSystemList(): SystemListResponse

    @GET("jeuInfos.php")
    suspend fun getGameInfo(
        @Query("crc") crcHexa: String,
        @Query("systemeid") systemId: Int,
        @Query("romnom") romName: String,
        @Query("romtaille") romSize: Long,
        @Query("romtype") romType: String = "rom",
    ): GameInfoResponse
}
