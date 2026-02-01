package com.hdaf.eduapp.presentation.home

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
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home Fragment - Main dashboard of the app.
 * 
 * Features:
 * - User greeting with progress
 * - Daily goal progress
 * - Continue learning section
 * - Recommended content
 * - Quick access to quizzes
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    
    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupObservers()
        setupClickListeners()
    }

    private fun setupViews() {
        // Setup RecyclerViews
        binding.rvRecommended.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }
        
        binding.rvRecent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeUiState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeUiState() {
        viewModel.uiState.collect { state ->
            // Update user greeting
            binding.tvGreeting.text = getString(R.string.home_greeting, state.userName)
            
            // Update progress
            binding.tvLevel.text = getString(R.string.home_level) + ": ${state.level}"
            binding.tvXp.text = "${state.totalXp} ${getString(R.string.home_xp)}"
            binding.tvStreak.text = "${state.streak} ${getString(R.string.home_streak)} 🔥"
            
            // Update daily goal progress
            binding.progressDailyGoal.progress = (state.dailyGoalProgress * 100).toInt()
            
            // Handle loading state
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            // Handle error state
            state.error?.let { error ->
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
                viewModel.onAction(HomeAction.ClearError)
            }
            
            // Accessibility announcement
            if (!state.isLoading && state.error == null) {
                accessibilityManager.announceForAccessibility(
                    "होम स्क्रीन। स्तर ${state.level}, ${state.totalXp} XP, ${state.streak} दिन की स्ट्रीक"
                )
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToBook -> {
                    val action = HomeFragmentDirections.actionHomeToChapterList(
                        bookId = event.bookId,
                        bookTitle = event.bookTitle
                    )
                    findNavController().navigate(action)
                }
                is HomeUiEvent.NavigateToQuiz -> {
                    val bundle = Bundle().apply {
                        putString("quizId", event.quizId)
                    }
                    findNavController().navigate(R.id.action_home_to_quizList, bundle)
                }
                is HomeUiEvent.ShowBadgeEarned -> {
                    Snackbar.make(
                        binding.root,
                        "🏅 ${event.badge.name}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    accessibilityManager.speak("बधाई! आपने ${event.badge.name} बैज अर्जित किया!")
                }
                is HomeUiEvent.ShowLevelUp -> {
                    Snackbar.make(
                        binding.root,
                        "🎉 ${getString(R.string.level_up_message)} स्तर ${event.newLevel}!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    accessibilityManager.speak("बधाई! आप स्तर ${event.newLevel} पर पहुंच गए!")
                }
                is HomeUiEvent.ShowError -> {
                    Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                }
                else -> { /* Handle other events */ }
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardDailyGoal.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_quizList)
        }
        
        binding.btnAllBooks.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_bookList)
        }
        
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
        
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAction(HomeAction.RefreshData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
