package com.focusloop.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusloop.app.data.datastore.SettingsDataStore
import com.focusloop.app.data.repository.GoalRepository
import com.focusloop.app.domain.model.Goal
import com.focusloop.app.ui.components.GradientButton
import com.focusloop.app.ui.theme.FocusPurple
import compose.icons.FeatherIcons
import compose.icons.feathericons.Plus
import compose.icons.feathericons.X
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddGoalViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var estimatedMinutes by mutableStateOf(0)

    fun save(onSaved: () -> Unit) {
        if (title.isBlank()) return
        viewModelScope.launch {
            goalRepository.addGoal(
                Goal(title = title.trim(), description = description.trim(), estimatedMinutes = estimatedMinutes)
            )
            onSaved()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(viewModel: AddGoalViewModel, onBack: () -> Unit) {
    val durations = listOf(0 to "No limit", 15 to "15 min", 30 to "30 min", 60 to "1 hour", 90 to "1.5 hours", 120 to "2 hours")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Goal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FeatherIcons.X, "Close") }
                },
                actions = {
                    TextButton(onClick = { viewModel.save(onBack) }) {
                        Text("Save", color = FocusPurple, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("What do you want to accomplish?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )

            Text("Target time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durations.forEach { (minutes, label) ->
                    FilterChip(
                        selected = viewModel.estimatedMinutes == minutes,
                        onClick = { viewModel.estimatedMinutes = minutes },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = "Add Goal",
                onClick = { viewModel.save(onBack) },
                modifier = Modifier.fillMaxWidth(),
                icon = FeatherIcons.Plus
            )
        }
    }
}
