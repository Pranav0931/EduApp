package com.hdaf.eduapp.presentation.flashcard

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
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
import com.hdaf.eduapp.accessibility.TTSManager
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentFlashcardStudyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Flashcard study screen with flip animation, spaced repetition rating,
 * and TTS accessibility support.
 */
@AndroidEntryPoint
class FlashcardStudyFragment : Fragment() {

    private var _binding: FragmentFlashcardStudyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashcardStudyViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var ttsManager: TTSManager
    private var isShowingFront = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardStudyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsManager = TTSManager.getInstance()
        if (!ttsManager.isReady) {
            ttsManager.initialize(requireContext())
        }

        setupToolbar()
        setupClickListeners()
        observeState()
        observeEvents()

        view.announceForAccessibility(getString(R.string.flashcards_title))
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupClickListeners() {
        // Card flip
        binding.cardFlashcard.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.flipCard()
        }

        binding.btnFlip.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.flipCard()
        }

        // Navigation
        binding.btnNext.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
            viewModel.nextCard()
        }

        binding.btnPrevious.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
            viewModel.previousCard()
        }

        // Spaced repetition ratings
        binding.btnAgain.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.ERROR)
            viewModel.rateCard(0)
        }

        binding.btnHard.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.rateCard(1)
        }

        binding.btnGood.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
            viewModel.rateCard(2)
        }

        binding.btnEasy.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
            viewModel.rateCard(3)
        }

        // TTS
        binding.fabSpeak.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            speakCurrentCard()
        }

        // Hint button
        binding.btnShowHint.setOnClickListener {
            val hint = viewModel.currentCard?.hint
            if (!hint.isNullOrBlank()) {
                Snackbar.make(binding.root, hint, Snackbar.LENGTH_LONG).show()
                ttsManager.speak("Hint: $hint")
            }
        }

        // Create deck button (in empty state)
        binding.btnCreateDeck.setOnClickListener {
            Snackbar.make(binding.root, "Coming soon", Snackbar.LENGTH_SHORT).show()
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
                        is FlashcardUiEvent.DeckCompleted -> {
                            accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
                            ttsManager.speak(getString(R.string.deck_completed))
                            Snackbar.make(
                                binding.root,
                                getString(R.string.deck_completed),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUi(state: FlashcardUiState) {
        // Loading
        binding.progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Empty state
        binding.layoutEmpty.visibility = if (state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
        binding.flashcardContainer.visibility = if (!state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE

        if (state.isEmpty || state.isLoading) return

        // Progress
        val total = state.cards.size
        val current = state.currentIndex + 1
        binding.tvProgress.text = "$current / $total"
        binding.progressBar.max = total
        binding.progressBar.progress = current

        // Card content
        val card = state.currentCard ?: return
        binding.tvFront.text = card.front
        binding.tvBack.text = card.back

        // Flip state
        if (state.isFlipped && isShowingFront) {
            flipToBack()
        } else if (!state.isFlipped && !isShowingFront) {
            flipToFront()
        }

        // Show hint button if hint exists
        binding.btnShowHint.visibility = if (!card.hint.isNullOrBlank() && !state.isFlipped) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Show rating buttons only when flipped (answer visible)
        binding.ratingButtons.visibility = if (state.isFlipped) View.VISIBLE else View.GONE

        // Show audio button if audio URL exists
        binding.btnPlayAudio.visibility = if (!card.audioUrl.isNullOrBlank() && state.isFlipped) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Navigation buttons
        binding.btnPrevious.isEnabled = state.currentIndex > 0
        binding.btnNext.isEnabled = state.currentIndex < state.cards.size - 1

        // Toolbar title
        binding.toolbar.title = state.deckName

        // Accessibility announcements
        if (state.isFlipped) {
            binding.cardFlashcard.contentDescription = getString(R.string.flashcard_flipped_to_answer) + ". " + card.back
        } else {
            binding.cardFlashcard.contentDescription = getString(R.string.flashcard_tap_to_flip) + ". " + card.front
        }
    }

    private fun flipToBack() {
        binding.cardFront.visibility = View.GONE
        binding.cardBack.visibility = View.VISIBLE
        isShowingFront = false
        view?.announceForAccessibility(getString(R.string.flashcard_flipped_to_answer))
    }

    private fun flipToFront() {
        binding.cardFront.visibility = View.VISIBLE
        binding.cardBack.visibility = View.GONE
        isShowingFront = true
        view?.announceForAccessibility(getString(R.string.flashcard_flipped_to_question))
    }

    private fun speakCurrentCard() {
        val card = viewModel.currentCard ?: return
        val state = viewModel.uiState.value
        val text = if (state.isFlipped) {
            "${getString(R.string.flashcard_answer)}: ${card.back}"
        } else {
            "${getString(R.string.flashcard_question)}: ${card.front}"
        }
        ttsManager.speak(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsManager.stop()
        _binding = null
    }
}
