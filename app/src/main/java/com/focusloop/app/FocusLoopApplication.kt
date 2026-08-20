package com.focusloop.app

import android.app.Application
import com.focusloop.app.data.datastore.AuthDataStore
import com.focusloop.app.data.datastore.SettingsDataStore
import com.focusloop.app.data.local.FocusLoopDatabase
import com.focusloop.app.data.repository.GoalRepository
import com.focusloop.app.data.repository.LearningRepository
import com.focusloop.app.data.repository.RecommendationRepository
import com.focusloop.app.data.repository.SessionRepository

/**
 * Application class acts as a simple manual DI container for MVP.
 * All repositories and data sources are singletons initialized here.
 * Replace with Hilt/Koin if the project grows.
 */
class FocusLoopApplication : Application() {

    val database by lazy { FocusLoopDatabase.getInstance(this) }
    val settingsDataStore by lazy { SettingsDataStore(this) }
    val authDataStore by lazy { AuthDataStore(this) }
    val goalRepository by lazy { GoalRepository(database.goalDao()) }
    val recommendationRepository by lazy { RecommendationRepository() }
    val sessionRepository by lazy {
        SessionRepository(
            database.distractionSessionDao(),
            database.focusSessionDao(),
            database.dailyStatsDao()
        )
    }
    val learningRepository by lazy { LearningRepository(database.learningQuestionDao()) }
}
