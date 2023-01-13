package com.wechantloup.gamelistoptimization.scraper.screenscraperfr

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class DevInterceptor(private val devMode: Boolean) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val url: HttpUrl = request.url.newBuilder()
            .addQueryParameter("devid", DEV_ID)
            .addQueryParameter("devpassword", DEV_PASSWORD)
            .addQueryParameter("softname", APP_NAME)
            .addDebugPassword()
            .build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }

    private fun HttpUrl.Builder.addDebugPassword() = apply {
        if (devMode) {
            addQueryParameter("devdebugpassword", DEV_DEBUG_PASSWORD)
        }
    }

    companion object {
        private const val DEV_ID = "WechantLoup"
        private const val DEV_PASSWORD = "TRO8xugAHKX"
        private const val DEV_DEBUG_PASSWORD = "yYa1xy24xoB"
        private const val APP_NAME = "GameManager"
    }
}
