package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.hdaf.eduapp.data.local.dao.ChapterAudioProgressDao
import com.hdaf.eduapp.data.local.entity.ChapterAudioProgressEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for chapter-wise audio learning.
 * 
 * Key features:
 * - Per-chapter audio state isolation
 * - Automatic progress persistence
 * - Resume from last position per chapter
 * - TalkBack compatibility with audio focus management
 * - Offline caching support
 * 
 * This solves the critical issue where all chapters shared a single audio state.
 */
@Singleton
class ChapterAudioManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chapterAudioProgressDao: ChapterAudioProgressDao,
    private val eduAccessibilityManager: EduAccessibilityManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private val _currentChapterId = MutableStateFlow<String?>(null)
    val currentChapterId: StateFlow<String?> = _currentChapterId.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    
    // Auto-save interval in milliseconds
    private val autoSaveIntervalMs = 5000L
    private var lastSaveTime = 0L
    
    /**
     * Load chapter audio progress and prepare for playback.
     * Returns the saved position to resume from.
     */
    suspend fun loadChapter(
        chapterId: String, 
        chapterTitle: String,
        bookId: String,
        totalDurationMs: Long
    ): Long {
        Timber.d("Loading chapter audio: $chapterId")
        
        // Save current chapter progress before switching
        _currentChapterId.value?.let { currentId ->
            if (currentId != chapterId) {
                saveProgress(currentId)
            }
        }
        
        _currentChapterId.value = chapterId
        _duration.value = totalDurationMs
        
        // Load or create progress entry
        val existingProgress = chapterAudioProgressDao.getProgress(chapterId)
        
        return if (existingProgress != null) {
            _currentPosition.value = existingProgress.positionMs
            _playbackSpeed.value = existingProgress.playbackSpeed
            Timber.d("Resuming chapter from position: ${existingProgress.positionMs}ms")
            existingProgress.positionMs
        } else {
            // Create new progress entry
            val newProgress = ChapterAudioProgressEntity(
                chapterId = chapterId,
                chapterTitle = chapterTitle,
                bookId = bookId,
                durationMs = totalDurationMs
            )
            chapterAudioProgressDao.upsertProgress(newProgress)
            _currentPosition.value = 0L
            _playbackSpeed.value = 1.0f
            Timber.d("Starting chapter from beginning")
            0L
        }
    }
    
    /**
     * Start or resume playback with audio focus management.
     * Handles TalkBack compatibility automatically.
     */
    fun play() {
        if (requestAudioFocus()) {
            _isPlaying.value = true
            Timber.d("Audio playback started")
        } else {
            Timber.w("Could not acquire audio focus")
        }
    }
    
    /**
     * Pause playback and save progress.
     */
    fun pause() {
        _isPlaying.value = false
        scope.launch {
            _currentChapterId.value?.let { chapterId ->
                saveProgress(chapterId)
            }
        }
        Timber.d("Audio playback paused")
    }
    
    /**
     * Toggle play/pause state.
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }
    
    /**
     * Update current position during playback.
     * Automatically saves progress periodically.
     */
    fun updatePosition(positionMs: Long) {
        _currentPosition.value = positionMs
        
        // Auto-save progress periodically
        val now = System.currentTimeMillis()
        if (now - lastSaveTime >= autoSaveIntervalMs) {
            lastSaveTime = now
            scope.launch {
                _currentChapterId.value?.let { chapterId ->
                    chapterAudioProgressDao.updatePosition(chapterId, positionMs)
                }
            }
        }
        
        // Check for chapter completion
        if (positionMs >= _duration.value - 1000 && _duration.value > 0) {
            onChapterCompleted()
        }
    }
    
    /**
     * Seek to a specific position.
     */
    fun seekTo(positionMs: Long) {
        _currentPosition.value = positionMs.coerceIn(0L, _duration.value)
        scope.launch {
            _currentChapterId.value?.let { chapterId ->
                chapterAudioProgressDao.updatePosition(chapterId, positionMs)
            }
        }
    }
    
    /**
     * Seek forward by specified milliseconds.
     */
    fun seekForward(deltaMs: Long = 10000L) {
        val newPosition = (_currentPosition.value + deltaMs).coerceAtMost(_duration.value)
        seekTo(newPosition)
    }
    
    /**
     * Seek backward by specified milliseconds.
     */
    fun seekBackward(deltaMs: Long = 10000L) {
        val newPosition = (_currentPosition.value - deltaMs).coerceAtLeast(0L)
        seekTo(newPosition)
    }
    
    /**
     * Set playback speed for current chapter.
     */
    fun setPlaybackSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
        _playbackSpeed.value = clampedSpeed
        
        scope.launch {
            _currentChapterId.value?.let { chapterId ->
                chapterAudioProgressDao.updatePlaybackSpeed(chapterId, clampedSpeed)
            }
        }
    }
    
    /**
     * Save current progress to database.
     */
    suspend fun saveProgress(chapterId: String? = _currentChapterId.value) {
        chapterId ?: return
        
        val existingProgress = chapterAudioProgressDao.getProgress(chapterId) ?: return
        
        val updatedProgress = existingProgress.copy(
            positionMs = _currentPosition.value,
            playbackSpeed = _playbackSpeed.value,
            lastPlayedAt = System.currentTimeMillis()
        )
        
        chapterAudioProgressDao.upsertProgress(updatedProgress)
        Timber.d("Saved audio progress for chapter: $chapterId at position: ${_currentPosition.value}ms")
    }
    
    /**
     * Get in-progress chapters for "Continue Learning" feature.
     */
    fun getInProgressChapters(): Flow<List<ChapterAudioProgressEntity>> {
        return chapterAudioProgressDao.getInProgressChapters()
    }
    
    /**
     * Get recently played chapters.
     */
    fun getRecentlyPlayed(limit: Int = 10): Flow<List<ChapterAudioProgressEntity>> {
        return chapterAudioProgressDao.getRecentlyPlayed(limit)
    }
    
    /**
     * Reset progress for a chapter (start over).
     */
    suspend fun resetChapterProgress(chapterId: String) {
        chapterAudioProgressDao.resetProgress(chapterId)
        if (_currentChapterId.value == chapterId) {
            _currentPosition.value = 0L
        }
    }
    
    /**
     * Handle chapter completion.
     */
    private fun onChapterCompleted() {
        scope.launch {
            _currentChapterId.value?.let { chapterId ->
                chapterAudioProgressDao.markCompleted(chapterId)
                Timber.d("Chapter completed: $chapterId")
            }
        }
        _isPlaying.value = false
        abandonAudioFocus()
    }
    
    /**
     * Request audio focus for playback.
     * Handles TalkBack compatibility.
     */
    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            
            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            hasAudioFocus
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            hasAudioFocus
        }
    }
    
    /**
     * Abandon audio focus when done.
     */
    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
        hasAudioFocus = false
    }
    
    /**
     * Audio focus change listener for TalkBack compatibility.
     * Pauses playback when TalkBack announces something.
     */
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                Timber.d("Audio focus lost permanently")
                pause()
                hasAudioFocus = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause for TalkBack announcements
                Timber.d("Audio focus lost transiently (TalkBack?)")
                if (_isPlaying.value) {
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower volume temporarily
                Timber.d("Audio focus ducking")
                // TTS handles volume internally
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                Timber.d("Audio focus gained")
                hasAudioFocus = true
                // Don't auto-resume, let user control
            }
        }
    }
    
    /**
     * Clean up resources.
     */
    fun release() {
        scope.launch {
            saveProgress()
        }
        abandonAudioFocus()
        _isPlaying.value = false
    }
}
