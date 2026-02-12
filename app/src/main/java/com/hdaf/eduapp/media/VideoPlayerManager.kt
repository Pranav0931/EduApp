package com.hdaf.eduapp.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VideoPlayerManager - Manages ExoPlayer instance for video playback.
 * Handles video loading, playback controls, and state management.
 */
@Singleton
class VideoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    
    private val _playerState = MutableStateFlow(VideoPlayerState())
    val playerState: StateFlow<VideoPlayerState> = _playerState.asStateFlow()
    
    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val isLoading = playbackState == Player.STATE_BUFFERING
            val isEnded = playbackState == Player.STATE_ENDED
            val isReady = playbackState == Player.STATE_READY
            
            _playerState.value = _playerState.value.copy(
                isBuffering = isLoading,
                isEnded = isEnded,
                isReady = isReady
            )
            
            if (isEnded) {
                _playerState.value = _playerState.value.copy(isPlaying = false)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackError.value = error.message ?: "Video playback error"
            _playerState.value = _playerState.value.copy(hasError = true)
        }
    }

    @OptIn(UnstableApi::class)
    fun initialize(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context)
                .build()
                .also { player ->
                    player.addListener(playerListener)
                    player.playWhenReady = false
                }
        }
        return exoPlayer!!
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun loadVideo(url: String, startPositionMs: Long = 0) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
            if (startPositionMs > 0) {
                player.seekTo(startPositionMs)
            }
            _playerState.value = _playerState.value.copy(
                videoUrl = url,
                isLoading = true,
                hasError = false
            )
            _playbackError.value = null
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun seekForward(seconds: Int = 10) {
        exoPlayer?.let { player ->
            val newPosition = player.currentPosition + (seconds * 1000)
            player.seekTo(minOf(newPosition, player.duration))
        }
    }

    fun seekBackward(seconds: Int = 10) {
        exoPlayer?.let { player ->
            val newPosition = player.currentPosition - (seconds * 1000)
            player.seekTo(maxOf(newPosition, 0))
        }
    }

    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0

    fun getDuration(): Long = exoPlayer?.duration ?: 0

    fun getProgressPercent(): Float {
        val duration = getDuration()
        return if (duration > 0) {
            getCurrentPosition().toFloat() / duration.toFloat()
        } else 0f
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun release() {
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
        _playerState.value = VideoPlayerState()
    }

    fun clearError() {
        _playbackError.value = null
        _playerState.value = _playerState.value.copy(hasError = false)
    }
}

data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val isEnded: Boolean = false,
    val hasError: Boolean = false,
    val videoUrl: String = ""
)
