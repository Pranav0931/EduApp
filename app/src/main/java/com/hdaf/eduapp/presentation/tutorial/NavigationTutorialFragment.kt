package com.hdaf.eduapp.presentation.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.TTSManager
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Navigation Tutorial Fragment
 * 
 * Interactive walkthrough explaining how to use the app's accessibility features.
 * Includes:
 * - Gesture navigation for blind users
 * - Visual features for deaf users
 * - Button and control explanations
 * - Audio narration with subtitles
 */
@AndroidEntryPoint
class NavigationTutorialFragment : Fragment() {

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private lateinit var ttsManager: TTSManager
    private lateinit var prefManager: PreferenceManager

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: MaterialButton
    private lateinit var fabSpeak: FloatingActionButton

    private lateinit var tutorialPages: List<TutorialPage>
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigation_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize singletons
        prefManager = PreferenceManager.getInstance(requireContext())
        ttsManager = TTSManager.getInstance()
        if (!ttsManager.isReady()) {
            ttsManager.initialize(requireContext())
        }

        initViews(view)
        setupTutorialPages()
        setupViewPager()
        setupIndicators()
        setupButtons()
        
        // Announce first page for screen readers
        announceCurrentPage()
    }

    private fun initViews(view: View) {
        viewPager = view.findViewById(R.id.viewPagerTutorial)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)
        btnNext = view.findViewById(R.id.btnNext)
        btnSkip = view.findViewById(R.id.btnSkip)
        fabSpeak = view.findViewById(R.id.fabSpeak)
    }

    private fun setupTutorialPages() {
        tutorialPages = listOf(
            TutorialPage(
                iconRes = R.drawable.ic_accessibility,
                titleRes = R.string.tutorial_welcome_title,
                descriptionRes = R.string.tutorial_welcome_description,
                subtitleRes = R.string.tutorial_welcome_subtitle,
                ttsText = getString(R.string.tutorial_welcome_tts)
            ),
            TutorialPage(
                iconRes = R.drawable.ic_gesture,
                titleRes = R.string.tutorial_gestures_title,
                descriptionRes = R.string.tutorial_gestures_description,
                subtitleRes = R.string.tutorial_gestures_subtitle,
                ttsText = getString(R.string.tutorial_gestures_tts)
            ),
            TutorialPage(
                iconRes = R.drawable.ic_hearing,
                titleRes = R.string.tutorial_deaf_title,
                descriptionRes = R.string.tutorial_deaf_description,
                subtitleRes = R.string.tutorial_deaf_subtitle,
                ttsText = getString(R.string.tutorial_deaf_tts)
            ),
            TutorialPage(
                iconRes = R.drawable.ic_visibility,
                titleRes = R.string.tutorial_blind_title,
                descriptionRes = R.string.tutorial_blind_description,
                subtitleRes = R.string.tutorial_blind_subtitle,
                ttsText = getString(R.string.tutorial_blind_tts)
            ),
            TutorialPage(
                iconRes = R.drawable.ic_quiz,
                titleRes = R.string.tutorial_navigation_title,
                descriptionRes = R.string.tutorial_navigation_description,
                subtitleRes = R.string.tutorial_navigation_subtitle,
                ttsText = getString(R.string.tutorial_navigation_tts)
            )
        )
    }

    private fun setupViewPager() {
        val adapter = TutorialPagerAdapter(tutorialPages)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                updateIndicators(position)
                updateNextButton(position)
                announceCurrentPage()
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
            }
        })
    }

    private fun setupIndicators() {
        indicatorContainer.removeAllViews()
        
        for (i in tutorialPages.indices) {
            val indicator = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.indicator_size),
                    resources.getDimensionPixelSize(R.dimen.indicator_size)
                ).apply {
                    marginStart = 8
                    marginEnd = 8
                }
                background = ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive)
                contentDescription = "Page ${i + 1} of ${tutorialPages.size}"
            }
            indicatorContainer.addView(indicator)
        }
        
        updateIndicators(0)
    }

    private fun updateIndicators(selectedPosition: Int) {
        for (i in 0 until indicatorContainer.childCount) {
            val indicator = indicatorContainer.getChildAt(i)
            val drawableRes = if (i == selectedPosition) {
                R.drawable.indicator_active
            } else {
                R.drawable.indicator_inactive
            }
            indicator.background = ContextCompat.getDrawable(requireContext(), drawableRes)
        }
    }

    private fun updateNextButton(position: Int) {
        if (position == tutorialPages.size - 1) {
            btnNext.text = getString(R.string.tutorial_get_started)
            btnNext.contentDescription = "Finish tutorial and start using the app"
        } else {
            btnNext.text = getString(R.string.onboarding_next)
            btnNext.contentDescription = "Go to next tutorial page"
        }
    }

    private fun setupButtons() {
        btnNext.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            
            if (currentPage < tutorialPages.size - 1) {
                viewPager.currentItem = currentPage + 1
            } else {
                completeTutorial()
            }
        }

        btnSkip.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            ttsManager.speak("Skipping tutorial")
            completeTutorial()
        }

        fabSpeak.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            speakCurrentPage()
        }
    }

    private fun speakCurrentPage() {
        val page = tutorialPages[currentPage]
        ttsManager.speak(page.ttsText)
    }

    private fun announceCurrentPage() {
        val page = tutorialPages[currentPage]
        val title = getString(page.titleRes)
        view?.announceForAccessibility("Page ${currentPage + 1} of ${tutorialPages.size}: $title")
    }

    private fun completeTutorial() {
        // Mark tutorial as completed
        prefManager.setBoolean("tutorial_completed", true)
        
        ttsManager.speak("Tutorial complete. Welcome to EduApp!")
        accessibilityManager.provideHapticFeedback(HapticType.SUCCESS)
        
        // Navigate to home
        findNavController().navigate(R.id.action_navigationTutorial_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsManager.stop()
    }
}
