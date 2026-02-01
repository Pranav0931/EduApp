package com.hdaf.eduapp.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.network.NetworkMonitor
import com.hdaf.eduapp.databinding.ActivityMainBinding
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
