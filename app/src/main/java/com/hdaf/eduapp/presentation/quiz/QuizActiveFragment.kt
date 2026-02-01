package com.hdaf.eduapp.presentation.quiz

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
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentQuizActiveBinding
import com.hdaf.eduapp.domain.model.QuizQuestion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Active quiz screen with questions, options, and timer.
 * Optimized for accessibility with voice feedback.
 */
@AndroidEntryPoint
class QuizActiveFragment : Fragment() {

    private var _binding: FragmentQuizActiveBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by viewModels()

    private val chapterId: String by lazy {
        arguments?.getString(ARG_CHAPTER_ID) ?: ""
    }

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizActiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupOptionClicks()
        setupNavigationButtons()
        observeUiState()

        viewModel.startQuiz(chapterId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                showExitConfirmation()
            }
        }
    }

    private fun setupOptionClicks() {
        binding.apply {
            optionA.setOnClickListener { selectOption(0) }
            optionB.setOnClickListener { selectOption(1) }
            optionC.setOnClickListener { selectOption(2) }
            optionD.setOnClickListener { selectOption(3) }
        }
    }

    private fun selectOption(index: Int) {
        accessibilityManager.provideHapticFeedback(HapticType.CLICK)
        viewModel.selectOption(index)
    }

    private fun setupNavigationButtons() {
        binding.apply {
            btnPrevious.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
                viewModel.previousQuestion()
            }

            btnNext.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
                viewModel.nextQuestion()
            }

            btnSubmit.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
                viewModel.submitQuiz()
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    private fun updateUi(state: QuizUiState) {
        binding.apply {
            progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            val questions = state.quiz?.questions ?: emptyList()
            if (questions.isNotEmpty()) {
                val question = questions[state.currentQuestionIndex]
                
                // Question progress
                toolbar.title = getString(
                    R.string.question_progress_format,
                    state.currentQuestionIndex + 1,
                    questions.size
                )
                progressQuiz.max = questions.size
                progressQuiz.progress = state.currentQuestionIndex + 1

                // Timer
                tvTimer.text = formatTime(state.timeRemainingSeconds)
                tvTimer.setTextColor(
                    if (state.timeRemainingSeconds <= 30) 
                        requireContext().getColor(R.color.error) 
                    else 
                        requireContext().getColor(R.color.on_surface_variant)
                )

                // Question
                tvQuestion.text = question.questionText
                
                // Options
                updateOptions(question, state.selectedAnswers[state.currentQuestionIndex])

                // Navigation buttons
                btnPrevious.isEnabled = state.currentQuestionIndex > 0
                btnNext.visibility = if (state.currentQuestionIndex < questions.size - 1) 
                    View.VISIBLE else View.GONE
                btnSubmit.visibility = if (state.currentQuestionIndex == questions.size - 1) 
                    View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateOptions(question: QuizQuestion, selectedIndex: Int?) {
        val options = listOf(
            binding.optionA to binding.tvOptionA,
            binding.optionB to binding.tvOptionB,
            binding.optionC to binding.tvOptionC,
            binding.optionD to binding.tvOptionD
        )

        options.forEachIndexed { index, (card, textView) ->
            if (index < question.options.size) {
                card.visibility = View.VISIBLE
                textView.text = question.options[index]
                
                // Selection state
                card.isChecked = selectedIndex == index
                card.strokeWidth = if (selectedIndex == index) 
                    resources.getDimensionPixelSize(R.dimen.card_stroke_selected) 
                else 0

                // Accessibility
                card.contentDescription = buildString {
                    append("विकल्प ${('अ' + index)}, ")
                    append(question.options[index])
                    if (selectedIndex == index) append(", चयनित")
                }
            } else {
                card.visibility = View.GONE
            }
        }
    }

    private fun handleEvent(event: QuizUiEvent) {
        when (event) {
            is QuizUiEvent.ShowSnackbar -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is QuizUiEvent.NavigateToResult -> {
                // Navigate to result - actual data will be passed via Bundle
                val resultState = viewModel.resultState.value
                val bundle = Bundle().apply {
                    putString(ARG_CHAPTER_ID, chapterId)
                    putInt("score", resultState.scorePercentage.toInt())
                    putInt("totalQuestions", resultState.totalQuestions)
                    putInt("correctAnswers", resultState.correctAnswers)
                }
                findNavController().navigate(R.id.action_quiz_to_result, bundle)
            }
            is QuizUiEvent.TimeUp -> {
                Snackbar.make(binding.root, "समय समाप्त!", Snackbar.LENGTH_SHORT).show()
                accessibilityManager.speak("समय समाप्त")
                viewModel.submitQuiz()
            }
            is QuizUiEvent.SpeakMessage -> {
                accessibilityManager.speak(event.message)
            }
            is QuizUiEvent.SpeakQuestion -> {
                accessibilityManager.speak(event.question)
            }
            else -> {}
        }
    }

    private fun showExitConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exit_quiz_title)
            .setMessage(R.string.exit_quiz_message)
            .setPositiveButton(R.string.exit) { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_CHAPTER_ID = "chapterId"
    }
}
