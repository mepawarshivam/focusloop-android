package com.focusloop.app.ui.challenges

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.datastore.SettingsDataStore
import com.focusloop.app.data.repository.LearningRepository
import com.focusloop.app.domain.model.LearningQuestion
import com.focusloop.app.domain.model.QuestionCategory
import com.focusloop.app.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ChevronRight

data class ChallengesUiState(
    val selectedCategory: QuestionCategory? = null,
    val currentQuestion: LearningQuestion? = null,
    val selectedAnswer: Int = -1,
    val answerRevealed: Boolean = false,
    val xpEarned: Int = 0,
    val totalLearningXp: Int = 0,
    val isLoading: Boolean = false
)

class ChallengesViewModel(
    private val learningRepository: LearningRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ChallengesUiState())
    val state: StateFlow<ChallengesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.learningXp.collect { xp ->
                _state.value = _state.value.copy(totalLearningXp = xp)
            }
        }
    }

    fun startChallenge(category: QuestionCategory? = null) {
        _state.value = _state.value.copy(isLoading = true, selectedCategory = category)
        viewModelScope.launch {
            val q = if (category != null) {
                learningRepository.getRandomQuestionByCategory(category)
            } else {
                learningRepository.getRandomQuestion()
            }
            _state.value = _state.value.copy(
                currentQuestion = q,
                selectedAnswer = -1,
                answerRevealed = false,
                xpEarned = 0,
                isLoading = false
            )
        }
    }

    fun submitAnswer(answerIndex: Int) {
        val question = _state.value.currentQuestion ?: return
        viewModelScope.launch {
            val correct = answerIndex == question.correctAnswer
            val xp = learningRepository.recordAttempt(question.id, correct)
            settingsDataStore.addLearningXp(xp)
            _state.value = _state.value.copy(selectedAnswer = answerIndex, answerRevealed = true, xpEarned = xp)
        }
    }

    fun nextQuestion() {
        startChallenge(_state.value.selectedCategory)
    }

    fun backToCategories() {
        _state.value = _state.value.copy(currentQuestion = null, selectedCategory = null, answerRevealed = false, selectedAnswer = -1)
    }
}

@Composable
fun ChallengesScreen(viewModel: ChallengesViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text("Challenges", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Keep your mind sharp while you take a break.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // XP display
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                "${state.totalLearningXp} Learning XP",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = state.currentQuestion,
            transitionSpec = { slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut() },
            label = "challenge_content"
        ) { question ->
            if (question == null) {
                CategorySelectionView(
                    onSelectCategory = { viewModel.startChallenge(it) },
                    onRandomChallenge = { viewModel.startChallenge(null) }
                )
            } else {
                QuestionView(
                    question = question,
                    selectedAnswer = state.selectedAnswer,
                    answerRevealed = state.answerRevealed,
                    xpEarned = state.xpEarned,
                    onSelectAnswer = { viewModel.submitAnswer(it) },
                    onNext = { viewModel.nextQuestion() },
                    onBack = { viewModel.backToCategories() }
                )
            }
        }
    }
}

@Composable
private fun CategorySelectionView(onSelectCategory: (QuestionCategory) -> Unit, onRandomChallenge: () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Today's challenge CTA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRandomChallenge() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FocusPurple)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Today's Challenge", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                    Text("Random · 2 minutes · +10 XP", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text("🎲", fontSize = 36.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Browse by category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        QuestionCategory.entries.forEach { category ->
            CategoryRow(category = category, onSelect = { onSelectCategory(category) })
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CategoryRow(category: QuestionCategory, onSelect: () -> Unit) {
    val (emoji, color) = when (category) {
        QuestionCategory.DSA -> "🌲" to FocusPurple
        QuestionCategory.SYSTEM_DESIGN -> "🏗️" to FocusTeal
        QuestionCategory.JAVASCRIPT -> "⚡" to FocusYellow
        QuestionCategory.PROGRAMMING -> "💻" to FocusOrange
        QuestionCategory.GENERAL -> "🌍" to FocusGreen
        QuestionCategory.MATH -> "📐" to FocusRed
        QuestionCategory.PRODUCTIVITY -> "🎯" to FocusPurpleLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(category.displayName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Icon(FeatherIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuestionView(
    question: LearningQuestion,
    selectedAnswer: Int,
    answerRevealed: Boolean,
    xpEarned: Int,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        TextButton(onClick = onBack) {
            Icon(FeatherIcons.ArrowLeft, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Categories")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            question.category.displayName.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = FocusPurple,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            question.question,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 30.sp
        )

        Spacer(Modifier.height(24.dp))

        question.options.forEachIndexed { idx, option ->
            val label = listOf("A", "B", "C", "D")[idx]
            val bgColor = when {
                !answerRevealed -> MaterialTheme.colorScheme.surface
                idx == question.correctAnswer -> FocusGreen.copy(alpha = 0.15f)
                idx == selectedAnswer && selectedAnswer != question.correctAnswer -> FocusRed.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
            val borderColor = when {
                !answerRevealed && idx == selectedAnswer -> FocusPurple
                answerRevealed && idx == question.correctAnswer -> FocusGreen
                answerRevealed && idx == selectedAnswer && selectedAnswer != question.correctAnswer -> FocusRed
                else -> MaterialTheme.colorScheme.outline
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .then(if (!answerRevealed) Modifier.clickable { onSelectAnswer(idx) } else Modifier)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = FocusPurple, modifier = Modifier.width(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(option, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (answerRevealed) {
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Explanation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("+$xpEarned XP earned", style = MaterialTheme.typography.labelLarge, color = FocusYellow, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FocusPurple)
            ) {
                Text("Next Challenge →", color = Color.White)
            }
        }
    }
}
