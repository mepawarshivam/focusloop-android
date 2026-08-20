package com.focusloop.app.data.repository

import com.focusloop.app.data.local.dao.*
import com.focusloop.app.data.local.entity.*
import com.focusloop.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepository(
    private val distractionDao: DistractionSessionDao,
    private val focusDao: FocusSessionDao,
    private val dailyStatsDao: DailyStatsDao
) {
    fun getDistractionSessionsSince(since: Long): Flow<List<DistractionSession>> =
        distractionDao.getSessionsSince(since).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getTotalDistractionMsSince(since: Long): Long =
        distractionDao.getTotalDistractionMsSince(since) ?: 0L

    suspend fun getTopDistractingApps(since: Long): List<AppUsageSummary> =
        distractionDao.getTopDistractingAppsSince(since)

    suspend fun saveDistractionSession(session: DistractionSession): Long =
        distractionDao.insert(session.toEntity())

    suspend fun updateDistractionSession(session: DistractionSession) =
        distractionDao.update(session.toEntity())

    fun getAllFocusSessions(): Flow<List<FocusSession>> =
        focusDao.getAllSessions().map { list -> list.map { it.toDomain() } }

    suspend fun getCompletedFocusSessionCount(since: Long): Int =
        focusDao.getCompletedSessionCountSince(since)

    suspend fun getTotalFocusMinutes(since: Long): Int =
        focusDao.getTotalFocusMinutesSince(since) ?: 0

    suspend fun saveFocusSession(session: FocusSession): Long =
        focusDao.insert(session.toEntity())

    suspend fun updateFocusSession(session: FocusSession) =
        focusDao.update(session.toEntity())

    fun getLastSevenDaysStats(): Flow<List<DailyStats>> =
        dailyStatsDao.getLastSevenDays().map { list -> list.map { it.toDomain() } }

    suspend fun updateDailyStats(date: Long, distractionMs: Long = 0, focusMs: Long = 0, interventions: Int = 0, sessions: Int = 0) {
        val existing = dailyStatsDao.getForDate(date)
        if (existing != null) {
            dailyStatsDao.update(existing.copy(
                totalDistractionMs = existing.totalDistractionMs + distractionMs,
                totalFocusMs = existing.totalFocusMs + focusMs,
                interventionCount = existing.interventionCount + interventions,
                sessionsCompleted = existing.sessionsCompleted + sessions
            ))
        } else {
            dailyStatsDao.insert(DailyStatsEntity(
                date = date,
                totalDistractionMs = distractionMs,
                totalFocusMs = focusMs,
                interventionCount = interventions,
                sessionsCompleted = sessions
            ))
        }
    }

    private fun DistractionSessionEntity.toDomain() = DistractionSession(
        id = id, packageName = packageName, appName = appName,
        startedAt = startedAt, endedAt = endedAt, durationMs = durationMs,
        interventionTriggered = interventionTriggered
    )

    private fun DistractionSession.toEntity() = DistractionSessionEntity(
        id = id, packageName = packageName, appName = appName,
        startedAt = startedAt, endedAt = endedAt, durationMs = durationMs,
        interventionTriggered = interventionTriggered
    )

    private fun FocusSessionEntity.toDomain() = FocusSession(
        id = id, goalId = goalId, goalTitle = goalTitle,
        startedAt = startedAt, endedAt = endedAt, durationMinutes = durationMinutes, completed = completed
    )

    private fun FocusSession.toEntity() = FocusSessionEntity(
        id = id, goalId = goalId, goalTitle = goalTitle,
        startedAt = startedAt, endedAt = endedAt, durationMinutes = durationMinutes, completed = completed
    )

    private fun DailyStatsEntity.toDomain() = DailyStats(
        id = id, date = date, totalDistractionMs = totalDistractionMs,
        totalFocusMs = totalFocusMs, interventionCount = interventionCount,
        sessionsCompleted = sessionsCompleted, focusXpEarned = focusXpEarned, learningXpEarned = learningXpEarned
    )
}
