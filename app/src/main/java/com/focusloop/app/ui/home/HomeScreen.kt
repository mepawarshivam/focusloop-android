package com.focusloop.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.focusloop.app.domain.model.Goal
import com.focusloop.app.ui.components.*
import com.focusloop.app.ui.theme.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Circle
import compose.icons.feathericons.MessageCircle
import compose.icons.feathericons.Play
import compose.icons.feathericons.Plus
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.Star
import compose.icons.feathericons.Target
import compose.icons.feathericons.TrendingUp
import compose.icons.feathericons.Zap
import compose.icons.feathericons.AlertTriangle
import compose.icons.feathericons.X
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartFocusSession: (goalId: Long, goalTitle: String) -> Unit,
    onAddGoal: () -> Unit,
    onOpenChat: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var containerSizePx by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSizePx = it.toSize() }
    ) {
    val density = LocalDensity.current
    val fabSizePx = with(density) { 56.dp.toPx() }
    val paddingPx = with(density) { 24.dp.toPx() }
    val minFabX = 0f
    val minFabY = 0f
    val maxFabX = (containerSizePx.width - fabSizePx - paddingPx).coerceAtLeast(0f)
    val maxFabY = (containerSizePx.height - fabSizePx - paddingPx).coerceAtLeast(0f)

    var fabX by rememberSaveable { mutableFloatStateOf(-1f) }
    var fabY by rememberSaveable { mutableFloatStateOf(-1f) }
    if (containerSizePx != androidx.compose.ui.geometry.Size.Zero) {
        if (fabX < 0f) fabX = maxFabX
        if (fabY < 0f) fabY = maxFabY
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header
        item {
            HomeHeader(
                greeting = state.greeting,
                isMonitoring = state.isMonitoring,
                streak = state.progress.currentStreak
            )
        }

        // Focus Score Ring
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                FocusRing(
                    progress = state.progress.focusScore / 100f,
                    value = "${state.progress.focusScore}",
                    label = "FOCUS SCORE",
                    size = 200.dp,
                    progressColor = FocusPurple
                )
            }
        }

        // Today's Stats
        item {
            TodayStats(
                distractionMs = state.todayDistractionMs,
                focusMs = state.todayFocusMs,
                sessions = state.todaySessions
            )
        }

        // XP + Level
        item {
            XpCard(progress = state.progress)
        }

        // Today's Goals header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Today's Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAddGoal) {
                    Icon(FeatherIcons.Plus, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Goals list
        if (state.goals.isEmpty()) {
            item { EmptyGoalsState(onAdd = onAddGoal) }
        } else {
            items(state.goals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    onStartSession = { onStartFocusSession(goal.id, goal.title) },
                    onComplete = { viewModel.completeGoal(goal) },
                    onRemove = { viewModel.removeGoal(goal) }
                )
            }
        }

        // Today's To-Dos header
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Today's To-Dos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        items(state.settings.todos, key = { it.id }) { todo ->
            TodoRow(
                todo = todo,
                onToggle = { viewModel.toggleTodo(todo.id) },
                onRemove = { viewModel.removeTodo(todo.id) }
            )
        }

        item {
            TodoQuickAdd(onAdd = { viewModel.addTodo(it) })
        }

        // Start Focus Session Button
        item {
            Spacer(Modifier.height(16.dp))
            GradientButton(
                text = "Start Focus Session",
                onClick = {
                    val goal = state.topGoal
                    if (goal != null) onStartFocusSession(goal.id, goal.title)
                    else onAddGoal()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                icon = FeatherIcons.Play
            )

            // Monitoring toggle
            Spacer(Modifier.height(16.dp))
            MonitoringToggle(
                isEnabled = state.isMonitoring,
                onToggle = { viewModel.toggleMonitoring() }
            )
        }
    }

        if (containerSizePx != androidx.compose.ui.geometry.Size.Zero) {
            FloatingActionButton(
                onClick = onOpenChat,
                containerColor = FocusPurple,
                contentColor = Color.White,
                modifier = Modifier
                    .offset {
                        IntOffset(fabX.roundToInt(), fabY.roundToInt())
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            fabX = (fabX + dragAmount.x).coerceIn(minFabX, maxFabX)
                            fabY = (fabY + dragAmount.y).coerceIn(minFabY, maxFabY)
                        }
                    }
            ) {
                Icon(FeatherIcons.MessageCircle, contentDescription = "Open coach chat")
            }
        }
    }
}

@Composable
private fun HomeHeader(greeting: String, isMonitoring: Boolean, streak: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Protect your attention.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (streak > 0) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(FeatherIcons.TrendingUp, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$streak day streak",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (isMonitoring) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PulsingDot(color = FocusGreen)
                Text(
                    "Monitoring active",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun TodayStats(distractionMs: Long, focusMs: Long, sessions: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatChip(
            icon = FeatherIcons.AlertTriangle,
            value = formatDuration(distractionMs),
            label = "Distraction",
            iconTint = FocusOrange
        )
        Divider(
            modifier = Modifier
                .height(48.dp)
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        StatChip(
            icon = FeatherIcons.RefreshCw,
            value = formatDuration(focusMs),
            label = "Recovered",
            iconTint = FocusTeal
        )
        Divider(
            modifier = Modifier
                .height(48.dp)
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        StatChip(
            icon = FeatherIcons.Star,
            value = "$sessions",
            label = "Sessions",
            iconTint = FocusPurple
        )
    }
}

@Composable
private fun XpCard(progress: com.focusloop.app.domain.model.UserProgress) {
    FocusCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        gradient = listOf(FocusPurple, FocusPurpleDark)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Level ${progress.level} · ${progress.levelTitle}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${progress.totalXp} XP",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Icon(FeatherIcons.Zap, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun GoalCard(goal: Goal, onStartSession: () -> Unit, onComplete: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .softShadow(cornerRadius = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        goal.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (goal.estimatedMinutes > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${goal.estimatedMinutes - goal.completedMinutes} min remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onComplete) {
                    Icon(
                        if (goal.completed) FeatherIcons.CheckCircle else FeatherIcons.Circle,
                        contentDescription = "Complete",
                        tint = if (goal.completed) FocusGreen else MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(FeatherIcons.X, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (goal.estimatedMinutes > 0) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { goal.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = FocusPurple,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${goal.progressPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onStartSession,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(FeatherIcons.Play, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Start Session", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun TodoRow(
    todo: com.focusloop.app.domain.model.TodoItem,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .softShadow(cornerRadius = 14.dp, elevation = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
            Icon(
                if (todo.completed) FeatherIcons.CheckCircle else FeatherIcons.Circle,
                contentDescription = "Toggle done",
                tint = if (todo.completed) FocusGreen else MaterialTheme.colorScheme.outline
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            todo.text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (todo.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (todo.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(FeatherIcons.X, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodoQuickAdd(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Add a to-do for today") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = androidx.compose.ui.text.input.ImeAction.Done
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
            if (text.isNotBlank()) {
                onAdd(text.trim())
                text = ""
            }
        }),
        trailingIcon = {
            IconButton(onClick = {
                if (text.isNotBlank()) {
                    onAdd(text.trim())
                    text = ""
                }
            }) {
                Icon(FeatherIcons.Plus, contentDescription = "Add to-do")
            }
        }
    )
}

@Composable
private fun EmptyGoalsState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(FeatherIcons.Target, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "What would you like to accomplish today?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedFocusButton(
            text = "Add Your First Goal",
            onClick = onAdd,
            icon = FeatherIcons.Plus
        )
    }
}

@Composable
private fun MonitoringToggle(isEnabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Focus Monitoring",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (isEnabled) "Watching for distraction" else "Monitoring is off",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FocusPurple)
        )
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0 min"
    val minutes = ms / 60000
    return if (minutes < 60) "${minutes}m" else "${minutes / 60}h ${minutes % 60}m"
}
