package com.etibe.app.etibe

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.R
import com.etibe.app.databinding.FragmentJoinEtibeBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.ErrorResponse
import com.etibe.app.utils.JoinCircleRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch

class JoinEtibe : Fragment() {

    private var _binding: FragmentJoinEtibeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinEtibeBinding.inflate(inflater, container, false)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {


            btnJoin.setOnClickListener {
                joinCircle()
            }

            tilInviteCode.setEndIconOnClickListener {
                Snackbar.make(root, "Enter the invite code shared with you", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun joinCircle() = lifecycleScope.launch {
        val inviteCode = binding.etInviteCode.text.toString().trim()

        if (inviteCode.isEmpty()) {
            binding.tilInviteCode.error = "Invite code is required"
            return@launch
        }

        showLoading(true)
        binding.btnJoin.isEnabled = false
        binding.btnJoin.text = "Joining..."

        binding.tvError.isVisible = false
        binding.tilInviteCode.error = null

        try {
            val request = JoinCircleRequest(inviteCode = inviteCode)
            val response = RetrofitClient.instance(requireContext()).joinCircle(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Snackbar.make(binding.root, "Successfully joined the Etibé!", Snackbar.LENGTH_LONG).show()


            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = parseError(errorBody) ?: "Failed to join circle"

                if (response.code() == 401) {
                    RetrofitClient.clearTokens(requireContext())
                } else {
                    binding.tvError.text = errorMsg
                    binding.tvError.isVisible = true
                }
            }
        } catch (e: Exception) {
            binding.tvError.text = "Network error: ${e.message}"
            binding.tvError.isVisible = true
        } finally {
            showLoading(false)
            binding.btnJoin.isEnabled = true
            binding.btnJoin.text = "Join Etibé"
        }
    }

    private fun parseError(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
            error.error?.message ?: error.error?.details?.validationErrors?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay?.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.progressBar?.indeterminateDrawable?.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary_green),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}