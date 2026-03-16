package com.intervaltimer.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intervaltimer.app.ui.screens.ActiveTimerScreen
import com.intervaltimer.app.ui.screens.SetupScreen
import com.intervaltimer.app.viewmodel.TimerViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val viewModel: TimerViewModel = viewModel()

    NavHost(navController = navController, startDestination = "setup") {
        composable("setup") {
            SetupScreen(
                viewModel = viewModel,
                onStartWorkout = { navController.navigate("active") },
            )
        }
        composable("active") {
            ActiveTimerScreen(
                viewModel = viewModel,
                onFinished = {
                    navController.popBackStack("setup", inclusive = false)
                },
            )
        }
    }
}
