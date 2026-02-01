package com.hdaf.eduapp.presentation.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentAudioPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Audio player fragment for listening to educational content.
 * Optimized for blind students with enhanced accessibility.
 */
@AndroidEntryPoint
class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AudioPlayerViewModel by viewModels()

    private val chapterId: String by lazy { arguments?.getString(ARG_CHAPTER_ID) ?: "" }
    private val chapterTitle: String by lazy { arguments?.getString(ARG_CHAPTER_TITLE) ?: "" }
    private val audioUrl: String by lazy { arguments?.getString(ARG_AUDIO_URL) ?: "" }

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            viewModel.updateProgress()
            handler.postDelayed(this, 1000)
        }
    }

    companion object {
        const val ARG_CHAPTER_ID = "chapterId"
        const val ARG_CHAPTER_TITLE = "chapterTitle"
        const val ARG_AUDIO_URL = "audioUrl"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupPlayerControls()
        setupSeekBar()
        setupSpeedControl()
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

    private fun setupPlayerControls() {
        binding.apply {
            btnPlay.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                viewModel.togglePlayPause()
            }

            btnRewind.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                viewModel.seekBackward()
            }

            btnForward.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                viewModel.seekForward()
            }

            btnPrevious.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
                viewModel.playPrevious()
            }

            btnNext.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
                viewModel.playNext()
            }

            // Accessibility descriptions
            btnPlay.contentDescription = getString(R.string.play_pause)
            btnRewind.contentDescription = getString(R.string.rewind_10_seconds)
            btnForward.contentDescription = getString(R.string.forward_10_seconds)
            btnPrevious.contentDescription = getString(R.string.previous_chapter)
            btnNext.contentDescription = getString(R.string.next_chapter)
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
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { viewModel.seekTo(it) }
                handler.post(updateProgressRunnable)
            }
        })
    }

    private fun setupSpeedControl() {
        binding.chipGroupSpeed.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val speed = when (checkedIds.first()) {
                    R.id.chip_speed_075 -> 0.75f
                    R.id.chip_speed_100 -> 1.0f
                    R.id.chip_speed_125 -> 1.25f
                    R.id.chip_speed_150 -> 1.5f
                    else -> 1.0f
                }
                viewModel.setPlaybackSpeed(speed)
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

    private fun updateUi(state: AudioPlayerUiState) {
        binding.apply {
            progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            tvChapterTitle.text = state.chapterTitle
            tvBookTitle.text = state.bookTitle

            // Play/Pause button
            btnPlay.setImageResource(
                if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )

            // Progress
            seekBar.max = state.duration
            seekBar.progress = state.currentPosition
            tvCurrentTime.text = formatTime(state.currentPosition)
            tvTotalTime.text = formatTime(state.duration)

            // Speed chip selection
            val speedChipId = when (state.playbackSpeed) {
                0.75f -> R.id.chip_speed_075
                1.25f -> R.id.chip_speed_125
                1.5f -> R.id.chip_speed_150
                else -> R.id.chip_speed_100
            }
            chipGroupSpeed.check(speedChipId)
        }
    }

    private fun handleEvent(event: AudioPlayerEvent) {
        when (event) {
            is AudioPlayerEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is AudioPlayerEvent.ChapterCompleted -> {
                Snackbar.make(binding.root, "अध्याय पूर्ण! 🎉", Snackbar.LENGTH_SHORT).show()
                accessibilityManager.speak("अध्याय पूर्ण")
            }
            is AudioPlayerEvent.NavigateToQuiz -> {
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
        viewModel.saveProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateProgressRunnable)
        _binding = null
    }
}
