package com.wechantloup.gamelistoptimization

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun MainScreen(
    viewModel: MainViewModel,
) {
    val state = viewModel.stateFlow.collectAsState()
    MainScreen(
        sources = state.value.sources,
        platforms = state.value.platforms,
        games = state.value.games,
        onSourceSelected = viewModel::setSource,
        onPlatformSelected = viewModel::setPlatform,
        onForChildClicked = viewModel::onGameSetForKids,
        onFavoriteClicked = viewModel::onGameSetFavorite,
        onCopyBackupClicked = viewModel::copyBackupValues,
        onAllChildClicked = viewModel::setAllForKids,
        onAllFavoriteClicked = viewModel::setAllFavorite,
    )
}

@Composable
fun MainScreen(
    sources: List<Source>,
    platforms: List<Platform>,
    games: List<Game>,
    onSourceSelected: (Source) -> Unit,
    onPlatformSelected: (Platform) -> Unit,
    onForChildClicked: (String, Boolean) -> Unit,
    onFavoriteClicked: (String, Boolean) -> Unit,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
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
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Dropdown(
                title = "Sources",
                values = sources,
                onValueSelected = onSourceSelected,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Dropdown(
                title = "Platforms",
                values = platforms,
                onValueSelected = onPlatformSelected,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Header(
                onCopyBackupClicked = onCopyBackupClicked,
                onAllChildClicked = onAllChildClicked,
                onAllFavoriteClicked = onAllFavoriteClicked
            )
            GameListItem(
                modifier = Modifier.weight(1f),
                games = games,
                onForChildClicked = onForChildClicked,
                onFavoriteClicked = onFavoriteClicked,
            )
        }
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        TextButton(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
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
    onForChildClicked: (String, Boolean) -> Unit,
    onFavoriteClicked: (String, Boolean) -> Unit,
) {
    LazyColumn(modifier) {
        games.forEach { game ->
            item {
                GameItem(
                    game = game,
                    onForChildClicked = { checked -> onForChildClicked(game.id, checked) },
                    onFavoriteClicked = { checked -> onFavoriteClicked(game.id, checked) }
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
) {
    GameItem(
        modifier = modifier,
        game.name ?: game.path,
        game.kidgame ?: false,
        game.favorite ?: false,
        onForChildClicked,
        onFavoriteClicked,
    )
}

@Composable
fun GameItem(
    modifier: Modifier = Modifier,
    name: String,
    isForChild: Boolean,
    isFavorite: Boolean,
    onForChildClicked: (Boolean) -> Unit,
    onFavoriteClicked: (Boolean) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
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

// ExposedDropdownMenuBox is experimental
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> Dropdown(
    title: String,
    values: List<T>,
    modifier: Modifier = Modifier,
    onValueSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption: T? by remember { mutableStateOf(null) }

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
            onValueChange = { Log.i("TOTO", "On text changed $it")},
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

    if (selectedOption == null && values.isNotEmpty()) {
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
