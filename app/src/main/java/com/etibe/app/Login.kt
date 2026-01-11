package com.etibe.app

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.etibe.app.databinding.FragmentLoginBinding


class Login : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.submit.setOnClickListener {
            showLoading(true)
            val intent = Intent(requireContext(), HomeActivity::class.java)
            activity?.startActivity(intent)
        }



        return binding.root
    }


    private fun showLoading(show: Boolean) {
        binding.progressBar.apply {
            indeterminateDrawable.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary_green),
                PorterDuff.Mode.SRC_IN
            )
            val loadingOverlay = binding.loadingOverlay
            loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


