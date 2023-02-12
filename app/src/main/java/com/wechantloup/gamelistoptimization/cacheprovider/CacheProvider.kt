package com.wechantloup.gamelistoptimization.cacheprovider

import android.content.Context
import java.io.File

class CacheProvider(context: Context) {
    private val root = context.cacheDir

    fun getTempDir(): File {
        val dir = File(root, "tmp")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
