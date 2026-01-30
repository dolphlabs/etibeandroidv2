package com.etibe.app.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputValidation()
        setupClickListeners()
    }

    private fun setupInputValidation() {
        // Clear errors on input change
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
            // or popBackStack() if login is previous screen
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
        binding.btnGetStarted.isEnabled = false
        binding.btnGetStarted.text = "Creating account..."

        // Clear previous errors
        clearAllFieldErrors()

        try {
            val request = RegisterRequest(
                email = binding.etEmail.text.toString().trim(),
                username = binding.etDisplayName.text.toString().trim(),
                firstName = binding.etDisplayName.text.toString().trim().split(" ").firstOrNull() ?: "",
                lastName = binding.etDisplayName.text.toString().trim().split(" ").lastOrNull() ?: "",
                password = binding.etPassword.text.toString()
            )

            val response = RetrofitClient.instance(requireContext()).register(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Toast.makeText(
                        context,
                        body.data?.message ?: "Registration successful! Check your email.",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().navigate(R.id.action_fragment_signup_to_login)
                } else {
                    showGeneralError("Registration failed – unexpected response")
                }
            } else {
                // Handle error response (400, 409, etc.)
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseBackendError(errorBodyString)

                // Show specific field errors if possible, or general message
                showFieldErrors(errorMessage)

                // Fallback general toast (only if no field-specific error)
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
            binding.btnGetStarted.isEnabled = true
            binding.btnGetStarted.text = "Get started"
        }
    }

    private fun parseBackendError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return ""

        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            val details = errorResponse.error?.details?.validationErrors

            if (!details.isNullOrEmpty()) {
                // Join all validation messages (or take first one)
                details.joinToString("\n")
            } else {
                errorResponse.error?.message ?: "Unknown error"
            }
        } catch (e: Exception) {
            "Error parsing server response"
        }
    }

    private fun showFieldErrors(errorMessage: String) {
        // Very basic keyword-based mapping – improve later with proper error codes
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
                // Fallback: show under confirm password or general
                binding.tilConfirmPassword.error = errorMessage
            }
        }
    }

    private fun showGeneralError(message: String) {
        // Option 1: Snackbar at bottom (recommended)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()

        // Option 2: Set error on a dedicated TextView (add one in layout if you want)
        // binding.tvGeneralError?.text = message
        // binding.tvGeneralError?.isVisible = true

        // Option 3: AlertDialog (if you prefer modal)
        // AlertDialog.Builder(requireContext())
        //     .setTitle("Registration Error")
        //     .setMessage(message)
        //     .setPositiveButton("OK", null)
        //     .show()
    }

    private fun clearAllFieldErrors() {
        binding.tilDisplayName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        // binding.tvGeneralError?.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}