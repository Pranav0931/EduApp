package com.hdaf.eduapp.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.FragmentOnboardingPageBinding

/**
 * Individual onboarding page showing a single feature.
 */
class OnboardingPageFragment : Fragment() {

    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    private val pagePosition: Int
        get() = arguments?.getInt(ARG_POSITION) ?: 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPage()
    }

    private fun setupPage() {
        val titles = resources.getStringArray(R.array.onboarding_titles)
        val descriptions = resources.getStringArray(R.array.onboarding_descriptions)
        val icons = listOf(
            R.drawable.ic_onboarding_audio,
            R.drawable.ic_onboarding_video,
            R.drawable.ic_onboarding_ai
        )

        binding.apply {
            tvTitle.text = titles.getOrNull(pagePosition) ?: ""
            tvDescription.text = descriptions.getOrNull(pagePosition) ?: ""
            ivIcon.setImageResource(icons.getOrNull(pagePosition) ?: R.drawable.ic_book)

            // Accessibility description
            root.contentDescription = "${titles.getOrNull(pagePosition)}. ${descriptions.getOrNull(pagePosition)}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
        }
    }
}
