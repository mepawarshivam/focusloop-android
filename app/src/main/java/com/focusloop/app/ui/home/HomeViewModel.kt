package com.focusloop.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.repository.UserDataRepository
import com.focusloop.app.data.repository.GoalRepository
import com.focusloop.app.data.repository.SessionRepository
import com.focusloop.app.domain.model.Goal
import com.focusloop.app.domain.model.UserProgress
import com.focusloop.app.domain.model.UserSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val greeting: String = "Good morning",
    val goals: List<Goal> = emptyList(),
    val topGoal: Goal? = null,
    val todayDistractionMs: Long = 0L,
    val todayFocusMs: Long = 0L,
    val todayInterventions: Int = 0,
    val todaySessions: Int = 0,
    val progress: UserProgress = UserProgress(),
    val settings: UserSettings = UserSettings(),
    val isMonitoring: Boolean = false,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val settingsDataStore: UserDataRepository,
    private val goalRepository: GoalRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine all data sources
            combine(
                goalRepository.activeGoals,
                settingsDataStore.settings,
                settingsDataStore.focusXp,
                settingsDataStore.learningXp,
                settingsDataStore.currentStreak,
                settingsDataStore.totalMinutesRecovered,
                settingsDataStore.totalFocusSessions,
                settingsDataStore.totalInterventions
            ) { goals, settings, focusXp, learningXp, streak, recovered, sessions, interventions ->
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val greeting = when {
                    hour < 12 -> "Good morning"
                    hour < 17 -> "Good afternoon"
                    else -> "Good evening"
                }
                val progress = UserProgress(
                    focusXp = focusXp,
                    learningXp = learningXp,
                    currentStreak = streak,
                    totalMinutesRecovered = recovered,
                    totalFocusSessions = sessions,
                    totalInterventions = interventions
                )
                _uiState.value = _uiState.value.copy(
                    greeting = greeting,
                    goals = goals,
                    topGoal = goals.firstOrNull(),
                    settings = settings,
                    isMonitoring = settings.monitoringEnabled,
                    progress = progress,
                    isLoading = false
                )
            }.launchIn(viewModelScope)

            // Load today's stats
            refreshTodayStats()
        }
    }

    private suspend fun refreshTodayStats() {
        val todayStart = getTodayStart()
        val distractionMs = sessionRepository.getTotalDistractionMsSince(todayStart)
        val focusMinutes = sessionRepository.getTotalFocusMinutes(todayStart)
        val sessionCount = sessionRepository.getCompletedFocusSessionCount(todayStart)

        _uiState.value = _uiState.value.copy(
            todayDistractionMs = distractionMs,
            todayFocusMs = focusMinutes * 60 * 1000L,
            todaySessions = sessionCount
        )
    }

    fun toggleMonitoring() {
        viewModelScope.launch {
            val newEnabled = !_uiState.value.settings.monitoringEnabled
            settingsDataStore.setMonitoringEnabled(newEnabled)
        }
    }

    fun completeGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.updateGoal(goal.copy(completed = true))
            settingsDataStore.addFocusXp(25)
        }
    }

    fun removeGoal(goal: Goal) {
        viewModelScope.launch { goalRepository.deleteGoal(goal) }
    }

    fun addTodo(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { settingsDataStore.addTodo(text.trim()) }
    }

    fun toggleTodo(id: String) {
        viewModelScope.launch { settingsDataStore.toggleTodo(id) }
    }

    fun removeTodo(id: String) {
        viewModelScope.launch { settingsDataStore.removeTodo(id) }
    }

    private fun getTodayStart(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

// Support combining 8 flows
private fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
    f1: Flow<T1>, f2: Flow<T2>, f3: Flow<T3>, f4: Flow<T4>,
    f5: Flow<T5>, f6: Flow<T6>, f7: Flow<T7>, f8: Flow<T8>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): Flow<R> = combine(f1, f2, f3, f4, f5, f6, f7, f8) { arr ->
    @Suppress("UNCHECKED_CAST")
    transform(arr[0] as T1, arr[1] as T2, arr[2] as T3, arr[3] as T4,
        arr[4] as T5, arr[5] as T6, arr[6] as T7, arr[7] as T8)
}
