package com.focusloop.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.repository.UserDataRepository
import com.focusloop.app.data.repository.GoalRepository
import com.focusloop.app.domain.model.Goal
import com.focusloop.app.domain.model.TodoItem
import com.focusloop.app.domain.model.UserSettings
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingState(
    val currentStep: Int = 0,
    val goals: List<Goal> = emptyList(),
    val todos: List<TodoItem> = emptyList(),
    val selectedHobbies: Set<String> = emptySet(),
    val selectedPackages: Set<String> = emptySet(),
    val interventionThresholdMs: Long = 5 * 60 * 1000L,
    val isCompleting: Boolean = false
)

class OnboardingViewModel(
    private val settingsDataStore: UserDataRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextStep() {
        _state.value = _state.value.copy(currentStep = _state.value.currentStep + 1)
    }

    fun previousStep() {
        if (_state.value.currentStep > 0) {
            _state.value = _state.value.copy(currentStep = _state.value.currentStep - 1)
        }
    }

    fun addGoal(title: String, description: String = "", estimatedMinutes: Int = 0) {
        val goal = Goal(
            title = title,
            description = description,
            estimatedMinutes = estimatedMinutes,
            priority = _state.value.goals.size
        )
        _state.value = _state.value.copy(goals = _state.value.goals + goal)
    }

    fun removeGoal(goal: Goal) {
        _state.value = _state.value.copy(goals = _state.value.goals - goal)
    }

    fun addTodo(text: String) {
        if (text.isBlank()) return
        val todo = TodoItem(id = UUID.randomUUID().toString(), text = text.trim())
        _state.value = _state.value.copy(todos = _state.value.todos + todo)
    }

    fun removeTodo(todo: TodoItem) {
        _state.value = _state.value.copy(todos = _state.value.todos - todo)
    }

    fun toggleHobby(hobby: String) {
        val current = _state.value.selectedHobbies
        _state.value = _state.value.copy(
            selectedHobbies = if (hobby in current) current - hobby else current + hobby
        )
    }

    fun togglePackage(packageName: String) {
        val current = _state.value.selectedPackages
        _state.value = _state.value.copy(
            selectedPackages = if (packageName in current) current - packageName else current + packageName
        )
    }

    fun setThreshold(ms: Long) {
        _state.value = _state.value.copy(interventionThresholdMs = ms)
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCompleting = true)
            // Save goals
            _state.value.goals.forEach { goalRepository.addGoal(it) }
            // Save settings
            settingsDataStore.updateSettings(
                UserSettings(
                    interventionThresholdMs = _state.value.interventionThresholdMs,
                    monitoredPackages = _state.value.selectedPackages,
                    hobbies = _state.value.selectedHobbies,
                    todos = _state.value.todos,
                    onboardingCompleted = true
                )
            )
            onComplete()
        }
    }
}
