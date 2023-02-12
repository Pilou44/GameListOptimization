package com.wechantloup.gamelistoptimization.data.scraper.screenscraperfr

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class FormatInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val url: HttpUrl = request.url.newBuilder()
            .addQueryParameter("output", OUTPUT_JSON)
            .build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }

    companion object {
        private const val OUTPUT_JSON = "json"
        private const val OUTPUT_XML = "XML"
    }
}
