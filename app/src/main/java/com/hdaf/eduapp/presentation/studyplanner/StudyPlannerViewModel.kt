package com.hdaf.eduapp.presentation.studyplanner

import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.data.local.dao.DailyStudySummaryDao
import com.hdaf.eduapp.data.local.dao.HomeworkReminderDao
import com.hdaf.eduapp.data.local.dao.StudyGoalDao
import com.hdaf.eduapp.data.local.dao.StudySessionDao
import com.hdaf.eduapp.data.local.entity.DailyStudySummaryEntity
import com.hdaf.eduapp.data.local.entity.HomeworkReminderEntity
import com.hdaf.eduapp.data.local.entity.StudyGoalEntity
import com.hdaf.eduapp.data.local.entity.StudySessionEntity
import com.hdaf.eduapp.presentation.base.BaseViewModel
import com.hdaf.eduapp.presentation.base.UiEvent
import com.hdaf.eduapp.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for Study Planner screen.
 * Manages study goals, homework reminders, study sessions and daily summaries.
 */
@HiltViewModel
class StudyPlannerViewModel @Inject constructor(
    private val studyGoalDao: StudyGoalDao,
    private val homeworkReminderDao: HomeworkReminderDao,
    private val studySessionDao: StudySessionDao,
    private val dailyStudySummaryDao: DailyStudySummaryDao
) : BaseViewModel<StudyPlannerUiState, StudyPlannerUiEvent>(StudyPlannerUiState()) {

    // Placeholder userId for homework reminders (which require userId)
    private val userId: String = "current_user"

    init {
        loadData()
    }

    private fun loadData() {
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            try {
                homeworkReminderDao.getPendingReminders(userId).collect { reminders ->
                    setState { copy(homeworkReminders = reminders, isLoading = false) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading homework reminders")
                setState { copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            try {
                studyGoalDao.getActiveGoals().collect { goals ->
                    setState { copy(studyGoals = goals) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading study goals")
            }
        }

        viewModelScope.launch {
            try {
                val todayStart = getTodayStartMillis()
                studySessionDao.getSessionsByDate(todayStart).collect { sessions ->
                    setState { copy(recentSessions = sessions) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading study sessions")
            }
        }

        viewModelScope.launch {
            try {
                val todayStart = getTodayStartMillis()
                val summary = dailyStudySummaryDao.getSummaryByDate(todayStart)
                setState {
                    copy(
                        todayMinutesStudied = summary?.totalMinutesStudied ?: 0,
                        todayChaptersCompleted = summary?.chaptersCompleted ?: 0
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading daily summary")
            }
        }
    }

    fun completeHomework(id: String) {
        viewModelScope.launch {
            try {
                homeworkReminderDao.markAsCompleted(id, System.currentTimeMillis())
                sendEvent(StudyPlannerUiEvent.HomeworkCompleted)
            } catch (e: Exception) {
                Timber.e(e, "Error completing homework")
            }
        }
    }

    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

// ==================== UI State ====================

data class StudyPlannerUiState(
    val isLoading: Boolean = true,
    val studyGoals: List<StudyGoalEntity> = emptyList(),
    val homeworkReminders: List<HomeworkReminderEntity> = emptyList(),
    val recentSessions: List<StudySessionEntity> = emptyList(),
    val todayMinutesStudied: Int = 0,
    val todayChaptersCompleted: Int = 0
) : UiState {
    val goalProgressPercent: Int
        get() {
            if (studyGoals.isEmpty()) return 0
            val completed = studyGoals.count { it.isCompleted }
            return (completed * 100) / studyGoals.size
        }
}

// ==================== UI Events ====================

sealed class StudyPlannerUiEvent : UiEvent {
    data object HomeworkCompleted : StudyPlannerUiEvent()
}
