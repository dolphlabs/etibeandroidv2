package com.etibe.app.auth

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.R
import com.etibe.app.databinding.FragmentCreateAccountOtpBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.ErrorResponse
import com.etibe.app.utils.VerifyEmailRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch

class CreateAccountOtp : Fragment() {

    private var _binding: FragmentCreateAccountOtpBinding? = null
    private val binding get() = _binding!!

    private var email: String = ""
    private var countdownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get email from arguments or SharedPreferences
        email = arguments?.getString("email")
            ?: requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("registered_email", "") ?: ""

        if (email.isBlank()) {
            Toast.makeText(context, "No email found. Please sign up again.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        binding.tvEmail.text = email

        setupOtpInputs()
        setupClickListeners()
        startResendTimer()
    }

    private fun setupOtpInputs() {
        val otpFields = listOf(
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        )

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    } else if (s?.isEmpty() == true && index > 0) {
                        otpFields[index - 1].requestFocus()
                    }
                }
            })

            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE && index == otpFields.size - 1) {
                    verifyOtp()
                    true
                } else false
            }
        }

        // Auto-focus first field
        otpFields.first().requestFocus()
    }

    private fun setupClickListeners() {
        binding.apply {
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }

            btnVerify.setOnClickListener {
                verifyOtp()
            }

            tvResend.setOnClickListener {
                if (tvResend.isEnabled) {
                    resendOtp()
                }
            }

            tvChangeEmail.setOnClickListener {
                findNavController().popBackStack() // or navigate back to signup
            }
        }
    }

    private fun verifyOtp() {
        val otp = getOtpInput()
        if (otp.length != 6) {
            Toast.makeText(context, "Please enter complete OTP", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val request = VerifyEmailRequest(email = email, otp = otp)
                val response = RetrofitClient.instance(requireContext()).verifyEmail(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(context, "Email verified! Wallet ready.", Toast.LENGTH_LONG).show()
                        // Navigate to home or onboarding complete screen
                        findNavController().navigate(R.id.action_createAccountOtp_to_login)
                    } else {
                        showError("Verification failed – unexpected response")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = parseError(errorBody) ?: "Invalid OTP or session"
                    showError(errorMsg)
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun resendOtp() {
        showLoading(true)
        binding.tvResend.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance(requireContext()).resendOtp()

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "New OTP sent to your email", Toast.LENGTH_LONG).show()
                    startResendTimer()
                } else {
                    showError("Failed to resend OTP")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun startResendTimer() {
        binding.tvResend.isEnabled = false
        binding.tvTimer.isVisible = true

        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(60_000, 1000) { // 60 seconds
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000)
                binding.tvTimer.text = " ($seconds s)"
            }

            override fun onFinish() {
                binding.tvResend.isEnabled = true
                binding.tvTimer.isVisible = false
            }
        }.start()
    }

    private fun getOtpInput(): String {
        return listOf(
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        ).joinToString("") { it.text.toString() }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay?.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnVerify.isEnabled = !show
        if (show) {
            binding.btnVerify.text = "Verifying..."
        } else {
            binding.btnVerify.text = "Verify"
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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

    override fun onDestroyView() {
        countdownTimer?.cancel()
        super.onDestroyView()
        _binding = null
    }
}