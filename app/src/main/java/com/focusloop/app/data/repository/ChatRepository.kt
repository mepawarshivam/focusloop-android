package com.focusloop.app.data.repository

import com.focusloop.app.domain.model.ChatMessage
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.TextPart

private const val MODEL_NAME = "gemini-3.6-flash"

/**
 * Wraps Firebase AI Logic's free-tier Gemini backend (GenerativeBackend.googleAI()).
 * Each send starts a fresh Chat seeded with the running history plus a persona
 * turn, since that's a simpler, more robust seam than relying on an
 * unverified system-instruction constructor parameter.
 */
class ChatRepository {

    private val generativeModel by lazy {
        FirebaseAI.getInstance(backend = GenerativeBackend.googleAI()).generativeModel(MODEL_NAME)
    }

    suspend fun sendMessage(persona: String, history: List<ChatMessage>, userMessage: String): String {
        val seededHistory = buildList {
            add(Content("user", listOf(TextPart(persona))))
            add(Content("model", listOf(TextPart("Got it — I'm ready to help."))))
            history.forEach { msg ->
                add(Content(if (msg.isUser) "user" else "model", listOf(TextPart(msg.text))))
            }
        }
        val chat = generativeModel.startChat(seededHistory)
        val response = chat.sendMessage(userMessage)
        return response.text?.takeIf { it.isNotBlank() }
            ?: "I couldn't come up with a reply just now — mind trying again?"
    }
}
