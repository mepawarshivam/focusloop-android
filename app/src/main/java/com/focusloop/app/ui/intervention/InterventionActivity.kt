package com.focusloop.app.ui.intervention

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.focusloop.app.FocusLoopApplication
import com.focusloop.app.domain.model.Goal
import com.focusloop.app.domain.model.RecommendationItem
import com.focusloop.app.domain.model.RecommendationType
import com.focusloop.app.ui.theme.FocusLoopTheme
import com.focusloop.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The intervention is launched as a standalone Activity so it can appear
 * over other apps when the overlay permission is granted, or as a normal
 * Activity otherwise. We intentionally avoid using a system-level overlay
 * Window to keep things simple and permission-friendly for the MVP.
 */
class InterventionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "Unknown app"
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "that app"
        val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0L)
        val goalTitle = intent.getStringExtra(EXTRA_GOAL_TITLE) ?: ""
        val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1L)

        val app = application as FocusLoopApplication
        val viewModel = ViewModelProvider(
            this,
            InterventionViewModelFactory(app, goalId, goalTitle)
        )[InterventionViewModel::class.java]

        setContent {
            FocusLoopTheme {
                InterventionScreen(
                    appName = appName,
                    durationMs = durationMs,
                    goalTitle = goalTitle,
                    viewModel = viewModel,
                    onDismiss = { finish() },
                    onGoToFocusSession = { id, title ->
                        // Signal monitoring service to start a focus session
                        setResult(RESULT_FOCUS_SESSION, Intent().apply {
                            putExtra(EXTRA_GOAL_ID, id)
                            putExtra(EXTRA_GOAL_TITLE, title)
                        })
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_DURATION_MS = "duration_ms"
        const val EXTRA_GOAL_TITLE = "goal_title"
        const val EXTRA_GOAL_ID = "goal_id"
        const val RESULT_FOCUS_SESSION = Activity.RESULT_FIRST_USER + 1
        const val RESULT_SNOOZE = Activity.RESULT_FIRST_USER + 2

        fun createIntent(
            context: Context,
            packageName: String,
            appName: String,
            durationMs: Long,
            goalTitle: String,
            goalId: Long
        ) = Intent(context, InterventionActivity::class.java).apply {
            putExtra(EXTRA_PACKAGE_NAME, packageName)
            putExtra(EXTRA_APP_NAME, appName)
            putExtra(EXTRA_DURATION_MS, durationMs)
            putExtra(EXTRA_GOAL_TITLE, goalTitle)
            putExtra(EXTRA_GOAL_ID, goalId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}

sealed class InterventionChoice {
    object None : InterventionChoice()
    object GoToGoal : InterventionChoice()
    object Challenge : InterventionChoice()
    object Reset : InterventionChoice()
    object Snooze : InterventionChoice()
}

enum class RecommendationTab { QUIZ, WATCH, READ, DISCOVER }

data class InterventionUiState(
    val choice: InterventionChoice = InterventionChoice.None,
    val question: com.focusloop.app.domain.model.LearningQuestion? = null,
    val selectedAnswer: Int = -1,
    val answerRevealed: Boolean = false,
    val xpEarned: Int = 0,
    val resetPrompt: String = "",
    val snoozeSecondsRemaining: Int = 0,
    val isLoadingQuestion: Boolean = false,
    val recommendationTab: RecommendationTab = RecommendationTab.QUIZ,
    val videoRecs: List<RecommendationItem> = emptyList(),
    val articleRecs: List<RecommendationItem> = emptyList(),
    val eventRecs: List<RecommendationItem> = emptyList()
)

class InterventionViewModel(
    private val app: FocusLoopApplication,
    private val goalId: Long,
    private val goalTitle: String
) : ViewModel() {

    private val _state = MutableStateFlow(InterventionUiState())
    val state: StateFlow<InterventionUiState> = _state.asStateFlow()

    fun chooseGoal() {
        _state.value = _state.value.copy(choice = InterventionChoice.GoToGoal)
    }

    fun chooseChallenge() {
        _state.value = _state.value.copy(choice = InterventionChoice.Challenge, isLoadingQuestion = true)
        viewModelScope.launch {
            val q = app.learningRepository.getRandomQuestion()
            val settings = app.settingsDataStore.settings.first()
            val todoTexts = settings.todos.filterNot { it.completed }.map { it.text }
            val videos = app.recommendationRepository.getRecommendations(settings.hobbies, goalTitle, RecommendationType.VIDEO, todoTexts)
            val articles = app.recommendationRepository.getRecommendations(settings.hobbies, goalTitle, RecommendationType.ARTICLE, todoTexts)
            val events = app.recommendationRepository.getRecommendations(settings.hobbies, goalTitle, RecommendationType.EVENT, todoTexts)
            _state.value = _state.value.copy(
                question = q,
                isLoadingQuestion = false,
                videoRecs = videos,
                articleRecs = articles,
                eventRecs = events
            )
        }
    }

    fun selectRecommendationTab(tab: RecommendationTab) {
        _state.value = _state.value.copy(recommendationTab = tab)
    }

    fun chooseReset() {
        _state.value = _state.value.copy(choice = InterventionChoice.Reset)
    }

    fun chooseSnooze() {
        _state.value = _state.value.copy(choice = InterventionChoice.Snooze, snoozeSecondsRemaining = 300)
        viewModelScope.launch {
            var remaining = 300
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.value = _state.value.copy(snoozeSecondsRemaining = remaining)
            }
        }
    }

    fun submitAnswer(answerIndex: Int) {
        val question = _state.value.question ?: return
        viewModelScope.launch {
            val correct = answerIndex == question.correctAnswer
            val xp = app.learningRepository.recordAttempt(question.id, correct)
            app.settingsDataStore.addLearningXp(xp)
            _state.value = _state.value.copy(
                selectedAnswer = answerIndex,
                answerRevealed = true,
                xpEarned = xp
            )
        }
    }

    fun backToChoices() {
        _state.value = InterventionUiState()
    }
}

class InterventionViewModelFactory(
    private val app: FocusLoopApplication,
    private val goalId: Long,
    private val goalTitle: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InterventionViewModel(app, goalId, goalTitle) as T
    }
}
