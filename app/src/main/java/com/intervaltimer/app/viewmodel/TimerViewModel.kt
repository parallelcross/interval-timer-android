package com.intervaltimer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerPhase { WORK, REST }

enum class SoundEvent {
    COUNTDOWN_TICK,   // Short beep for last 3 seconds
    PHASE_WORK,       // Work phase starting
    PHASE_REST,       // Rest phase starting
    WORKOUT_COMPLETE, // Workout finished
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

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _soundEvents = MutableSharedFlow<SoundEvent>(extraBufferCapacity = 5)
    val soundEvents: SharedFlow<SoundEvent> = _soundEvents.asSharedFlow()

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
        _soundEvents.tryEmit(SoundEvent.PHASE_WORK)
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
            _state.value = current.copy(
                currentPhase = TimerPhase.WORK,
                remainingSeconds = current.workSeconds,
            )
            _soundEvents.tryEmit(SoundEvent.PHASE_WORK)
        } else if (current.currentSet > 1) {
            _state.value = current.copy(
                currentSet = current.currentSet - 1,
                currentPhase = TimerPhase.WORK,
                remainingSeconds = current.workSeconds,
            )
            _soundEvents.tryEmit(SoundEvent.PHASE_WORK)
        } else {
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
                    val newRemaining = current.remainingSeconds - 1
                    _state.value = current.copy(remainingSeconds = newRemaining)
                    // Countdown beeps for last 3 seconds
                    if (newRemaining in 1..3) {
                        _soundEvents.tryEmit(SoundEvent.COUNTDOWN_TICK)
                    }
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
                if (current.currentSet >= current.sets && current.skipLastRest) {
                    _state.value = current.copy(
                        isRunning = false,
                        isFinished = true,
                        remainingSeconds = 0,
                    )
                    _soundEvents.tryEmit(SoundEvent.WORKOUT_COMPLETE)
                } else if (current.currentSet >= current.sets) {
                    _state.value = current.copy(
                        currentPhase = TimerPhase.REST,
                        remainingSeconds = current.restSeconds,
                    )
                    _soundEvents.tryEmit(SoundEvent.PHASE_REST)
                } else {
                    _state.value = current.copy(
                        currentPhase = TimerPhase.REST,
                        remainingSeconds = current.restSeconds,
                    )
                    _soundEvents.tryEmit(SoundEvent.PHASE_REST)
                }
            }
            TimerPhase.REST -> {
                if (current.currentSet >= current.sets) {
                    _state.value = current.copy(
                        isRunning = false,
                        isFinished = true,
                        remainingSeconds = 0,
                    )
                    _soundEvents.tryEmit(SoundEvent.WORKOUT_COMPLETE)
                } else {
                    _state.value = current.copy(
                        currentSet = current.currentSet + 1,
                        currentPhase = TimerPhase.WORK,
                        remainingSeconds = current.workSeconds,
                    )
                    _soundEvents.tryEmit(SoundEvent.PHASE_WORK)
                }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
