package com.wechantloup.gamelistoptimization.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wechantloup.gamelistoptimization.R
import com.wechantloup.gamelistoptimization.ui.compose.BackButton

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsState()
    SettingsScreen(
        login = state.value.login,
        password = state.value.password,
        updateAccount = viewModel::updateAccount,
        onBackPressed = onBackPressed,
    )
}

@Composable
private fun SettingsScreen(
    login: String,
    password: String,
    updateAccount: (String, String) -> Unit,
    onBackPressed: () -> Unit,
) {
    var modified by remember { mutableStateOf(false) }
    var newLogin by remember { mutableStateOf(login) }
    var newPassword by remember { mutableStateOf(password) }
    val scaffoldState = rememberScaffoldState()

    val saveAndGoBack: () -> Unit = {
        if (modified) updateAccount(newLogin, newPassword)
        onBackPressed()
    }

    BackHandler {
        saveAndGoBack()
    }

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
            Text(text = "ScreenScraper Account")
            OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = newLogin,
                onValueChange = {
                    modified = true
                    newLogin = it
                },
                label = { Text("Login") }
            )
            OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = newPassword,
                onValueChange = {
                    modified = true
                    newPassword = it
                },
                label = { Text("Password") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        login = "Toto",
        password = "qwerty",
        updateAccount = { _, _ -> },
        onBackPressed = {},
    )
}
