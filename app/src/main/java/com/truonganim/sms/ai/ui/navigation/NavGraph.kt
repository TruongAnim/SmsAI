package com.truonganim.sms.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.truonganim.sms.ai.ui.screens.home.HomeScreen
import com.truonganim.sms.ai.ui.screens.home.HomeViewModel
import com.truonganim.sms.ai.ui.screens.home.HomeTab
import com.truonganim.sms.ai.ui.screens.thread.ThreadScreen
import com.truonganim.sms.ai.ui.screens.thread.ThreadViewModel
import com.truonganim.sms.ai.ui.screens.thread.ThreadViewModelFactoryProvider
import com.truonganim.sms.ai.ui.screens.permission.PermissionScreen
import com.truonganim.sms.ai.ui.screens.splash.SplashScreen
import com.truonganim.sms.ai.ui.screens.conversation.NewConversationScreen
import com.truonganim.sms.ai.ui.screens.calls.CallDetailScreen
import com.truonganim.sms.ai.domain.model.PhoneNumber
import com.truonganim.sms.ai.utils.PhoneNumberUtils
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.truonganim.sms.ai.domain.model.Call
import com.truonganim.sms.ai.domain.model.CallType
import com.truonganim.sms.ai.utils.CallUtils

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Permission : Screen("permission")
    object Home : Screen("home")
    object Thread : Screen("thread/{threadId}/{address}") {
        fun createRoute(threadId: Long, address: String): String {
            val preparedAddress = address.replace("+", "%2B")
            val encodedAddress = URLEncoder.encode(preparedAddress, StandardCharsets.UTF_8.toString())
            return "thread/$threadId/$encodedAddress"
        }
    }
    object NewConversation : Screen("new_conversation")
    object CallDetail : Screen("call_detail/{number}/{name}") {
        fun createRoute(number: String, name: String?) = "call_detail/$number/${name ?: "Unknown"}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

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
                },
                onCallMessage = { phoneNumber ->
                    scope.launch {
                        // First try with normalized number
                        val normalizedNumber = PhoneNumberUtils.normalizePhoneNumber(phoneNumber)
                        var threadId = homeViewModel.getThreadIdForAddress(normalizedNumber)
                        Log.d("NavGraph", "Tried normalized number $normalizedNumber, got threadId: $threadId")
                        
                        // If not found, try with original number
                        if (threadId == 0L) {
                            threadId = homeViewModel.getThreadIdForAddress(phoneNumber)
                            Log.d("NavGraph", "Tried original number $phoneNumber, got threadId: $threadId")
                        }
                        
                        // Use normalized number for the conversation
                        val addressToUse = normalizedNumber
                        Log.d("NavGraph", "Navigating to thread - phoneNumber: $addressToUse, threadId: $threadId")
                        
                        navController.navigate(Screen.Thread.createRoute(threadId, addressToUse))
                    }
                },
                onCallDetail = { call ->
                    navController.navigate(Screen.CallDetail.createRoute(call.number, call.name))
                },
                initialTab = if (navController.previousBackStackEntry?.destination?.route == Screen.CallDetail.route) {
                    HomeTab.CALLS
                } else {
                    HomeTab.MESSAGES
                }
            )
        }

        composable(Screen.NewConversation.route) {
            val messageViewModel: HomeViewModel = hiltViewModel()
            NewConversationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactSelected = { phoneNumber ->
                    scope.launch {
                        // First try with normalized number
                        var threadId = messageViewModel.getThreadIdForAddress(phoneNumber.normalizedNumber)
                        Log.d("NavGraph", "Tried normalized number ${phoneNumber.normalizedNumber}, got threadId: $threadId")
                        
                        // If not found, try with original number
                        if (threadId == 0L) {
                            threadId = messageViewModel.getThreadIdForAddress(phoneNumber.number)
                            Log.d("NavGraph", "Tried original number ${phoneNumber.number}, got threadId: $threadId")
                        }
                        
                        // Use normalized number for new conversations
                        val addressToUse = phoneNumber.normalizedNumber
                        Log.d("NavGraph", "Navigating to thread - phoneNumber: $addressToUse, threadId: $threadId")
                        
                        navController.navigate(Screen.Thread.createRoute(threadId, addressToUse)) {
                            popUpTo(Screen.NewConversation.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.Thread.route,
            arguments = listOf(
                navArgument("threadId") { type = NavType.LongType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            val encodedAddress = backStackEntry.arguments?.getString("address") ?: ""
            val address = URLDecoder.decode(encodedAddress, StandardCharsets.UTF_8.toString())
            
            Log.d("NavGraph", "Decoded address: $address")
            
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

        composable(
            route = Screen.CallDetail.route,
            arguments = listOf(
                navArgument("number") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val number = backStackEntry.arguments?.getString("number") ?: return@composable
            val name = backStackEntry.arguments?.getString("name") ?: "Unknown"
            val context = LocalContext.current
            
            val call = Call(
                id = 0L,
                number = number,
                name = if (name == "Unknown") null else name,
                timestamp = System.currentTimeMillis(),
                duration = 0L,
                type = CallType.UNKNOWN
            )
            
            CallDetailScreen(
                call = call,
                onNavigateBack = { navController.popBackStack() },
                onCallClick = { phoneNumber -> 
                    CallUtils.makePhoneCall(context, phoneNumber)
                },
                onMessageClick = { phoneNumber ->
                    scope.launch {
                        val threadId = homeViewModel.getThreadIdForAddress(phoneNumber)
                        navController.navigate(Screen.Thread.createRoute(threadId, phoneNumber)) {
                            popUpTo(Screen.CallDetail.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
} 