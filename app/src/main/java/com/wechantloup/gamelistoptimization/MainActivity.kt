package com.wechantloup.gamelistoptimization

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wechantloup.gamelistoptimization.game.EditGameScreen
import com.wechantloup.gamelistoptimization.game.GameScreen
import com.wechantloup.gamelistoptimization.game.GameViewModel
import com.wechantloup.gamelistoptimization.game.GameViewModelFactory
import com.wechantloup.gamelistoptimization.main.EditPlatformScreen
import com.wechantloup.gamelistoptimization.main.MainScreen
import com.wechantloup.gamelistoptimization.main.MainViewModel
import com.wechantloup.gamelistoptimization.main.MainViewModelFactory
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.Scraper
import com.wechantloup.gamelistoptimization.theme.WechantTheme

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        NavHostController(this).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
    }

    private val provider = GameListProvider()
    private val scraper = Scraper()

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this, provider)
    }

    private val gameViewModel by viewModels<GameViewModel> {
        GameViewModelFactory(this, provider, scraper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WechantTheme {
                NavigationHost(
                    navController = navController,
                    viewModel = mainViewModel,
                )
            }
        }
    }

    @Composable
    private fun NavigationHost(
        navController: NavHostController,
        viewModel: MainViewModel,
        modifier: Modifier = Modifier,
    ) {
        NavHost(navController = navController, startDestination = MAIN_SCREEN, modifier) {

            composable(MAIN_SCREEN) {
                MainScreen(
                    viewModel = viewModel,
                    onEditPlatformClicked = { navController.navigate(EDIT_PLATFORM_SCREEN) },
                    onGameClicked = { source, platform, game ->
                        gameViewModel.openGame(source, platform, game)
                        navController.navigate(GAME_SCREEN)
                    },
                )
            }

            composable(EDIT_PLATFORM_SCREEN) {
                EditPlatformScreen(
                    viewModel = viewModel,
                    onBackPressed = { navController.popBackStack(route = MAIN_SCREEN, inclusive = false) },
                )
            }

            composable(GAME_SCREEN) {
                GameScreen(
                    viewModel = gameViewModel,
                    onBackPressed = {
                        mainViewModel.refresh()
                        navController.popBackStack(route = MAIN_SCREEN, inclusive = false)
                    },
                    onEditClicked = {
                        navController.navigate(EDIT_GAME_SCREEN)
                    },
                )
            }

            composable(EDIT_GAME_SCREEN) {
                EditGameScreen(
                    viewModel = gameViewModel,
                    onBackPressed = { navController.popBackStack(route = GAME_SCREEN, inclusive = false) },
                )
            }
        }
    }

    companion object {

        private const val TAG = "MainActivity"

        // Screens
        private const val MAIN_SCREEN = "main_screen"
        private const val EDIT_PLATFORM_SCREEN = "edit_platform_screen"
        private const val GAME_SCREEN = "game_screen"
        private const val EDIT_GAME_SCREEN = "edit_game_screen"
    }
}
