package com.wechantloup.gamelistoptimization.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FullScreenLoader(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
    ) {
        content()
        if (isVisible) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {

                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f)
                ) {}
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize(0.5f)
                        .aspectRatio(1f),
                    strokeWidth = 10.dp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullScreenLoaderPreview() {
    FullScreenLoader(
        modifier = Modifier.fillMaxSize(),
        isVisible = true,
        content = {}
    )
}
