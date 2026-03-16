package com.intervaltimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.intervaltimer.app.navigation.AppNavGraph
import com.intervaltimer.app.ui.theme.IntervalTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntervalTimerTheme {
                AppNavGraph()
            }
        }
    }
}
