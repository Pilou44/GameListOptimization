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
import com.wechantloup.gamelistoptimization.data.AccountRepository
import com.wechantloup.gamelistoptimization.data.cacheprovider.CacheProvider
import com.wechantloup.gamelistoptimization.ui.game.EditGameScreen
import com.wechantloup.gamelistoptimization.ui.game.GameScreen
import com.wechantloup.gamelistoptimization.ui.game.GameViewModel
import com.wechantloup.gamelistoptimization.ui.game.GameViewModelFactory
import com.wechantloup.gamelistoptimization.ui.main.MainScreen
import com.wechantloup.gamelistoptimization.ui.main.MainViewModel
import com.wechantloup.gamelistoptimization.ui.main.MainViewModelFactory
import com.wechantloup.gamelistoptimization.ui.platform.EditPlatformScreen
import com.wechantloup.gamelistoptimization.ui.platform.PlatformViewModel
import com.wechantloup.gamelistoptimization.ui.platform.PlatformViewModelFactory
import com.wechantloup.gamelistoptimization.data.sambaprovider.GameListProvider
import com.wechantloup.gamelistoptimization.data.scraper.Scraper
import com.wechantloup.gamelistoptimization.ui.settings.SettingsScreen
import com.wechantloup.gamelistoptimization.ui.settings.SettingsViewModel
import com.wechantloup.gamelistoptimization.ui.settings.SettingsViewModelFactory
import com.wechantloup.gamelistoptimization.ui.theme.WechantTheme
import com.wechantloup.gamelistoptimization.data.webdownloader.WebDownloader
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        NavHostController(this).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
    }

    private val accountRepository by lazy { AccountRepository(this) }
    private val provider by lazy { GameListProvider() }
    private val scraper by lazy { Scraper(accountRepository::getAccount) }
    private val webDownloader by lazy { WebDownloader() }
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
        SettingsViewModelFactory(this, accountRepository)
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
                    openSettings = {
                        navController.navigate(SETTINGS_SCREEN)
                    }
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

            composable(SETTINGS_SCREEN) {
                settingsViewModel.reload()
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
        private const val SETTINGS_SCREEN = "settings_screen"
    }
}
