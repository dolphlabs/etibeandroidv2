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

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.imageUploaded.setImageURI(uri)
                binding.imageUploaded.visibility = View.VISIBLE
                // Optional: hide placeholder text or "Upload Logo" button
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
        // Frequency dropdown
        val frequencies = arrayOf("WEEKLY", "BI_WEEKLY", "MONTHLY")
        val freqAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, frequencies)
        binding.etFrequency.setAdapter(freqAdapter)
        binding.etFrequency.setText("BI_WEEKLY", false)

        // Payment order dropdown
        val orders = arrayOf("Names numbered by entry order", "Random", "Custom")
        val orderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, orders)
        binding.etPaymentOrder.setAdapter(orderAdapter)
        binding.etPaymentOrder.setText("Names numbered by entry order", false)
    }

    private fun setupClickListeners() {
        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            // Image upload
            cvUploadLogo.setOnClickListener { openImagePicker() }
            btnUpload.setOnClickListener { openImagePicker() }

            // Currency dropdown
            tilCurrency.setOnClickListener { showCurrencyBottomSheet() }
            etCurrency.setOnClickListener { showCurrencyBottomSheet() }

            // Start date picker
            tilStartDate.setOnClickListener { showDatePicker() }
            etStartDate.setOnClickListener { showDatePicker() }

            btnCreate.setOnClickListener {
                if (!validateInputs()) return@setOnClickListener
                createCircle()
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

    private fun createCircle() = lifecycleScope.launch {
        showLoading(true)
        binding.btnCreate.isEnabled = false
        binding.btnCreate.text = "Creating..."

        try {
            val membersText = binding.etMembers.text.toString().trim()
            val memberList = membersText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val request = CircleCreateRequest(
                name = binding.etGroupName.text.toString().trim(),
                description = "", // add field later if needed
                logoUrl = "",     // upload later if you add image upload API
                contributionSettings = CircleCreateRequest.ContributionSettings(
                    amount = binding.etAmount.text.toString().trim(),
                    currency = selectedCurrency,
                    frequency = binding.etFrequency.text.toString(),
                    gracePeriodDays = binding.etGracePeriod.text.toString().toIntOrNull() ?: 7,
                    penaltyPercentage = "0"
                ),
                maxMembers = 50,  // make configurable later
                startDate = selectedStartDate,
                isPrivate = binding.switchPrivate.isChecked
            )

            val response = RetrofitClient.instance(requireContext()).createCircle(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Toast.makeText(context, "Etibé created successfully!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack() // or go to group details
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to create group"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}