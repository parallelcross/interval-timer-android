# Interval Timer - Context Summary

## What is this?
An Android interval timer app for workouts with spoken voice coaching. The user configures sets, work duration, and rest duration, then runs a full-screen color-coded timer that announces phase changes and counts down aloud. Designed to duck music volume during announcements.

## Tech Stack
- **Language:** Kotlin 2.3.10
- **UI:** Jetpack Compose (BOM 2026.02.01)
- **Build:** AGP 9.0.1, Gradle 9.3.1
- **Architecture:** Single-Activity, ViewModel + StateFlow, Navigation Compose
- **Audio:** Android TextToSpeech + AudioFocusRequest (AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK), TTS volume at 50%
- **Min SDK:** 26 | **Target SDK:** 36
- **Package:** `com.intervaltimer.app`

## Project Structure
```
app/src/main/java/com/intervaltimer/app/
├── MainActivity.kt              # Single activity, edge-to-edge, splash theme switch
├── navigation/
│   └── AppNavGraph.kt           # setup -> active timer navigation
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Work green, rest blue, dark theme colors
│   │   └── Theme.kt             # Dark Material3 theme
│   └── screens/
│       ├── SetupScreen.kt       # Configure sets, work/rest durations
│       └── ActiveTimerScreen.kt # Full-screen countdown with TTS + audio focus
└── viewmodel/
    └── TimerViewModel.kt        # Timer logic, phase management, speech events
```

## Screens
1. **Setup Screen** - Dark themed config screen with +/- controls for sets (1-99), work time (5s-60min), rest time (5s-60min). Toggle to skip last rest period. Start button shows total workout duration.
2. **Active Timer Screen** - Full-screen color-coded display. Green background for work, blue for rest. Shows current set, phase, large countdown, total remaining time. Pause/resume, skip forward/back controls. Spoken announcements with audio focus ducking.

## Key Design Decisions
- **Voice announcements** — TTS speaks "Starting work, Set X of Y", "Starting rest", countdown from 10, "Workout complete"
- **Audio focus ducking** — requests AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK before each utterance, debounced 1.5s release so focus isn't dropped between countdown numbers
- **TTS volume at 50%** — KEY_PARAM_VOLUME set to 0.5f so announcements don't overpower music
- **SpeechEvent flow** — ViewModel emits sealed class SpeechEvent via SharedFlow, UI layer collects and plays via TTS (keeps ViewModel testable)
- **Full-screen color changes** — green (work) / blue (rest) with animated transitions
- **Dark theme setup** — easy on the eyes, consistent with gym/workout context
- **Splash screen** — plain white background, no icon (icon has baked-in rounded corners that look bad circle-cropped by Android 12+ splash)
- **Launcher icon** — PNG ic_launcher in all density buckets (not adaptive icon XML), designed externally
- **No dependency injection** — simple app doesn't need Hilt, uses Compose `viewModel()`
- **Shared ViewModel** — single ViewModel instance shared between setup and active screens via NavGraph-scoped creation
- **Edge-to-edge** — uses navigationBarsPadding() on both screens to keep controls above system nav bar

## What was done last (2026-03-15)
- Initial project creation with full setup and active timer screens
- Replaced tone beeps with TextToSpeech voice announcements
- Added AudioFocusRequest to duck music during speech
- Debounced audio focus release (1.5s) so music doesn't unduck between countdown numbers
- Reduced TTS volume to 50% to be less intrusive over music
- Spoken countdown from 10 for each phase, phase start/end announcements
- Fixed navigation bar overlap on both setup and active timer screens
- Fixed +/- button alignment padding in counter cards
- Replaced placeholder vector icons with designed PNG launcher icons (cropped version)
- White splash screen without icon (avoids ugly circle crop)
- Cleaned up accidentally committed build artifacts, added app/release/ to .gitignore
- Updated README with voice/audio focus documentation
