package com.focusloop.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.focusloop.app.domain.model.TodoItem
import com.focusloop.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focusloop_settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val INTERVENTION_THRESHOLD = longPreferencesKey("intervention_threshold_ms")
        val COOLDOWN_DURATION = longPreferencesKey("cooldown_duration_ms")
        val MONITORED_PACKAGES = stringPreferencesKey("monitored_packages_json")
        val MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val DEVELOPER_MODE_ENABLED = booleanPreferencesKey("developer_mode_enabled")
        val DEMO_THRESHOLD = longPreferencesKey("demo_threshold_ms")
        val HOBBIES = stringPreferencesKey("hobbies_json")
        val TODOS = stringPreferencesKey("todos_json")
        val FOCUS_XP = intPreferencesKey("focus_xp")
        val LEARNING_XP = intPreferencesKey("learning_xp")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val TOTAL_MINUTES_RECOVERED = intPreferencesKey("total_minutes_recovered")
        val TOTAL_FOCUS_SESSIONS = intPreferencesKey("total_focus_sessions")
        val TOTAL_INTERVENTIONS = intPreferencesKey("total_interventions")
        val LAST_STREAK_DATE = longPreferencesKey("last_streak_date")
    }

    val settings: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            val packagesJson = prefs[Keys.MONITORED_PACKAGES] ?: "[]"
            val packages = try {
                Json.decodeFromString<Set<String>>(packagesJson)
            } catch (_: Exception) {
                emptySet()
            }
            val hobbiesJson = prefs[Keys.HOBBIES] ?: "[]"
            val hobbies = try {
                Json.decodeFromString<Set<String>>(hobbiesJson)
            } catch (_: Exception) {
                emptySet()
            }
            val todosJson = prefs[Keys.TODOS] ?: "[]"
            val todos = try {
                Json.decodeFromString<List<TodoItem>>(todosJson)
            } catch (_: Exception) {
                emptyList()
            }
            UserSettings(
                interventionThresholdMs = prefs[Keys.INTERVENTION_THRESHOLD] ?: (5 * 60 * 1000L),
                cooldownDurationMs = prefs[Keys.COOLDOWN_DURATION] ?: (15 * 60 * 1000L),
                monitoredPackages = packages,
                monitoringEnabled = prefs[Keys.MONITORING_ENABLED] ?: false,
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: false,
                darkModeEnabled = prefs[Keys.DARK_MODE_ENABLED] ?: false,
                developerModeEnabled = prefs[Keys.DEVELOPER_MODE_ENABLED] ?: false,
                demoThresholdMs = prefs[Keys.DEMO_THRESHOLD] ?: (10 * 1000L),
                hobbies = hobbies,
                todos = todos
            )
        }

    suspend fun updateSettings(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.INTERVENTION_THRESHOLD] = settings.interventionThresholdMs
            prefs[Keys.COOLDOWN_DURATION] = settings.cooldownDurationMs
            prefs[Keys.MONITORED_PACKAGES] = Json.encodeToString(settings.monitoredPackages)
            prefs[Keys.MONITORING_ENABLED] = settings.monitoringEnabled
            prefs[Keys.ONBOARDING_COMPLETED] = settings.onboardingCompleted
            prefs[Keys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            prefs[Keys.VIBRATION_ENABLED] = settings.vibrationEnabled
            prefs[Keys.SOUND_ENABLED] = settings.soundEnabled
            prefs[Keys.DARK_MODE_ENABLED] = settings.darkModeEnabled
            prefs[Keys.DEVELOPER_MODE_ENABLED] = settings.developerModeEnabled
            prefs[Keys.DEMO_THRESHOLD] = settings.demoThresholdMs
            prefs[Keys.HOBBIES] = Json.encodeToString(settings.hobbies)
            prefs[Keys.TODOS] = Json.encodeToString(settings.todos)
        }
    }

    suspend fun setHobbies(hobbies: Set<String>) {
        context.dataStore.edit { it[Keys.HOBBIES] = Json.encodeToString(hobbies) }
    }

    suspend fun addTodo(text: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                Json.decodeFromString<List<TodoItem>>(prefs[Keys.TODOS] ?: "[]")
            } catch (_: Exception) {
                emptyList()
            }
            val updated = current + TodoItem(id = java.util.UUID.randomUUID().toString(), text = text)
            prefs[Keys.TODOS] = Json.encodeToString(updated)
        }
    }

    suspend fun toggleTodo(id: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                Json.decodeFromString<List<TodoItem>>(prefs[Keys.TODOS] ?: "[]")
            } catch (_: Exception) {
                emptyList()
            }
            val updated = current.map { if (it.id == id) it.copy(completed = !it.completed) else it }
            prefs[Keys.TODOS] = Json.encodeToString(updated)
        }
    }

    suspend fun removeTodo(id: String) {
        context.dataStore.edit { prefs ->
            val current = try {
                Json.decodeFromString<List<TodoItem>>(prefs[Keys.TODOS] ?: "[]")
            } catch (_: Exception) {
                emptyList()
            }
            prefs[Keys.TODOS] = Json.encodeToString(current.filterNot { it.id == id })
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MONITORING_ENABLED] = enabled }
    }

    // XP and gamification
    val focusXp: Flow<Int> = context.dataStore.data.map { it[Keys.FOCUS_XP] ?: 0 }
    val learningXp: Flow<Int> = context.dataStore.data.map { it[Keys.LEARNING_XP] ?: 0 }
    val currentStreak: Flow<Int> = context.dataStore.data.map { it[Keys.CURRENT_STREAK] ?: 0 }
    val totalMinutesRecovered: Flow<Int> = context.dataStore.data.map { it[Keys.TOTAL_MINUTES_RECOVERED] ?: 0 }
    val totalFocusSessions: Flow<Int> = context.dataStore.data.map { it[Keys.TOTAL_FOCUS_SESSIONS] ?: 0 }
    val totalInterventions: Flow<Int> = context.dataStore.data.map { it[Keys.TOTAL_INTERVENTIONS] ?: 0 }

    suspend fun addFocusXp(amount: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_XP] = (prefs[Keys.FOCUS_XP] ?: 0) + amount
        }
    }

    suspend fun addLearningXp(amount: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LEARNING_XP] = (prefs[Keys.LEARNING_XP] ?: 0) + amount
        }
    }

    suspend fun addRecoveredMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTAL_MINUTES_RECOVERED] = (prefs[Keys.TOTAL_MINUTES_RECOVERED] ?: 0) + minutes
            prefs[Keys.TOTAL_FOCUS_SESSIONS] = (prefs[Keys.TOTAL_FOCUS_SESSIONS] ?: 0) + 1
        }
    }

    suspend fun recordIntervention() {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTAL_INTERVENTIONS] = (prefs[Keys.TOTAL_INTERVENTIONS] ?: 0) + 1
        }
    }

    suspend fun updateStreak(today: Long) {
        context.dataStore.edit { prefs ->
            val lastDate = prefs[Keys.LAST_STREAK_DATE] ?: 0L
            val dayMs = 24 * 60 * 60 * 1000L
            val currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0
            prefs[Keys.CURRENT_STREAK] = if (today - lastDate < dayMs * 2) currentStreak + 1 else 1
            prefs[Keys.LAST_STREAK_DATE] = today
        }
    }
}
