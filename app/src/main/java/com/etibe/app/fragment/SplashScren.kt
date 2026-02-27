package com.etibe.app.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.etibe.app.R
import com.etibe.app.databinding.FragmentSplashScrenBinding

class SplashScren : Fragment() {

    private var _binding: FragmentSplashScrenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSplashScrenBinding.inflate(inflater, container, false)

        Handler(Looper.getMainLooper()).postDelayed({
            routeUser()
        }, 2000)   // 2 sec splash

        return binding.root
    }

    private fun routeUser() {

        val prefs = requireActivity()
            .getSharedPreferences("etibe_prefs", Context.MODE_PRIVATE)

        val onboardingCompleted =
            prefs.getBoolean("onboarding_completed", false)

        val isLoggedIn =
            prefs.getBoolean("is_logged_in", false)

        when {
            isLoggedIn -> {
                // User already logged in → go Home
              //  findNavController().navigate(R.id.action_splashScren_to_homeFragment)
            }

            onboardingCompleted -> {
                // Finished onboarding → go Login
                findNavController().navigate(R.id.action_splashScren_to_login)
            }

            else -> {
                // First install → show onboarding
                findNavController().navigate(R.id.action_splashScren_to_onboarding)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}