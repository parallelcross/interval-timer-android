package com.intervaltimer.app.viewmodel

import androidx.lifecycle.ViewModel
import com.intervaltimer.app.service.TimerManager
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class TimerPhase { WORK, REST }

sealed class SpeechEvent {
    data class Speak(val text: String) : SpeechEvent()
}

data class TimerState(
    val sets: Int = 8,
    val workSeconds: Int = 20,
    val restSeconds: Int = 10,
    val skipLastRest: Boolean = true,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentSet: Int = 1,
    val currentPhase: TimerPhase = TimerPhase.WORK,
    val remainingSeconds: Int = 20,
    val isFinished: Boolean = false,
) {
    val totalWorkoutSeconds: Int
        get() {
            val workTotal = sets * workSeconds
            val restSets = if (skipLastRest) (sets - 1) else sets
            val restTotal = restSets.coerceAtLeast(0) * restSeconds
            return workTotal + restTotal
        }

    val totalRemainingSeconds: Int
        get() {
            if (isFinished) return 0
            var total = remainingSeconds
            val isWork = currentPhase == TimerPhase.WORK
            val remainingSets = sets - currentSet
            if (isWork) {
                if (!(skipLastRest && currentSet == sets)) {
                    total += restSeconds
                }
                total += remainingSets * workSeconds
                val remainingRestSets = if (skipLastRest) (remainingSets - 1).coerceAtLeast(0) else remainingSets
                total += remainingRestSets * restSeconds
            } else {
                total += remainingSets * workSeconds
                val remainingRestSets = if (skipLastRest) (remainingSets - 1).coerceAtLeast(0) else remainingSets
                total += remainingRestSets * restSeconds
            }
            return total
        }
}

class TimerViewModel : ViewModel() {

    val state: StateFlow<TimerState> = TimerManager.state
    val speechEvents: SharedFlow<SpeechEvent> = TimerManager.speechEvents

    fun updateSets(delta: Int) = TimerManager.updateSets(delta)
    fun updateWorkSeconds(delta: Int) = TimerManager.updateWorkSeconds(delta)
    fun updateRestSeconds(delta: Int) = TimerManager.updateRestSeconds(delta)
    fun toggleSkipLastRest() = TimerManager.toggleSkipLastRest()
    fun startWorkout() = TimerManager.startWorkout()
    fun togglePause() = TimerManager.togglePause()
    fun stopWorkout() = TimerManager.stopWorkout()
    fun skipForward() = TimerManager.skipForward()
    fun skipBackward() = TimerManager.skipBackward()

    fun setCountdownSeconds(seconds: Int) {
        TimerManager.countdownFrom = seconds
    }

    val countdownFrom: Int get() = TimerManager.countdownFrom
}
