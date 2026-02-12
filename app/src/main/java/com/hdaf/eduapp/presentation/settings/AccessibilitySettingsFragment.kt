package com.hdaf.eduapp.presentation.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.TTSManager
import com.hdaf.eduapp.accessibility.VoiceGuidanceManager
import com.hdaf.eduapp.core.accessibility.AudioDescriptionManager
import com.hdaf.eduapp.core.accessibility.BlindGestureManager
import com.hdaf.eduapp.core.accessibility.BrailleHapticManager
import com.hdaf.eduapp.core.accessibility.CaptionManager
import com.hdaf.eduapp.core.accessibility.DeafAlertManager
import com.hdaf.eduapp.core.accessibility.SignLanguageManager
import com.hdaf.eduapp.core.accessibility.VoiceControlManager
import com.hdaf.eduapp.databinding.FragmentAccessibilitySettingsBinding
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

/**
 * Fragment for detailed accessibility settings.
 * Includes voice speed, pitch controls, and TalkBack settings.
 * Now includes deaf/blind specific features.
 */
@AndroidEntryPoint
class AccessibilitySettingsFragment : Fragment() {

    private var _binding: FragmentAccessibilitySettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    
    // Deaf mode managers
    @Inject
    lateinit var deafAlertManager: DeafAlertManager
    
    @Inject
    lateinit var signLanguageManager: SignLanguageManager
    
    @Inject
    lateinit var captionManager: CaptionManager
    
    // Blind mode managers
    @Inject
    lateinit var voiceControlManager: VoiceControlManager
    
    @Inject
    lateinit var blindGestureManager: BlindGestureManager
    
    @Inject
    lateinit var audioDescriptionManager: AudioDescriptionManager
    
    @Inject
    lateinit var brailleHapticManager: BrailleHapticManager

    private lateinit var prefManager: PreferenceManager
    private lateinit var voiceGuidance: VoiceGuidanceManager
    private lateinit var ttsManager: TTSManager

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

        prefManager = PreferenceManager.getInstance(requireContext())
        voiceGuidance = VoiceGuidanceManager.getInstance(requireContext())
        ttsManager = TTSManager.getInstance()

        setupToolbar()
        loadSettings()
        setupListeners()
        setupVoiceControls()
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

        binding.switchTalkback.isChecked = prefManager.isVoiceGuidanceEnabled()
        binding.switchHighContrast.isChecked = prefManager.isHighContrastEnabled()
        binding.switchLargeText.isChecked = prefManager.isLargeTextEnabled()
        binding.switchHaptic.isChecked = prefManager.isHapticFeedbackEnabled()
        
        // Load deaf mode settings
        binding.switchSignLanguage.isChecked = sharedPreferences.getBoolean("sign_language_enabled", false)
        binding.switchCaptions.isChecked = sharedPreferences.getBoolean("captions_enabled", false)
        binding.switchVisualAlerts.isChecked = sharedPreferences.getBoolean("visual_alerts_enabled", false)
        
        // Load blind mode settings
        binding.switchVoiceControl.isChecked = sharedPreferences.getBoolean("voice_control_enabled", false)
        binding.switchGestureNav.isChecked = sharedPreferences.getBoolean("gesture_nav_enabled", true)
        binding.switchAudioDesc.isChecked = sharedPreferences.getBoolean("audio_desc_enabled", true)
        binding.switchBrailleHaptics.isChecked = sharedPreferences.getBoolean("braille_haptics_enabled", false)
    }

    private fun setupVoiceControls() {
        // Load current values
        val currentSpeed = prefManager.getTtsSpeed()
        val currentPitch = prefManager.getVoicePitch()

        binding.sliderVoiceSpeed.value = currentSpeed
        binding.sliderVoicePitch.value = currentPitch
        
        updateSpeedDisplay(currentSpeed)
        updatePitchDisplay(currentPitch)

        // Speed slider listener
        binding.sliderVoiceSpeed.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                updateSpeedDisplay(value)
                prefManager.setTtsSpeed(value)
                ttsManager.setSpeechRate(value)
            }
        }

        // Pitch slider listener
        binding.sliderVoicePitch.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                updatePitchDisplay(value)
                prefManager.setVoicePitch(value)
                voiceGuidance.setSpeechPitch(value)
            }
        }

        // Test voice button
        binding.btnTestVoice.setOnClickListener {
            val testMessage = getString(R.string.test_voice_message)
            ttsManager.speak(testMessage)
        }
    }

    private fun updateSpeedDisplay(speed: Float) {
        binding.tvVoiceSpeed.text = String.format(Locale.getDefault(), "%.2fx", speed)
    }

    private fun updatePitchDisplay(pitch: Float) {
        binding.tvVoicePitch.text = String.format(Locale.getDefault(), "%.1f", pitch)
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
            prefManager.setVoiceGuidanceEnabled(isChecked)
            voiceGuidance.setEnabled(isChecked)
            if (isChecked) {
                voiceGuidance.announce(getString(R.string.talkback_support) + " enabled", 
                    VoiceGuidanceManager.AnnouncementType.CONFIRMATION)
            }
        }

        binding.switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
            prefManager.setHighContrastEnabled(isChecked)
            // Could trigger theme change here
        }

        binding.switchLargeText.setOnCheckedChangeListener { _, isChecked ->
            prefManager.setLargeTextEnabled(isChecked)
            // Could trigger font scale change here
        }

        binding.switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            prefManager.setHapticFeedbackEnabled(isChecked)
            voiceGuidance.setHapticEnabled(isChecked)
        }
        
        // Deaf mode listeners
        binding.switchSignLanguage.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("sign_language_enabled", isChecked).apply()
            signLanguageManager.setEnabled(isChecked)
            if (isChecked) {
                Toast.makeText(requireContext(), "Sign Language enabled / सांकेतिक भाषा सक्षम", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.switchCaptions.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("captions_enabled", isChecked).apply()
            captionManager.setEnabled(isChecked)
            if (isChecked) {
                Toast.makeText(requireContext(), "Captions enabled / कैप्शन सक्षम", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.switchVisualAlerts.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("visual_alerts_enabled", isChecked).apply()
            deafAlertManager.setEnabled(isChecked)
            if (isChecked) {
                Toast.makeText(requireContext(), "Visual alerts enabled / दृश्य अलर्ट सक्षम", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnTestDeaf.setOnClickListener {
            testDeafFeatures()
        }
        
        // Blind mode listeners
        binding.switchVoiceControl.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("voice_control_enabled", isChecked).apply()
            voiceControlManager.setEnabled(isChecked)
            if (isChecked) {
                voiceGuidance.announce("Voice control enabled. Say commands like 'next' or 'play'.", 
                    VoiceGuidanceManager.AnnouncementType.CONFIRMATION)
            }
        }
        
        binding.switchGestureNav.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("gesture_nav_enabled", isChecked).apply()
            blindGestureManager.setEnabled(isChecked)
            if (isChecked) {
                voiceGuidance.announce("Gesture navigation enabled. Swipe to navigate.", 
                    VoiceGuidanceManager.AnnouncementType.CONFIRMATION)
            }
        }
        
        binding.switchAudioDesc.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("audio_desc_enabled", isChecked).apply()
            audioDescriptionManager.setEnabled(isChecked)
            if (isChecked) {
                voiceGuidance.announce("Audio descriptions enabled. Images and charts will be described.", 
                    VoiceGuidanceManager.AnnouncementType.CONFIRMATION)
            }
        }
        
        binding.switchBrailleHaptics.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("braille_haptics_enabled", isChecked).apply()
            brailleHapticManager.setEnabled(isChecked)
            if (isChecked) {
                // Demo the braille haptic pattern
                brailleHapticManager.indicateQuizResult(true)
                voiceGuidance.announce("Braille haptics enabled. Feel vibration patterns for navigation.", 
                    VoiceGuidanceManager.AnnouncementType.CONFIRMATION)
            }
        }
        
        binding.btnTestBlind.setOnClickListener {
            testBlindFeatures()
        }
    }
    
    private fun testDeafFeatures() {
        // Show visual alert demo
        deafAlertManager.showAlert(
            com.hdaf.eduapp.core.accessibility.AlertType.SUCCESS,
            "Test successful!",
            "परीक्षण सफल!"
        )
        
        // Show caption
        captionManager.showCaption(
            text = "This is a test caption. / यह एक परीक्षण कैप्शन है।",
            speaker = "EduApp"
        )
        
        Toast.makeText(requireContext(), getString(R.string.deaf_mode_enabled), Toast.LENGTH_LONG).show()
    }
    
    private fun testBlindFeatures() {
        // Announce with TTS
        voiceGuidance.announce(
            getString(R.string.blind_mode_enabled),
            VoiceGuidanceManager.AnnouncementType.CONFIRMATION
        )
        
        // Demo braille haptic pattern
        brailleHapticManager.indicateDirection(com.hdaf.eduapp.core.accessibility.Direction.FORWARD)
        
        // Demo audio description
        val description = audioDescriptionManager.describeMathEquation("2 + 3 = 5", isHindi = false)
        voiceGuidance.announce(
            "Math example: ${description.fullDescription}",
            VoiceGuidanceManager.AnnouncementType.INFORMATION
        )
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
                voiceGuidance.announceSelection(modeNames[which])
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
