package com.hdaf.eduapp.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.TTSManager
import com.hdaf.eduapp.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * User profile screen showing stats, badges, and achievements.
 * Features colorful gradient design with full TalkBack accessibility.
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var badgeAdapter: BadgeAdapter
    private var ttsManager: TTSManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsManager = TTSManager.getInstance()

        setupBadgesRecyclerView()
        setupClickListeners()
        observeUiState()
        
        // Announce screen for accessibility
        view.announceForAccessibility(getString(R.string.profile_accessibility_intro))
    }

    private fun speakText(text: String) {
        try {
            if (ttsManager?.isReady == true) {
                ttsManager?.speak(text)
            }
        } catch (e: Exception) {
            Timber.e(e, "TTS error")
        }
    }

    private fun setupBadgesRecyclerView() {
        badgeAdapter = BadgeAdapter { badge ->
            // Show badge details with voice feedback
            speakText("${badge.name}. ${badge.description}")
            Snackbar.make(binding.root, badge.description, Snackbar.LENGTH_SHORT).show()
        }

        binding.rvBadges.apply {
            adapter = badgeAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            
            // Accessibility: Set content description for the grid
            contentDescription = getString(R.string.badges_grid)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Stats section click listeners with null safety
            layoutStreak?.setOnClickListener {
                val streakText = tvStreakCount?.text?.toString() ?: "0"
                speakText("Your streak is $streakText days. Keep learning!")
            }

            layoutQuizzes?.setOnClickListener {
                val quizzesText = tvQuizzesCompleted?.text?.toString() ?: "0"
                speakText("You have completed $quizzesText quizzes.")
            }

            layoutBadgesCount?.setOnClickListener {
                val badgesText = tvBadgesCount?.text?.toString() ?: "0"
                speakText("You have earned $badgesText badges.")
                try {
                    findNavController().navigate(R.id.badgesFragment)
                } catch (e: Exception) {
                    Timber.e(e, "Navigation error to badges")
                }
            }

            cardDailyChallenge?.setOnClickListener {
                val challengeDesc = tvChallengeDescription?.text?.toString() ?: "Daily challenge"
                val xpText = tvChallengeXp?.text?.toString() ?: "+75 XP"
                speakText("Daily Challenge: $challengeDesc. Reward: $xpText")
            }

            btnViewAllBadges?.setOnClickListener {
                speakText("Opening all badges")
                try {
                    findNavController().navigate(R.id.badgesFragment)
                } catch (e: Exception) {
                    Timber.e(e, "Navigation error to badges")
                }
            }

            btnDownloads.setOnClickListener {
                speakText("Opening downloads")
                Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
            }

            btnHistory.setOnClickListener {
                speakText("Opening learning history")
                Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: ProfileUiState) {
        binding.apply {
            // Show/hide loading indicator
            progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            // Show main content when not loading
            val contentAlpha = if (state.isLoading) 0.5f else 1.0f
            (root as? ViewGroup)?.getChildAt(0)?.alpha = contentAlpha

            // Update user profile section - always show something
            val user = state.user
            val level = user?.level ?: 1
            val xp = user?.xp ?: 0
            val xpToNext = user?.xpToNextLevel ?: 100
            
            // Level display (colorful design)
            tvLevelNumber?.text = level.toString()
            tvLevel?.text = getLevelTitle(level)

            // XP display
            tvXp?.text = getString(R.string.xp_format, xp)
            val xpRemaining = maxOf(0, xpToNext - (xp % maxOf(1, xpToNext)))
            tvXpNeeded?.text = "$xpRemaining XP to next level"
            
            progressXp?.max = maxOf(1, xpToNext)
            progressXp?.progress = xp % maxOf(1, xpToNext)

            // Stats section - always show values (default to 0)
            val stats = state.stats
            tvStreakCount?.text = (stats?.currentStreak ?: 0).toString()
            tvQuizzesCompleted?.text = (stats?.quizzesCompleted ?: 0).toString()
            
            val earnedBadges = state.badges.count { badge -> badge.isUnlocked }
            val totalBadges = maxOf(state.badges.size, 1)
            tvBadgesCount?.text = "$earnedBadges/$totalBadges"
            
            // Update accessibility content descriptions
            layoutStreak?.contentDescription = "Streak: ${stats?.currentStreak ?: 0} days"
            layoutQuizzes?.contentDescription = "Quizzes completed: ${stats?.quizzesCompleted ?: 0}"
            layoutBadgesCount?.contentDescription = "Badges earned: $earnedBadges of $totalBadges"

            // Badges - show default if empty
            val badgesToShow = if (state.badges.isEmpty()) {
                emptyList() // Will show empty state
            } else {
                state.badges.take(6) // Show first 6 for 3x2 grid
            }
            badgeAdapter.submitList(badgesToShow)
            btnViewAllBadges?.visibility = if (state.badges.size > 6) View.VISIBLE else View.GONE
            
            // Daily challenge - set default values
            tvChallengeXp?.text = "+75 XP"
            tvChallengeDescription?.text = getString(R.string.daily_challenge_default)
        }
    }
    
    private fun getLevelTitle(level: Int): String {
        return when {
            level <= 5 -> "Beginner"
            level <= 10 -> "Learner"
            level <= 20 -> "Explorer"
            level <= 35 -> "Achiever"
            level <= 50 -> "Master"
            else -> "Champion"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
