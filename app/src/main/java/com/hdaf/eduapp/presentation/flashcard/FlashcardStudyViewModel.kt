package com.hdaf.eduapp.presentation.flashcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.data.local.dao.FlashcardDao
import com.hdaf.eduapp.data.local.dao.FlashcardDeckDao
import com.hdaf.eduapp.data.local.entity.FlashcardEntity
import com.hdaf.eduapp.presentation.base.BaseViewModel
import com.hdaf.eduapp.presentation.base.UiEvent
import com.hdaf.eduapp.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for flashcard study screen.
 * Manages card navigation, flip state, and spaced repetition ratings.
 */
@HiltViewModel
class FlashcardStudyViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val flashcardDeckDao: FlashcardDeckDao,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<FlashcardUiState, FlashcardUiEvent>(FlashcardUiState()) {

    private val deckId: String? = savedStateHandle["deckId"]

    init {
        loadCards()
    }

    private fun loadCards() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val id = deckId
                if (id == null) {
                    setState { copy(isLoading = false, isEmpty = true) }
                    return@launch
                }

                val deck = flashcardDeckDao.getDeckById(id)
                val cards = flashcardDao.getCardsForReview(id, System.currentTimeMillis(), 20)

                if (cards.isEmpty()) {
                    // If no cards due for review, get all cards from deck
                    flashcardDao.getFlashcardsByDeck(id).collect { allCards ->
                        setState {
                            copy(
                                isLoading = false,
                                isEmpty = allCards.isEmpty(),
                                cards = allCards,
                                currentIndex = 0,
                                deckName = deck?.name ?: "Flashcards",
                                isFlipped = false
                            )
                        }
                    }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            isEmpty = false,
                            cards = cards,
                            currentIndex = 0,
                            deckName = deck?.name ?: "Flashcards",
                            isFlipped = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading flashcards")
                setState { copy(isLoading = false, isEmpty = true) }
            }
        }
    }

    fun flipCard() {
        setState { copy(isFlipped = !isFlipped) }
    }

    fun nextCard() {
        val state = currentState
        if (state.currentIndex < state.cards.size - 1) {
            setState { copy(currentIndex = currentIndex + 1, isFlipped = false) }
        } else {
            sendEvent(FlashcardUiEvent.DeckCompleted)
        }
    }

    fun previousCard() {
        if (currentState.currentIndex > 0) {
            setState { copy(currentIndex = currentIndex - 1, isFlipped = false) }
        }
    }

    /**
     * Rate the current card using spaced repetition intervals.
     * @param rating 0=Again, 1=Hard, 2=Good, 3=Easy
     */
    fun rateCard(rating: Int) {
        val state = currentState
        val card = state.currentCard ?: return

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val intervalMs = when (rating) {
                    0 -> 60_000L               // Again: 1 minute
                    1 -> 10 * 60_000L          // Hard: 10 minutes
                    2 -> 24 * 3600_000L        // Good: 1 day
                    3 -> 4 * 24 * 3600_000L    // Easy: 4 days
                    else -> 24 * 3600_000L
                }

                val isCorrect = rating >= 2
                val updatedCard = card.copy(
                    lastReviewedAt = now,
                    nextReviewAt = now + intervalMs,
                    reviewCount = card.reviewCount + 1,
                    correctCount = card.correctCount + if (isCorrect) 1 else 0
                )
                flashcardDao.updateFlashcard(updatedCard)

                // Update mastered count if card is "Easy"
                if (rating == 3 && deckId != null) {
                    val deck = flashcardDeckDao.getDeckById(deckId)
                    if (deck != null) {
                        flashcardDeckDao.updateDeck(
                            deck.copy(masteredCount = deck.masteredCount + 1)
                        )
                    }
                }

                setState { copy(reviewedCount = reviewedCount + 1) }
                nextCard()
            } catch (e: Exception) {
                Timber.e(e, "Error rating flashcard")
            }
        }
    }

    val currentCard: FlashcardEntity?
        get() = currentState.currentCard
}

// ==================== UI State ====================

data class FlashcardUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val cards: List<FlashcardEntity> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val deckName: String = "Flashcards",
    val reviewedCount: Int = 0
) : UiState {
    val currentCard: FlashcardEntity?
        get() = cards.getOrNull(currentIndex)

    val progress: Float
        get() = if (cards.isEmpty()) 0f else (currentIndex + 1).toFloat() / cards.size
}

// ==================== UI Events ====================

sealed class FlashcardUiEvent : UiEvent {
    data object DeckCompleted : FlashcardUiEvent()
}
