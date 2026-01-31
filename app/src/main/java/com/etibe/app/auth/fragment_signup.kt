package com.etibe.app.auth

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.R
import com.etibe.app.databinding.FragmentSignupBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.ErrorResponse
import com.etibe.app.utils.RegisterRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch

class fragment_signup : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputValidation()
        setupClickListeners()
    }

    private fun setupInputValidation() {
        listOf(
            binding.etDisplayName to binding.tilDisplayName,
            binding.etEmail to binding.tilEmail,
            binding.etPassword to binding.tilPassword,
            binding.etConfirmPassword to binding.tilConfirmPassword
        ).forEach { (et, til) ->
            et.doAfterTextChanged { til.error = null }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnGetStarted.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            registerUser()
        }

        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_signup_to_login)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val displayName = binding.etDisplayName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (displayName.isEmpty()) {
            binding.tilDisplayName.error = "Display name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun registerUser() = lifecycleScope.launch {
        // Show loading
        showLoading(true)
        binding.btnGetStarted.isEnabled = false
        binding.btnGetStarted.text = "Creating account..."

        // Clear previous errors
        clearAllFieldErrors()

        try {
            val emailInput = binding.etEmail.text.toString().trim()
            val request = RegisterRequest(
                email = emailInput,
                username = binding.etDisplayName.text.toString().trim(),
                firstName = binding.etDisplayName.text.toString().trim().split(" ").firstOrNull()
                    ?: "",
                lastName = binding.etDisplayName.text.toString().trim().split(" ").lastOrNull()
                    ?: "",
                password = binding.etPassword.text.toString()
            )

            val response = RetrofitClient.instance(requireContext()).register(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    // Save email to SharedPreferences
                    saveEmailToPreferences(emailInput)

                    Snackbar.make(
                        binding.root,
                        body.data?.message ?: "Registration successful! Check your email.",
                        Snackbar.LENGTH_LONG
                    ).show()

                    findNavController().navigate(R.id.action_fragment_signup_to_createAccountOtp)
                } else {
                    showGeneralError("Registration failed – unexpected response")
                }
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseBackendError(errorBodyString)

                showFieldErrors(errorMessage)

                if (errorMessage.isNotBlank()) {
                    showGeneralError(errorMessage)
                } else {
                    val fallbackMsg = when (response.code()) {
                        400 -> "Invalid input – please check your details"
                        409 -> "Email or username already exists"
                        else -> "Server error (${response.code()})"
                    }
                    showGeneralError(fallbackMsg)
                }
            }
        } catch (e: Exception) {
            showGeneralError("Network error: ${e.message ?: "Something went wrong"}")
        } finally {
            showLoading(false)
            binding.btnGetStarted.isEnabled = true
            binding.btnGetStarted.text = "Get started"
        }
    }

    // ─────────────────────────────────────────────
    // Loading state
    // ─────────────────────────────────────────────
    private fun showLoading(show: Boolean) {
        binding.loadingOverlay?.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            binding.progressBar?.indeterminateDrawable?.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary_green),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    // ─────────────────────────────────────────────
    // Save email
    // ─────────────────────────────────────────────
    private fun saveEmailToPreferences(email: String) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("registered_email", email)
            .apply()
    }

    // ─────────────────────────────────────────────
    // Error parsing & display (unchanged from your version)
    // ─────────────────────────────────────────────
    private fun parseBackendError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return ""

        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            val details = errorResponse.error?.details?.validationErrors
            if (!details.isNullOrEmpty()) {
                details.joinToString("\n")
            } else {
                errorResponse.error?.message ?: "Unknown error"
            }
        } catch (e: Exception) {
            "Error parsing server response"
        }
    }

    private fun showFieldErrors(errorMessage: String) {
        when {
            errorMessage.contains("Username", ignoreCase = true) -> {
                binding.tilDisplayName.error = errorMessage
            }

            errorMessage.contains("Email", ignoreCase = true) -> {
                binding.tilEmail.error = errorMessage
            }

            errorMessage.contains("Password", ignoreCase = true) -> {
                binding.tilPassword.error = errorMessage
            }

            else -> {
                binding.tilConfirmPassword.error = errorMessage
            }
        }
    }

    private fun showGeneralError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun clearAllFieldErrors() {
        binding.tilDisplayName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}