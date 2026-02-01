package com.hdaf.eduapp.presentation.book

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentBookListBinding
import com.hdaf.eduapp.domain.model.Book
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Displays list of books for a selected class.
 * Supports grid layout with accessibility optimizations.
 * 
 * Features:
 * - Offline-first book loading
 * - Pull-to-refresh
 * - Error handling with retry
 * - Accessibility announcements
 * - Book download support
 */
@AndroidEntryPoint
class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookListViewModel by viewModels()
    private val args: BookListFragmentArgs by navArgs()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var bookAdapter: BookAdapter
    
    private var hasAnnouncedBooks = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupRetryButton()
        observeUiState()

        // Load books for the specified class
        args.classId?.let { classId ->
            Timber.d("Loading books for class: $classId")
            viewModel.loadBooks(classId)
        } ?: run {
            Timber.e("No classId provided to BookListFragment")
            showError(getString(R.string.error_no_class_selected))
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
            findNavController().popBackStack()
        }
        
        // Update title based on class
        args.classId?.let { classId ->
            binding.toolbar.title = getString(R.string.std_format, classId)
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            onBookClick = { book -> navigateToChapters(book) },
            onDownloadClick = { book -> viewModel.downloadBook(book) }
        )

        binding.rvBooks.apply {
            adapter = bookAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
            
            // Improve scroll performance
            setItemViewCacheSize(10)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary_magenta,
            R.color.secondary_purple
        )
        
        binding.swipeRefresh.setOnRefreshListener {
            hasAnnouncedBooks = false
            args.classId?.let { viewModel.refreshBooks(it) }
        }
    }
    
    private fun setupRetryButton() {
        // Add retry functionality to empty state 
        binding.btnRetry.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            args.classId?.let { viewModel.loadBooks(it) }
        }
        
        // Fallback: also allow clicking on the empty layout
        binding.layoutEmpty.setOnClickListener {
            args.classId?.let { viewModel.loadBooks(it) }
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

    private fun updateUi(state: BookListUiState) {
        // Update refresh indicator
        binding.swipeRefresh.isRefreshing = state.isRefreshing
        
        // Loading state (only show spinner when no content)
        binding.progressBar.visibility = if (state.isLoading && state.books.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        // Update book list
        if (state.books.isNotEmpty()) {
            bookAdapter.submitList(state.books)
            binding.rvBooks.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            
            // Accessibility announcement (only once per load)
            if (!hasAnnouncedBooks && accessibilityManager.isAccessibilityEnabled()) {
                hasAnnouncedBooks = true
                val announcement = getString(R.string.books_loaded_announcement, state.books.size)
                accessibilityManager.announceForAccessibility(announcement)
            }
        }
        
        // Empty state
        if (state.showEmptyState) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvBooks.visibility = View.GONE
            
            if (accessibilityManager.isAccessibilityEnabled()) {
                accessibilityManager.announceForAccessibility(getString(R.string.no_books_available))
            }
        }
        
        // Error state
        if (state.showErrorState) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvBooks.visibility = View.GONE
            
            // Announce error for accessibility
            if (accessibilityManager.isAccessibilityEnabled()) {
                state.error?.let { accessibilityManager.announceForAccessibility(it) }
            }
        }
    }

    private fun handleEvent(event: BookListEvent) {
        when (event) {
            is BookListEvent.ShowError -> {
                showError(event.message)
            }
            is BookListEvent.DownloadStarted -> {
                showSnackbar(getString(R.string.download_started))
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            }
            is BookListEvent.DownloadComplete -> {
                showSnackbar(getString(R.string.download_complete))
                accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
            }
            is BookListEvent.DownloadFailed -> {
                showError(event.error)
                accessibilityManager.provideHapticFeedback(HapticType.ERROR)
            }
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                args.classId?.let { viewModel.loadBooks(it) }
            }
            .show()
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToChapters(book: Book) {
        accessibilityManager.provideHapticFeedback(HapticType.CLICK)
        
        // Announce navigation for accessibility
        if (accessibilityManager.isAccessibilityEnabled()) {
            accessibilityManager.speak("${book.title} खोल रहे हैं")
        }
        
        findNavController().navigate(
            BookListFragmentDirections.actionBookListToChapterList(book.id, book.title)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
