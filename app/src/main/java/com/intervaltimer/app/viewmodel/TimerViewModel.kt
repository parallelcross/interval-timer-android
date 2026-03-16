package com.intervaltimer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerPhase { WORK, REST }

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
            // Remaining sets after current
            val remainingSets = sets - currentSet
            if (isWork) {
                // Rest of current set's rest + remaining full sets
                if (!(skipLastRest && currentSet == sets)) {
                    total += restSeconds
                }
                total += remainingSets * workSeconds
                val remainingRestSets = if (skipLastRest) (remainingSets - 1).coerceAtLeast(0) else remainingSets
                total += remainingRestSets * restSeconds
            } else {
                // Remaining full sets
                total += remainingSets * workSeconds
                val remainingRestSets = if (skipLastRest) (remainingSets - 1).coerceAtLeast(0) else remainingSets
                total += remainingRestSets * restSeconds
            }
            return total
        }
}

class TimerViewModel : ViewModel() {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun updateSets(delta: Int) {
        _state.value = _state.value.let {
            it.copy(sets = (it.sets + delta).coerceIn(1, 99))
        }
    }

    fun updateWorkSeconds(delta: Int) {
        _state.value = _state.value.let {
            it.copy(workSeconds = (it.workSeconds + delta).coerceIn(5, 3600))
        }
    }

    fun updateRestSeconds(delta: Int) {
        _state.value = _state.value.let {
            it.copy(restSeconds = (it.restSeconds + delta).coerceIn(5, 3600))
        }
    }

    fun toggleSkipLastRest() {
        _state.value = _state.value.copy(skipLastRest = !_state.value.skipLastRest)
    }

    fun startWorkout() {
        _state.value = _state.value.copy(
            isRunning = true,
            isPaused = false,
            currentSet = 1,
            currentPhase = TimerPhase.WORK,
            remainingSeconds = _state.value.workSeconds,
            isFinished = false,
        )
        startTicking()
    }

    fun togglePause() {
        val current = _state.value
        if (current.isPaused) {
            _state.value = current.copy(isPaused = false)
            startTicking()
        } else {
            timerJob?.cancel()
            _state.value = current.copy(isPaused = true)
        }
    }

    fun stopWorkout() {
        timerJob?.cancel()
        _state.value = _state.value.copy(
            isRunning = false,
            isPaused = false,
            isFinished = false,
        )
    }

    fun skipForward() {
        timerJob?.cancel()
        advancePhase()
        if (_state.value.isRunning && !_state.value.isPaused && !_state.value.isFinished) {
            startTicking()
        }
    }

    fun skipBackward() {
        timerJob?.cancel()
        val current = _state.value
        if (current.currentPhase == TimerPhase.REST) {
            // Go back to work phase of current set
            _state.value = current.copy(
                currentPhase = TimerPhase.WORK,
                remainingSeconds = current.workSeconds,
            )
        } else if (current.currentSet > 1) {
            // Go back to work phase of previous set
            _state.value = current.copy(
                currentSet = current.currentSet - 1,
                currentPhase = TimerPhase.WORK,
                remainingSeconds = current.workSeconds,
            )
        } else {
            // Already at first set work, just reset timer
            _state.value = current.copy(remainingSeconds = current.workSeconds)
        }
        if (!current.isPaused) {
            startTicking()
        }
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _state.value
                if (current.remainingSeconds > 1) {
                    _state.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                } else {
                    advancePhase()
                    if (_state.value.isFinished) break
                }
            }
        }
    }

    private fun advancePhase() {
        val current = _state.value
        when (current.currentPhase) {
            TimerPhase.WORK -> {
                // Check if this is the last set and skip last rest is enabled
                if (current.currentSet >= current.sets && current.skipLastRest) {
                    // Workout complete
                    _state.value = current.copy(
                        isRunning = false,
                        isFinished = true,
                        remainingSeconds = 0,
                    )
                } else if (current.currentSet >= current.sets) {
                    // Last set, go to rest then finish
                    _state.value = current.copy(
                        currentPhase = TimerPhase.REST,
                        remainingSeconds = current.restSeconds,
                    )
                } else {
                    // Go to rest
                    _state.value = current.copy(
                        currentPhase = TimerPhase.REST,
                        remainingSeconds = current.restSeconds,
                    )
                }
            }
            TimerPhase.REST -> {
                if (current.currentSet >= current.sets) {
                    // Workout complete
                    _state.value = current.copy(
                        isRunning = false,
                        isFinished = true,
                        remainingSeconds = 0,
                    )
                } else {
                    // Next set, work phase
                    _state.value = current.copy(
                        currentSet = current.currentSet + 1,
                        currentPhase = TimerPhase.WORK,
                        remainingSeconds = current.workSeconds,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
