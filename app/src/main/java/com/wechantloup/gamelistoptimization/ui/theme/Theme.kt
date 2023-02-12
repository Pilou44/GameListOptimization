package com.wechantloup.gamelistoptimization.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun ProvideDimens(
    dimensions: Dimensions,
    content: @Composable () -> Unit
) {
    val dimensionSet = remember { dimensions }
    CompositionLocalProvider(LocalAppDimens provides dimensionSet, content = content)
}

private val LocalAppDimens = staticCompositionLocalOf {
    defaultDimensions
}

@Composable
fun WechantTheme(
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val dimensions = when {
        configuration.screenWidthDp >= 360 -> defaultDimensions // Todo Just an example of how to use it
        else -> defaultDimensions
    }

    ProvideDimens(dimensions = dimensions) {
        MaterialTheme {
            CompositionLocalProvider(
                content = content,
            )
        }
    }
}

val Dimens: Dimensions
    @Composable
    get() = LocalAppDimens.current
