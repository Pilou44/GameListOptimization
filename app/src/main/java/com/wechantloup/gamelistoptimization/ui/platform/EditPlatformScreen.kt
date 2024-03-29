package com.wechantloup.gamelistoptimization.ui.platform

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import androidx.compose.ui.tooling.preview.Preview
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.ui.compose.BackButton
import com.wechantloup.gamelistoptimization.ui.compose.FullScreenLoader
import com.wechantloup.gamelistoptimization.ui.compose.ScrapErrorDialog
import com.wechantloup.gamelistoptimization.model.ScrapResult

@Composable
fun EditPlatformScreen(
    viewModel: PlatformViewModel,
    onBackPressed: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    val platform = state.value.platform
    val platformName = platform.toString()
    EditPlatformScreen(
        platformName = platformName,
        isLoaderVisible = state.value.showLoader,
        errors = state.value.errors,
        clearErrors = viewModel::clearErrors,
        save = viewModel::savePlatform,
        updateName = viewModel::updateName,
        onCleanClicked = viewModel::cleanPlatform,
        scrapAllGames = viewModel::scrapAllGames,
        onBackPressed = onBackPressed,
    )
}

@Composable
fun EditPlatformScreen(
    platformName: String,
    isLoaderVisible: Boolean,
    errors: List<ScrapResult>,
    clearErrors: () -> Unit,
    save: (() -> Unit) -> Unit,
    updateName: (String) -> Unit,
    onCleanClicked: () -> Unit,
    scrapAllGames: () -> Unit,
    onBackPressed: () -> Unit,
) {
    var modified by remember { mutableStateOf(false) }

    val saveAndGoBack: () -> Unit = {
        if (modified) save(onBackPressed) else onBackPressed()
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
        FullScreenLoader(
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            isVisible = isLoaderVisible,
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
            ) {
                TextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    value = platformName,
                    onValueChange = {
                        modified = true
                        updateName(it)
                    },
                    label = { Text("Label") }
                )
                Button(
                    onClick = {
                        modified = true
                        onCleanClicked()
                    }
                ) {
                    Text(text = "Clean")
                }
                Button(
                    onClick = {
                        modified = true
                        scrapAllGames()
                    }
                ) {
                    Text(text = "Scrap all games")
                }
            }
            if (errors.isNotEmpty()) {
                ScrapErrorDialog(
                    errors = errors,
                    clearErrors = clearErrors,
                    modifier = Modifier,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPlatformScreenPreview() {
    EditPlatformScreen(
        platformName = "Megadrive",
        isLoaderVisible = false,
        errors = emptyList(),
        clearErrors = {},
        save = {},
        updateName = {},
        onCleanClicked = {},
        scrapAllGames = {},
        onBackPressed = {},
    )
}
