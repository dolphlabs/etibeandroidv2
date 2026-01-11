package com.etibe.app

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.etibe.app.databinding.FragmentSplashScrenBinding


class SplashScren : Fragment() {
    private var _binding: FragmentSplashScrenBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSplashScrenBinding.inflate(inflater, container, false)
        Handler().postDelayed({
            findNavController().navigate(R.id.action_splashScren_to_onboarding)
        }, 3000)
        return binding.root
    }


}


