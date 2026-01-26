package com.etibe.app

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.databinding.FragmentLoginBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.LoginRequest
import com.etibe.app.utils.User
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient


class Login : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val okHttpClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Optional: real-time validation (nice UX)
        binding.emailEt.doAfterTextChanged {
            binding.emailAddress.error = null
        }
        binding.etPassword.doAfterTextChanged {
            binding.passwordLayout.error = null
        }
        binding.emailEt.doOnTextChanged { text, start, before, count ->
            binding.emailAddress.error = null
            binding.emailAddress.isErrorEnabled = false   // optional - clears red outline faster
        }

        binding.etPassword.doOnTextChanged { _, _, _, _ ->
            binding.passwordLayout.error = null
            binding.passwordLayout.isErrorEnabled = false
        }
        binding.submit.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            login()
        }

        return binding.root
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.emailEt.text.toString().trim().isEmpty()) {
            binding.emailAddress.error = "Email is required"
            isValid = false
        }

        if (binding.etPassword.text.toString().trim().isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun login() = lifecycleScope.launch {
        showLoading(true)
        binding.submit.isEnabled = false

        try {
            val request = LoginRequest(
                identifier = binding.emailEt.text.toString().trim(),
                password = binding.etPassword.text.toString().trim()
            )

            val response = RetrofitClient.instance(requireContext()).login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    // Success!
                    body.data?.user?.let { user ->
                        // Save user & token/session
                        // Example: DataStore, EncryptedSharedPrefs, etc.
                        saveUserSession(user)
                    }


                    startActivity(Intent(requireContext(), HomeActivity::class.java))
                    requireActivity().finish()
                } else {
                    showError(body?.data?.message ?: "Login failed")
                }
            } else {
                when (response.code()) {
                    401 -> showError("Invalid credentials")
                    400 -> showError("Bad request - check your input")
                    else -> showError("Server error (${response.code()})")
                }
            }

        } catch (e: Exception) {
            showError("Network error: ${e.message ?: "Something went wrong"}")
        } finally {
            showLoading(false)
            binding.submit.isEnabled = true
        }
    }


    private fun showError(message: String) {
        // You can use a dedicated error TextView (add one to layout if needed)
        // For now using snackbar / toast is simple
        binding.submit.text = "Continue"  // reset if you changed it
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Login Failed")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()

        // OR simpler:
        // Toast.ma
    }

    private fun saveUserSession(user: User) {
            val prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("user_id", user.id)
                putString("email", user.email)
                putString("username", user.username)
                putString("full_name", user.fullName)
                putBoolean("is_verified", user.isVerified)
                putBoolean("is_logged_in", true)           // ← important flag!
                apply()
            }
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


