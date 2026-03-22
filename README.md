# Interval Timer

A clean workout interval timer for Android with spoken voice coaching. Built with Jetpack Compose, it announces phase changes and counts down aloud so you can stay focused on your workout while listening to music.

## Features

- **Configurable workouts** — Set number of rounds (1–99), work duration (5s–60min), and rest duration (5s–60min)
- **Voice announcements** — Speaks phase transitions ("Starting work, Set 1 of 8"), progress cues (25%, 50%, 75%), and configurable countdown before phase ends
- **Audio focus** — Automatically ducks your music volume during announcements, then restores it
- **Visual feedback** — Full-screen color changes (green for work, blue for rest, orange for warmup) with animated transitions
- **Smart controls** — Skip forward/back between intervals, pause/resume anytime
- **Skip last rest** — Toggle to end your workout immediately after the final work set
- **1-minute warmup** — Optional countdown to get ready before your first set
- **Total time display** — See total workout duration before starting and remaining time while running
- **Background execution** — Foreground service with notification controls keeps the timer running when your screen is off
- **Completion celebration** — Animated confetti when you finish your workout
- **Voice countdown settings** — Slider to set countdown duration (0–20 seconds) or turn it off entirely
- **Dark theme** — Easy on the eyes during workouts

## Screenshots

### Setup Screen
Configure your workout with intuitive +/- controls:
- **Sets:** 1–99 rounds
- **Work:** 5 seconds to 60 minutes per round
- **Rest:** 5 seconds to 60 minutes between rounds
- Toggle skip last rest and warmup countdown

### Active Timer
Full-screen countdown with:
- Large, readable timer
- Current set and phase indicator
- Total remaining time
- Pause, skip forward, and skip back controls
- Animated color transitions between phases
- Spoken countdown and phase announcements

## Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.3.10 |
| Jetpack Compose BOM | 2026.02.01 |
| AGP | 9.0.1 |
| Gradle | 9.3.1 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |

## Building

Open in Android Studio or build from command line:

```bash
./gradlew assembleDebug
```

## Architecture

Single-activity app using Navigation Compose with a shared `TimerManager` singleton:

- `SetupScreen` — Workout configuration with sets, work/rest durations, toggles
- `ActiveTimerScreen` — Full-screen countdown timer with TTS and audio focus management
- `CompletionScreen` — Animated confetti celebration
- `SettingsScreen` — Voice countdown slider and open source credits
- `TimerManager` — Centralized timer engine shared between ViewModel and foreground service
- `TimerService` — Foreground service with notification controls and wake lock

Uses Android `TextToSpeech` for voice announcements and `AudioFocusRequest` with `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` to temporarily lower music volume during speech. Settings persisted via DataStore.

No external dependencies beyond AndroidX/Compose — keeps the app lightweight and fast.

## License

[MIT License](LICENSE)
