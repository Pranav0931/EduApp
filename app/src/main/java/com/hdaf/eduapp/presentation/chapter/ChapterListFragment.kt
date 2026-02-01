package com.hdaf.eduapp.presentation.chapter

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
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentChapterListBinding
import com.hdaf.eduapp.domain.model.Chapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Displays list of chapters for a selected book.
 * Shows progress for each chapter and handles navigation to reader.
 */
@AndroidEntryPoint
class ChapterListFragment : Fragment() {

    private var _binding: FragmentChapterListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChapterListViewModel by viewModels()

    private val bookId: String by lazy { arguments?.getString(ARG_BOOK_ID) ?: "" }
    private val bookTitle: String by lazy { arguments?.getString(ARG_BOOK_TITLE) ?: "" }

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var chapterAdapter: ChapterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChapterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeUiState()

        viewModel.loadChapters(bookId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = bookTitle
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun setupRecyclerView() {
        chapterAdapter = ChapterAdapter(
            onChapterClick = { chapter -> navigateToReader(chapter) },
            onDownloadClick = { chapter -> viewModel.downloadChapter(chapter) }
        )

        binding.rvChapters.apply {
            adapter = chapterAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshChapters(bookId)
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

    private fun updateUi(state: ChapterListUiState) {
        binding.swipeRefresh.isRefreshing = state.isLoading
        binding.progressBar.visibility = if (state.isLoading && state.chapters.isEmpty()) View.VISIBLE else View.GONE

        chapterAdapter.submitList(state.chapters)

        // Book progress
        binding.progressBook.progress = state.overallProgress
        binding.tvProgress.text = getString(R.string.chapter_progress_format, state.overallProgress)

        // Empty state
        binding.layoutEmpty.visibility = if (state.chapters.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
        binding.rvChapters.visibility = if (state.chapters.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun handleEvent(event: ChapterListEvent) {
        when (event) {
            is ChapterListEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is ChapterListEvent.DownloadComplete -> {
                Snackbar.make(binding.root, "अध्याय डाउनलोड पूर्ण", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToReader(chapter: Chapter) {
        accessibilityManager.provideHapticFeedback(HapticType.CLICK)
        
        val bundle = Bundle().apply {
            putString(ARG_CHAPTER_ID, chapter.id)
            putString(ARG_CHAPTER_TITLE, chapter.title)
        }
        
        // Determine content type based on available media in the chapter
        when {
            chapter.hasAudio -> {
                findNavController().navigate(R.id.action_chapterList_to_audioPlayer, bundle)
            }
            chapter.hasVideo -> {
                findNavController().navigate(R.id.action_chapterList_to_videoPlayer, bundle)
            }
            else -> {
                findNavController().navigate(R.id.action_chapterList_to_reader, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_BOOK_ID = "bookId"
        const val ARG_BOOK_TITLE = "bookTitle"
        const val ARG_CHAPTER_ID = "chapterId"
        const val ARG_CHAPTER_TITLE = "chapterTitle"
    }
}
