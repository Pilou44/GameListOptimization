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
import com.google.accompanist.themeadapter.material.MdcTheme

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        NavHostController(this).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
    }

    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MdcTheme {
                NavigationHost(
                    navController = navController,
                    viewModel = viewModel,
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
                    onEditPlatformClicked = { navController.navigate(EDIT_PLATFORM_SCREEN) }
                )
            }

            composable(EDIT_PLATFORM_SCREEN) {
                EditPlatformScreen()
            }

//            composable(LIVE_SCREEN) {
//                LivePhotoScreen(
//                    liveViewModel = liveViewModel,
//                    takePhoto = { liveViewModel.takePhoto() },
//                    ratio = ratio,
//                )
//            }
//
//            composable(
//                route = PRINT_SCREEN,
//                arguments = listOf(
//                    navArgument(ARG_PHOTO_ID) {
//                        type = NavType.StringType
//                        nullable = false
//                    }
//                ),
//            ) { backStackEntry ->
//                val photoId = backStackEntry.arguments?.getString(ARG_PHOTO_ID) ?: return@composable
//                printViewModel.newPhoto(photoId)
//                PrintPhotoScreen(printViewModel = printViewModel)
//            }
//
//            composable(WAITING_FOR_PRINT_SCREEN) {
//                WaitingForPrintScreen(
//                    onFinish = { navController.popBackStack(route = STAND_BY_SCREEN, inclusive = false) }
//                )
//            }
//
//            composable(route = ADMINISTRATION_SCREEN) {
//                AdministrationScreen(scaffoldState, administrationViewModel)
//            }
//
//            composable(route = RELOAD_PRINTER_SCREEN) {
//                ReloadPrinterScreen(
//                    onReloadFinished = { navController.popBackStack(route = STAND_BY_SCREEN, inclusive = false) }
//                )
//            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        // Screens
        private const val MAIN_SCREEN = "main_screen"
        private const val EDIT_PLATFORM_SCREEN = "edit_platform_screen"
    }
}
