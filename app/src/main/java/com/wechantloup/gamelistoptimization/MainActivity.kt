package com.wechantloup.gamelistoptimization

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wechantloup.gamelistoptimization.cacheprovider.CacheProvider
import com.wechantloup.gamelistoptimization.game.EditGameScreen
import com.wechantloup.gamelistoptimization.game.GameScreen
import com.wechantloup.gamelistoptimization.game.GameViewModel
import com.wechantloup.gamelistoptimization.game.GameViewModelFactory
import com.wechantloup.gamelistoptimization.main.MainScreen
import com.wechantloup.gamelistoptimization.main.MainViewModel
import com.wechantloup.gamelistoptimization.main.MainViewModelFactory
import com.wechantloup.gamelistoptimization.platform.EditPlatformScreen
import com.wechantloup.gamelistoptimization.platform.PlatformViewModel
import com.wechantloup.gamelistoptimization.platform.PlatformViewModelFactory
import com.wechantloup.gamelistoptimization.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.scraper.Scraper
import com.wechantloup.gamelistoptimization.settings.SettingsScreen
import com.wechantloup.gamelistoptimization.settings.SettingsViewModel
import com.wechantloup.gamelistoptimization.settings.SettingsViewModelFactory
import com.wechantloup.gamelistoptimization.theme.WechantTheme
import com.wechantloup.gamelistoptimization.webdownloader.WebDownloader
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        NavHostController(this).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
    }

    private val preferencesRepository by lazy { PreferencesRepository(this) }
    private val provider = GameListProvider()
    private val scraper by lazy { Scraper(preferencesRepository) }
    private val webDownloader = WebDownloader()
    private val cacheProvider by lazy { CacheProvider(this) }

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this, provider)
    }

    private val gameViewModel by viewModels<GameViewModel> {
        GameViewModelFactory(this, provider, scraper, webDownloader, cacheProvider)
    }

    private val platformViewModel by viewModels<PlatformViewModel> {
        PlatformViewModelFactory(this, provider, scraper, webDownloader, cacheProvider)
    }

    private val settingsViewModel by viewModels<SettingsViewModel> {
        SettingsViewModelFactory(this, preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WechantTheme {
                NavigationHost(
                    navController = navController,
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            provider.close()
        }
    }

    @Composable
    private fun NavigationHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
    ) {
        NavHost(navController = navController, startDestination = MAIN_SCREEN, modifier) {

            composable(MAIN_SCREEN) {
                MainScreen(
                    viewModel = mainViewModel,
                    onEditPlatformClicked = { source, platform ->
                        platformViewModel.setPlatform(source, platform)
                        navController.navigate(EDIT_PLATFORM_SCREEN)
                    },
                    onGameClicked = { source, platform, game ->
                        gameViewModel.openGame(source, platform, game)
                        navController.navigate(GAME_SCREEN)
                    },
                )
            }

            composable(EDIT_PLATFORM_SCREEN) {
                EditPlatformScreen(
                    viewModel = platformViewModel,
                    onBackPressed = {
                        mainViewModel.refresh()
                        navController.popBackStack(route = MAIN_SCREEN, inclusive = false)
                    },
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

            composable(SETTINGS_GAME_SCREEN) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackPressed = { navController.popBackStack(route = MAIN_SCREEN, inclusive = false) },
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
        private const val SETTINGS_GAME_SCREEN = "settings_game_screen"
    }
}
