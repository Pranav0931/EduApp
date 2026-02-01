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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentQuizListBinding
import com.hdaf.eduapp.domain.model.Quiz
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Displays list of available quizzes and allows generating AI quizzes.
 * Supports accessibility features for all user modes.
 */
@AndroidEntryPoint
class QuizListFragment : Fragment() {

    private var _binding: FragmentQuizListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizListViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var quizAdapter: QuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        observeUiState()

        viewModel.loadQuizzes()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter(
            onQuizClick = { quiz -> navigateToQuiz(quiz) }
        )

        binding.rvQuizzes.apply {
            adapter = quizAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadQuizzes()
        }
    }

    private fun setupFab() {
        binding.fabCreateQuiz.setOnClickListener {
            showChapterSelectionDialog()
        }
        
        binding.btnGenerateAiQuiz.setOnClickListener {
            showChapterSelectionDialog()
        }
    }
    
    private fun showChapterSelectionDialog() {
        accessibilityManager.provideHapticFeedback(HapticType.CLICK)
        
        // Show dialog to select chapter for AI quiz
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_chapter)
            .setMessage(R.string.select_chapter_for_quiz)
            .setPositiveButton(R.string.browse_chapters) { _, _ ->
                // Navigate to book list to select chapter
                findNavController().navigate(R.id.action_quizList_to_bookList)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun navigateToQuiz(quiz: Quiz) {
        accessibilityManager.provideHapticFeedback(HapticType.CLICK)
        accessibilityManager.speak(getString(R.string.starting_quiz, quiz.title))
        
        val action = QuizListFragmentDirections.actionQuizListToQuiz(
            quizId = quiz.id
        )
        findNavController().navigate(action)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.swipeRefresh.isRefreshing = state.isLoading
                    binding.progressBar.visibility = if (state.isLoading && state.quizzes.isEmpty()) View.VISIBLE else View.GONE
                    
                    if (state.quizzes.isEmpty() && !state.isLoading) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvQuizzes.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvQuizzes.visibility = View.VISIBLE
                        quizAdapter.submitList(state.quizzes)
                    }
                    
                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
