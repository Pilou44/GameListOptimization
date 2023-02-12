package com.wechantloup.gamelistoptimization.ui.compose

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.wechantloup.gamelistoptimization.R

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    IconButton(
        modifier = modifier.wrapContentSize(),
        onClick = onBackPressed
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back_24),
            contentDescription = "Back",
            tint = MaterialTheme.colors.onSurface,
        )
    }
}
