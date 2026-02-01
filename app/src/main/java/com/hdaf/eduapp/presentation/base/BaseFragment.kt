package com.hdaf.eduapp.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base Fragment class with ViewBinding support.
 * 
 * Features:
 * - Type-safe ViewBinding with proper lifecycle handling
 * - Lifecycle-aware Flow collection
 * - Safe binding access
 * 
 * @param VB The ViewBinding type
 */
abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    
    /**
     * ViewBinding instance. Only valid between onCreateView and onDestroyView.
     */
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "ViewBinding is only valid between onCreateView and onDestroyView"
        )

    /**
     * Check if binding is available.
     */
    protected val isBindingAvailable: Boolean
        get() = _binding != null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Setup UI elements. Called in onViewCreated.
     */
    protected abstract fun setupUI()
    
    /**
     * Setup observers for ViewModel state/events. Called in onViewCreated.
     */
    protected abstract fun setupObservers()

    /**
     * Collect a Flow in a lifecycle-aware manner.
     * Collection starts when lifecycle is at least STARTED and stops when STOPPED.
     */
    protected fun <T> collectFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest(action)
            }
        }
    }
    
    /**
     * Collect a StateFlow in a lifecycle-aware manner.
     */
    protected fun <T> collectState(flow: Flow<T>, action: (T) -> Unit) {
        collectFlow(flow) { action(it) }
    }
    
    /**
     * Collect events in a lifecycle-aware manner.
     */
    protected fun <T> collectEvents(flow: Flow<T>, action: (T) -> Unit) {
        collectFlow(flow) { action(it) }
    }
}
