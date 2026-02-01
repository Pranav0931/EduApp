package com.hdaf.eduapp.presentation.reader

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentReaderBinding
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reader fragment for displaying chapter text content.
 * Supports text-to-speech, adjustable text size, and accessibility features.
 */
@AndroidEntryPoint
class ReaderFragment : Fragment() {

    private var _binding: FragmentReaderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReaderViewModel by viewModels()
    private val args: ReaderFragmentArgs by navArgs()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager
    
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var currentTextSize = 16f
    private var isReading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupTextSizeControls()
        setupReadAloudFab()
        setupAccessibility()
        observeUiState()

        viewModel.loadChapter(args.chapterId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupTextSizeControls() {
        // Load saved text size
        currentTextSize = sharedPreferences.getFloat("reader_text_size", 16f)
        updateTextSize()

        binding.btnDecreaseText.setOnClickListener {
            if (currentTextSize > 12f) {
                currentTextSize -= 2f
                updateTextSize()
                saveTextSize()
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
            }
        }

        binding.btnIncreaseText.setOnClickListener {
            if (currentTextSize < 32f) {
                currentTextSize += 2f
                updateTextSize()
                saveTextSize()
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
            }
        }
    }

    private fun updateTextSize() {
        binding.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize)
    }

    private fun saveTextSize() {
        sharedPreferences.edit()
            .putFloat("reader_text_size", currentTextSize)
            .apply()
    }

    private fun setupReadAloudFab() {
        binding.fabReadAloud.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            
            if (isReading) {
                stopReading()
            } else {
                startReading()
            }
        }
    }

    private fun startReading() {
        isReading = true
        binding.fabReadAloud.setImageResource(R.drawable.ic_stop)
        binding.fabReadAloud.contentDescription = getString(R.string.stop_reading)
        
        val content = binding.tvContent.text.toString()
        accessibilityManager.speak(content)
    }

    private fun stopReading() {
        isReading = false
        binding.fabReadAloud.setImageResource(R.drawable.ic_mic)
        binding.fabReadAloud.contentDescription = getString(R.string.read_aloud)
        
        accessibilityManager.stopSpeaking()
    }

    private fun setupAccessibility() {
        val modeOrdinal = sharedPreferences.getInt("accessibility_mode", 0)
        val mode = AccessibilityModeType.entries.getOrElse(modeOrdinal) { AccessibilityModeType.NORMAL }

        when (mode) {
            AccessibilityModeType.BLIND -> {
                // Auto-start reading for blind users
                binding.fabReadAloud.postDelayed({ startReading() }, 1000)
            }
            AccessibilityModeType.LOW_VISION -> {
                // Use larger text for low vision
                currentTextSize = 24f
                updateTextSize()
            }
            AccessibilityModeType.SLOW_LEARNER -> {
                // Slightly larger text and slower TTS
                currentTextSize = 20f
                updateTextSize()
            }
            else -> { /* Default behavior */ }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                    if (state.chapter != null) {
                        binding.tvChapterTitle.text = state.chapter.title
                        binding.tvContent.text = state.chapter.contentText ?: getString(R.string.no_content_available)
                        binding.tvContent.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                        
                        binding.toolbar.title = state.chapter.title
                    } else if (!state.isLoading) {
                        binding.tvContent.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    }

                    state.errorMessage?.let { message ->
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isReading) {
            stopReading()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
