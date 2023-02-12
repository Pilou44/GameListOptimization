package com.wechantloup.gamelistoptimization.data.scraper.screenscraperfr

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class UserInterceptor(
    private val getAccount: () -> Pair<String, String>,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val (login, password) = getAccount()
        var request: Request = chain.request()

        if (login.isBlank() || password.isBlank()) {
            return chain.proceed(request)
        }

        val url: HttpUrl = request.url.newBuilder()
            .addQueryParameter("ssid", login)
            .addQueryParameter("sspassword", password)
            .build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}
