package com.wechantloup.gamelistoptimization.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.wechantloup.gamelistoptimization.BackButton
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.theme.Dimens

@Composable
fun EditGameScreen(
    viewModel: GameViewModel,
    onBackPressed: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    EditGameScreen(
        editableGame = requireNotNull(state.value.game),
        image = state.value.image,
        onBackPressed = onBackPressed,
        saveGame = viewModel::saveGame,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun EditGameScreen(
    editableGame: Game,
    image: String?,
    onBackPressed: () -> Unit,
    saveGame: (Game) -> Unit,
) {
    var game by remember { mutableStateOf(editableGame) }
    var modified by remember { mutableStateOf(false) }

    val saveAndGoBack: () -> Unit = {
        if (modified)
            saveGame(game)
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = Dimens.spacingS,
                    start = Dimens.spacingS,
                    end = Dimens.spacingS,
                    bottom = Dimens.spacingS + paddingValues.calculateBottomPadding(),
                )
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    modifier = Modifier.padding(Dimens.spacingS),
                    value = game.name ?: game.path,
                    onValueChange = {
                        game = game.copy(name = it)
                        modified = true
                    },
                    label = { Text(stringResource(R.string.game_name)) },
                )
                TextField(
                    modifier = Modifier.padding(Dimens.spacingS),
                    value = game.developer ?: "",
                    onValueChange = {
                        game = game.copy(developer = it)
                        modified = true
                    },
                    label = { Text(stringResource(R.string.game_developer)) },
                )
                TextField(
                    modifier = Modifier.padding(Dimens.spacingS),
                    value = game.publisher ?: "",
                    onValueChange = {
                        game = game.copy(publisher = it)
                        modified = true
                    },
                    label = { Text(stringResource(R.string.game_publisher)) },
                )
                TextField(
                    modifier = Modifier.padding(Dimens.spacingS),
                    value = game.desc ?: "",
                    onValueChange = {
                        game = game.copy(desc = it)
                        modified = true
                    },
                    label = { Text(stringResource(R.string.game_desc)) },
                )
            }
            GlideImage(
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimens.spacingS),
                model = image,
                contentDescription = game.name,
            )
        }
    }
}
