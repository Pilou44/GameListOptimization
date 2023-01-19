package com.wechantloup.gamelistoptimization.scraper.screenscraperfr

import com.wechantloup.gamelistoptimization.BuildConfig
import com.wechantloup.gamelistoptimization.utils.FlipperUtils
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OkHttpClientFactory {

    fun createScreenScraperFr() = createService(
        baseUrl = "https://www.screenscraper.fr/api2/",
        serviceClass = ScreenScraperFrService::class.java,
        client = createClient(),
    )

    private fun createClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(DevInterceptor(BuildConfig.DEBUG))
            .addInterceptor(FormatInterceptor())
//            .addInterceptor(UserInterceptor) // ToDo
            .addNetworkInterceptor(FlipperUtils.createInterceptor())
            .build()


    companion object {
        private val defaultConverter by lazy { GsonConverterFactory.create() }

        private fun <T> createService(
            baseUrl: String,
            serviceClass: Class<T>,
            client: OkHttpClient,
            converter: Converter.Factory = defaultConverter,
        ): T = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(converter)
            .client(client)
            .build()
            .create(serviceClass)
    }
}
