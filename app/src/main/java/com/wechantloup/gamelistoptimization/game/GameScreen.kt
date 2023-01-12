package com.wechantloup.gamelistoptimization.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.compose.BackButton
import com.wechantloup.gamelistoptimization.compose.Dropdown
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.theme.Dimens

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackPressed: () -> Unit,
    onEditClicked: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    GameScreen(
        game = state.value.game,
        image = state.value.image,
        copyDestinations = state.value.copyDestinations,
        onCopyClicked = viewModel::copyGame,
        onBackPressed = onBackPressed,
        onEditClicked = onEditClicked,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GameScreen(
    game: Game?,
    image: String?,
    copyDestinations: List<Source>,
    onCopyClicked: (Source) -> Unit,
    onBackPressed: () -> Unit,
    onEditClicked: () -> Unit,
) {
    BackHandler {
        onBackPressed()
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
                navigationIcon = { BackButton(onBackPressed = onBackPressed) },
                actions = {
                    Button(onClick = onEditClicked) {
                        Text(text = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        top = Dimens.spacingS,
                        start = Dimens.spacingS,
                        end = Dimens.spacingS,
                        bottom = Dimens.spacingS + paddingValues.calculateBottomPadding(),
                    )
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        modifier = Modifier.padding(Dimens.spacingS),
                        text = game?.name ?: game?.path ?: "",
                    )
                    Field(
                        modifier = Modifier.padding(Dimens.spacingS),
                        name = stringResource(R.string.game_developer),
                        value = game?.developer ?: "",
                    )
                    Field(
                        modifier = Modifier.padding(Dimens.spacingS),
                        name = stringResource(R.string.game_publisher),
                        value = game?.publisher ?: "",
                    )
                }
                GlideImage(
                    modifier = Modifier
                        .weight(1f)
                        .padding(Dimens.spacingS),
                    model = image,
                    contentDescription = game?.name,
                )
            }
            Text(
                modifier = Modifier.padding(Dimens.spacingS),
                text = game?.desc ?: "",
            )
            CopyZone(
                modifier = Modifier.align(CenterHorizontally),
                sources = copyDestinations,
                onCopyClicked = onCopyClicked,
            )
        }
    }
}

@Composable
private fun Field(
    modifier: Modifier = Modifier,
    name: String,
    value: String,
) {
    Row(modifier = modifier) {
        Text(
            modifier = Modifier,
            text = name,
        )
        Text(
            modifier = Modifier.padding(start = Dimens.spacingXs),
            text = value,
        )
    }
}

@Composable
private fun CopyZone(
    modifier: Modifier = Modifier,
    sources: List<Source>,
    onCopyClicked: (Source) -> Unit,
) {
    var source: Source? by remember { mutableStateOf(null) }
    Row(modifier = modifier) {
        Text(
            modifier = Modifier.align(CenterVertically),
            text = stringResource(R.string.copy_to),
        )
        Dropdown(
            modifier = Modifier
                .align(CenterVertically)
                .padding(start = Dimens.spacingS, end = Dimens.spacingS),
            title = stringResource(R.string.source),
            values = sources,
            onValueSelected = { source = it },
        )
        Button(
            modifier = Modifier.align(CenterVertically),
            enabled = sources.isNotEmpty(),
            onClick = { source?.let { onCopyClicked(it) } },
        ) {
            Text(text = stringResource(R.string.btn_copy))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    val game = Game(
        id = null,
        source = null,
        path = "path",
        name = "Name",
        desc = LoremIpsum(words = 50).values.first(),
        rating = null,
        releasedate = null,
        developer = "Developer",
        publisher = "Publisher",
        genre = null,
        players = null,
        image = null,
        marquee = null,
        video = null,
        genreid = null,
        favorite = null,
        kidgame = null,
        hidden = null,
    )

    GameScreen(
        game = game,
        image = null,
        copyDestinations = emptyList(),
        onCopyClicked = {},
        onBackPressed = {},
        onEditClicked = {},
    )
}
