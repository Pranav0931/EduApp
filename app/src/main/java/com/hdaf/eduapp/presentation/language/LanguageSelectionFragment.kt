package com.hdaf.eduapp.presentation.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.TTSManager
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentLanguageSelectionBinding
import com.hdaf.eduapp.utils.LocaleHelper
import com.hdaf.eduapp.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Language selection screen shown on first app launch.
 * Allows users to choose between Hindi, English, or Marathi.
 * Includes TTS support for blind users.
 */
@AndroidEntryPoint
class LanguageSelectionFragment : Fragment() {

    private var _binding: FragmentLanguageSelectionBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var prefManager: PreferenceManager
    private lateinit var ttsManager: TTSManager

    private var selectedLanguage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager.getInstance(requireContext())
        ttsManager = TTSManager.getInstance()
        
        if (!ttsManager.isReady()) {
            ttsManager.initialize(requireContext())
        }

        setupClickListeners()
        setupAccessibility()

        // Announce screen for accessibility
        view.postDelayed({
            speakWelcome()
        }, 500)
    }

    private fun setupClickListeners() {
        binding.cardHindi.setOnClickListener {
            selectLanguage("hi")
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            ttsManager.speak("हिंदी चुनी गई")
        }

        binding.cardEnglish.setOnClickListener {
            selectLanguage("en")
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            ttsManager.speak("English selected")
        }

        binding.cardMarathi.setOnClickListener {
            selectLanguage("mr")
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            ttsManager.speak("मराठी निवडली")
        }

        binding.btnContinue.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
            applyLanguageAndContinue()
        }

        binding.fabSpeak.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            speakAllOptions()
        }
    }

    private fun selectLanguage(languageCode: String) {
        selectedLanguage = languageCode

        // Update UI to show selection
        binding.ivHindiCheck.visibility = if (languageCode == "hi") View.VISIBLE else View.GONE
        binding.ivEnglishCheck.visibility = if (languageCode == "en") View.VISIBLE else View.GONE
        binding.ivMarathiCheck.visibility = if (languageCode == "mr") View.VISIBLE else View.GONE

        // Update card strokes to show selection
        val selectedStrokeColor = resources.getColor(R.color.primary_magenta, null)
        val unselectedStrokeColor = android.graphics.Color.TRANSPARENT

        binding.cardHindi.strokeColor = if (languageCode == "hi") selectedStrokeColor else unselectedStrokeColor
        binding.cardEnglish.strokeColor = if (languageCode == "en") selectedStrokeColor else unselectedStrokeColor
        binding.cardMarathi.strokeColor = if (languageCode == "mr") selectedStrokeColor else unselectedStrokeColor

        // Enable continue button
        binding.btnContinue.isEnabled = true
    }

    private fun applyLanguageAndContinue() {
        selectedLanguage?.let { langCode ->
            // Save language preference
            prefManager.putString("selected_language", langCode)
            
            // Apply locale
            LocaleHelper.setLocale(requireContext(), langCode)

            // Navigate to onboarding
            findNavController().navigate(
                LanguageSelectionFragmentDirections.actionLanguageSelectionToOnboarding()
            )
        }
    }

    private fun setupAccessibility() {
        // Set focus change listeners for TalkBack
        binding.cardHindi.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ttsManager.speak("हिंदी। Hindi language option।")
            }
        }

        binding.cardEnglish.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ttsManager.speak("English। अंग्रेज़ी भाषा।")
            }
        }

        binding.cardMarathi.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ttsManager.speak("मराठी। Marathi language option।")
            }
        }
    }

    private fun speakWelcome() {
        val message = "Welcome to EduApp। अपनी भाषा चुनें। Choose your language। " +
                "तीन विकल्प हैं: हिंदी, English, या मराठी। " +
                "Three options are available: Hindi, English, or Marathi।"
        ttsManager.speak(message)
    }

    private fun speakAllOptions() {
        val message = "भाषा विकल्प। Language options। " +
                "पहला: हिंदी। First: Hindi। " +
                "दूसरा: English। Second: English। " +
                "तीसरा: मराठी। Third: Marathi। " +
                "कृपया अपनी पसंदीदा भाषा चुनें। Please select your preferred language।"
        ttsManager.speak(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
