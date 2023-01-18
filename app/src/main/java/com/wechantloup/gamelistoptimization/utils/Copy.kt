package com.wechantloup.gamelistoptimization.utils

import java.io.InputStream
import java.io.OutputStream

fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } != -1) {
        target.write(buf, 0, length)
    }
}
