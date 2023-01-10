package com.wechantloup.gamelistoptimization

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
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.GameList
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.utils.serialize

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onEditPlatformClicked: () -> Unit,
    onGameClicked: (serializedSource: String, platformPath: String, gamePath: String) -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    MainScreen(
        sources = state.value.sources,
        currentSource = state.value.currentSource,
        platforms = state.value.platforms,
        currentPlatform = state.value.currentPlatform,
        games = state.value.games,
        isBackupAvailable = state.value.hasBackup,
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
    currentSource: Source?,
    platforms: List<Platform>,
    currentPlatform: Platform?,
    games: List<Game>,
    isBackupAvailable: Boolean,
    onSourceSelected: (Source) -> Unit,
    onPlatformSelected: (Platform) -> Unit,
    onForChildClicked: (String, Boolean) -> Unit,
    onFavoriteClicked: (String, Boolean) -> Unit,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
    onEditPlatformClicked: () -> Unit,
    onGameClicked: (serializedSource: String, platformPath: String, gamePath: String) -> Unit,
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
            Dropdown(
                title = stringResource(R.string.source),
                values = sources,
                selectedValue = currentSource,
                onValueSelected = onSourceSelected,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Platform(
                platforms = platforms,
                selectedPlatform = currentPlatform,
                onPlatformSelected = onPlatformSelected,
                onEditClicked = onEditPlatformClicked,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Header(
                isBackupAvailable = isBackupAvailable,
                onCopyBackupClicked = onCopyBackupClicked,
                onAllChildClicked = onAllChildClicked,
                onAllFavoriteClicked = onAllFavoriteClicked
            )
            GameListItem(
                modifier = Modifier.weight(1f),
                games = games,
                source = currentSource.serialize(),
                platformPath = currentPlatform?.path,
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
    selectedPlatform: Platform?,
    onPlatformSelected: (Platform) -> Unit,
    onEditClicked: () -> Unit,
) {
    Row(modifier = modifier) {
        Dropdown(
            title = stringResource(R.string.platform),
            values = platforms,
            selectedValue = selectedPlatform,
            onValueSelected = onPlatformSelected,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        IconButton(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.space_xl))
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
                .size(dimensionResource(id = R.dimen.space_xl))
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
                .size(dimensionResource(id = R.dimen.space_xl))
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
    games: List<Game>,
    source: String,
    platformPath: String?,
    onForChildClicked: (path: String, Boolean) -> Unit,
    onFavoriteClicked: (path: String, Boolean) -> Unit,
    onGameClicked: (serializedSource: String, platformPath: String, gamePath: String) -> Unit,
) {
    LazyColumn(modifier) {
        games.forEach { game ->
            item {
                GameItem(
                    game = game,
                    onForChildClicked = { checked -> onForChildClicked(game.path, checked) },
                    onFavoriteClicked = { checked -> onFavoriteClicked(game.path, checked) },
                    onGameClicked = { onGameClicked(source, requireNotNull(platformPath), game.path) }
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
                    .padding(dimensionResource(R.dimen.space_s)),
                text = name,
            )
            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(dimensionResource(id = R.dimen.space_xl)),
                checked = isForChild,
                onCheckedChange = onForChildClicked,
            )
            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(dimensionResource(id = R.dimen.space_xl)),
                checked = isFavorite,
                onCheckedChange = onFavoriteClicked,
            )
        }
    }
}

// ExposedDropdownMenuBox is experimental
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> Dropdown(
    modifier: Modifier = Modifier,
    title: String,
    values: List<T>,
    selectedValue: T? = null,
    onValueSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption: T? by remember { mutableStateOf(selectedValue) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            enabled = values.isNotEmpty(),
            readOnly = true,
            value = selectedOption?.toString() ?: "",
            onValueChange = {},
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            values.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onValueSelected(selectionOption)
                        selectedOption = selectionOption
                        expanded = false
                    },
                ) {
                    Text(text = selectionOption.toString())
                }
            }
        }
    }

    if (values.isNotEmpty() && (selectedOption == null || !values.contains(selectedOption))) {
        val value = values[0]
        selectedOption = value
        onValueSelected(value)
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
    val emptyGameList = GameList(
        platform = "Megadrive",
        provider = null,
        games = emptyList(),
    )
    val pf1 = Platform(
        gameList = emptyGameList,
        gameListBackup = null,
        path = "",
    )
    Platform(
        platforms = listOf(pf1),
        selectedPlatform = null,
        onPlatformSelected = {},
        onEditClicked = {},
    )
}

@Preview(showBackground = true)
@Composable
fun DropdownPreview() {
    Dropdown(
        title = "Games",
        values = listOf("Sonic", "Sonic2"),
        onValueSelected = {}
    )
}
