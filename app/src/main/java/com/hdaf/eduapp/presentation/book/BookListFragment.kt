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
import javax.inject.Inject

/**
 * Displays list of books for a selected class.
 * Supports grid layout with accessibility optimizations.
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
        observeUiState()

        // Load books for the specified class
        args.classId?.let { viewModel.loadBooks(it) }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
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
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            args.classId?.let { viewModel.refreshBooks(it) }
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
        binding.swipeRefresh.isRefreshing = state.isLoading
        binding.progressBar.visibility = if (state.isLoading && state.books.isEmpty()) View.VISIBLE else View.GONE

        bookAdapter.submitList(state.books)

        // Empty state
        binding.layoutEmpty.visibility = if (state.books.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
        binding.rvBooks.visibility = if (state.books.isNotEmpty()) View.VISIBLE else View.GONE

        // Accessibility announcement
        if (state.books.isNotEmpty() && accessibilityManager.isAccessibilityEnabled()) {
            accessibilityManager.announceForAccessibility(
                "${state.books.size} किताबें उपलब्ध हैं"
            )
        }
    }

    private fun handleEvent(event: BookListEvent) {
        when (event) {
            is BookListEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is BookListEvent.DownloadStarted -> {
                Snackbar.make(binding.root, "डाउनलोड शुरू हो गया", Snackbar.LENGTH_SHORT).show()
            }
            is BookListEvent.DownloadComplete -> {
                Snackbar.make(binding.root, "डाउनलोड पूर्ण", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToChapters(book: Book) {
        accessibilityManager.provideHapticFeedback(HapticType.CLICK)
        findNavController().navigate(
            BookListFragmentDirections.actionBookListToChapterList(book.id, book.title)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
