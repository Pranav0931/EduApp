package com.hdaf.eduapp.presentation.mode

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
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentModeSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Mode selection screen for choosing between Audio and Video modes.
 * Critical accessibility decision point.
 */
@AndroidEntryPoint
class ModeSelectionFragment : Fragment() {

    private var _binding: FragmentModeSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ModeSelectionViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeEvents()
        announceForAccessibility()
    }

    private fun setupClickListeners() {
        binding.cardAudioMode.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.selectAudioMode()
        }

        binding.cardVideoMode.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.selectVideoMode()
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        ModeSelectionEvent.NavigateToPhoneInput -> {
                            navigateToPhoneInput()
                        }
                    }
                }
            }
        }
    }

    private fun announceForAccessibility() {
        if (accessibilityManager.isAccessibilityEnabled()) {
            accessibilityManager.announceForAccessibility(
                getString(R.string.talkback_mode_selection)
            )
        }
    }

    private fun navigateToPhoneInput() {
        findNavController().navigate(
            ModeSelectionFragmentDirections.actionModeSelectionToPhoneInput()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
