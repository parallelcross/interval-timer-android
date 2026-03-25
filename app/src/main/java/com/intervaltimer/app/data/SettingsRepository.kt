package com.intervaltimer.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val countdownKey = intPreferencesKey("countdown_seconds")
    private val setsKey = intPreferencesKey("sets")
    private val workSecondsKey = intPreferencesKey("work_seconds")
    private val restSecondsKey = intPreferencesKey("rest_seconds")
    private val skipLastRestKey = booleanPreferencesKey("skip_last_rest")
    private val warmupEnabledKey = booleanPreferencesKey("warmup_enabled")

    val countdownSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[countdownKey] ?: 3
    }

    suspend fun setCountdownSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[countdownKey] = seconds
        }
    }

    val setupPreferences: Flow<SetupPreferences> = context.dataStore.data.map { prefs ->
        SetupPreferences(
            sets = prefs[setsKey] ?: 8,
            workSeconds = prefs[workSecondsKey] ?: 20,
            restSeconds = prefs[restSecondsKey] ?: 10,
            skipLastRest = prefs[skipLastRestKey] ?: true,
            warmupEnabled = prefs[warmupEnabledKey] ?: false,
        )
    }

    suspend fun saveSetup(prefs: SetupPreferences) {
        context.dataStore.edit { store ->
            store[setsKey] = prefs.sets
            store[workSecondsKey] = prefs.workSeconds
            store[restSecondsKey] = prefs.restSeconds
            store[skipLastRestKey] = prefs.skipLastRest
            store[warmupEnabledKey] = prefs.warmupEnabled
        }
    }
}

data class SetupPreferences(
    val sets: Int = 8,
    val workSeconds: Int = 20,
    val restSeconds: Int = 10,
    val skipLastRest: Boolean = true,
    val warmupEnabled: Boolean = false,
)
