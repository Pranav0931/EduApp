package com.hdaf.eduapp.presentation.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.FragmentLeaderboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Leaderboard screen showing top performers.
 */
@AndroidEntryPoint
class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeaderboardViewModel by viewModels()
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupChipFilter()
        setupSwipeRefresh()
        observeUiState()

        viewModel.loadLeaderboard()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter { user ->
            // Navigate to user profile if needed
        }

        binding.rvLeaderboard.apply {
            adapter = leaderboardAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupChipFilter() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val filter = when (checkedIds.first()) {
                    R.id.chip_weekly -> LeaderboardFilter.WEEKLY
                    R.id.chip_monthly -> LeaderboardFilter.MONTHLY
                    R.id.chip_all_time -> LeaderboardFilter.ALL_TIME
                    else -> LeaderboardFilter.WEEKLY
                }
                viewModel.loadLeaderboard(filter)
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshLeaderboard()
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

    private fun updateUi(state: LeaderboardUiState) {
        binding.apply {
            swipeRefresh.isRefreshing = state.isLoading
            progressLoading.visibility = if (state.isLoading && state.entries.isEmpty()) View.VISIBLE else View.GONE

            leaderboardAdapter.submitList(state.entries)

            // Current user rank card
            state.currentUserEntry?.let { entry ->
                cardUserRank.visibility = View.VISIBLE
                tvUserRank.text = "#${entry.rank}"
                tvUserName.text = entry.userName
                tvUserXp.text = "${entry.xp} XP"
            } ?: run {
                cardUserRank.visibility = View.GONE
            }

            // Empty state
            layoutEmpty.visibility = if (state.entries.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
            rvLeaderboard.visibility = if (state.entries.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
