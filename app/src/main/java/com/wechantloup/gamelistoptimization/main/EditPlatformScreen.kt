package com.wechantloup.gamelistoptimization.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.compose.BackButton

@Composable
fun EditPlatformScreen(
    viewModel: MainViewModel,
    onBackPressed: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    val platform = state.value.platforms[state.value.currentPlatformIndex]
    val platformName = platform.toString()
    EditPlatformScreen(
        platformName = platformName,
        saveName = viewModel::savePlatformName,
        onBackPressed = onBackPressed,
    )
}

@Composable
fun EditPlatformScreen(
    platformName: String,
    saveName: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
    var name by remember { mutableStateOf(platformName) }

    val saveAndGoBack: () -> Unit = {
        saveName(name)
        onBackPressed()
    }

    BackHandler {
        saveAndGoBack()
    }

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name)
                    )
                },
                backgroundColor = MaterialTheme.colors.surface,
                navigationIcon = { BackButton(onBackPressed = saveAndGoBack) },
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            TextField(
                modifier = Modifier.align(Alignment.Center),
                value = name,
                onValueChange = { name = it },
                label = { Text("Label") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPlatformScreenPreview() {
    EditPlatformScreen(
        platformName = "Megadrive",
        saveName = {},
        onBackPressed = {},
    )
}
