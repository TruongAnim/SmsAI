package com.truonganim.sms.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.truonganim.sms.ai.ui.screens.home.HomeScreen
import com.truonganim.sms.ai.ui.screens.home.HomeViewModel
import com.truonganim.sms.ai.ui.screens.thread.ThreadScreen
import com.truonganim.sms.ai.ui.screens.thread.ThreadViewModel
import com.truonganim.sms.ai.ui.screens.thread.ThreadViewModelFactoryProvider
import com.truonganim.sms.ai.ui.screens.permission.PermissionScreen
import com.truonganim.sms.ai.ui.screens.splash.SplashScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.hilt.navigation.compose.hiltViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Permission : Screen("permission")
    object Home : Screen("home")
    object Thread : Screen("thread/{threadId}/{address}") {
        fun createRoute(threadId: Long, address: String): String {
            val encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString())
            return "thread/$threadId/$encodedAddress"
        }
    }
    object NewConversation : Screen("new_conversation")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToPermission = {
                    navController.navigate(Screen.Permission.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permission.route) {
            PermissionScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onConversationClick = { threadId, address ->
                    navController.navigate(Screen.Thread.createRoute(threadId, address))
                },
                onNewConversation = {
                    navController.navigate(Screen.NewConversation.route)
                }
            )
        }

        composable(Screen.NewConversation.route) {
            // TODO: Implement NewConversationScreen
            // For now, we'll just pop back
            navController.popBackStack()
        }

        composable(
            route = Screen.Thread.route,
            arguments = listOf(
                navArgument("threadId") { type = NavType.LongType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            val address = URLDecoder.decode(
                backStackEntry.arguments?.getString("address"),
                StandardCharsets.UTF_8.toString()
            )
            
            val factoryProvider: ThreadViewModelFactoryProvider = hiltViewModel()
            val threadViewModel: ThreadViewModel = viewModel(
                factory = factoryProvider.createViewModelFactory(threadId, address)
            )
            
            ThreadScreen(
                viewModel = threadViewModel,
                onNavigateBack = {
                    homeViewModel.refresh()
                    navController.popBackStack()
                }
            )
        }
    }
} 