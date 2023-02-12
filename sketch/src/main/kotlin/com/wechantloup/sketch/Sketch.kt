package com.wechantloup.sketch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.util.Size
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.FileOutputStream
import java.io.OutputStream

class Sketch(private val context: Context, width: Int, height: Int) {

    private val bitmap: Bitmap
    private val canvas: Canvas

    init {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    fun drawImage(uri: String, x: Int, y: Int, width: Int, height: Int) {
        val imageSize = Size(width, height)
        val bitmap = context.getBitmap(uri, imageSize)

        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
    }

    fun save(path: String) {
        val stream: OutputStream = FileOutputStream(path)
        /* Write bitmap to file using JPEG or PNG and 80% quality hint for JPEG. */
        bitmap.compress(
            CompressFormat.PNG,
            80,
            stream
        )
        stream.close()
    }

    companion object {

        private fun Context.getBitmap(uri: String, size: Size? = null): Bitmap {
            val requestBuilder = Glide.with(this)
                .asBitmap()
                .load(uri)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)

            return if (size == null) {
                requestBuilder.submit().get()
            } else {
                requestBuilder.override(size.width, size.height).submit().get()
            }
        }
    }
}
