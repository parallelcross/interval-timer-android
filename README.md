# Interval Timer

A clean, distraction-free interval timer for Android workouts. Built with Jetpack Compose and designed to provide visual cues without interrupting your music.

## Features

- **Configurable workouts** — Set number of rounds, work duration, and rest duration
- **Visual-only feedback** — Full-screen color changes (green for work, blue for rest) so your audio keeps playing uninterrupted
- **Smart controls** — Skip forward/back between intervals, pause/resume anytime
- **Skip last rest** — Toggle to end your workout immediately after the final work set
- **Total time display** — See total workout duration before starting and remaining time while running
- **Dark theme** — Easy on the eyes during workouts

## Screenshots

### Setup Screen
Configure your workout with intuitive +/- controls:
- **Sets:** 1–99 rounds
- **Work:** 5 seconds to 60 minutes per round
- **Rest:** 5 seconds to 60 minutes between rounds

### Active Timer
Full-screen countdown with:
- Large, readable timer
- Current set and phase indicator
- Total remaining time
- Pause, skip forward, and skip back controls
- Animated color transitions between phases

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

Single-activity app using Navigation Compose with a shared ViewModel:

- `SetupScreen` — Workout configuration
- `ActiveTimerScreen` — Full-screen countdown timer
- `TimerViewModel` — Timer state management with coroutine-based countdown

No external dependencies beyond AndroidX/Compose — keeps the app lightweight and fast.

## License

Private repository.
