package com.wechantloup.gamelistoptimization.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.compose.BackButton
import com.wechantloup.gamelistoptimization.compose.FullScreenLoader
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.theme.Dimens

@Composable
fun EditGameScreen(
    viewModel: GameViewModel,
    onBackPressed: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    EditGameScreen(
        game = requireNotNull(state.value.game),
        image = state.value.image,
        isLoaderVisible = state.value.showLoader,
        onBackPressed = onBackPressed,
        onGameChanged = viewModel::onGameChanged,
        saveGame = viewModel::saveGame,
        scrapGame = viewModel::scrapGame,
    )
}

@Composable
private fun EditGameScreen(
    game: Game,
    image: String?,
    isLoaderVisible: Boolean,
    onBackPressed: () -> Unit,
    onGameChanged: (Game) -> Unit,
    saveGame: (() -> Unit) -> Unit,
    scrapGame: () -> Unit,
) {
    var modified by remember { mutableStateOf(false) }

    val saveAndGoBack: () -> Unit = {
        if (modified) saveGame(onBackPressed) else onBackPressed()
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
                actions = {
                    Button(onClick = {
                        modified = true
                        scrapGame()
                    }) {
                        Text(text = "Scrap")
                    }
                },
            )
        }
    ) { paddingValues ->
        FullScreenLoader(
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            isVisible = isLoaderVisible,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.spacingS)
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                    TextField(
                        modifier = Modifier.padding(Dimens.spacingS),
                        value = game.name ?: game.path,
                        onValueChange = {
                            onGameChanged(game.copy(name = it))
                            modified = true
                        },
                        label = { Text(stringResource(R.string.game_name)) },
                    )
                    TextField(
                        modifier = Modifier.padding(Dimens.spacingS),
                        value = game.developer ?: "",
                        onValueChange = {
                            onGameChanged(game.copy(developer = it))
                            modified = true
                        },
                        label = { Text(stringResource(R.string.game_developer)) },
                    )
                    TextField(
                        modifier = Modifier.padding(Dimens.spacingS),
                        value = game.publisher ?: "",
                        onValueChange = {
                            onGameChanged(game.copy(publisher = it))
                            modified = true
                        },
                        label = { Text(stringResource(R.string.game_publisher)) },
                    )
                    TextField(
                        modifier = Modifier.padding(Dimens.spacingS),
                        value = game.desc ?: "",
                        onValueChange = {
                            onGameChanged(game.copy(desc = it))
                            modified = true
                        },
                        label = { Text(stringResource(R.string.game_desc)) },
                    )
                }
                AsyncImage(
                    modifier = Modifier
                        .padding(Dimens.spacingS),
                    model = image,
                    contentDescription = game.name,
                )
            }
        }
    }
}
