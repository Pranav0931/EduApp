package com.hdaf.eduapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.Badge
import com.hdaf.eduapp.domain.model.BadgeCategory
import com.hdaf.eduapp.domain.model.UserProfile
import com.hdaf.eduapp.domain.model.UserStats
import com.hdaf.eduapp.domain.usecase.user.GetUserBadgesUseCase
import com.hdaf.eduapp.domain.usecase.user.GetUserProfileUseCase
import com.hdaf.eduapp.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getUserBadgesUseCase: GetUserBadgesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Load profile automatically when ViewModel is created
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load user profile
            try {
                getUserProfileUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Timber.d("Profile loaded successfully: ${result.data}")
                            _uiState.update { it.copy(user = result.data, error = null) }
                        }
                        is Resource.Error -> {
                            Timber.e("Profile load error: ${result.message}")
                            // Use default profile instead of showing error
                            _uiState.update { 
                                it.copy(
                                    user = createDefaultProfile(),
                                    error = null // Don't show error, show default instead
                                ) 
                            }
                        }
                        is Resource.Loading -> {
                            // Continue loading
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading profile")
                _uiState.update { it.copy(user = createDefaultProfile()) }
            }
        }

        viewModelScope.launch {
            // Load stats
            try {
                getUserStatsUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(stats = result.data) }
                        }
                        is Resource.Error -> {
                            // Use default stats
                            _uiState.update { it.copy(stats = createDefaultStats()) }
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading stats")
                _uiState.update { it.copy(stats = createDefaultStats()) }
            }
        }

        viewModelScope.launch {
            // Load badges
            try {
                getUserBadgesUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    badges = result.data ?: getDefaultBadges()
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    badges = getDefaultBadges()
                                ) 
                            }
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading badges")
                _uiState.update { 
                    it.copy(isLoading = false, badges = getDefaultBadges()) 
                }
            }
        }
    }
    
    /**
     * Create a default user profile for new users or when data is unavailable
     */
    private fun createDefaultProfile(): UserProfile {
        return UserProfile(
            id = "guest",
            name = "Student",
            phone = "",
            avatarUrl = null,
            classLevel = 1,
            medium = "hi",
            xp = 0,
            level = 1,
            xpToNextLevel = 100,
            streakDays = 0,
            leaderboardRank = 0
        )
    }
    
    /**
     * Create default stats for new users
     */
    private fun createDefaultStats(): UserStats {
        return UserStats(
            currentStreak = 0,
            longestStreak = 0,
            booksCompleted = 0,
            chaptersCompleted = 0,
            quizzesCompleted = 0,
            totalMinutesLearned = 0,
            leaderboardRank = 0,
            totalXpEarned = 0,
            averageQuizScore = 0
        )
    }
    
    /**
     * Get default badges for gamification display
     */
    private fun getDefaultBadges(): List<Badge> {
        return listOf(
            Badge(
                id = "first_steps",
                name = "First Steps",
                description = "Complete your first chapter",
                iconUrl = "badge_first_steps",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.LEARNING
            ),
            Badge(
                id = "quiz_master",
                name = "Quiz Master",
                description = "Get 100% on a quiz",
                iconUrl = "badge_quiz_master",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.QUIZ
            ),
            Badge(
                id = "streak_3",
                name = "3-Day Streak",
                description = "Learn for 3 days in a row",
                iconUrl = "badge_streak",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.STREAK
            ),
            Badge(
                id = "bookworm",
                name = "Bookworm",
                description = "Complete a full book",
                iconUrl = "badge_bookworm",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.ACHIEVEMENT
            ),
            Badge(
                id = "early_bird",
                name = "Early Bird",
                description = "Study before 8 AM",
                iconUrl = "badge_early_bird",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.ACHIEVEMENT
            ),
            Badge(
                id = "night_owl",
                name = "Night Owl",
                description = "Study after 9 PM",
                iconUrl = "badge_night_owl",
                isUnlocked = false,
                unlockedAt = null,
                category = BadgeCategory.ACHIEVEMENT
            )
        )
    }

    fun retry() {
        loadProfile()
    }
}

data class ProfileUiState(
    val isLoading: Boolean = true, // Start with loading true
    val user: UserProfile? = null,
    val stats: UserStats? = null,
    val badges: List<Badge> = emptyList(),
    val error: String? = null
)
