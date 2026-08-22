package com.focusloop.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.focusloop.app.domain.model.DistractingApp
import com.focusloop.app.domain.model.Goal
import com.focusloop.app.domain.model.KnownDistractingApps
import com.focusloop.app.ui.components.AppLogo
import com.focusloop.app.ui.components.AppSelectionCard
import com.focusloop.app.ui.components.GradientButton
import com.focusloop.app.ui.components.HobbySelectionCard
import com.focusloop.app.ui.components.OutlinedFocusButton
import com.focusloop.app.ui.theme.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ArrowRight
import compose.icons.feathericons.Check
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.Circle
import compose.icons.feathericons.Plus
import compose.icons.feathericons.Star
import compose.icons.feathericons.X

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    AnimatedContent(
        targetState = state.currentStep,
        transitionSpec = {
            slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
        },
        label = "onboarding_step"
    ) { step ->
        when (step) {
            0 -> WelcomeStep(onNext = { viewModel.nextStep() })
            1 -> GoalsStep(
                goals = state.goals,
                onAddGoal = viewModel::addGoal,
                onRemoveGoal = viewModel::removeGoal,
                onNext = { viewModel.nextStep() },
                onBack = { viewModel.previousStep() }
            )
            2 -> TodosStep(
                todos = state.todos,
                onAddTodo = viewModel::addTodo,
                onRemoveTodo = viewModel::removeTodo,
                onNext = { viewModel.nextStep() },
                onBack = { viewModel.previousStep() }
            )
            3 -> HobbiesStep(
                selectedHobbies = state.selectedHobbies,
                onToggleHobby = viewModel::toggleHobby,
                onNext = { viewModel.nextStep() },
                onBack = { viewModel.previousStep() }
            )
            4 -> AppsStep(
                selectedPackages = state.selectedPackages,
                onTogglePackage = viewModel::togglePackage,
                onNext = { viewModel.nextStep() },
                onBack = { viewModel.previousStep() }
            )
            5 -> TimingStep(
                selectedThresholdMs = state.interventionThresholdMs,
                onSelectThreshold = viewModel::setThreshold,
                onComplete = { viewModel.completeOnboarding(onComplete) },
                onBack = { viewModel.previousStep() },
                isLoading = state.isCompleting
            )
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "logo_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F0E1A), Color(0xFF1A1040), Color(0xFF0F0E1A)))
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(60.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppLogo(size = 100.dp)

            Spacer(Modifier.height(24.dp))

            Text(
                "FocusLoop",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "Take back your attention.",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "FocusLoop notices when distraction turns into endless scrolling and gives you a gentle way back.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0AEC8),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            GradientButton(
                text = "Get Started",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                gradient = listOf(FocusPurple, FocusTeal)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Your data, your account · Never sold or shared",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B6880),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GoalsStep(
    goals: List<Goal>,
    onAddGoal: (String, String, Int) -> Unit,
    onRemoveGoal: (Goal) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var goalText by remember { mutableStateOf("") }
    var showInput by remember { mutableStateOf(false) }

    val suggestions = listOf(
        "Finish my coding project", "Study system design", "Exercise",
        "Read a book", "Apply for jobs", "Work on my business", "Learn something new"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        OnboardingHeader(step = 2, total = 6, onBack = onBack)

        Spacer(Modifier.height(32.dp))

        Text(
            "What are you trying to accomplish?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "FocusLoop will remind you of these when distraction strikes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Suggestions — always visible, toggle on/off, pick as many as you like
            item {
                Text(
                    "Suggestions:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            items(suggestions) { suggestion ->
                val addedGoal = goals.find { it.title == suggestion }
                SuggestionChip(
                    text = suggestion,
                    isSelected = addedGoal != null,
                    onClick = {
                        if (addedGoal != null) onRemoveGoal(addedGoal) else onAddGoal(suggestion, "", 0)
                    }
                )
            }

            // Custom goals (not part of the suggestion set)
            val customGoals = goals.filterNot { goal -> suggestions.any { it == goal.title } }
            if (customGoals.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Your own:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(customGoals) { goal ->
                    GoalChip(title = goal.title, onRemove = { onRemoveGoal(goal) })
                }
            }

            // Input field
            item {
                Spacer(Modifier.height(8.dp))
                if (showInput) {
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        label = { Text("Your goal") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (goalText.isNotBlank()) {
                                onAddGoal(goalText.trim(), "", 0)
                                goalText = ""
                                showInput = false
                            }
                        }),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (goalText.isNotBlank()) {
                                    onAddGoal(goalText.trim(), "", 0)
                                    goalText = ""
                                    showInput = false
                                }
                            }) {
                                Icon(FeatherIcons.Check, "Add")
                            }
                        }
                    )
                } else {
                    TextButton(
                        onClick = { showInput = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(FeatherIcons.Plus, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add custom goal")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        GradientButton(
            text = if (goals.isEmpty()) "Skip for now" else "Continue →",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GoalChip(title: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(FeatherIcons.CheckCircle, null, tint = FocusPurple, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(FeatherIcons.X, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SuggestionChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) FocusPurple else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        if (isSelected) {
            Icon(FeatherIcons.CheckCircle, null, tint = FocusPurple, modifier = Modifier.size(20.dp))
        } else {
            Icon(FeatherIcons.Plus, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TodosStep(
    todos: List<com.focusloop.app.domain.model.TodoItem>,
    onAddTodo: (String) -> Unit,
    onRemoveTodo: (com.focusloop.app.domain.model.TodoItem) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var todoText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        OnboardingHeader(step = 3, total = 6, onBack = onBack)

        Spacer(Modifier.height(32.dp))

        Text(
            "What's on your plate today?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Add as many to-dos as you want. You can always add more later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(todos, key = { it.id }) { todo ->
                GoalChip(title = todo.text, onRemove = { onRemoveTodo(todo) })
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = todoText,
                    onValueChange = { todoText = it },
                    label = { Text("Add a to-do") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (todoText.isNotBlank()) {
                            onAddTodo(todoText.trim())
                            todoText = ""
                        }
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (todoText.isNotBlank()) {
                                onAddTodo(todoText.trim())
                                todoText = ""
                            }
                        }) {
                            Icon(FeatherIcons.Plus, "Add")
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GradientButton(
            text = if (todos.isEmpty()) "Skip for now" else "Continue (${todos.size} added) →",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HobbiesStep(
    selectedHobbies: Set<String>,
    onToggleHobby: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var customHobby by remember { mutableStateOf("") }
    val predefinedNames = remember { com.focusloop.app.domain.model.HobbyTags.all.map { it.first }.toSet() }
    val customHobbies = selectedHobbies.filterNot { it in predefinedNames }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        OnboardingHeader(step = 4, total = 6, onBack = onBack)

        Spacer(Modifier.height(32.dp))

        Text(
            "What do you enjoy outside of work?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Pick as many as you like, or add your own — we'll use these to personalize your reset breaks.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(com.focusloop.app.domain.model.HobbyTags.all) { (hobby, icon) ->
                HobbySelectionCard(
                    hobby = hobby,
                    icon = icon,
                    isSelected = hobby in selectedHobbies,
                    onToggle = { onToggleHobby(hobby) }
                )
            }

            items(customHobbies) { hobby ->
                HobbySelectionCard(
                    hobby = hobby,
                    icon = FeatherIcons.Star,
                    isSelected = true,
                    onToggle = { onToggleHobby(hobby) }
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = customHobby,
                    onValueChange = { customHobby = it },
                    label = { Text("Add your own") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (customHobby.isNotBlank()) {
                            onToggleHobby(customHobby.trim())
                            customHobby = ""
                        }
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (customHobby.isNotBlank()) {
                                onToggleHobby(customHobby.trim())
                                customHobby = ""
                            }
                        }) {
                            Icon(FeatherIcons.Plus, "Add")
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GradientButton(
            text = if (selectedHobbies.isEmpty()) "Skip for now" else "Continue (${selectedHobbies.size} selected) →",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AppsStep(
    selectedPackages: Set<String>,
    onTogglePackage: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        OnboardingHeader(step = 5, total = 6, onBack = onBack)

        Spacer(Modifier.height(32.dp))

        Text(
            "Which apps steal your attention?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "FocusLoop will watch these for signs of endless scrolling.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(KnownDistractingApps.defaults) { app ->
                AppSelectionCard(
                    app = app,
                    isSelected = app.packageName in selectedPackages,
                    onToggle = { onTogglePackage(app.packageName) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GradientButton(
            text = if (selectedPackages.isEmpty()) "Skip for now" else "Continue (${selectedPackages.size} selected) →",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TimingStep(
    selectedThresholdMs: Long,
    onSelectThreshold: (Long) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
) {
    val options = listOf(
        Triple("Gentle", "10 minutes", 10 * 60 * 1000L),
        Triple("Balanced", "5 minutes", 5 * 60 * 1000L),
        Triple("Strong", "2 minutes", 2 * 60 * 1000L)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        OnboardingHeader(step = 6, total = 6, onBack = onBack)

        Spacer(Modifier.height(32.dp))

        Text(
            "When should FocusLoop step in?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "After this much time in a distracting app, you'll get a gentle nudge.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        options.forEach { (label, sublabel, ms) ->
            TimingOption(
                label = label,
                sublabel = sublabel,
                isSelected = selectedThresholdMs == ms,
                onSelect = { onSelectThreshold(ms) }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "You're all set.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "FocusLoop will keep an eye out — never judging, always supporting.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = if (isLoading) "Setting up..." else "Start Protecting My Focus",
            onClick = { if (!isLoading) onComplete() },
            modifier = Modifier.fillMaxWidth(),
            icon = if (isLoading) null else FeatherIcons.ArrowRight
        )
    }
}

@Composable
private fun TimingOption(label: String, sublabel: String, isSelected: Boolean, onSelect: () -> Unit) {
    val borderColor = if (isSelected) FocusPurple else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onSelect)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(sublabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(50))
                    .background(FocusPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(FeatherIcons.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun OnboardingHeader(step: Int, total: Int, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(FeatherIcons.ArrowLeft, "Back")
        }
        Spacer(Modifier.width(8.dp))
        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i + 1 == step) 20.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (i + 1 == step) FocusPurple else MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}
