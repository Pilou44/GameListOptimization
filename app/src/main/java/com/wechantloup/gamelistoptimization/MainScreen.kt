package com.wechantloup.gamelistoptimization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
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
    Column() {

    }
}

@Composable
fun MainScreen(
    games: List<Game>,
    onForChildClicked: (Boolean) -> Unit,
    onFavoriteClicked: (Boolean) -> Unit,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
    onForChildClicked: (Boolean) -> Unit,
    onFavoriteClicked: (Boolean) -> Unit,
) {
    LazyColumn(modifier) {
        games.forEach { game ->
            item {
                GameItem(
                    game = game,
                    onForChildClicked = onForChildClicked,
                    onFavoriteClicked = onFavoriteClicked
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