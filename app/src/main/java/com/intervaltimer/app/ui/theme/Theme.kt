package com.intervaltimer.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    secondary = RestBlue,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = CardSurface,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
)

@Composable
fun IntervalTimerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
