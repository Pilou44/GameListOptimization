package com.wechantloup.gamelistoptimization

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wechantloup.gamelistoptimization.game.GameScreen
import com.wechantloup.gamelistoptimization.game.GameViewModel
import com.wechantloup.gamelistoptimization.game.GameViewModelFactory
import com.wechantloup.gamelistoptimization.model.Source
import com.wechantloup.gamelistoptimization.theme.WechantTheme
import com.wechantloup.gamelistoptimization.utils.deserialize
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        NavHostController(this).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
    }

    private val provider = GameListProvider()

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this, provider)
    }

    private val gameViewModel by viewModels<GameViewModel> {
        GameViewModelFactory(this, provider)
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
                        val encodedSource = URLEncoder.encode(source, Charsets.UTF_8.name())
                        val encodedPlatform = URLEncoder.encode(platform, Charsets.UTF_8.name())
                        val encodedGame = URLEncoder.encode(game, Charsets.UTF_8.name())
                        navController.navigate("$GAME_SCREEN_NAME/$encodedSource/$encodedPlatform/$encodedGame")
                    },
                )
            }

            composable(EDIT_PLATFORM_SCREEN) {
                EditPlatformScreen(
                    viewModel = viewModel,
                    onBackPressed = { navController.popBackStack(route = MAIN_SCREEN, inclusive = false) }
                )
            }

            composable(
                route = GAME_SCREEN,
                arguments = listOf(
                    navArgument(ARG_GAME_PATH) {
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument(ARG_PLATFORM_PATH) {
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument(ARG_SERIALIZED_SOURCE) {
                        type = NavType.StringType
                        nullable = false
                    },
                ),
            ) { backStackEntry ->
                val source: Source = backStackEntry.arguments?.getString(ARG_SERIALIZED_SOURCE)?.deserialize() ?: return@composable
                val platformPath = backStackEntry.arguments?.getString(ARG_PLATFORM_PATH) ?: return@composable
                val gamePath = backStackEntry.arguments?.getString(ARG_GAME_PATH) ?: return@composable
                gameViewModel.openGame(source, platformPath, gamePath)
                GameScreen(
                    viewModel = gameViewModel,
                    onBackPressed = { navController.popBackStack(route = MAIN_SCREEN, inclusive = false) }
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val ARG_GAME_PATH = "gamePath"
        private const val ARG_PLATFORM_PATH = "argPlatformPath"
        private const val ARG_SERIALIZED_SOURCE = "argSerializedSource"

        // Screens
        private const val MAIN_SCREEN = "main_screen"
        private const val EDIT_PLATFORM_SCREEN = "edit_platform_screen"
        private const val GAME_SCREEN_NAME = "game_screen"
        private const val GAME_SCREEN = "$GAME_SCREEN_NAME/{$ARG_SERIALIZED_SOURCE}/{$ARG_PLATFORM_PATH}/{$ARG_GAME_PATH}"
    }
}
