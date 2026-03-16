package com.intervaltimer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervaltimer.app.data.SettingsRepository
import com.intervaltimer.app.ui.theme.AccentGreen
import com.intervaltimer.app.ui.theme.CardSurface
import com.intervaltimer.app.viewmodel.TimerViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: TimerViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repository = SettingsRepository(context)
    val countdownSeconds by repository.countdownSeconds.collectAsState(initial = viewModel.countdownFrom)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 60.dp, bottom = 24.dp),
    ) {
        // Header with back button
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Countdown timer setting
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(
                    text = "VOICE COUNTDOWN",
                    color = AccentGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (countdownSeconds == 0) "Off" else "${countdownSeconds}s before phase ends",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = countdownSeconds.toFloat(),
                    onValueChange = { newValue ->
                        val seconds = newValue.roundToInt()
                        viewModel.setCountdownSeconds(seconds)
                        scope.launch { repository.setCountdownSeconds(seconds) }
                    },
                    valueRange = 0f..20f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Off", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "20s", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Open source libraries
        Text(
            text = "Open Source Libraries",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LibraryItem(
            name = "Jetpack Compose",
            description = "Modern Android UI toolkit",
            license = "Apache License 2.0",
        )
        LibraryItem(
            name = "Jetpack Compose Material 3",
            description = "Material Design 3 components for Compose",
            license = "Apache License 2.0",
        )
        LibraryItem(
            name = "AndroidX Core KTX",
            description = "Kotlin extensions for Android framework APIs",
            license = "Apache License 2.0",
        )
        LibraryItem(
            name = "AndroidX Lifecycle",
            description = "Lifecycle-aware components and ViewModel",
            license = "Apache License 2.0",
        )
        LibraryItem(
            name = "AndroidX Navigation Compose",
            description = "Navigation framework for Jetpack Compose",
            license = "Apache License 2.0",
        )
        LibraryItem(
            name = "AndroidX DataStore",
            description = "Data storage solution for preferences",
            license = "Apache License 2.0",
        )
        LibraryItem(
            name = "AndroidX Activity Compose",
            description = "Compose integration with Activity",
            license = "Apache License 2.0",
        )
        LibraryItem(
            name = "Material Icons Extended",
            description = "Extended Material Design icons for Compose",
            license = "Apache License 2.0",
        )
    }
}

@Composable
private fun LibraryItem(
    name: String,
    description: String,
    license: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
            Text(
                text = license,
                color = AccentGreen.copy(alpha = 0.8f),
                fontSize = 12.sp,
            )
        }
    }
}
