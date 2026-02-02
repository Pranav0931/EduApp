package com.hdaf.eduapp.data.local.dao

import androidx.room.*
import com.hdaf.eduapp.data.local.entity.StudySessionEntity
import com.hdaf.eduapp.data.local.entity.StudyPlanEntity
import com.hdaf.eduapp.data.local.entity.StudyGoalEntity
import com.hdaf.eduapp.data.local.entity.DailyStudySummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for study session operations.
 */
@Dao
interface StudySessionDao {
    
    @Query("SELECT * FROM study_sessions ORDER BY scheduledDate DESC, startTime ASC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE scheduledDate = :date ORDER BY startTime ASC")
    fun getSessionsByDate(date: Long): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE scheduledDate >= :startDate AND scheduledDate <= :endDate ORDER BY scheduledDate ASC, startTime ASC")
    fun getSessionsInRange(startDate: Long, endDate: Long): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE chapterId = :chapterId ORDER BY scheduledDate DESC")
    fun getSessionsByChapter(chapterId: String): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY scheduledDate DESC")
    fun getSessionsBySubject(subjectId: String): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE isCompleted = 0 AND scheduledDate >= :today ORDER BY scheduledDate ASC, startTime ASC")
    fun getUpcomingSessions(today: Long): Flow<List<StudySessionEntity>>
    
    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): StudySessionEntity?
    
    @Query("SELECT * FROM study_sessions WHERE isCompleted = 0 AND scheduledDate = :date ORDER BY startTime ASC LIMIT 1")
    suspend fun getNextSessionForToday(date: Long): StudySessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySessionEntity>)
    
    @Update
    suspend fun updateSession(session: StudySessionEntity)
    
    @Query("UPDATE study_sessions SET isCompleted = 1, actualDurationMinutes = :actualDuration, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun markSessionCompleted(sessionId: String, actualDuration: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteSession(session: StudySessionEntity)
    
    @Query("DELETE FROM study_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)
    
    @Query("DELETE FROM study_sessions WHERE scheduledDate < :date AND isCompleted = 1")
    suspend fun deleteOldCompletedSessions(date: Long)
    
    @Query("SELECT SUM(CASE WHEN isCompleted = 1 THEN COALESCE(actualDurationMinutes, durationMinutes) ELSE 0 END) FROM study_sessions WHERE scheduledDate >= :startDate AND scheduledDate <= :endDate")
    suspend fun getTotalStudyMinutesInRange(startDate: Long, endDate: Long): Int?
    
    @Query("SELECT COUNT(*) FROM study_sessions WHERE isCompleted = 1 AND scheduledDate >= :startDate AND scheduledDate <= :endDate")
    suspend fun getCompletedSessionCountInRange(startDate: Long, endDate: Long): Int
}

/**
 * DAO for study plan operations.
 */
@Dao
interface StudyPlanDao {
    
    @Query("SELECT * FROM study_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<StudyPlanEntity>>
    
    @Query("SELECT * FROM study_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlan(): Flow<StudyPlanEntity?>
    
    @Query("SELECT * FROM study_plans WHERE id = :planId")
    suspend fun getPlanById(planId: String): StudyPlanEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlanEntity)
    
    @Update
    suspend fun updatePlan(plan: StudyPlanEntity)
    
    @Query("UPDATE study_plans SET isActive = 0")
    suspend fun deactivateAllPlans()
    
    @Query("UPDATE study_plans SET isActive = 1 WHERE id = :planId")
    suspend fun activatePlan(planId: String)
    
    @Delete
    suspend fun deletePlan(plan: StudyPlanEntity)
    
    @Query("DELETE FROM study_plans WHERE id = :planId")
    suspend fun deletePlanById(planId: String)
}

/**
 * DAO for study goal operations.
 */
@Dao
interface StudyGoalDao {
    
    @Query("SELECT * FROM study_goals ORDER BY targetDate ASC")
    fun getAllGoals(): Flow<List<StudyGoalEntity>>
    
    @Query("SELECT * FROM study_goals WHERE isCompleted = 0 ORDER BY targetDate ASC")
    fun getActiveGoals(): Flow<List<StudyGoalEntity>>
    
    @Query("SELECT * FROM study_goals WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedGoals(): Flow<List<StudyGoalEntity>>
    
    @Query("SELECT * FROM study_goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): StudyGoalEntity?
    
    @Query("SELECT * FROM study_goals WHERE isCompleted = 0 AND targetDate >= :today ORDER BY targetDate ASC LIMIT 1")
    suspend fun getNextActiveGoal(today: Long): StudyGoalEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: StudyGoalEntity)
    
    @Update
    suspend fun updateGoal(goal: StudyGoalEntity)
    
    @Query("UPDATE study_goals SET progressMinutes = :progressMinutes, progressChapters = :progressChapters, progressQuizScore = :progressQuizScore WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: String, progressMinutes: Int, progressChapters: Int, progressQuizScore: Int)
    
    @Query("UPDATE study_goals SET isCompleted = 1, completedAt = :completedAt WHERE id = :goalId")
    suspend fun markGoalCompleted(goalId: String, completedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteGoal(goal: StudyGoalEntity)
    
    @Query("DELETE FROM study_goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: String)
}

/**
 * DAO for daily study summary operations.
 */
@Dao
interface DailyStudySummaryDao {
    
    @Query("SELECT * FROM daily_study_summaries ORDER BY date DESC")
    fun getAllSummaries(): Flow<List<DailyStudySummaryEntity>>
    
    @Query("SELECT * FROM daily_study_summaries WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getSummariesInRange(startDate: Long, endDate: Long): Flow<List<DailyStudySummaryEntity>>
    
    @Query("SELECT * FROM daily_study_summaries WHERE date = :date")
    suspend fun getSummaryByDate(date: Long): DailyStudySummaryEntity?
    
    @Query("SELECT * FROM daily_study_summaries ORDER BY date DESC LIMIT :days")
    suspend fun getRecentSummaries(days: Int): List<DailyStudySummaryEntity>
    
    @Query("SELECT MAX(streak) FROM daily_study_summaries")
    suspend fun getMaxStreak(): Int?
    
    @Query("SELECT streak FROM daily_study_summaries WHERE date = :date")
    suspend fun getCurrentStreak(date: Long): Int?
    
    @Query("SELECT SUM(totalMinutesStudied) FROM daily_study_summaries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalMinutesInRange(startDate: Long, endDate: Long): Int?
    
    @Query("SELECT AVG(averageQuizScore) FROM daily_study_summaries WHERE date >= :startDate AND date <= :endDate AND quizzesCompleted > 0")
    suspend fun getAverageQuizScoreInRange(startDate: Long, endDate: Long): Float?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailyStudySummaryEntity)
    
    @Update
    suspend fun updateSummary(summary: DailyStudySummaryEntity)
    
    @Query("DELETE FROM daily_study_summaries WHERE date < :beforeDate")
    suspend fun deleteOldSummaries(beforeDate: Long)
}
