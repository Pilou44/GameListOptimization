package com.wechantloup.gamelistoptimization.utils

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils.shouldEnableFlipper
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.Interceptor

object FlipperUtils {

    private val networkFlipperPlugin = NetworkFlipperPlugin()

    /** Initializes Flipper in debug only. no-op in release mode. */
    fun init(context: Context, devMode: Boolean) {
        if (!devMode || !shouldEnableFlipper(context)) return

        SoLoader.init(context, false)

        val client = AndroidFlipperClient.getInstance(context)
        client.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
        client.addPlugin(networkFlipperPlugin)
        client.addPlugin(DatabasesFlipperPlugin(context))
        client.start()
    }

    /**
     * From [Flipper documentation](https://fbflipper.com/docs/setup/network-plugin) :
     * ```
     * As interceptors can modify the request and response,
     * add the Flipper interceptor after all others to get an accurate view of the network traffic.
     * ```
     *
     * In release, the Flipper interceptor is not created.
     */
    fun createInterceptor(): Interceptor = FlipperOkhttpInterceptor(networkFlipperPlugin)
}
