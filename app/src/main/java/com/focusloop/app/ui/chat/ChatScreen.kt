package com.focusloop.app.ui.chat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.repository.ChatRepository
import com.focusloop.app.data.repository.GoalRepository
import com.focusloop.app.data.repository.UserDataRepository
import com.focusloop.app.domain.model.ChatMessage
import com.focusloop.app.domain.model.Goal
import com.focusloop.app.domain.model.UserSettings
import com.focusloop.app.ui.theme.FocusPurple
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Send
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val settingsDataStore: UserDataRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    text = "Hey! I'm your FocusLoop coach. Tell me what's on your mind — I can suggest a quick activity, help you plan your day, or just chat about your goals.",
                    isUser = false
                )
            )
        )
    )
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _state.value.isSending) return

        val historyBeforeSend = _state.value.messages
        val userMsg = ChatMessage(text = trimmed, isUser = true)
        _state.update { it.copy(messages = it.messages + userMsg, isSending = true, error = null) }

        viewModelScope.launch {
            try {
                val settings = settingsDataStore.settings.first()
                val topGoal = goalRepository.getTopGoal()
                val persona = buildPersona(settings, topGoal)
                val reply = chatRepository.sendMessage(persona, historyBeforeSend, trimmed)
                _state.update { it.copy(messages = it.messages + ChatMessage(text = reply, isUser = false), isSending = false) }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "sendMessage failed", e)
                _state.update {
                    it.copy(isSending = false, error = "Couldn't reach the assistant — check your connection and try again.")
                }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}

private fun buildPersona(settings: UserSettings, topGoal: Goal?): String {
    val hobbies = settings.hobbies.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "not specified yet"
    val todos = settings.todos.filterNot { it.completed }.take(5).joinToString("; ") { it.text }
        .ifBlank { "none right now" }
    val goal = topGoal?.title ?: "no active goal set"
    return """
        You are the FocusLoop coach — a warm, encouraging companion inside a screen-time and focus app.
        You help this person build better habits, suggest realistic schedules, and recommend quick
        activities tied to their interests instead of doomscrolling.
        Keep replies short (2-4 sentences), calm, and conversational — never preachy or clinical.

        About this person:
        - Hobbies/interests: $hobbies
        - Top goal: $goal
        - Open to-dos: $todos

        Use this context naturally when it's relevant, but don't force it into every reply.
    """.trimIndent()
}

@Composable
fun ChatScreen(viewModel: ChatViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.messages.size, state.isSending) {
        val lastIndex = state.messages.size + (if (state.isSending) 1 else 0) - 1
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                }
                Column {
                    Text("Coach", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Your personal focus companion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(state.messages, key = { _, msg -> msg.id }) { _, msg ->
                ChatBubble(msg)
            }
            if (state.isSending) {
                item { TypingBubble() }
            }
        }

        if (state.error != null) {
            Text(
                state.error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ask your coach anything…") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input)
                        input = ""
                    }
                },
                enabled = !state.isSending && input.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = FocusPurple)
            ) {
                Icon(FeatherIcons.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (message.isUser) 18.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 18.dp
                    )
                )
                .background(if (message.isUser) FocusPurple else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TypingBubble() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { index ->
                    BouncingDot(delayMillis = index * 150)
                }
            }
        }
    }
}

@Composable
private fun BouncingDot(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typing_dot")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_offset"
    )
    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(7.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant)
    )
}
