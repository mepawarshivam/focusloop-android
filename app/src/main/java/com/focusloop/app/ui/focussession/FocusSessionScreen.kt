package com.focusloop.app.ui.focussession

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.repository.UserDataRepository
import com.focusloop.app.data.repository.SessionRepository
import com.focusloop.app.domain.model.FocusSession
import com.focusloop.app.ui.components.FocusRing
import com.focusloop.app.ui.theme.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Zap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusSessionState(
    val goalTitle: String = "",
    val totalSeconds: Int = 600, // 10 minutes default
    val secondsRemaining: Int = 600,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val xpEarned: Int = 0
)

class FocusSessionViewModel(
    private val settingsDataStore: UserDataRepository,
    private val sessionRepository: SessionRepository,
    goalId: Long,
    goalTitle: String
) : ViewModel() {

    private val _state = MutableStateFlow(FocusSessionState(goalTitle = goalTitle))
    val state: StateFlow<FocusSessionState> = _state.asStateFlow()

    private var sessionId: Long = -1L
    private val startTime = System.currentTimeMillis()

    init {
        startSession(goalId, goalTitle)
    }

    private fun startSession(goalId: Long, goalTitle: String) {
        viewModelScope.launch {
            sessionId = sessionRepository.saveFocusSession(
                FocusSession(
                    goalId = goalId,
                    goalTitle = goalTitle,
                    startedAt = startTime
                )
            )
        }
        _state.value = _state.value.copy(isRunning = true)
        runTimer()
    }

    private fun runTimer() {
        viewModelScope.launch {
            while (_state.value.secondsRemaining > 0 && _state.value.isRunning) {
                delay(1000)
                _state.value = _state.value.copy(
                    secondsRemaining = _state.value.secondsRemaining - 1
                )
            }
            if (_state.value.secondsRemaining <= 0) {
                completeSession()
            }
        }
    }

    fun finishEarly() {
        _state.value = _state.value.copy(isRunning = false)
        viewModelScope.launch { completeSession() }
    }

    private suspend fun completeSession() {
        val elapsed = (System.currentTimeMillis() - startTime) / 60000
        val minutes = elapsed.toInt().coerceAtLeast(1)
        val xp = minutes * 5 // 5 XP per minute focused

        sessionRepository.updateFocusSession(
            FocusSession(
                id = sessionId,
                goalId = -1,
                goalTitle = _state.value.goalTitle,
                startedAt = startTime,
                endedAt = System.currentTimeMillis(),
                durationMinutes = minutes,
                completed = true
            )
        )

        settingsDataStore.addFocusXp(xp)
        settingsDataStore.addRecoveredMinutes(minutes)

        _state.value = _state.value.copy(isComplete = true, isRunning = false, xpEarned = xp)
    }
}

@Composable
fun FocusSessionScreen(
    viewModel: FocusSessionViewModel,
    onDone: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F0E1A), Color(0xFF1A1040)))
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = state.isComplete,
            transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(400)) },
            label = "session_content"
        ) { complete ->
            if (complete) {
                SessionCompletePanel(state = state, onDone = onDone)
            } else {
                ActiveSessionPanel(state = state, onFinishEarly = { viewModel.finishEarly() })
            }
        }
    }
}

@Composable
private fun ActiveSessionPanel(state: FocusSessionState, onFinishEarly: () -> Unit) {
    val progress = 1f - (state.secondsRemaining.toFloat() / state.totalSeconds)
    val min = state.secondsRemaining / 60
    val sec = state.secondsRemaining % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "FOCUS SESSION",
            style = MaterialTheme.typography.labelLarge,
            color = FocusPurpleLight,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            state.goalTitle,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        FocusRing(
            progress = progress,
            value = "%02d:%02d".format(min, sec),
            label = "remaining",
            size = 220.dp,
            strokeWidth = 16.dp,
            progressColor = FocusTeal,
            trackColor = Color(0x33FFFFFF)
        )

        Spacer(Modifier.height(48.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = FocusTeal,
            trackColor = Color(0x33FFFFFF)
        )

        Spacer(Modifier.height(40.dp))

        OutlinedButton(
            onClick = onFinishEarly,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB0AEC8)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A3860))
        ) {
            Text("Finish Early")
        }
    }
}

@Composable
private fun SessionCompletePanel(state: FocusSessionState, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(FeatherIcons.CheckCircle, contentDescription = null, tint = FocusTeal, modifier = Modifier.size(40.dp))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Nice work.",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "You reclaimed ${(state.totalSeconds - state.secondsRemaining) / 60 + 1} minutes\nfrom distraction.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFB0AEC8),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x33FFFFFF))
                .padding(horizontal = 32.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(FeatherIcons.Zap, contentDescription = null, tint = FocusYellow, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "+${state.xpEarned} Focus XP",
                style = MaterialTheme.typography.headlineSmall,
                color = FocusYellow,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FocusPurple)
        ) {
            Text("Back to Home", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
