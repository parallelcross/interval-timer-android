package com.intervaltimer.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intervaltimer.app.data.SettingsRepository
import com.intervaltimer.app.data.SetupPreferences
import com.intervaltimer.app.service.TimerManager
import com.intervaltimer.app.ui.screens.ActiveTimerScreen
import com.intervaltimer.app.ui.screens.CompletionScreen
import com.intervaltimer.app.ui.screens.SettingsScreen
import com.intervaltimer.app.ui.screens.SetupScreen
import com.intervaltimer.app.viewmodel.TimerViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val viewModel: TimerViewModel = viewModel()
    val context = LocalContext.current

    // Load saved settings and auto-save setup changes
    LaunchedEffect(Unit) {
        val repo = SettingsRepository(context)
        viewModel.setCountdownSeconds(repo.countdownSeconds.first())
        val setup = repo.setupPreferences.first()
        TimerManager.loadSetup(setup.sets, setup.workSeconds, setup.restSeconds, setup.skipLastRest, setup.warmupEnabled)

        TimerManager.state
            .map { SetupPreferences(it.sets, it.workSeconds, it.restSeconds, it.skipLastRest, it.warmupEnabled) }
            .distinctUntilChanged()
            .collect { repo.saveSetup(it) }
    }

    NavHost(navController = navController, startDestination = "setup") {
        composable("setup") {
            SetupScreen(
                viewModel = viewModel,
                onStartWorkout = { navController.navigate("active") },
                onOpenSettings = { navController.navigate("settings") },
            )
        }
        composable("active") {
            ActiveTimerScreen(
                viewModel = viewModel,
                onStopped = {
                    navController.popBackStack("setup", inclusive = false)
                },
                onCompleted = {
                    navController.navigate("completion") {
                        popUpTo("setup") { inclusive = false }
                    }
                },
            )
        }
        composable("completion") {
            CompletionScreen(
                onDone = {
                    navController.popBackStack("setup", inclusive = false)
                },
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
