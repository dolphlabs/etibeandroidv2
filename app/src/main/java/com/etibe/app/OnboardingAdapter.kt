package com.etibe.app
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter



class OnboardingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    companion object {
        const val TOTAL_PAGES = 3
    }

    override fun getItemCount(): Int = TOTAL_PAGES

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingPageFragment.newInstance(
                title = "Your Circle Deserves\nMore Than Guesswork",
                description = "Traditional contributions get messy, missed turns, confusion, zero transparency. You need a system that actually keeps everyone aligned.",
                imageRes = R.drawable.img_onboarding_1,
                highlightWords = arrayOf("missed turns,", "confusion,", "zero transparency."),
                showBackButton = false
            )
            1 -> OnboardingPageFragment.newInstance(
                title = "Etibe Reinvented With\nCrypto Precision",
                description = "Track contributions, secure payments, automate order, and keep the entire circle accountable — all powered by blockchain-level trust.",
                imageRes = R.drawable.img_onboarding_2,
                highlightWords = arrayOf("secure payments,", "automate order,", " and keep the entire circle accountable"),
                highlightColor = R.color.highlight_red,
                showBackButton = true
            )
            2 -> OnboardingPageFragment.newInstance(
                title = "Build Your Circle.\nSecure Your Savings.",
                description = "",
                imageRes = R.drawable.img_onboarding_3,
                isLastPage = true,
                showBackButton = true
            )
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}