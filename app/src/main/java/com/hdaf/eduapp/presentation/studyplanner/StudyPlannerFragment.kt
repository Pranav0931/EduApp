package com.hdaf.eduapp.presentation.studyplanner

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
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.TTSManager
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentStudyPlannerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Study Planner screen showing today's summary, study goals,
 * homework reminders, and recent study sessions.
 */
@AndroidEntryPoint
class StudyPlannerFragment : Fragment() {

    private var _binding: FragmentStudyPlannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudyPlannerViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var ttsManager: TTSManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsManager = TTSManager.getInstance()
        if (!ttsManager.isReady) {
            ttsManager.initialize(requireContext())
        }

        setupToolbar()
        setupRecyclerViews()
        setupClickListeners()
        observeState()
        observeEvents()

        view.announceForAccessibility(getString(R.string.study_planner_title))
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerViews() {
        binding.rvStudyGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHomework.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        binding.fabSpeak.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            speakSummary()
        }

        binding.cardTodaySummary.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            speakSummary()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is StudyPlannerUiEvent.HomeworkCompleted -> {
                            accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
                            ttsManager.speak(getString(R.string.homework_completed))
                            Snackbar.make(
                                binding.root,
                                getString(R.string.homework_completed),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUi(state: StudyPlannerUiState) {
        // Loading
        binding.progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Today's summary
        binding.tvStudyMinutes.text = state.todayMinutesStudied.toString()
        binding.tvChaptersCompleted.text = state.todayChaptersCompleted.toString()
        binding.tvGoalProgress.text = "${state.goalProgressPercent}%"

        // Study goals
        binding.tvEmptyGoals.visibility = if (state.studyGoals.isEmpty()) View.VISIBLE else View.GONE
        binding.rvStudyGoals.visibility = if (state.studyGoals.isNotEmpty()) View.VISIBLE else View.GONE

        // Homework reminders
        binding.tvEmptyHomework.visibility = if (state.homeworkReminders.isEmpty()) View.VISIBLE else View.GONE
        binding.rvHomework.visibility = if (state.homeworkReminders.isNotEmpty()) View.VISIBLE else View.GONE

        // Recent sessions
        binding.tvEmptySessions.visibility = if (state.recentSessions.isEmpty()) View.VISIBLE else View.GONE
        binding.rvSessions.visibility = if (state.recentSessions.isNotEmpty()) View.VISIBLE else View.GONE

        // Accessibility content descriptions
        binding.cardTodaySummary.contentDescription =
            "${state.todayMinutesStudied} minutes studied, " +
            "${state.todayChaptersCompleted} chapters completed, " +
            "${state.goalProgressPercent}% goal progress"
    }

    private fun speakSummary() {
        val state = viewModel.uiState.value
        val text = "Today's study summary. " +
                "${state.todayMinutesStudied} minutes studied. " +
                "${state.todayChaptersCompleted} chapters completed. " +
                "${state.goalProgressPercent} percent goal progress. " +
                "${state.studyGoals.size} active goals. " +
                "${state.homeworkReminders.size} pending homework."
        ttsManager.speak(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsManager.stop()
        _binding = null
    }
}
