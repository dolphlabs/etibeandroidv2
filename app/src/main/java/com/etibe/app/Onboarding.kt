package com.etibe.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.etibe.app.databinding.FragmentOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator


class Onboarding : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var onboardingAdapter: OnboardingAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(this)
        binding.viewPager.adapter = onboardingAdapter

        // Connect the TabLayout indicator dots with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        // Handle page changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgressBar(position)
                updateNavigationVisibility(position)
            }
        })
    }

    private fun updateProgressBar(position: Int) {
        val progress = ((position + 1) * 100) / OnboardingAdapter.TOTAL_PAGES
        binding.progressBar.progress = progress
    }

    private fun updateNavigationVisibility(position: Int) {
        // Show/hide back button based on position
        binding.btnBack.visibility = if (position > 0) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




