package com.hdaf.eduapp.core.ui.components

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.ui.UiState

/**
 * Unified state view that handles Loading, Error, Empty, and Success states consistently.
 * 
 * Features:
 * - Smooth animated transitions between states
 * - Accessibility support with proper announcements
 * - Customizable messages and icons
 * - Retry button for error states
 * - Production-ready design following Material 3
 * 
 * Usage in XML:
 * ```xml
 * <com.hdaf.eduapp.core.ui.components.StateView
 *     android:id="@+id/stateView"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:emptyText="@string/no_items"
 *     app:emptyIcon="@drawable/ic_empty" />
 * ```
 */
class StateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val loadingContainer: View
    private val errorContainer: View
    private val emptyContainer: View
    private val contentContainer: FrameLayout
    
    private val progressIndicator: CircularProgressIndicator
    private val loadingText: TextView
    
    private val errorIcon: ImageView
    private val errorTitle: TextView
    private val errorMessage: TextView
    private val retryButton: MaterialButton
    
    private val emptyIcon: ImageView
    private val emptyTitle: TextView
    private val emptyMessage: TextView
    private val emptyActionButton: MaterialButton
    
    private var onRetryClickListener: (() -> Unit)? = null
    private var onEmptyActionClickListener: (() -> Unit)? = null
    
    // Animation duration for transitions
    private val transitionDuration = 200L
    
    init {
        LayoutInflater.from(context).inflate(R.layout.view_state, this, true)
        
        loadingContainer = findViewById(R.id.loading_container)
        errorContainer = findViewById(R.id.error_container)
        emptyContainer = findViewById(R.id.empty_container)
        contentContainer = findViewById(R.id.content_container)
        
        progressIndicator = findViewById(R.id.progress_indicator)
        loadingText = findViewById(R.id.loading_text)
        
        errorIcon = findViewById(R.id.error_icon)
        errorTitle = findViewById(R.id.error_title)
        errorMessage = findViewById(R.id.error_message)
        retryButton = findViewById(R.id.retry_button)
        
        emptyIcon = findViewById(R.id.empty_icon)
        emptyTitle = findViewById(R.id.empty_title)
        emptyMessage = findViewById(R.id.empty_message)
        emptyActionButton = findViewById(R.id.empty_action_button)
        
        retryButton.setOnClickListener { onRetryClickListener?.invoke() }
        emptyActionButton.setOnClickListener { onEmptyActionClickListener?.invoke() }
        
        // Initialize with loading hidden
        hideAllStates()
    }
    
    /**
     * Set the current state with smooth transitions.
     */
    fun <T> setState(state: UiState<T>) {
        when (state) {
            is UiState.Loading -> showLoading()
            is UiState.Success -> showContent()
            is UiState.Error -> showError(state.message, state.retryAction)
            is UiState.Empty -> showEmpty()
        }
    }
    
    /**
     * Show loading state.
     */
    fun showLoading(message: String? = null) {
        hideAllStatesAnimated()
        loadingContainer.fadeIn()
        message?.let { loadingText.text = it }
        announceForAccessibility(context.getString(R.string.loading))
    }
    
    /**
     * Show content (success) state.
     */
    fun showContent() {
        hideAllStatesAnimated()
        contentContainer.fadeIn()
    }
    
    /**
     * Show error state.
     */
    fun showError(message: String, retryAction: (() -> Unit)? = null) {
        hideAllStatesAnimated()
        errorContainer.fadeIn()
        errorMessage.text = message
        onRetryClickListener = retryAction
        retryButton.visibility = if (retryAction != null) View.VISIBLE else View.GONE
        
        // Announce error for accessibility
        announceForAccessibility(context.getString(R.string.error_occurred, message))
    }
    
    /**
     * Show error with title.
     */
    fun showError(title: String, message: String, retryAction: (() -> Unit)? = null) {
        showError(message, retryAction)
        errorTitle.text = title
        errorTitle.visibility = View.VISIBLE
    }
    
    /**
     * Show empty state.
     */
    fun showEmpty(message: String? = null, actionText: String? = null, action: (() -> Unit)? = null) {
        hideAllStatesAnimated()
        emptyContainer.fadeIn()
        
        message?.let { emptyMessage.text = it }
        
        if (actionText != null && action != null) {
            emptyActionButton.text = actionText
            emptyActionButton.visibility = View.VISIBLE
            onEmptyActionClickListener = action
        } else {
            emptyActionButton.visibility = View.GONE
        }
        
        announceForAccessibility(message ?: context.getString(R.string.no_data_available))
    }
    
    /**
     * Set custom loading message.
     */
    fun setLoadingMessage(message: String) {
        loadingText.text = message
    }
    
    /**
     * Set custom empty state configuration.
     */
    fun setEmptyState(
        icon: Int? = null,
        title: String? = null,
        message: String? = null,
        actionText: String? = null,
        action: (() -> Unit)? = null
    ) {
        icon?.let { emptyIcon.setImageResource(it) }
        title?.let { 
            emptyTitle.text = it 
            emptyTitle.visibility = View.VISIBLE
        }
        message?.let { emptyMessage.text = it }
        if (actionText != null && action != null) {
            emptyActionButton.text = actionText
            emptyActionButton.visibility = View.VISIBLE
            onEmptyActionClickListener = action
        }
    }
    
    /**
     * Set custom error icon.
     */
    fun setErrorIcon(iconRes: Int) {
        errorIcon.setImageResource(iconRes)
    }
    
    /**
     * Set retry button listener.
     */
    fun setOnRetryClickListener(listener: () -> Unit) {
        onRetryClickListener = listener
    }
    
    /**
     * Add content view to the content container.
     */
    fun setContentView(view: View) {
        contentContainer.removeAllViews()
        contentContainer.addView(view)
    }
    
    private fun hideAllStates() {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        emptyContainer.visibility = View.GONE
        contentContainer.visibility = View.GONE
    }
    
    private fun hideAllStatesAnimated() {
        listOf(loadingContainer, errorContainer, emptyContainer, contentContainer).forEach { view ->
            if (view.visibility == View.VISIBLE) {
                view.fadeOut()
            }
        }
    }
    
    private fun View.fadeIn() {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(transitionDuration)
            .start()
    }
    
    private fun View.fadeOut() {
        animate()
            .alpha(0f)
            .setDuration(transitionDuration)
            .withEndAction { visibility = View.GONE }
            .start()
    }
    
    /**
     * Accessibility announcement helper.
     */
    private fun announceForAccessibility(message: String) {
        // Post to ensure view is attached
        post {
            announceForAccessibility(message)
        }
    }
}
