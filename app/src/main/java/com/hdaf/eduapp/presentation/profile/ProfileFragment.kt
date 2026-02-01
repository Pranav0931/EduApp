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
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * User profile screen showing stats, badges, and achievements.
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var badgeAdapter: BadgeAdapter

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

        setupToolbar()
        setupBadgesRecyclerView()
        setupClickListeners()
        observeUiState()

        viewModel.loadProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.settingsFragment)
                        true
                    }
                    R.id.action_edit -> {
                        findNavController().navigate(R.id.editProfileFragment)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupBadgesRecyclerView() {
        badgeAdapter = BadgeAdapter { badge ->
            // Show badge details
            Snackbar.make(binding.root, badge.description, Snackbar.LENGTH_SHORT).show()
        }

        binding.rvBadges.apply {
            adapter = badgeAdapter
            layoutManager = GridLayoutManager(requireContext(), 4)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            cardStreak.setOnClickListener {
                // TODO: Implement streak detail screen
                Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
            }

            cardLeaderboard.setOnClickListener {
                findNavController().navigate(R.id.leaderboardFragment)
            }

            btnViewAllBadges.setOnClickListener {
                // TODO: Implement all badges screen
                Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
            }

            btnDownloads.setOnClickListener {
                // TODO: Implement downloads screen
                Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
            }

            btnHistory.setOnClickListener {
                // TODO: Implement history screen
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
            progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            state.user?.let { user ->
                // User info
                tvName.text = user.name
                tvClass.text = getString(R.string.class_format, user.classLevel, user.medium)

                // Avatar
                if (!user.avatarUrl.isNullOrEmpty()) {
                    ivAvatar.load(user.avatarUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_person)
                        error(R.drawable.ic_person)
                    }
                }

                // Level and XP
                tvLevel.text = getString(R.string.level_format, user.level)
                tvXp.text = getString(R.string.xp_format, user.xp)
                progressXp.max = user.xpToNextLevel
                progressXp.progress = user.xp % user.xpToNextLevel
                tvXpProgress.text = getString(
                    R.string.xp_progress_format,
                    user.xp % user.xpToNextLevel,
                    user.xpToNextLevel
                )
            }

            state.stats?.let { stats ->
                // Streak
                tvStreakCount.text = stats.currentStreak.toString()
                tvStreakLabel.text = getString(R.string.day_streak)

                // Stats
                tvBooksCompleted.text = stats.booksCompleted.toString()
                tvChaptersCompleted.text = stats.chaptersCompleted.toString()
                tvQuizzesCompleted.text = stats.quizzesCompleted.toString()
                tvTotalMinutes.text = stats.totalMinutesLearned.toString()

                // Leaderboard rank
                tvRank.text = "#${stats.leaderboardRank}"
            }

            // Badges
            badgeAdapter.submitList(state.badges.take(8)) // Show first 8
            btnViewAllBadges.visibility = if (state.badges.size > 8) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
