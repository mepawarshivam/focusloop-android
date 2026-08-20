# FocusLoop 🧠

> Don't punish distraction. Interrupt it, create awareness, and offer an easy path back to the user's goals.

FocusLoop is a behavioral intervention Android app that detects prolonged use of distracting apps (TikTok, Instagram, YouTube, Reddit, X, etc.) and gently redirects users toward their goals.

---

## Architecture

```
UI (Jetpack Compose)
 ↓
ViewModel (state management, business logic coordination)
 ↓
Repository (data abstraction layer)
 ↓
Local Data (Room DB + DataStore)
```

The monitoring loop runs in a `FocusMonitoringService` (foreground service):

```
FocusMonitoringService
  └── polls UsageStatsManager every 15s
        └── detects monitored app in foreground
              └── tracks continuous session duration
                    └── triggers InterventionActivity when threshold reached
```

---

## Core Loop

```
DISTRACTION → DETECTION → INTERRUPTION → REFLECTION → REDIRECTION → REWARD
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| State | StateFlow + ViewModel |
| Database | Room |
| Preferences | DataStore |
| Background | Foreground Service |
| Detection | UsageStatsManager |
| Async | Kotlin Coroutines |
| Min SDK | Android 10 (API 29) |

---

## Project Structure

```
com.focusloop.app
├── FocusLoopApplication.kt     — DI container (manual, no framework needed)
├── MainActivity.kt             — Entry point, navigation host
├── data/
│   ├── datastore/              — UserSettings persistence
│   │   └── SettingsDataStore.kt
│   ├── local/
│   │   ├── FocusLoopDatabase.kt
│   │   ├── SeedData.kt         — 40+ learning questions
│   │   ├── dao/                — Room DAOs
│   │   └── entity/             — Room entities
│   └── repository/             — GoalRepository, SessionRepository, LearningRepository
├── domain/model/               — Domain models (Goal, DistractionSession, UserProgress...)
├── service/
│   ├── FocusMonitoringService.kt   — Core monitoring engine
│   └── BootReceiver.kt             — Restart monitoring after reboot
├── ui/
│   ├── theme/                  — Colors, Typography, Theme
│   ├── components/             — Shared Composables (FocusRing, GradientButton...)
│   ├── onboarding/             — 4-step onboarding flow
│   ├── home/                   — Home screen + Add Goal
│   ├── insights/               — Analytics dashboard
│   ├── challenges/             — Micro-learning challenges
│   ├── settings/               — App settings
│   ├── intervention/           — Intervention Activity + Screen
│   └── focussession/           — Focus session timer
└── Screen.kt                   — Navigation routes
```

---

## Getting Started

### Prerequisites

1. **Android Studio Hedgehog** (2023.1.1) or newer
2. **Android SDK 35** (API 35)
3. **Android device or emulator** running Android 10+ (API 29+)

### Setup

1. Clone the repo:
   ```bash
   git clone <repo-url>
   cd social-media-sherpa
   ```

2. Open in Android Studio:
   - File → Open → select the `social-media-sherpa` folder

3. Android Studio will sync Gradle automatically.

4. Create `local.properties` if it doesn't exist:
   ```
   sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
   ```

### Build & Run

```bash
./gradlew assembleDebug
```

Or use the **Run** button in Android Studio (⇧F10).

---

## Required Android Permissions

You must grant these permissions manually the first time:

### 1. Usage Access (REQUIRED for detection)
- Settings → Privacy → Usage Access → FocusLoop → Enable

### 2. Display Over Other Apps (RECOMMENDED for intervention overlay)
- Settings → Apps → FocusLoop → Display over other apps → Enable

### 3. Notification Permission
- Granted via system prompt on first launch (Android 13+)

---

## Demo Mode

To test the intervention flow without waiting 5 minutes:

1. Open **Settings** in the app
2. Enable **Developer Mode**
3. The demo threshold is set to 10 seconds by default
4. Tap **"Trigger Test Intervention"** for an instant demo

Or: enable monitoring → open a monitored app → wait 10 seconds → intervention appears.

---

## Demo User Journey

```
Install app
 ↓
Onboarding (4 steps)
 ↓
Create goal: "Finish my portfolio"
 ↓
Select Instagram as distracting app
 ↓
Enable Developer Mode → 10 second threshold
 ↓
Start monitoring (toggle on Home screen)
 ↓
Open Instagram → wait 10 seconds
 ↓
Intervention screen appears
 ↓
Select "2-Minute Challenge"
 ↓
Answer a DSA question
 ↓
Receive +10 Learning XP
 ↓
Return to Home
 ↓
See updated stats: +XP, recovered time, focus score
```

---

## Privacy

FocusLoop is privacy-first by design:

- ✅ All data stays on-device
- ✅ No network requests
- ✅ No analytics SDK
- ✅ No crash reporting to external servers
- ✅ No user accounts required
- ✅ UsageStats data is never transmitted

---

## Development Phases

| Phase | Status |
|---|---|
| Phase 1: Project setup & theme | ✅ Complete |
| Phase 2: UI with fake data | ✅ Complete |
| Phase 3: Room + DataStore persistence | ✅ Complete |
| Phase 4: UsageStatsManager | ✅ Complete |
| Phase 5: FocusMonitoringService | ✅ Complete |
| Phase 6: Intervention triggering | ✅ Complete |
| Phase 7: Micro-learning | ✅ Complete (40+ questions) |
| Phase 8: Gamification | ✅ Complete |
| Phase 9: Analytics | ✅ Complete |
| Phase 10: Polish | 🔄 Ongoing |

---

## License

MIT
