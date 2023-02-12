package com.wechantloup.gamelistoptimization.data.webdownloader

import com.wechantloup.gamelistoptimization.utils.FlipperUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WebDownloader {

    private val httpClient: OkHttpClient =
        OkHttpClient.Builder()
            .addNetworkInterceptor(FlipperUtils.createInterceptor())
            .build()

    suspend fun download(url: String, dest: File) = withContext(Dispatchers.IO) {
        val assetResponse = httpClient.sendGetRequest(url)
        dest.writeResponse(assetResponse)
    }

    private suspend fun OkHttpClient.sendGetRequest(url: String): Response {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return sendRequest(request)
    }

    private class UnsuccessfulException(response: Response, serializedBody: String) :
        Exception("${response.requestUrl()} - ${response.code} - body: $serializedBody")

    private class NoBodyException(response: Response) : Exception("${response.requestUrl()} - ${response.code}")

    companion object {

        private suspend fun OkHttpClient.sendRequest(request: Request): Response = suspendCoroutine { continuation ->
            try {
                newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response)
                    }
                })
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        private fun File.writeResponse(response: Response) {
            response.useSuccessfulBodyOrThrow {
                writeBytes(it.bytes())
            }
        }

        private fun Response.useSuccessfulBodyOrThrow(action: (ResponseBody) -> Unit) {
            use { response ->
                val responseBody = response.body ?: throw NoBodyException(response)
                responseBody.use { body ->
                    if (response.isSuccessful) {
                        action(body)
                    } else {
                        throw UnsuccessfulException(response, body.string())
                    }
                }
            }
        }

        private fun Response.requestUrl(): String = request.url.toString()
    }
}
