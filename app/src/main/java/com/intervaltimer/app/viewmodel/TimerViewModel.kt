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

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _speechEvents = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = 10)
    val speechEvents: SharedFlow<SpeechEvent> = _speechEvents.asSharedFlow()

    private var timerJob: Job? = null

    private var countdownFrom = 3

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
        speak("Starting work. Set 1 of ${_state.value.sets}")
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
            speak("Starting work. Set ${current.currentSet} of ${current.sets}")
        } else if (current.currentSet > 1) {
            _state.value = current.copy(
                currentSet = current.currentSet - 1,
                currentPhase = TimerPhase.WORK,
                remainingSeconds = current.workSeconds,
            )
            speak("Starting work. Set ${current.currentSet - 1} of ${current.sets}")
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

                    val phaseName = if (current.currentPhase == TimerPhase.WORK) "work" else "rest"
                    val limit = countdownFrom.coerceAtMost(current.let {
                        if (it.currentPhase == TimerPhase.WORK) it.workSeconds else it.restSeconds
                    } - 1)

                    if (newRemaining == limit) {
                        speak("Finishing $phaseName in $newRemaining")
                    } else if (newRemaining in 1 until limit) {
                        speak("$newRemaining")
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
                    speak("Workout complete!")
                } else if (current.currentSet >= current.sets) {
                    _state.value = current.copy(
                        currentPhase = TimerPhase.REST,
                        remainingSeconds = current.restSeconds,
                    )
                    speak("Starting rest")
                } else {
                    _state.value = current.copy(
                        currentPhase = TimerPhase.REST,
                        remainingSeconds = current.restSeconds,
                    )
                    speak("Starting rest")
                }
            }
            TimerPhase.REST -> {
                if (current.currentSet >= current.sets) {
                    _state.value = current.copy(
                        isRunning = false,
                        isFinished = true,
                        remainingSeconds = 0,
                    )
                    speak("Workout complete!")
                } else {
                    val nextSet = current.currentSet + 1
                    _state.value = current.copy(
                        currentSet = nextSet,
                        currentPhase = TimerPhase.WORK,
                        remainingSeconds = current.workSeconds,
                    )
                    speak("Starting work. Set $nextSet of ${current.sets}")
                }
            }
        }
    }

    private fun speak(text: String) {
        _speechEvents.tryEmit(SpeechEvent.Speak(text))
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
