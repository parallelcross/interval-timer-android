# Interval Timer - Context Summary

## What is this?
An Android interval timer app for workouts with spoken voice coaching. The user configures sets, work duration, and rest duration, then runs a full-screen color-coded timer that announces phase changes and counts down aloud. Runs in a foreground service so the device doesn't sleep during workouts.

## Tech Stack
- **Language:** Kotlin 2.3.10
- **UI:** Jetpack Compose (BOM 2026.02.01)
- **Build:** AGP 9.0.1, Gradle 9.3.1
- **Architecture:** Single-Activity, ViewModel delegates to singleton TimerManager, Navigation Compose
- **Audio:** Android TextToSpeech + AudioFocusRequest (AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK), TTS volume at 50%
- **Persistence:** DataStore Preferences (countdown setting)
- **Min SDK:** 26 | **Target SDK:** 36
- **Package:** `com.intervaltimer.app`

## Project Structure
```
app/src/main/java/com/intervaltimer/app/
├── MainActivity.kt              # Single activity, edge-to-edge, splash theme, notification permission
├── data/
│   └── SettingsRepository.kt    # DataStore preferences for countdown seconds
├── navigation/
│   └── AppNavGraph.kt           # setup -> active -> completion, settings routes
├── service/
│   ├── TimerManager.kt          # Singleton timer logic shared between service and UI
│   └── TimerService.kt          # Foreground service with notification controls + wake lock
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Work green, rest blue, dark theme colors
│   │   └── Theme.kt             # Dark Material3 theme
│   └── screens/
│       ├── SetupScreen.kt       # Configure sets, work/rest durations, settings gear
│       ├── ActiveTimerScreen.kt # Full-screen countdown with TTS + audio focus
│       ├── CompletionScreen.kt  # Confetti celebration on workout finish
│       └── SettingsScreen.kt    # Countdown config slider, OSS library list
└── viewmodel/
    └── TimerViewModel.kt        # Thin delegate to TimerManager, exposes state + speech events
```

## Screens
1. **Setup Screen** - Dark themed config with +/- controls for sets (1-99), work time (5s-60min), rest time (5s-60min). Skip last rest toggle. 1-minute warmup toggle. Settings gear icon. Start button shows total duration.
2. **Active Timer Screen** - Full-screen color-coded display. Green for work, blue for rest. Current set, phase, large countdown, total remaining. Pause/resume, skip forward/back. Spoken announcements with audio focus ducking. Starts foreground service.
3. **Completion Screen** - "Workout Complete!" with animated confetti. Done button returns to setup.
4. **Settings Screen** - Voice countdown slider (off to 20s, persisted via DataStore). Open source library list.

## Key Design Decisions
- **Foreground service** — TimerService keeps timer running when screen is off, holds PARTIAL_WAKE_LOCK (1hr max)
- **Notification controls** — Prev, Pause/Resume, Next, Stop actions in notification. Tapping notification opens app.
- **TimerManager singleton** — Timer logic lives in a process-level singleton, shared between service and ViewModel
- **Warmup countdown** — Optional 1-minute warmup phase before first work set, orange background, "GET READY" label
- **Voice announcements** — TTS speaks "Starting work, Set X of Y", "Starting rest", countdown from configurable seconds (default 3), "Workout complete"
- **Progress cues** — For work intervals > 60s, speaks "25% done", "Halfway there", "75% done"
- **Audio focus ducking** — AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK, debounced 1.5s release between countdown numbers
- **TTS volume at 50%** — KEY_PARAM_VOLUME 0.5f so announcements don't overpower music
- **SpeechEvent flow** — TimerManager emits sealed class SpeechEvent via SharedFlow, UI layer plays via TTS
- **Full-screen color changes** — green (work) / blue (rest) with animated transitions
- **Splash screen** — plain white background, no icon (baked-in rounded corners look bad circle-cropped)
- **Launcher icon** — PNG ic_launcher in all density buckets, designed externally
- **Edge-to-edge** — navigationBarsPadding() on all screens

## What was done last (2026-03-20)
- Changed default countdown to 3 seconds
- Added settings screen with configurable countdown (off to 20s) via DataStore, OSS library list
- Added completion screen with confetti animation
- Added foreground service with notification controls (Prev, Pause/Resume, Next, Stop)
- TimerManager singleton for shared timer state between service and UI
- Partial wake lock to prevent device sleep during workouts
- Notification permission request on Android 13+
- Fixed Float/Double type mismatches in confetti animation
- Added optional 1-minute warmup countdown before first work set (WARMUP phase, orange UI)
- Added progress cues at 25%, 50%, 75% for work intervals longer than 60 seconds
