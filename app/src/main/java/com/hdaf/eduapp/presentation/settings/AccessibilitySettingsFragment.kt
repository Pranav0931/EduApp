package com.hdaf.eduapp.presentation.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.FragmentAccessibilitySettingsBinding
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment for detailed accessibility settings.
 */
@AndroidEntryPoint
class AccessibilitySettingsFragment : Fragment() {

    private var _binding: FragmentAccessibilitySettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccessibilitySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadSettings() {
        val modeOrdinal = sharedPreferences.getInt("accessibility_mode", 0)
        val mode = AccessibilityModeType.entries.getOrElse(modeOrdinal) { AccessibilityModeType.NORMAL }
        updateModeDisplay(mode)

        binding.switchTalkback.isChecked = sharedPreferences.getBoolean("talkback_enabled", false)
        binding.switchHighContrast.isChecked = sharedPreferences.getBoolean("high_contrast_enabled", false)
        binding.switchLargeText.isChecked = sharedPreferences.getBoolean("large_text_enabled", false)
        binding.switchHaptic.isChecked = sharedPreferences.getBoolean("haptic_enabled", true)
    }

    private fun updateModeDisplay(mode: AccessibilityModeType) {
        val modeName = when (mode) {
            AccessibilityModeType.NORMAL -> getString(R.string.mode_regular)
            AccessibilityModeType.BLIND -> getString(R.string.mode_blind)
            AccessibilityModeType.DEAF -> getString(R.string.mode_deaf)
            AccessibilityModeType.LOW_VISION -> getString(R.string.mode_low_vision)
            AccessibilityModeType.SLOW_LEARNER -> getString(R.string.mode_slow_learner)
        }
        binding.tvCurrentMode.text = modeName
    }

    private fun setupListeners() {
        binding.cardMode.setOnClickListener {
            showModeSelectionDialog()
        }

        binding.switchTalkback.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("talkback_enabled", isChecked).apply()
        }

        binding.switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("high_contrast_enabled", isChecked).apply()
        }

        binding.switchLargeText.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("large_text_enabled", isChecked).apply()
        }

        binding.switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("haptic_enabled", isChecked).apply()
        }
    }

    private fun showModeSelectionDialog() {
        val modes = AccessibilityModeType.entries.toTypedArray()
        val modeNames = modes.map { mode ->
            when (mode) {
                AccessibilityModeType.NORMAL -> getString(R.string.mode_regular)
                AccessibilityModeType.BLIND -> getString(R.string.mode_blind)
                AccessibilityModeType.DEAF -> getString(R.string.mode_deaf)
                AccessibilityModeType.LOW_VISION -> getString(R.string.mode_low_vision)
                AccessibilityModeType.SLOW_LEARNER -> getString(R.string.mode_slow_learner)
            }
        }.toTypedArray()

        val currentMode = sharedPreferences.getInt("accessibility_mode", 0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.accessibility_mode)
            .setSingleChoiceItems(modeNames, currentMode) { dialog, which ->
                val selectedMode = modes[which]
                sharedPreferences.edit().putInt("accessibility_mode", which).apply()
                updateModeDisplay(selectedMode)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
