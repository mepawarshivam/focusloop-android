package com.focusloop.app.data.local.dao

import androidx.room.*
import com.focusloop.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE completed = 0 ORDER BY priority ASC, createdAt DESC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY priority ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE completed = 0 ORDER BY priority ASC LIMIT 1")
    suspend fun getTopGoal(): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)
}

@Dao
interface DistractionSessionDao {
    @Query("SELECT * FROM distraction_sessions WHERE startedAt >= :since ORDER BY startedAt DESC")
    fun getSessionsSince(since: Long): Flow<List<DistractionSessionEntity>>

    @Query("SELECT SUM(durationMs) FROM distraction_sessions WHERE startedAt >= :since")
    suspend fun getTotalDistractionMsSince(since: Long): Long?

    @Query("SELECT packageName, appName, SUM(durationMs) as totalMs FROM distraction_sessions WHERE startedAt >= :since GROUP BY packageName ORDER BY totalMs DESC LIMIT 5")
    suspend fun getTopDistractingAppsSince(since: Long): List<AppUsageSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: DistractionSessionEntity): Long

    @Update
    suspend fun update(session: DistractionSessionEntity)
}

data class AppUsageSummary(val packageName: String, val appName: String, val totalMs: Long)

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE startedAt >= :since AND completed = 1")
    suspend fun getCompletedSessionCountSince(since: Long): Int

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE startedAt >= :since AND completed = 1")
    suspend fun getTotalFocusMinutesSince(since: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity): Long

    @Update
    suspend fun update(session: FocusSessionEntity)
}

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getLastSevenDays(): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: Long): DailyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: DailyStatsEntity): Long

    @Update
    suspend fun update(stats: DailyStatsEntity)
}

@Dao
interface LearningQuestionDao {
    @Query("SELECT * FROM learning_questions WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByCategory(category: String): LearningQuestionEntity?

    @Query("SELECT * FROM learning_questions ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandom(): LearningQuestionEntity?

    @Query("SELECT COUNT(*) FROM learning_questions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<LearningQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: LearningAttemptEntity): Long
}
