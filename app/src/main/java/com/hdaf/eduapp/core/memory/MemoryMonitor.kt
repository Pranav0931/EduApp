package com.hdaf.eduapp.core.memory

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors device memory and handles low memory situations.
 * 
 * Features:
 * - Real-time memory monitoring
 * - Automatic resource cleanup triggers
 * - Graceful degradation for low memory devices
 * - Accessibility-friendly warnings
 */
@Singleton
class MemoryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : ComponentCallbacks2 {
    
    private val activityManager = 
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    private val _memoryState = MutableStateFlow(MemoryState.NORMAL)
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()
    
    private val _availableMemoryMB = MutableStateFlow(getAvailableMemory())
    val availableMemoryMB: StateFlow<Long> = _availableMemoryMB.asStateFlow()
    
    private val memoryCleanupCallbacks = mutableListOf<() -> Unit>()
    
    /**
     * Check if device is low on memory.
     */
    fun isLowMemory(): Boolean {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.lowMemory
    }
    
    /**
     * Get available memory in MB.
     */
    fun getAvailableMemory(): Long {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.availMem / (1024 * 1024)
    }
    
    /**
     * Get total memory in MB.
     */
    fun getTotalMemory(): Long {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024 * 1024)
    }
    
    /**
     * Get memory usage percentage.
     */
    fun getMemoryUsagePercent(): Int {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val used = memInfo.totalMem - memInfo.availMem
        return ((used.toFloat() / memInfo.totalMem) * 100).toInt()
    }
    
    /**
     * Check if we should reduce features.
     */
    fun shouldReduceFeatures(): Boolean {
        return _memoryState.value == MemoryState.CRITICAL ||
               _memoryState.value == MemoryState.LOW
    }
    
    /**
     * Check if we can load heavy content (like large audio/video).
     */
    fun canLoadHeavyContent(): Boolean {
        return _memoryState.value == MemoryState.NORMAL && 
               getAvailableMemory() > 100 // At least 100MB available
    }
    
    /**
     * Register a callback to be called when memory needs cleanup.
     */
    fun registerCleanupCallback(callback: () -> Unit) {
        memoryCleanupCallbacks.add(callback)
    }
    
    /**
     * Unregister a cleanup callback.
     */
    fun unregisterCleanupCallback(callback: () -> Unit) {
        memoryCleanupCallbacks.remove(callback)
    }
    
    /**
     * Trigger memory cleanup.
     */
    fun triggerCleanup() {
        memoryCleanupCallbacks.forEach { it.invoke() }
    }
    
    /**
     * Get accessibility-friendly memory status.
     */
    fun getAccessibleMemoryStatus(isHindi: Boolean = false): String {
        return when (_memoryState.value) {
            MemoryState.NORMAL -> {
                if (isHindi) "मेमोरी सामान्य है"
                else "Memory is normal"
            }
            MemoryState.MODERATE -> {
                if (isHindi) "मेमोरी मध्यम है। कुछ सुविधाएं धीमी हो सकती हैं।"
                else "Memory is moderate. Some features may be slower."
            }
            MemoryState.LOW -> {
                if (isHindi) "मेमोरी कम है। कृपया अन्य ऐप्स बंद करें।"
                else "Memory is low. Please close other apps."
            }
            MemoryState.CRITICAL -> {
                if (isHindi) "मेमोरी बहुत कम है। ऐप अस्थिर हो सकता है।"
                else "Memory is critically low. App may become unstable."
            }
        }
    }
    
    // ComponentCallbacks2 implementation
    
    override fun onTrimMemory(level: Int) {
        _availableMemoryMB.value = getAvailableMemory()
        
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                _memoryState.value = MemoryState.CRITICAL
                triggerCleanup()
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                _memoryState.value = MemoryState.LOW
                triggerCleanup()
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                _memoryState.value = MemoryState.MODERATE
            }
            else -> {
                _memoryState.value = MemoryState.NORMAL
            }
        }
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        // Not used, but required by interface
    }
    
    override fun onLowMemory() {
        _memoryState.value = MemoryState.CRITICAL
        _availableMemoryMB.value = getAvailableMemory()
        triggerCleanup()
    }
    
    /**
     * Recommendations for resource usage based on memory state.
     */
    fun getResourceRecommendations(): ResourceRecommendations {
        return when (_memoryState.value) {
            MemoryState.NORMAL -> ResourceRecommendations(
                maxCacheSize = 50, // MB
                enableAnimations = true,
                enablePrefetch = true,
                maxConcurrentDownloads = 3,
                imageQuality = ImageQuality.HIGH
            )
            MemoryState.MODERATE -> ResourceRecommendations(
                maxCacheSize = 30,
                enableAnimations = true,
                enablePrefetch = false,
                maxConcurrentDownloads = 2,
                imageQuality = ImageQuality.MEDIUM
            )
            MemoryState.LOW -> ResourceRecommendations(
                maxCacheSize = 15,
                enableAnimations = false,
                enablePrefetch = false,
                maxConcurrentDownloads = 1,
                imageQuality = ImageQuality.LOW
            )
            MemoryState.CRITICAL -> ResourceRecommendations(
                maxCacheSize = 5,
                enableAnimations = false,
                enablePrefetch = false,
                maxConcurrentDownloads = 0,
                imageQuality = ImageQuality.MINIMAL
            )
        }
    }
}

/**
 * Memory state levels.
 */
enum class MemoryState {
    NORMAL,
    MODERATE,
    LOW,
    CRITICAL
}

/**
 * Resource usage recommendations based on memory state.
 */
data class ResourceRecommendations(
    val maxCacheSize: Int, // MB
    val enableAnimations: Boolean,
    val enablePrefetch: Boolean,
    val maxConcurrentDownloads: Int,
    val imageQuality: ImageQuality
)

/**
 * Image quality levels.
 */
enum class ImageQuality {
    HIGH,
    MEDIUM,
    LOW,
    MINIMAL
}
