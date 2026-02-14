package com.etibe.app

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.databinding.FragmentCreateEtibeBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.CircleCreateRequest
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class fragment_create_etibe : Fragment() {

    private var _binding: FragmentCreateEtibeBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var selectedCurrency: String = "NEAR"
    private var selectedStartDate: String? = null
    private var createdCircleId: String? = null  // ← store ID after creation

    // Image picker
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.imageUploaded.setImageURI(uri)
                binding.imageUploaded.visibility = View.VISIBLE
                binding.btnUpload.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEtibeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        val frequencies = arrayOf("WEEKLY", "BI_WEEKLY", "MONTHLY")
        val freqAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, frequencies)
        binding.etFrequency.setAdapter(freqAdapter)
        binding.etFrequency.setText("BI_WEEKLY", false)

        val orders = arrayOf("Names numbered by entry order", "Random", "Custom")
        val orderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, orders)
        binding.etPaymentOrder.setAdapter(orderAdapter)
        binding.etPaymentOrder.setText("Names numbered by entry order", false)
    }

    private fun setupClickListeners() {
        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            cvUploadLogo.setOnClickListener { openImagePicker() }
            btnUpload.setOnClickListener { openImagePicker() }

            tilCurrency.setOnClickListener { showCurrencyBottomSheet() }
            etCurrency.setOnClickListener { showCurrencyBottomSheet() }

            tilStartDate.setOnClickListener { showDatePicker() }
            etStartDate.setOnClickListener { showDatePicker() }

            btnCreate.setOnClickListener {
                if (!validateInputs()) return@setOnClickListener
                createAndActivateCircle()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun showCurrencyBottomSheet() {
        SelectTokenBottomSheet(
            selectedToken = selectedCurrency,
            onTokenSelected = { newToken ->
                selectedCurrency = newToken
                binding.etCurrency.setText(newToken)
            }
        ).show(parentFragmentManager, "SelectToken")
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Start Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedStartDate = sdf.format(Date(selection))
            binding.etStartDate.setText(selectedStartDate)
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etGroupName.text.toString().trim().isEmpty()) {
            binding.tilGroupName.error = "Group name is required"
            isValid = false
        }

        if (binding.etAmount.text.toString().trim().isEmpty()) {
            binding.tilAmount.error = "Contribution amount is required"
            isValid = false
        }

        if (binding.etGracePeriod.text.toString().trim().isEmpty()) {
            binding.tilGracePeriod.error = "Grace period is required"
            isValid = false
        }

        val membersText = binding.etMembers.text.toString().trim()
        if (membersText.isEmpty()) {
            binding.tilMembers.error = "At least 2 members required"
            isValid = false
        } else {
            val count = membersText.split(",").filter { it.trim().isNotEmpty() }.size
            if (count < 2) {
                binding.tilMembers.error = "Minimum 2 members required"
                isValid = false
            }
        }

        if (selectedStartDate == null) {
            binding.tilStartDate.error = "Start date is required"
            isValid = false
        }

        return isValid
    }

    private fun createAndActivateCircle() = lifecycleScope.launch {
        showLoading(true)
        binding.btnCreate.isEnabled = false
        binding.btnCreate.text = "Creating..."

        try {
            // Step 1: Create circle
            val request = CircleCreateRequest(
                name = binding.etGroupName.text.toString().trim(),
                description = "",
                logoUrl = "",
                contributionSettings = CircleCreateRequest.ContributionSettings(
                    amount = binding.etAmount.text.toString().trim(),
                    currency = selectedCurrency,
                    frequency = binding.etFrequency.text.toString(),
                    gracePeriodDays = binding.etGracePeriod.text.toString().toIntOrNull() ?: 7,
                    penaltyPercentage = "0"
                ),
                maxMembers = 50,
                startDate = selectedStartDate,
                isPrivate = binding.switchPrivate.isChecked
            )

            val createResponse = RetrofitClient.instance(requireContext()).createCircle(request)

            if (createResponse.isSuccessful && createResponse.body()?.success == true) {
                val createdData = createResponse.body()?.data?.data
                createdCircleId = createdData?.id

                if (createdCircleId.isNullOrBlank()) {
                    showError("Circle created but no ID received")
                    return@launch
                }

                Toast.makeText(context, "Etibé created! Activating...", Toast.LENGTH_SHORT).show()

                // Step 2: Activate circle
                binding.btnCreate.text = "Activating..."
                val activateResponse = RetrofitClient.instance(requireContext()).activateCircle(createdCircleId!!)

                if (activateResponse.isSuccessful && activateResponse.body()?.success == true) {
                    val message = activateResponse.body()?.data?.message
                        ?: "Etibé activated successfully!"

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                    // Navigate back or to group details
                    findNavController().popBackStack()
                    // Optional: go to details
                    // val bundle = Bundle().apply { putString("circleId", createdCircleId) }
                    // findNavController().navigate(R.id.action_createEtibe_to_groupDetails, bundle)
                } else {
                    val errorMsg = activateResponse.errorBody()?.string() ?: "Failed to activate circle"
                    showError("Activation failed: $errorMsg")
                }
            } else {
                val errorMsg = createResponse.errorBody()?.string() ?: "Failed to create Etibé"
                showError(errorMsg)
            }
        } catch (e: Exception) {
            showError("Network error: ${e.message ?: "Unknown error"}")
        } finally {
            showLoading(false)
            binding.btnCreate.isEnabled = true
            binding.btnCreate.text = "Create Etibé"
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

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        // Optional: show Snackbar or set error TextView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}