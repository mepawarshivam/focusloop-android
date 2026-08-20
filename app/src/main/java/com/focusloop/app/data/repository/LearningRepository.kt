package com.focusloop.app.data.repository

import com.focusloop.app.data.local.dao.LearningQuestionDao
import com.focusloop.app.data.local.entity.LearningAttemptEntity
import com.focusloop.app.data.local.entity.LearningQuestionEntity
import com.focusloop.app.domain.model.*

class LearningRepository(private val dao: LearningQuestionDao) {

    suspend fun getRandomQuestion(): LearningQuestion? =
        dao.getRandom()?.toDomain()

    suspend fun getRandomQuestionByCategory(category: QuestionCategory): LearningQuestion? =
        dao.getRandomByCategory(category.name)?.toDomain()

    suspend fun recordAttempt(questionId: Long, correct: Boolean): Int {
        val xp = if (correct) 10 else 2 // Always give some XP for trying
        dao.insert(LearningAttemptEntity(
            questionId = questionId,
            answeredCorrectly = correct,
            xpEarned = xp
        ))
        return xp
    }

    private fun LearningQuestionEntity.toDomain() = LearningQuestion(
        id = id,
        category = QuestionCategory.valueOf(category),
        question = question,
        options = listOf(option1, option2, option3, option4),
        correctAnswer = correctAnswer,
        explanation = explanation,
        difficulty = Difficulty.valueOf(difficulty)
    )
}
