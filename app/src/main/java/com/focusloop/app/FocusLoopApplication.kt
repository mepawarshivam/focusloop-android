package com.focusloop.app

import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.focusloop.app.data.local.FocusLoopDatabase
import com.focusloop.app.data.repository.AuthRepository
import com.focusloop.app.data.repository.ChatRepository
import com.focusloop.app.data.repository.GoalRepository
import com.focusloop.app.data.repository.LearningRepository
import com.focusloop.app.data.repository.MicrolearningRepository
import com.focusloop.app.data.repository.RecommendationRepository
import com.focusloop.app.data.repository.SessionRepository
import com.focusloop.app.data.repository.UserDataRepository

/**
 * Application class acts as a simple manual DI container for MVP.
 * All repositories and data sources are singletons initialized here.
 * Replace with Hilt/Koin if the project grows.
 *
 * Accounts and account-bound data (settings, hobbies, to-dos, goals,
 * gamification counters) are backed by Firebase Auth + Firestore — see
 * AuthRepository, UserDataRepository, and GoalRepository. Session logs and
 * the quiz bank stay local (Room); they're either high-frequency device
 * telemetry or static content, not something that needs cross-device sync.
 */
class FocusLoopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }

    val database by lazy { FocusLoopDatabase.getInstance(this) }
    val authDataStore by lazy { AuthRepository() }
    val settingsDataStore by lazy { UserDataRepository(authRepository = authDataStore) }
    val goalRepository by lazy { GoalRepository(authRepository = authDataStore) }
    val recommendationRepository by lazy { RecommendationRepository() }
    val chatRepository by lazy { ChatRepository() }
    val microlearningRepository by lazy { MicrolearningRepository() }
    val sessionRepository by lazy {
        SessionRepository(
            database.distractionSessionDao(),
            database.focusSessionDao(),
            database.dailyStatsDao()
        )
    }
    val learningRepository by lazy { LearningRepository(database.learningQuestionDao()) }
}
