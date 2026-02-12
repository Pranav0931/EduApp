package com.hdaf.eduapp.presentation.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash screen shown on app launch.
 * Handles:
 * - Brand animation
 * - Auth state check
 * - Navigation to appropriate destination
 */
@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start animations
        startLogoAnimation()

        // Observe navigation state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Delay minimum 2 seconds for branding
                delay(2000)

                viewModel.navigationState.collect { navState ->
                    when (navState) {
                        SplashNavigationState.LanguageSelection -> {
                            navigateToLanguageSelection()
                        }
                        SplashNavigationState.Onboarding -> {
                            navigateToOnboarding()
                        }
                        SplashNavigationState.Home -> {
                            navigateToHome()
                        }
                        SplashNavigationState.Loading -> {
                            // Still checking auth state
                        }
                    }
                }
            }
        }

        // Announce for accessibility
        if (accessibilityManager.isAccessibilityEnabled()) {
            accessibilityManager.announceForAccessibility(
                getString(R.string.app_name) + " लोड हो रहा है"
            )
        }
    }

    private fun startLogoAnimation() {
        // Scale up animation for logo
        val scaleX = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_X, 0f, 1f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_Y, 0f, 1f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }

        // Fade in animation for app name
        val fadeIn = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f).apply {
            duration = 600
            startDelay = 400
        }

        // Fade in for tagline
        val taglineFadeIn = ObjectAnimator.ofFloat(binding.tvTagline, View.ALPHA, 0f, 1f).apply {
            duration = 600
            startDelay = 600
        }

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeIn, taglineFadeIn)
            start()
        }
    }

    private fun navigateToLanguageSelection() {
        findNavController().navigate(
            SplashFragmentDirections.actionSplashToLanguageSelection()
        )
    }

    private fun navigateToOnboarding() {
        findNavController().navigate(
            SplashFragmentDirections.actionSplashToOnboarding()
        )
    }

    private fun navigateToHome() {
        findNavController().navigate(
            SplashFragmentDirections.actionSplashToHome()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
