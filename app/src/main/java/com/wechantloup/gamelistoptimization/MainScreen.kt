package com.wechantloup.gamelistoptimization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Column(modifier = Modifier.fillMaxSize()) {
        Spinner(
            values = sources,
            onValueSelected = onSourceSelected,
        )
        Spinner(
            values = platforms,
            onValueSelected = onPlatformSelected,
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

@Composable
fun <T> Spinner(
    values: List<T>,
    onValueSelected: (T) -> Unit,
) {
    // State variables
    val selectedValue = remember { mutableStateOf(values.firstOrNull()) }
    val expanded = remember { mutableStateOf(false)}

    Box(Modifier.fillMaxWidth(),contentAlignment = Alignment.Center) {
        Row(
            Modifier
                .padding(24.dp)
                .clickable {
                    expanded.value = !expanded.value
                }
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) { // Anchor view
            val text = selectedValue.value?.toString() ?: ""
            Text(text = text, fontSize = 18.sp,modifier = Modifier.padding(end = 8.dp)) // Country name label
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "")

            //
            DropdownMenu(expanded = expanded.value, onDismissRequest = {
                expanded.value = false
            }) {
                values.forEach{ value ->
                    DropdownMenuItem(onClick = {
                        expanded.value = false
                        selectedValue.value = value
                        onValueSelected(value)
                    }) {
                        Text(text = value.toString())
                    }
                }
            }
        }
    }

}

//@Composable
//fun DropDownList(
//    requestToOpen: Boolean = false,
//    list: List<String>,
//    request: (Boolean) -> Unit,
//    selectedString: (String) -> Unit
//) {
//    DropdownMenu(
//        modifier = Modifier.fillMaxWidth(),
////        toggle = {
////            // Implement your toggle
////        },
//        expanded = requestToOpen,
//        onDismissRequest = { request(false) },
//    ) {
//        list.forEach {
//            DropdownMenuItem(
//                modifier = Modifier.fillMaxWidth(),
//                onClick = {
//                    request(false)
//                    selectedString(it)
//                }
//            ) {
//                Text(it, modifier = Modifier.wrapContentWidth())
//            }
//        }
//    }
//}
//@Composable
//fun CountrySelection() {
//    val countryList = listOf(
//        "United state",
//        "Australia",
//        "Japan",
//        "India",
//    )
//    val text = remember { mutableStateOf("") } // initial value
//    val isOpen = remember { mutableStateOf(false) } // initial value
//    val openCloseOfDropDownList: (Boolean) -> Unit = {
//        isOpen.value = it
//    }
//    val userSelectedString: (String) -> Unit = {
//        text.value = it
//    }
//    Box {
//        Column {
//            OutlinedTextField(
//                value = text.value,
//                onValueChange = { text.value = it },
//                label = { Text(text = "TextFieldTitle") },
//                modifier = Modifier.fillMaxWidth()
//            )
//            DropDownList(
//                requestToOpen = isOpen.value,
//                list = countryList,
//                openCloseOfDropDownList,
//                userSelectedString
//            )
//        }
//        Spacer(
//            modifier = Modifier
//                .matchParentSize()
//                .background(Color.Transparent)
//                .padding(10.dp)
//                .clickable { isOpen.value = true }
//        )
//    }
//}

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
fun SpinnerPreview() {
    Spinner(
        values = listOf("Sonic", "Sonic2"),
        onValueSelected = {}
    )
}