package com.focusloop.app.data.repository

import com.focusloop.app.domain.model.Flashcard
import com.focusloop.app.domain.model.ReflectionPrompt
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.GenerativeBackend

private const val MODEL_NAME = "gemini-3.6-flash"
private const val FLASHCARD_COUNT = 5

/**
 * Generates short, personalized microlearning content (flashcards, reflection
 * prompts) via Gemini. Content is plain pipe/line-delimited text rather than
 * JSON — simpler to parse reliably from a small model without a strict
 * schema, and resilient to minor formatting drift.
 */
class MicrolearningRepository {

    private val generativeModel by lazy {
        FirebaseAI.getInstance(backend = GenerativeBackend.googleAI()).generativeModel(MODEL_NAME)
    }

    suspend fun generateFlashcards(topic: String): List<Flashcard> {
        val prompt = """
            Create exactly $FLASHCARD_COUNT short, genuinely interesting flashcards about "$topic"
            for someone taking a two-minute break from doomscrolling. Each card should teach one
            real, specific, memorable fact or tip — not generic advice.

            Respond with EXACTLY $FLASHCARD_COUNT lines, one flashcard per line, formatted as:
            front text|||back text
            The front is a short question or prompt (under 10 words). The back is the answer or
            fact (under 30 words). No numbering, no markdown, no extra commentary, no blank lines.
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        return response.text.orEmpty().lines()
            .mapNotNull { line ->
                val parts = line.split("|||")
                if (parts.size == 2) Flashcard(parts[0].trim(), parts[1].trim()) else null
            }
            .take(FLASHCARD_COUNT)
    }

    suspend fun generateReflection(topic: String): ReflectionPrompt {
        val prompt = """
            Share one short, genuinely interesting insight (2-3 sentences, under 60 words) about
            "$topic" that could reframe how someone spends the next few minutes instead of
            scrolling their phone. Then ask one short, thought-provoking reflection question
            (under 15 words) tied to that insight.

            Respond in EXACTLY this two-line format, no markdown, no extra commentary:
            INSIGHT: <the insight>
            QUESTION: <the question>
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        val text = response.text.orEmpty()
        val insight = Regex("INSIGHT:\\s*(.+)").find(text)?.groupValues?.get(1)?.trim().orEmpty()
        val question = Regex("QUESTION:\\s*(.+)").find(text)?.groupValues?.get(1)?.trim().orEmpty()
        return ReflectionPrompt(
            insight = insight.ifBlank { "Every break is a chance to learn something small." },
            question = question.ifBlank { "What's one thing you're curious about right now?" }
        )
    }
}
