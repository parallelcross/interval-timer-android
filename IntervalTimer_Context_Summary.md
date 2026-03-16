# Interval Timer - Context Summary

## What is this?
A minimal Android interval timer app for workouts. Designed for visual-only cues so it doesn't interrupt audio playback. The user configures sets, work duration, and rest duration, then runs a full-screen color-coded timer.

## Tech Stack
- **Language:** Kotlin 2.3.10
- **UI:** Jetpack Compose (BOM 2026.02.01)
- **Build:** AGP 9.0.1, Gradle 9.3.1
- **Architecture:** Single-Activity, ViewModel + StateFlow, Navigation Compose
- **Min SDK:** 26 | **Target SDK:** 36
- **Package:** `com.intervaltimer.app`

## Project Structure
```
app/src/main/java/com/intervaltimer/app/
├── MainActivity.kt              # Single activity, edge-to-edge
├── navigation/
│   └── AppNavGraph.kt           # setup -> active timer navigation
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Work green, rest blue, dark theme colors
│   │   └── Theme.kt             # Dark Material3 theme
│   └── screens/
│       ├── SetupScreen.kt       # Configure sets, work/rest durations
│       └── ActiveTimerScreen.kt # Full-screen countdown with color phases
└── viewmodel/
    └── TimerViewModel.kt        # Timer logic, phase management, state
```

## Screens
1. **Setup Screen** - Dark themed config screen with +/- controls for sets (1-99), work time (5s-60min), rest time (5s-60min). Toggle to skip last rest period. Start button shows total workout duration.
2. **Active Timer Screen** - Full-screen color-coded display. Green background for work, blue for rest. Shows current set, phase, large countdown, total remaining time. Pause/resume, skip forward/back controls.

## Key Design Decisions
- **Visual-only cues** — no sounds or vibrations, so it won't interrupt music/podcasts
- **Full-screen color changes** — green (work) / blue (rest) are immediately noticeable at a glance
- **Dark theme setup** — easy on the eyes, consistent with gym/workout context
- **No dependency injection** — simple app doesn't need Hilt, uses Compose `viewModel()`
- **Shared ViewModel** — single ViewModel instance shared between setup and active screens via NavGraph-scoped creation

## What was done last (2026-03-15)
- Initial project creation with full setup and active timer screens
- ViewModel with timer logic, phase management, skip forward/back
- Dark theme with green (work) and blue (rest) color coding
- Animated color transitions between work and rest phases
- Skip last rest toggle
- Total remaining time display
- Adaptive launcher icon
