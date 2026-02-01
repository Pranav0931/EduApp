package com.hdaf.eduapp.presentation.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Pager adapter for onboarding pages.
 */
class OnboardingPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return OnboardingPageFragment.newInstance(position)
    }
}
