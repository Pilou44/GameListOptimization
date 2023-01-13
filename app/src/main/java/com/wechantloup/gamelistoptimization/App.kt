package com.wechantloup.gamelistoptimization

import android.app.Application
import com.wechantloup.gamelistoptimization.utils.FlipperUtils

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FlipperUtils.init(this, BuildConfig.DEBUG)
    }
}
