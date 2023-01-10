package com.wechantloup.gamelistoptimization.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.wechantloup.gamelistoptimization.BackButton
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.model.Game

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackPressed: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    GameScreen(
        game = state.value.game,
        image = state.value.image,
        onBackPressed = onBackPressed,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GameScreen(
    game: Game?,
    image: String?,
    onBackPressed: () -> Unit,
) {
    val saveAndGoBack: () -> Unit = {
//        saveName(name) Todo
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row() {
                Text(text = game?.name ?: game?.path ?: "")
                GlideImage(
                    model = image,
                    contentDescription = game?.name,
//                    modifier = Modifier.padding(padding).clickable(onClick = onClick).fillParentMaxSize(),
                )
            }
        }
    }
}
