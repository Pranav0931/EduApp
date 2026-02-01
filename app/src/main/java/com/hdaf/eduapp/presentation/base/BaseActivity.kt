package com.hdaf.eduapp.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.hdaf.eduapp.utils.LocaleHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base Activity class with ViewBinding support.
 * 
 * Features:
 * - Type-safe ViewBinding
 * - Lifecycle-aware Flow collection
 * - Common UI setup patterns
 * - Locale/language support
 * 
 * @param VB The ViewBinding type
 */
abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    private var _binding: VB? = null
    
    /**
     * ViewBinding instance. Only valid between onCreate and onDestroy.
     */
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "ViewBinding is only valid between onCreate and onDestroy"
        )
        
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Setup UI elements. Called in onCreate.
     */
    protected abstract fun setupUI()
    
    /**
     * Setup observers for ViewModel state/events. Called in onCreate.
     */
    protected abstract fun setupObservers()

    /**
     * Collect a Flow in a lifecycle-aware manner.
     * Collection starts when lifecycle is at least STARTED and stops when STOPPED.
     */
    protected fun <T> collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest(action)
            }
        }
    }
    
    /**
     * Collect a StateFlow in a lifecycle-aware manner.
     * Useful for UI state observation.
     */
    protected fun <T> collectState(flow: Flow<T>, action: (T) -> Unit) {
        collectFlow(flow) { action(it) }
    }
    
    /**
     * Collect a SharedFlow of events in a lifecycle-aware manner.
     * Useful for one-time events like navigation.
     */
    protected fun <T> collectEvents(flow: Flow<T>, action: (T) -> Unit) {
        collectFlow(flow) { action(it) }
    }
}
