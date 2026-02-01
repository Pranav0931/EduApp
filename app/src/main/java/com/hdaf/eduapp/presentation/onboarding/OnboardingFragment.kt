package com.hdaf.eduapp.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Onboarding screen showing app features and mode selection.
 * Accessible design for both blind and deaf users.
 */
@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var pagerAdapter: OnboardingPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupClickListeners()
        observeUiState()
    }

    private fun setupViewPager() {
        pagerAdapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentPage(position)
                updatePageIndicator(position)
                announcePageForAccessibility(position)
            }
        })

        // Connect dots indicator
        binding.dotsIndicator.attachTo(binding.viewPager)
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < pagerAdapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                navigateToModeSelection()
            }
        }

        binding.btnSkip.setOnClickListener {
            navigateToModeSelection()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: OnboardingUiState) {
        binding.btnNext.text = if (state.isLastPage) {
            getString(R.string.onboarding_get_started)
        } else {
            getString(R.string.onboarding_next)
        }

        binding.btnSkip.visibility = if (state.isLastPage) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun updatePageIndicator(position: Int) {
        viewModel.setCurrentPage(position)
    }

    private fun announcePageForAccessibility(position: Int) {
        if (accessibilityManager.isAccessibilityEnabled()) {
            val pageTitles = resources.getStringArray(R.array.onboarding_titles)
            if (position < pageTitles.size) {
                accessibilityManager.announceForAccessibility(
                    "पेज ${position + 1}. ${pageTitles[position]}"
                )
            }
        }
    }

    private fun navigateToModeSelection() {
        viewModel.completeOnboarding()
        findNavController().navigate(
            OnboardingFragmentDirections.actionOnboardingToModeSelection()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
