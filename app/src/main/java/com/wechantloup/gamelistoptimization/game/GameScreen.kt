package com.wechantloup.gamelistoptimization

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource

@Composable
fun GameScreen(
    viewModel: MainViewModel,
    gamePath: String,
    onBackPressed: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    GameScreen(
        platformName = gamePath,
        saveName = viewModel::savePlatformName,
        onBackPressed = onBackPressed,
    )
}

@Composable
fun GameScreen(
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
