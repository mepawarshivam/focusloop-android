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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusloop.app.domain.model.Flashcard
import com.focusloop.app.domain.model.LearningQuestion
import com.focusloop.app.domain.model.ReflectionPrompt
import com.focusloop.app.domain.model.RecommendationItem
import com.focusloop.app.domain.model.RecommendationType
import com.focusloop.app.ui.theme.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.BookOpen
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Clock
import compose.icons.feathericons.ExternalLink
import compose.icons.feathericons.Feather
import compose.icons.feathericons.HelpCircle
import compose.icons.feathericons.Layers
import compose.icons.feathericons.MapPin
import compose.icons.feathericons.PlayCircle
import compose.icons.feathericons.TrendingUp
import compose.icons.feathericons.Wind
import compose.icons.feathericons.X
import compose.icons.feathericons.XCircle
import compose.icons.feathericons.Youtube
import compose.icons.feathericons.Zap

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
                    state = state,
                    onSelectTab = { viewModel.selectRecommendationTab(it) },
                    onSelectAnswer = { viewModel.submitAnswer(it) },
                    onOpenVideo = { viewModel.openVideo(it) },
                    onCloseVideo = { viewModel.closeVideo() },
                    onFlipFlashcard = { viewModel.flipFlashcard() },
                    onNextFlashcard = { viewModel.nextFlashcard() },
                    onReflectionAnswerChange = { viewModel.updateReflectionAnswer(it) },
                    onSubmitReflection = { viewModel.submitReflection() },
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

        IconBadge(icon = FeatherIcons.HelpCircle, tint = FocusPurpleLight, size = 76.dp)

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

        // Action buttons — Microlearning is the flagship path, shown first and larger
        InterventionButton(
            "Quick Microlearning",
            FocusTealDark,
            onChooseChallenge,
            icon = FeatherIcons.HelpCircle,
            subtitle = "Quiz · Flashcards · Video · Reflect — 2 min"
        )
        Spacer(Modifier.height(12.dp))
        InterventionButton("Get Back To My Goal", FocusPurple, onChooseGoal, icon = FeatherIcons.TrendingUp)
        Spacer(Modifier.height(12.dp))
        InterventionButton("Take a Quick Reset", FocusGreen, onChooseReset, icon = FeatherIcons.Wind)
        Spacer(Modifier.height(12.dp))
        InterventionButton("Give Me 5 More Minutes", Color(0xFF3A3860), onChooseSnooze, icon = FeatherIcons.Clock, isSubtle = true)

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
private fun InterventionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    isSubtle: Boolean = false,
    subtitle: String? = null
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle != null) 72.dp else 60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSubtle) color else color.copy(alpha = 0.9f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
        }
        if (subtitle != null) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        } else {
            Text(
                text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color, size: Dp = 64.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(Color(0x22FFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.45f))
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
        IconBadge(icon = FeatherIcons.TrendingUp, tint = FocusPurpleLight, size = 76.dp)
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
    state: InterventionUiState,
    onSelectTab: (RecommendationTab) -> Unit,
    onSelectAnswer: (Int) -> Unit,
    onOpenVideo: (String) -> Unit,
    onCloseVideo: () -> Unit,
    onFlipFlashcard: () -> Unit,
    onNextFlashcard: () -> Unit,
    onReflectionAnswerChange: (String) -> Unit,
    onSubmitReflection: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val activeTab = state.recommendationTab

    if (state.watchingUrl != null) {
        InAppVideoPlayer(url = state.watchingUrl, onClose = onCloseVideo)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        IconBadge(icon = FeatherIcons.HelpCircle, tint = FocusTeal, size = 60.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            "MICROLEARNING",
            style = MaterialTheme.typography.labelLarge,
            color = FocusTeal,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(24.dp))

        RecommendationTabRow(activeTab = activeTab, onSelectTab = onSelectTab)

        Spacer(Modifier.height(28.dp))

        when (activeTab) {
            RecommendationTab.WATCH -> RecommendationList(state.videoRecs, FeatherIcons.Youtube, onOpenVideo)
            RecommendationTab.READ -> RecommendationList(state.articleRecs, FeatherIcons.BookOpen, onOpenVideo)
            RecommendationTab.DISCOVER -> RecommendationList(state.eventRecs, FeatherIcons.MapPin, onOpenVideo)
            RecommendationTab.QUIZ -> QuizContent(
                question = state.question,
                selectedAnswer = state.selectedAnswer,
                answerRevealed = state.answerRevealed,
                xpEarned = state.xpEarned,
                isLoading = state.isLoadingQuestion,
                onSelectAnswer = onSelectAnswer,
                onDone = onDone
            )
            RecommendationTab.FLASHCARDS -> FlashcardsContent(
                cards = state.flashcards,
                index = state.flashcardIndex,
                flipped = state.flashcardFlipped,
                isLoading = state.flashcardsLoading,
                isError = state.flashcardsError,
                onFlip = onFlipFlashcard,
                onNext = onNextFlashcard,
                onDone = onDone
            )
            RecommendationTab.REFLECT -> ReflectContent(
                reflection = state.reflection,
                answer = state.reflectionAnswer,
                submitted = state.reflectionSubmitted,
                xpEarned = state.xpEarned,
                isLoading = state.reflectionLoading,
                isError = state.reflectionError,
                onAnswerChange = onReflectionAnswerChange,
                onSubmit = onSubmitReflection,
                onDone = onDone
            )
        }

        Spacer(Modifier.height(16.dp))
        val hideBack = (activeTab == RecommendationTab.QUIZ && state.answerRevealed) ||
            (activeTab == RecommendationTab.REFLECT && state.reflectionSubmitted)
        if (!hideBack) {
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
        Triple(RecommendationTab.FLASHCARDS, "Cards", FeatherIcons.Layers),
        Triple(RecommendationTab.REFLECT, "Reflect", FeatherIcons.Feather),
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
private fun RecommendationList(items: List<RecommendationItem>, typeIcon: ImageVector, onOpen: (String) -> Unit) {
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
                    .clickable { onOpen(item.url) }
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
                    FeatherIcons.PlayCircle,
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
private fun InAppVideoPlayer(url: String, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(FeatherIcons.X, contentDescription = "Close", tint = Color.White)
            }
            Text(
                "Watching in FocusLoop",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFB0AEC8)
            )
        }
        androidx.compose.ui.viewinterop.AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                android.webkit.WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webViewClient = android.webkit.WebViewClient()
                    webChromeClient = android.webkit.WebChromeClient()
                    loadUrl(url)
                }
            }
        )
    }
}

@Composable
private fun FlashcardsContent(
    cards: List<Flashcard>,
    index: Int,
    flipped: Boolean,
    isLoading: Boolean,
    isError: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        when {
            isLoading -> {
                CircularProgressIndicator(color = FocusTeal)
                Spacer(Modifier.height(12.dp))
                Text("Generating flashcards for you…", color = Color(0xFFB0AEC8))
            }
            isError || cards.isEmpty() -> {
                Text("Couldn't load flashcards right now.", color = Color(0xFFB0AEC8), textAlign = TextAlign.Center)
            }
            index >= cards.size -> {
                Icon(FeatherIcons.CheckCircle, contentDescription = null, tint = FocusGreen, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text("You've been through all the cards.", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(24.dp))
                InterventionButton("Done", FocusPurple, onDone, icon = FeatherIcons.CheckCircle)
            }
            else -> {
                val card = cards[index]
                Text(
                    "${index + 1} / ${cards.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8B88A8)
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(FocusPurple, FocusTealDark)))
                        .clickable(onClick = onFlip)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (flipped) card.back else card.front,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = if (flipped) FontWeight.Normal else FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (flipped) "Tap the card to flip back" else "Tap the card to reveal the answer",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8B88A8)
                )
                Spacer(Modifier.height(24.dp))
                InterventionButton(
                    if (index == cards.size - 1) "Finish" else "Next Card",
                    FocusTealDark,
                    onNext,
                    icon = FeatherIcons.ArrowRight
                )
            }
        }
    }
}

@Composable
private fun ReflectContent(
    reflection: ReflectionPrompt?,
    answer: String,
    submitted: Boolean,
    xpEarned: Int,
    isLoading: Boolean,
    isError: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDone: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        when {
            isLoading -> {
                CircularProgressIndicator(color = FocusTeal)
                Spacer(Modifier.height(12.dp))
                Text("Thinking of something worth sharing…", color = Color(0xFFB0AEC8))
            }
            isError || reflection == null -> {
                Text("Couldn't load a reflection prompt right now.", color = Color(0xFFB0AEC8), textAlign = TextAlign.Center)
            }
            submitted -> {
                Icon(FeatherIcons.CheckCircle, contentDescription = null, tint = FocusGreen, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text("Nice reflection.", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(FeatherIcons.Zap, contentDescription = null, tint = FocusYellow, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("+$xpEarned Learning XP", color = FocusYellow, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
                InterventionButton("Done", FocusPurple, onDone, icon = FeatherIcons.CheckCircle)
            }
            else -> {
                Text(
                    reflection.insight,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    reflection.question,
                    style = MaterialTheme.typography.titleSmall,
                    color = FocusTeal,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = onAnswerChange,
                    placeholder = { Text("Type a quick answer…", color = Color(0xFF6B6880)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = FocusTeal,
                        unfocusedBorderColor = Color(0xFF3A3860)
                    )
                )
                Spacer(Modifier.height(20.dp))
                InterventionButton("Submit", FocusTealDark, onSubmit, icon = FeatherIcons.CheckCircle)
            }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCorrect) FeatherIcons.CheckCircle else FeatherIcons.XCircle,
                    contentDescription = null,
                    tint = if (isCorrect) FocusGreen else FocusRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isCorrect) "Correct!" else "Not quite...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isCorrect) FocusGreen else FocusRed,
                    fontWeight = FontWeight.Bold
                )
            }
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
                Icon(FeatherIcons.Zap, contentDescription = null, tint = FocusYellow, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("+$xpEarned Learning XP", color = FocusYellow, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
            InterventionButton("Nice. Let's go!", FocusPurple, onDone, icon = FeatherIcons.TrendingUp)
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
            IconBadge(icon = FeatherIcons.Wind, tint = FocusGreen, size = 76.dp)
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
            InterventionButton("Done", FocusGreen, { if (reflection.isNotBlank()) step = 1 }, icon = FeatherIcons.CheckCircle)
        } else {
            Text("Nice.\n\nNow let's make that happen.", style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))
            InterventionButton("Start Focus Session", FocusPurple, onGoToGoal, icon = FeatherIcons.TrendingUp)
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
        IconBadge(icon = FeatherIcons.Clock, tint = FocusPurpleLight, size = 76.dp)
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
            Text("Time's up!", style = MaterialTheme.typography.titleLarge, color = FocusYellow)
        }
        TextButton(onClick = onDismiss) {
            Text("Dismiss", color = Color(0xFF6B6880))
        }
    }
}
