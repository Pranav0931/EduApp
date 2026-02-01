package com.hdaf.eduapp.presentation.home

import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.Book
import com.hdaf.eduapp.domain.model.Chapter
import com.hdaf.eduapp.domain.model.UserProgress
import com.hdaf.eduapp.presentation.base.UiEvent
import com.hdaf.eduapp.presentation.base.UiState

/**
 * UI State for Home Screen.
 * Follows UDF (Unidirectional Data Flow) pattern.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userClass: Int = 1,
    val level: Int = 1,
    val totalXp: Int = 0,
    val streak: Int = 0,
    val userProgress: UserProgress? = null,
    val recentChapters: List<Chapter> = emptyList(),
    val recommendedBooks: List<Book> = emptyList(),
    val dailyGoalProgress: Float = 0f,
    val isOfflineMode: Boolean = false,
    val error: String? = null
) : UiState

/**
 * UI Events that can be triggered from Home Screen.
 */
sealed class HomeUiEvent : UiEvent {
    data class NavigateToBook(val bookId: String, val bookTitle: String = "") : HomeUiEvent()
    data class NavigateToChapter(val chapterId: String) : HomeUiEvent()
    data class NavigateToQuiz(val quizId: String) : HomeUiEvent()
    data object NavigateToProfile : HomeUiEvent()
    data object NavigateToLeaderboard : HomeUiEvent()
    data object NavigateToSettings : HomeUiEvent()
    data class ShowSnackbar(val message: String) : HomeUiEvent()
    data class ShowBadgeEarned(val badge: Badge) : HomeUiEvent()
    data class ShowLevelUp(val newLevel: Int) : HomeUiEvent()
    data class ShowError(val message: String) : HomeUiEvent()
    data object ShowStreakCelebration : HomeUiEvent()
}

/**
 * User actions from Home Screen.
 */
sealed class HomeAction {
    data object LoadData : HomeAction()
    data object Refresh : HomeAction()
    data object RefreshData : HomeAction()
    data object ClearError : HomeAction()
    data class SelectBook(val bookId: String) : HomeAction()
    data class ContinueReading(val chapterId: String) : HomeAction()
    data object OpenProfile : HomeAction()
    data object OpenLeaderboard : HomeAction()
    data object OpenSettings : HomeAction()
    data object DismissError : HomeAction()
}
