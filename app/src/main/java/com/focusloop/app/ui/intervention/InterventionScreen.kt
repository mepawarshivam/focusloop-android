package com.focusloop.app.ui.intervention

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusloop.app.domain.model.LearningQuestion
import com.focusloop.app.domain.model.RecommendationItem
import com.focusloop.app.domain.model.RecommendationType
import com.focusloop.app.ui.theme.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.BookOpen
import compose.icons.feathericons.ExternalLink
import compose.icons.feathericons.HelpCircle
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.Youtube

@Composable
fun InterventionScreen(
    appName: String,
    durationMs: Long,
    goalTitle: String,
    viewModel: InterventionViewModel,
    onDismiss: () -> Unit,
    onGoToFocusSession: (goalId: Long, title: String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF00F0E1A), Color(0xF01A1040))
                )
            )
    ) {
        AnimatedContent(
            targetState = state.choice,
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(300))
            },
            label = "intervention_content"
        ) { choice ->
            when (choice) {
                is InterventionChoice.None -> MainInterventionPanel(
                    appName = appName,
                    durationMs = durationMs,
                    goalTitle = goalTitle,
                    onChooseGoal = { viewModel.chooseGoal() },
                    onChooseChallenge = { viewModel.chooseChallenge() },
                    onChooseReset = { viewModel.chooseReset() },
                    onChooseSnooze = { viewModel.chooseSnooze() }
                )
                is InterventionChoice.GoToGoal -> GoToGoalPanel(
                    goalTitle = goalTitle,
                    onStartSession = { onGoToFocusSession(-1, goalTitle) },
                    onBack = { viewModel.backToChoices() }
                )
                is InterventionChoice.Challenge -> ChallengePanel(
                    question = state.question,
                    selectedAnswer = state.selectedAnswer,
                    answerRevealed = state.answerRevealed,
                    xpEarned = state.xpEarned,
                    isLoading = state.isLoadingQuestion,
                    activeTab = state.recommendationTab,
                    videoRecs = state.videoRecs,
                    articleRecs = state.articleRecs,
                    eventRecs = state.eventRecs,
                    onSelectTab = { viewModel.selectRecommendationTab(it) },
                    onSelectAnswer = { viewModel.submitAnswer(it) },
                    onDone = onDismiss,
                    onBack = { viewModel.backToChoices() }
                )
                is InterventionChoice.Reset -> QuickResetPanel(
                    onDone = onDismiss,
                    onGoToGoal = { onGoToFocusSession(-1, goalTitle) },
                    onBack = { viewModel.backToChoices() }
                )
                is InterventionChoice.Snooze -> SnoozePanel(
                    secondsRemaining = state.snoozeSecondsRemaining,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun MainInterventionPanel(
    appName: String,
    durationMs: Long,
    goalTitle: String,
    onChooseGoal: () -> Unit,
    onChooseChallenge: () -> Unit,
    onChooseReset: () -> Unit,
    onChooseSnooze: () -> Unit
) {
    val minutes = durationMs / 60000
    val seconds = (durationMs % 60000) / 1000
    val timeStr = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Brain icon with pulse
        Text("🧠", fontSize = 52.sp)

        Spacer(Modifier.height(16.dp))

        Text(
            "QUICK REALITY CHECK",
            style = MaterialTheme.typography.labelLarge,
            color = FocusPurpleLight,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "You've been scrolling for",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFB0AEC8),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Large animated time display
        AnimatedTimer(timeStr = timeStr)

        Spacer(Modifier.height(8.dp))

        Text(
            "on $appName",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFB0AEC8)
        )

        if (goalTitle.isNotBlank()) {
            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22FFFFFF))
                    .padding(20.dp)
            ) {
                Text(
                    "Today's goal",
                    style = MaterialTheme.typography.labelMedium,
                    color = FocusTeal,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "\"$goalTitle\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "What would you like to do?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFB0AEC8),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // Action buttons
        InterventionButton("🚀  Get Back To My Goal", FocusPurple, onChooseGoal)
        Spacer(Modifier.height(12.dp))
        InterventionButton("🧠  2-Minute Challenge", FocusTealDark, onChooseChallenge)
        Spacer(Modifier.height(12.dp))
        InterventionButton("🌱  Take a Quick Reset", FocusGreen, onChooseReset)
        Spacer(Modifier.height(12.dp))
        InterventionButton("⏱  Give Me 5 More Minutes", Color(0xFF3A3860), onChooseSnooze, isSubtle = true)

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun AnimatedTimer(timeStr: String) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "timer_scale"
    )
    Text(
        text = timeStr,
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Black,
        color = Color.White
    )
}

@Composable
private fun InterventionButton(text: String, color: Color, onClick: () -> Unit, isSubtle: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSubtle) color else color.copy(alpha = 0.9f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun GoToGoalPanel(goalTitle: String, onStartSession: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🚀", fontSize = 52.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            "Let's make the next\n10 minutes count.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        if (goalTitle.isNotBlank()) {
            Text(
                "\"$goalTitle\"",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0AEC8),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(40.dp))
        InterventionButton("Start 10-Minute Focus Session", FocusPurple, onStartSession)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) {
            Text("← Back", color = Color(0xFFB0AEC8))
        }
    }
}

@Composable
private fun ChallengePanel(
    question: LearningQuestion?,
    selectedAnswer: Int,
    answerRevealed: Boolean,
    xpEarned: Int,
    isLoading: Boolean,
    activeTab: RecommendationTab,
    videoRecs: List<RecommendationItem>,
    articleRecs: List<RecommendationItem>,
    eventRecs: List<RecommendationItem>,
    onSelectTab: (RecommendationTab) -> Unit,
    onSelectAnswer: (Int) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text("🧠", fontSize = 42.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "TAKE A MICRO-BREAK",
            style = MaterialTheme.typography.labelLarge,
            color = FocusTeal,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(24.dp))

        RecommendationTabRow(activeTab = activeTab, onSelectTab = onSelectTab)

        Spacer(Modifier.height(28.dp))

        when (activeTab) {
            RecommendationTab.WATCH -> RecommendationList(videoRecs, FeatherIcons.Youtube)
            RecommendationTab.READ -> RecommendationList(articleRecs, FeatherIcons.BookOpen)
            RecommendationTab.DISCOVER -> RecommendationList(eventRecs, FeatherIcons.MapPin)
            RecommendationTab.QUIZ -> QuizContent(
                question = question,
                selectedAnswer = selectedAnswer,
                answerRevealed = answerRevealed,
                xpEarned = xpEarned,
                isLoading = isLoading,
                onSelectAnswer = onSelectAnswer,
                onDone = onDone
            )
        }

        Spacer(Modifier.height(16.dp))
        if (!(activeTab == RecommendationTab.QUIZ && answerRevealed)) {
            TextButton(onClick = onBack) {
                Text("← Back", color = Color(0xFFB0AEC8))
            }
        }
    }
}

@Composable
private fun RecommendationTabRow(activeTab: RecommendationTab, onSelectTab: (RecommendationTab) -> Unit) {
    val tabs = listOf(
        Triple(RecommendationTab.QUIZ, "Quiz", FeatherIcons.HelpCircle),
        Triple(RecommendationTab.WATCH, "Watch", FeatherIcons.Youtube),
        Triple(RecommendationTab.READ, "Read", FeatherIcons.BookOpen),
        Triple(RecommendationTab.DISCOVER, "Discover", FeatherIcons.MapPin)
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs) { (tab, label, icon) ->
            val selected = tab == activeTab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) FocusTeal else Color(0x22FFFFFF))
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) Color(0xFF0F0E1A) else Color(0xFFB0AEC8)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color(0xFF0F0E1A) else Color(0xFFB0AEC8)
                )
            }
        }
    }
}

@Composable
private fun RecommendationList(items: List<RecommendationItem>, typeIcon: ImageVector) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1AFFFFFF))
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(FocusPurple, FocusTeal))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(typeIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B88A8)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    FeatherIcons.ExternalLink,
                    contentDescription = null,
                    tint = Color(0xFF8B88A8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (items.isEmpty()) {
            Text(
                "No suggestions yet — add some hobbies in onboarding to personalize this.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB0AEC8),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            )
        }
    }
}

@Composable
private fun QuizContent(
    question: LearningQuestion?,
    selectedAnswer: Int,
    answerRevealed: Boolean,
    xpEarned: Int,
    isLoading: Boolean,
    onSelectAnswer: (Int) -> Unit,
    onDone: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isLoading) {
            CircularProgressIndicator(color = FocusTeal)
        } else if (question == null) {
            Text("No question available", color = Color.White)
        } else if (!answerRevealed) {
            // Question
            Text(
                question.question,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(32.dp))
            // Options
            question.options.forEachIndexed { idx, option ->
                val label = listOf("A", "B", "C", "D")[idx]
                Button(
                    onClick = { onSelectAnswer(idx) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x33FFFFFF)
                    )
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$label.",
                            style = MaterialTheme.typography.labelLarge,
                            color = FocusTeal,
                            modifier = Modifier.width(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            // Answer revealed
            val isCorrect = selectedAnswer == question.correctAnswer
            Text(
                if (isCorrect) "✓ Correct!" else "✗ Not quite...",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isCorrect) FocusGreen else FocusRed,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                question.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB0AEC8),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FFFFFF))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚡", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text("+$xpEarned Learning XP", color = FocusYellow, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
            InterventionButton("Nice. Let's go! 🚀", FocusPurple, onDone)
        }
    }
}

@Composable
private fun QuickResetPanel(onDone: () -> Unit, onGoToGoal: () -> Unit, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var reflection by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (step == 0) {
            Text("🌱", fontSize = 52.sp)
            Spacer(Modifier.height(24.dp))
            Text("QUICK RESET", style = MaterialTheme.typography.labelLarge, color = FocusGreen, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                "Take one breath.\n\nLook away from the screen.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "What's one thing you want to accomplish before tonight?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0AEC8),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = reflection,
                onValueChange = { reflection = it },
                placeholder = { Text("Write it here...", color = Color(0xFF6B6880)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = FocusGreen,
                    unfocusedBorderColor = Color(0xFF3A3860)
                )
            )
            Spacer(Modifier.height(24.dp))
            InterventionButton("Done ✓", FocusGreen, { if (reflection.isNotBlank()) step = 1 })
        } else {
            Text("Nice.\n\nNow let's make that happen.", style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))
            InterventionButton("🚀 Start Focus Session", FocusPurple, onGoToGoal)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onDone) { Text("Return to app", color = Color(0xFFB0AEC8)) }
        }

        if (step == 0) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBack) { Text("← Back", color = Color(0xFFB0AEC8)) }
        }
    }
}

@Composable
private fun SnoozePanel(secondsRemaining: Int, onDismiss: () -> Unit) {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⏱", fontSize = 52.sp)
        Spacer(Modifier.height(24.dp))
        Text("Okay.", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("I'll give you 5 more minutes.\nBut I'll check in again afterward.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFB0AEC8), textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Text("%02d:%02d".format(minutes, seconds), style = MaterialTheme.typography.displayMedium, color = FocusPurpleLight, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text("remaining", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B6880))
        Spacer(Modifier.height(40.dp))
        if (secondsRemaining <= 0) {
            Text("Time's up! 👋", style = MaterialTheme.typography.titleLarge, color = FocusYellow)
        }
        TextButton(onClick = onDismiss) {
            Text("Dismiss", color = Color(0xFF6B6880))
        }
    }
}
