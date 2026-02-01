package com.hdaf.eduapp.presentation.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
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
import com.hdaf.eduapp.databinding.FragmentVideoPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Video player fragment for visual educational content.
 * Includes sign language overlay support for deaf students.
 */
@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VideoPlayerViewModel by viewModels()

    private val chapterId: String by lazy {
        arguments?.getString(ARG_CHAPTER_ID) ?: ""
    }
    private val chapterTitle: String by lazy {
        arguments?.getString(ARG_CHAPTER_TITLE) ?: ""
    }
    private val videoUrl: String by lazy {
        arguments?.getString(ARG_VIDEO_URL) ?: ""
    }

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            viewModel.updateProgress()
            handler.postDelayed(this, 1000)
        }
    }

    private val hideControlsRunnable = Runnable {
        hideControls()
    }

    companion object {
        const val ARG_CHAPTER_ID = "chapterId"
        const val ARG_CHAPTER_TITLE = "chapterTitle"
        const val ARG_VIDEO_URL = "videoUrl"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Keep screen on during video playback
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupToolbar()
        setupPlayerControls()
        setupSeekBar()
        setupSignLanguageToggle()
        setupVideoSurfaceClick()
        observeUiState()

        viewModel.loadChapter(chapterId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = chapterTitle
            setNavigationOnClickListener {
                viewModel.saveProgress()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupVideoSurfaceClick() {
        binding.videoView.setOnClickListener {
            toggleControls()
        }
        
        binding.layoutControls.setOnClickListener {
            // Keep controls visible when clicked
            scheduleHideControls()
        }
    }

    private fun toggleControls() {
        if (binding.layoutControls.visibility == View.VISIBLE) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        binding.toolbar.visibility = View.VISIBLE
        binding.layoutControls.visibility = View.VISIBLE
        scheduleHideControls()
    }

    private fun hideControls() {
        if (viewModel.uiState.value.isPlaying) {
            binding.toolbar.visibility = View.GONE
            binding.layoutControls.visibility = View.GONE
        }
    }

    private fun scheduleHideControls() {
        handler.removeCallbacks(hideControlsRunnable)
        handler.postDelayed(hideControlsRunnable, 3000)
    }

    private fun setupPlayerControls() {
        binding.apply {
            btnPlay.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                viewModel.togglePlayPause()
                scheduleHideControls()
            }

            btnRewind.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                viewModel.seekBackward()
                scheduleHideControls()
            }

            btnForward.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                viewModel.seekForward()
                scheduleHideControls()
            }

            btnFullscreen.setOnClickListener {
                viewModel.toggleFullscreen()
            }
        }
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateProgressRunnable)
                handler.removeCallbacks(hideControlsRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { viewModel.seekTo(it) }
                handler.post(updateProgressRunnable)
                scheduleHideControls()
            }
        })
    }

    private fun setupSignLanguageToggle() {
        binding.btnSignLanguage.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.toggleSignLanguage()
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

    private fun updateUi(state: VideoPlayerUiState) {
        binding.apply {
            progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            // Play/Pause button
            btnPlay.setImageResource(
                if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )

            // Progress
            seekBar.max = state.duration
            seekBar.progress = state.currentPosition
            tvCurrentTime.text = formatTime(state.currentPosition)
            tvTotalTime.text = formatTime(state.duration)

            // Sign language overlay visibility
            layoutSignLanguage.visibility = if (state.showSignLanguage) View.VISIBLE else View.GONE
            btnSignLanguage.isSelected = state.showSignLanguage
            btnSignLanguage.setImageResource(
                if (state.showSignLanguage) R.drawable.ic_sign_language_on 
                else R.drawable.ic_sign_language
            )

            // Fullscreen icon
            btnFullscreen.setImageResource(
                if (state.isFullscreen) R.drawable.ic_fullscreen_exit 
                else R.drawable.ic_fullscreen
            )
        }
    }

    private fun handleEvent(event: VideoPlayerEvent) {
        when (event) {
            is VideoPlayerEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is VideoPlayerEvent.ChapterCompleted -> {
                Snackbar.make(binding.root, "वीडियो पूर्ण! 🎉", Snackbar.LENGTH_SHORT).show()
            }
            is VideoPlayerEvent.NavigateToQuiz -> {
                val bundle = Bundle().apply {
                    putString("chapterId", event.chapterId)
                }
                findNavController().navigate(R.id.quizFragment, bundle)
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateProgressRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateProgressRunnable)
        handler.removeCallbacks(hideControlsRunnable)
        viewModel.saveProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        handler.removeCallbacks(updateProgressRunnable)
        handler.removeCallbacks(hideControlsRunnable)
        _binding = null
    }
}
