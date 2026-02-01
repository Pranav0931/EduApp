package com.hdaf.eduapp.presentation.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentQuizResultBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Quiz result screen showing score, performance, and rewards.
 */
@AndroidEntryPoint
class QuizResultFragment : Fragment() {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

    private val chapterId: String by lazy { arguments?.getString(ARG_CHAPTER_ID) ?: "" }
    private val score: Int by lazy { arguments?.getInt(ARG_SCORE) ?: 0 }
    private val totalQuestions: Int by lazy { arguments?.getInt(ARG_TOTAL_QUESTIONS) ?: 0 }
    private val correctAnswers: Int by lazy { arguments?.getInt(ARG_CORRECT_ANSWERS) ?: 0 }

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayResults()
        setupButtons()
        announceForAccessibility()
    }

    private fun displayResults() {
        val percentage = (score * 100) / totalQuestions.coerceAtLeast(1)
        
        binding.apply {
            // Score display
            tvScore.text = "${score}/${totalQuestions}"
            tvPercentage.text = getString(R.string.percentage_format, percentage)
            progressScore.progress = percentage

            // Performance message and emoji
            val (message, emoji, color) = when {
                percentage >= 90 -> Triple(
                    getString(R.string.excellent_performance),
                    "🏆",
                    R.color.success
                )
                percentage >= 70 -> Triple(
                    getString(R.string.good_performance),
                    "🎉",
                    R.color.primary
                )
                percentage >= 50 -> Triple(
                    getString(R.string.average_performance),
                    "💪",
                    R.color.warning
                )
                else -> Triple(
                    getString(R.string.needs_improvement),
                    "📚",
                    R.color.error
                )
            }

            tvEmoji.text = emoji
            tvMessage.text = message
            tvMessage.setTextColor(requireContext().getColor(color))

            // Stats
            tvCorrect.text = correctAnswers.toString()
            tvIncorrect.text = (totalQuestions - correctAnswers).toString()
            tvAccuracy.text = getString(R.string.percentage_format, percentage)

            // XP earned (simple calculation)
            val xpEarned = correctAnswers * 10
            tvXpEarned.text = "+$xpEarned XP"
        }
    }

    private fun setupButtons() {
        binding.apply {
            btnRetry.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                findNavController().popBackStack()
            }

            btnHome.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.NAVIGATION)
                findNavController().navigate(R.id.homeFragment)
            }

            btnShare.setOnClickListener {
                accessibilityManager.provideHapticFeedback(HapticType.CLICK)
                shareResults()
            }
        }
    }

    private fun announceForAccessibility() {
        val percentage = (score * 100) / totalQuestions.coerceAtLeast(1)
        accessibilityManager.speak(
            "प्रश्नोत्तरी पूर्ण। आपका स्कोर ${score} में से ${totalQuestions}। " +
            "प्रतिशत $percentage"
        )
    }

    private fun shareResults() {
        val percentage = (score * 100) / totalQuestions.coerceAtLeast(1)
        val shareText = buildString {
            append("मैंने EduApp पर प्रश्नोत्तरी पूर्ण की! 🎉\n")
            append("स्कोर: ${score}/${totalQuestions} ($percentage%)\n")
            append("EduApp - सबके लिए शिक्षा!")
        }

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        startActivity(android.content.Intent.createChooser(intent, getString(R.string.share_results)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_CHAPTER_ID = "chapterId"
        const val ARG_SCORE = "score"
        const val ARG_TOTAL_QUESTIONS = "totalQuestions"
        const val ARG_CORRECT_ANSWERS = "correctAnswers"
    }
}
