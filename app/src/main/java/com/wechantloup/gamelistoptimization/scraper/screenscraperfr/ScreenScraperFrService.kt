package com.wechantloup.gamelistoptimization.scraper.screenscraperfr

import com.wechantloup.gamelistoptimization.scraper.screenscraperfr.model.SystemListResponse
import retrofit2.http.GET

interface ScreenScraperFrService {

    @GET("systemesListe.php")
    suspend fun getSystemList(): SystemListResponse
}
