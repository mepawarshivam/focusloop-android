# FocusLoop — Android AI Focus & Doomscrolling Intervention App

## Project Overview
FocusLoop is a behavioral intervention Android app that detects prolonged use of distracting apps and gently redirects users toward their goals.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean MVVM (UI → ViewModel → Repository → Local Data)
- **Database**: Room
- **Preferences**: DataStore
- **Background**: Foreground Service + WorkManager
- **Detection**: UsageStatsManager
- **Async**: Kotlin Coroutines + StateFlow
- **Min SDK**: Android 10 (API 29)

## Project Checklist

- [x] Create copilot-instructions.md
- [x] Scaffold Android project structure
- [x] Create theme and base architecture
- [x] Build onboarding UI
- [x] Build main screens UI
- [x] Build intervention UI
- [x] Implement Room database
- [x] Implement DataStore and repositories
- [x] Implement UsageStatsManager
- [x] Implement FocusMonitoringService
- [x] Implement micro-learning question bank
- [x] Implement gamification system
- [x] Add demo mode and permission handling
- [ ] Polish animations and README

## Architecture Notes
- Keep all behavioral rules in domain/service layer
- Privacy-first: all data stays on device, no network calls
- Battery-conscious: no aggressive polling, use Android-supported background mechanisms
- Graceful permission degradation — never crash on denied permissions
