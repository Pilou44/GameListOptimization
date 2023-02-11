package com.wechantloup.gamelistoptimization.scraper.screenscraperfr

import com.wechantloup.gamelistoptimization.usecase.AccountUseCase
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class UserInterceptor(
    private val accountUseCase: AccountUseCase,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val (login, password) = accountUseCase.getAccount()
        var request: Request = chain.request()
        val url: HttpUrl = request.url.newBuilder()
            .addNonEmptyQueryParameter("ssid", login)
            .addNonEmptyQueryParameter("sspassword", password)
            .build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }

    private fun HttpUrl.Builder.addNonEmptyQueryParameter(name: String, value: String?) = apply {
        if (value.isNullOrEmpty()) return@apply

        addQueryParameter(name, value)
    }
}
