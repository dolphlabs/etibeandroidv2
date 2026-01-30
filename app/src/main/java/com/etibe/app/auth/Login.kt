package com.etibe.app.auth

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.activity.HomeActivity
import com.etibe.app.R
import com.etibe.app.databinding.FragmentLoginBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.LoginRequest
import com.etibe.app.utils.LoginResponse
import kotlinx.coroutines.launch

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Clear errors on input
        binding.emailEt.doAfterTextChanged {
            binding.emailAddress.error = null
            binding.emailAddress.isErrorEnabled = false
        }

        binding.etPassword.doAfterTextChanged {
            binding.passwordLayout.error = null
            binding.passwordLayout.isErrorEnabled = false
        }

        binding.submit.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            login()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val email = binding.emailEt.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.emailAddress.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun login() = lifecycleScope.launch {
        showLoading(true)
        binding.submit.isEnabled = false
        binding.submit.text = "Logging in..."

        try {
            val request = LoginRequest(
                identifier = binding.emailEt.text.toString().trim(),
                password = binding.etPassword.text.toString().trim()
            )

            val api = RetrofitClient.instance(requireContext())
            val response = api.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    saveUserSession(body)
                    startActivity(Intent(requireContext(), HomeActivity::class.java))
                    requireActivity().finish()
                } else {
                    showError(body?.data?.message ?: "Login failed - no data")
                }
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Bad request - please check your input"
                    401 -> "Invalid email or password"
                    429 -> "Too many attempts - try again later"
                    in 500..599 -> "Server error (${response.code()})"
                    else -> "Unexpected error (${response.code()})"
                }
                showError(errorMsg)
            }
        } catch (e: Exception) {
            showError("Connection error: ${e.message ?: "Please check your internet"}")
        } finally {
            showLoading(false)
            binding.submit.isEnabled = true
            binding.submit.text = "Continue"
        }
    }

    private fun saveUserSession(response: LoginResponse) {
        val data = response.data ?: return  // early exit if no data

        // Save tokens using the secure method from RetrofitClient
        RetrofitClient.saveTokens(
            context = requireContext(),
            authToken = data.accessToken,
            refreshToken = data.refreshToken
        )

        // Optional: still save some non-sensitive user info if you want
        // (but avoid saving sensitive data here)
        val prefs = requireActivity().getSharedPreferences("info", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_id", data.user.id)
            putString("email", data.user.email)
            putString("username", data.user.username)
            putString("full_name", data.user.fullName)
            putBoolean("is_verified", data.user.isVerified)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Login Failed")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE

        // Optional: color the progress (if you want to keep it)
        binding.progressBar.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.primary_green),
            PorterDuff.Mode.SRC_IN
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}