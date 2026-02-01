package com.hdaf.eduapp.presentation

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.VoiceNavigationManager
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.network.NetworkMonitor
import com.hdaf.eduapp.databinding.ActivityMainBinding
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.ui.EduAIChatBottomSheet
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Activity hosting the Navigation Component.
 * 
 * Features:
 * - Single Activity architecture
 * - Bottom navigation for main screens
 * - Network connectivity monitoring
 * - Accessibility support
 * - Deep link handling
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager
    
    @Inject
    lateinit var voiceNavigationManager: VoiceNavigationManager
    
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    
    private var isVoiceListening = false
    
    // Permission launcher for microphone
    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceListening()
        } else {
            Toast.makeText(this, R.string.mic_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    // Destinations where bottom nav should be visible
    private val bottomNavDestinations = setOf(
        R.id.homeFragment,
        R.id.bookListFragment,
        R.id.quizListFragment,
        R.id.profileFragment,
        R.id.leaderboardFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Keep splash screen until initial data is loaded
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        observeNetworkStatus()
        observeUiState()
        setupAccessibility()
        setupVoiceControl()
        setupAccessibilityMode()
        setupAiChatFab()
    }
    
    private fun setupAiChatFab() {
        binding.fabAiChat.setOnClickListener {
            accessibilityManager.provideHapticFeedback(com.hdaf.eduapp.core.accessibility.HapticType.CLICK)
            showAiChat()
        }
    }
    
    private fun showAiChat() {
        val chatBottomSheet = EduAIChatBottomSheet.newInstance()
        chatBottomSheet.show(supportFragmentManager, "EduAIChat")
    }
    
    private fun setupVoiceControl() {
        binding.fabVoiceControl.setOnClickListener {
            if (isVoiceListening) {
                stopVoiceListening()
            } else {
                checkMicPermissionAndListen()
            }
        }
        
        // Observe voice navigation commands
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                voiceNavigationManager.recognizedText.collect { text ->
                    text?.let { handleVoiceCommand(it) }
                }
            }
        }
    }
    
    private fun checkMicPermissionAndListen() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceListening()
            }
            else -> {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startVoiceListening() {
        isVoiceListening = true
        binding.fabVoiceControl.setImageResource(R.drawable.ic_mic_active)
        voiceNavigationManager.startListening()
        accessibilityManager.announceForAccessibility(getString(R.string.voice_listening))
        Toast.makeText(this, R.string.voice_listening, Toast.LENGTH_SHORT).show()
    }
    
    private fun stopVoiceListening() {
        isVoiceListening = false
        binding.fabVoiceControl.setImageResource(R.drawable.ic_mic)
        voiceNavigationManager.stopListening()
    }
    
    private fun handleVoiceCommand(command: String) {
        stopVoiceListening()
        
        when {
            command.contains("home", ignoreCase = true) || 
            command.contains("होम", ignoreCase = true) -> {
                navController.navigate(R.id.homeFragment)
            }
            command.contains("book", ignoreCase = true) || 
            command.contains("किताब", ignoreCase = true) -> {
                navController.navigate(R.id.bookListFragment)
            }
            command.contains("quiz", ignoreCase = true) || 
            command.contains("क्विज़", ignoreCase = true) -> {
                navController.navigate(R.id.quizListFragment)
            }
            command.contains("profile", ignoreCase = true) || 
            command.contains("प्रोफाइल", ignoreCase = true) -> {
                navController.navigate(R.id.profileFragment)
            }
            command.contains("leaderboard", ignoreCase = true) || 
            command.contains("लीडरबोर्ड", ignoreCase = true) -> {
                navController.navigate(R.id.leaderboardFragment)
            }
            command.contains("setting", ignoreCase = true) || 
            command.contains("सेटिंग", ignoreCase = true) -> {
                navController.navigate(R.id.settingsFragment)
            }
            command.contains("back", ignoreCase = true) || 
            command.contains("वापस", ignoreCase = true) -> {
                navController.popBackStack()
            }
            else -> {
                Toast.makeText(this, getString(R.string.voice_command_not_recognized), Toast.LENGTH_SHORT).show()
            }
        }
        
        accessibilityManager.announceForAccessibility(getString(R.string.voice_command_executed, command))
    }
    
    private fun setupAccessibilityMode() {
        // Load saved accessibility mode and update UI accordingly
        val modeOrdinal = sharedPreferences.getInt("accessibility_mode", 0)
        val mode = AccessibilityModeType.entries.getOrElse(modeOrdinal) { AccessibilityModeType.NORMAL }
        applyAccessibilityMode(mode)
        
        // Observe preference changes
        sharedPreferences.registerOnSharedPreferenceChangeListener { prefs, key ->
            if (key == "accessibility_mode") {
                val newModeOrdinal = prefs.getInt(key, 0)
                val newMode = AccessibilityModeType.entries.getOrElse(newModeOrdinal) { AccessibilityModeType.NORMAL }
                applyAccessibilityMode(newMode)
            }
        }
    }
    
    private fun applyAccessibilityMode(mode: AccessibilityModeType) {
        when (mode) {
            AccessibilityModeType.NORMAL -> {
                binding.fabVoiceControl.visibility = View.GONE
                binding.subtitleContainer.visibility = View.GONE
            }
            AccessibilityModeType.BLIND -> {
                binding.fabVoiceControl.visibility = View.VISIBLE
                binding.subtitleContainer.visibility = View.GONE
                accessibilityManager.announceForAccessibility(getString(R.string.mode_blind_activated))
            }
            AccessibilityModeType.DEAF -> {
                binding.fabVoiceControl.visibility = View.GONE
                binding.subtitleContainer.visibility = View.VISIBLE
            }
            AccessibilityModeType.LOW_VISION -> {
                binding.fabVoiceControl.visibility = View.VISIBLE
                binding.subtitleContainer.visibility = View.GONE
            }
            AccessibilityModeType.SLOW_LEARNER -> {
                binding.fabVoiceControl.visibility = View.GONE
                binding.subtitleContainer.visibility = View.GONE
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Setup bottom navigation with nav controller
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Handle destination changes to show/hide bottom nav
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility = if (destination.id in bottomNavDestinations) {
                View.VISIBLE
            } else {
                View.GONE
            }
            
            // Announce screen change for accessibility
            destination.label?.let { label ->
                accessibilityManager.announceForAccessibility(label.toString())
            }
        }
        
        // Handle deep links
        handleDeepLink()
    }

    private fun handleDeepLink() {
        intent?.data?.let { uri ->
            Timber.d("Deep link received: $uri")
            
            // Handle different deep link paths
            when (uri.pathSegments.firstOrNull()) {
                "book" -> {
                    val bookId = uri.pathSegments.getOrNull(1)
                    bookId?.let {
                        navController.navigate(R.id.bookListFragment)
                    }
                }
                "quiz" -> {
                    val quizId = uri.pathSegments.getOrNull(1)
                    quizId?.let {
                        val bundle = Bundle().apply {
                            putString("quizId", it)
                        }
                        navController.navigate(R.id.quizFragment, bundle)
                    }
                }
                "leaderboard" -> {
                    navController.navigate(R.id.leaderboardFragment)
                }
                else -> {
                    // Unknown deep link path, navigate to home
                    Timber.d("Unknown deep link path: ${uri.path}")
                }
            }
        }
    }

    private fun observeNetworkStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isConnected
                    .distinctUntilChanged()
                    .collect { isConnected ->
                        if (!isConnected) {
                            showOfflineSnackbar()
                        }
                    }
            }
        }
    }

    private fun showOfflineSnackbar() {
        Snackbar.make(
            binding.root,
            R.string.offline_message,
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.dismiss) { dismiss() }
            anchorView = binding.bottomNavigation
            show()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MainUiEvent.ShowSnackbar -> {
                            Snackbar.make(
                                binding.root,
                                event.message,
                                Snackbar.LENGTH_SHORT
                            ).apply {
                                anchorView = binding.bottomNavigation
                                show()
                            }
                        }
                        is MainUiEvent.NavigateToAuth -> {
                            navController.navigate(R.id.authNavGraph)
                        }
                        is MainUiEvent.NavigateToHome -> {
                            navController.navigate(R.id.homeFragment)
                        }
                    }
                }
            }
        }
    }

    private fun setupAccessibility() {
        // Enable TTS announcements
        accessibilityManager.initializeTts()
        
        // Set content descriptions for bottom nav items
        binding.bottomNavigation.menu.apply {
            findItem(R.id.homeFragment)?.contentDescription = getString(R.string.nav_home)
            findItem(R.id.bookListFragment)?.contentDescription = getString(R.string.nav_books)
            findItem(R.id.quizListFragment)?.contentDescription = getString(R.string.nav_quizzes)
            findItem(R.id.profileFragment)?.contentDescription = getString(R.string.nav_profile)
            findItem(R.id.leaderboardFragment)?.contentDescription = getString(R.string.nav_leaderboard)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityManager.shutdown()
        voiceNavigationManager.stopListening()
    }
}

/**
 * UI events for MainActivity.
 */
sealed interface MainUiEvent {
    data class ShowSnackbar(val message: String) : MainUiEvent
    data object NavigateToAuth : MainUiEvent
    data object NavigateToHome : MainUiEvent
}
