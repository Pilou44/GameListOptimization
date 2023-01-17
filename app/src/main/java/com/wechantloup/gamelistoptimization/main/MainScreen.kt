package com.wechantloup.gamelistoptimization.main

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.compose.Dropdown
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.theme.Dimens

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onEditPlatformClicked: () -> Unit,
    onGameClicked: (Source, Platform, Game) -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    MainScreen(
        sources = state.value.sources,
        currentSourceIndex = state.value.currentSourceIndex,
        platforms = state.value.platforms,
        currentPlatformIndex = state.value.currentPlatformIndex,
        onSourceSelected = viewModel::setSource,
        onPlatformSelected = viewModel::setPlatform,
        onForChildClicked = viewModel::onGameSetForKids,
        onFavoriteClicked = viewModel::onGameSetFavorite,
        onCopyBackupClicked = viewModel::copyBackupValues,
        onAllChildClicked = viewModel::setAllForKids,
        onAllFavoriteClicked = viewModel::setAllFavorite,
        onEditPlatformClicked = onEditPlatformClicked,
        onGameClicked = onGameClicked,
    )
}

@Composable
fun MainScreen(
    sources: List<Source>,
    currentSourceIndex: Int,
    platforms: List<Platform>,
    currentPlatformIndex: Int,
    onSourceSelected: (Source) -> Unit,
    onPlatformSelected: (Platform) -> Unit,
    onForChildClicked: (String, Boolean) -> Unit,
    onFavoriteClicked: (String, Boolean) -> Unit,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
    onEditPlatformClicked: () -> Unit,
    onGameClicked: (serializedSource: Source, platform: Platform, game: Game) -> Unit,
) {
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
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            val currentSource = if (currentSourceIndex == -1) null else sources[currentSourceIndex]
            val currentPlatform = if (currentPlatformIndex == -1) null else platforms[currentPlatformIndex]
            Dropdown(
                title = stringResource(R.string.source),
                values = sources,
                selectedIndex = currentSourceIndex,
                onValueSelected = onSourceSelected,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Platform(
                platforms = platforms,
                selectedPlatformIndex = currentPlatformIndex,
                onPlatformSelected = onPlatformSelected,
                onEditClicked = onEditPlatformClicked,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Header(
                isBackupAvailable = currentPlatform?.hasBackup() == true,
                onCopyBackupClicked = onCopyBackupClicked,
                onAllChildClicked = onAllChildClicked,
                onAllFavoriteClicked = onAllFavoriteClicked
            )
            GameListItem(
                modifier = Modifier.weight(1f),
                source = currentSource,
                platform = currentPlatform,
                onForChildClicked = onForChildClicked,
                onFavoriteClicked = onFavoriteClicked,
                onGameClicked = onGameClicked,
            )
        }
    }
}

@Composable
fun Platform(
    modifier: Modifier = Modifier,
    platforms: List<Platform>,
    selectedPlatformIndex: Int,
    onPlatformSelected: (Platform) -> Unit,
    onEditClicked: () -> Unit,
) {
    Row(modifier = modifier) {
        Log.d("MainScreen", "Set platform drop down")
        Dropdown(
            title = stringResource(R.string.platform),
            values = platforms,
            selectedIndex = selectedPlatformIndex,
            onValueSelected = onPlatformSelected,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        IconButton(
            modifier = Modifier
                .size(Dimens.spacingLXl)
                .align(Alignment.CenterVertically),
            onClick = onEditClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit_24),
                contentDescription = "Edit platform name",
                tint = MaterialTheme.colors.primary,
            )
        }
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    isBackupAvailable: Boolean,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        TextButton(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            enabled = isBackupAvailable,
            onClick = onCopyBackupClicked,
        ) {
            Text(
                text = stringResource(R.string.button_label_copy_backup)
            )
        }
        IconButton(
            modifier = Modifier
                .size(Dimens.spacingLXl)
                .align(Alignment.CenterVertically),
            onClick = onAllChildClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_child_24),
                contentDescription = "All for kids",
                tint = MaterialTheme.colors.primary,
            )
        }
        IconButton(
            modifier = Modifier
                .size(Dimens.spacingLXl)
                .align(Alignment.CenterVertically),
            onClick = onAllFavoriteClicked) {
            Icon(
                painter = painterResource(R.drawable.ic_star_24),
                contentDescription = "All favorites",
                tint = MaterialTheme.colors.primary,
            )
        }
    }
}

@Composable
fun GameListItem(
    modifier: Modifier = Modifier,
    source: Source?,
    platform: Platform?,
    onForChildClicked: (path: String, Boolean) -> Unit,
    onFavoriteClicked: (path: String, Boolean) -> Unit,
    onGameClicked: (serializedSource: Source, platform: Platform, game: Game) -> Unit,
) {
    LazyColumn(modifier) {
        platform?.games?.forEach { game ->
            item {
                GameItem(
                    game = game,
                    onForChildClicked = { checked -> onForChildClicked(game.path, checked) },
                    onFavoriteClicked = { checked -> onFavoriteClicked(game.path, checked) },
                    onGameClicked = { onGameClicked(requireNotNull(source), platform, game) }
                )
            }
        }
    }
}

@Composable
fun GameItem(
    modifier: Modifier = Modifier,
    game: Game,
    onForChildClicked: (Boolean) -> Unit,
    onFavoriteClicked: (Boolean) -> Unit,
    onGameClicked: () -> Unit,
) {
    GameItem(
        modifier = modifier,
        game.name ?: game.path,
        game.kidgame ?: false,
        game.favorite ?: false,
        onForChildClicked,
        onFavoriteClicked,
        onGameClicked,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GameItem(
    modifier: Modifier = Modifier,
    name: String,
    isForChild: Boolean,
    isFavorite: Boolean,
    onForChildClicked: (Boolean) -> Unit,
    onFavoriteClicked: (Boolean) -> Unit,
    onGameClicked: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onGameClicked,
    ) {
        Row {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
                    .padding(Dimens.spacingS),
                text = name,
            )
            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(Dimens.spacingLXl),
                checked = isForChild,
                onCheckedChange = onForChildClicked,
            )
            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(Dimens.spacingLXl),
                checked = isFavorite,
                onCheckedChange = onFavoriteClicked,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    Header(
        onCopyBackupClicked = {},
        onAllChildClicked = {},
        onAllFavoriteClicked = {},
        isBackupAvailable = true,
    )
}

@Preview(showBackground = true)
@Composable
fun GameItemPreview() {
    GameItem(
        name = "Sonic",
        isForChild = true,
        isFavorite = false,
        onForChildClicked = {},
        onFavoriteClicked = {},
        onGameClicked = {},
    )
}

@Preview(showBackground = true)
@Composable
fun PlatformPreview() {
    val pf1 = Platform(
        name = "Megadrive",
        games = emptyList(),
        gamesBackup = null,
        path = "",
        system = "megadrive",
        extensions = emptyList(),
    )
    Platform(
        platforms = listOf(pf1),
        selectedPlatformIndex = 0,
        onPlatformSelected = {},
        onEditClicked = {},
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val source = Source(
        name = "Source",
        ip = "",
        path = "",
        login = "",
        password = "",
    )
    val pf1 = Platform(
        name = "Megadrive",
        games = emptyList(),
        gamesBackup = null,
        path = "",
        system = "megadrive",
        extensions = emptyList(),
    )
    MainScreen(
        sources = listOf(source),
        currentSourceIndex = 0,
        platforms = listOf(pf1),
        currentPlatformIndex = 0,
        onSourceSelected = {},
        onPlatformSelected = {},
        onForChildClicked = { _, _ -> },
        onFavoriteClicked = { _, _ -> },
        onCopyBackupClicked = {},
        onAllChildClicked = {},
        onAllFavoriteClicked = {},
        onEditPlatformClicked = {},
        onGameClicked = { _, _, _ -> },
    )
}
