package com.etibe.app




import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.etibe.app.databinding.DialogTopUpBinding

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TopUpBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogTopUpBinding? = null
    private val binding get() = _binding!!

    private var selectedAmount: Int = 0
    private val exchangeRate = 850.0 // 1 USDC = 850 NGN

    private var onTopUpSuccessListener: ((Int) -> Unit)? = null

    companion object {
        const val TAG = "TopUpBottomSheetDialog"
    }

    fun setOnTopUpSuccessListener(listener: (Int) -> Unit) {
        onTopUpSuccessListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // Setup amount input
        binding.etAmount.doAfterTextChanged { text ->
            val amount = text.toString().toIntOrNull() ?: 0
            updateNgnEquivalent(amount)

            // Clear chip selection when user types
            binding.chipGroup.clearCheck()
        }

        // Set default payment method
        binding.rbCryptoWallet.isChecked = true
    }

    private fun setupClickListeners() {
        binding.apply {
            // Close button
            ivClose.setOnClickListener {
                dismiss()
            }

            // Amount chips
            chipGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.chip50 -> setAmount(50)
                    R.id.chip100 -> setAmount(100)
                    R.id.chip200 -> setAmount(200)
                    R.id.chip500 -> setAmount(500)
                }
            }

            // Top up button
            btnTopUpNow.setOnClickListener {
                handleTopUp()
            }

            // Cancel button
            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun setAmount(amount: Int) {
        selectedAmount = amount
        binding.etAmount.setText(amount.toString())
        updateNgnEquivalent(amount)
    }

    private fun updateNgnEquivalent(usdcAmount: Int) {
        val ngnAmount = usdcAmount * exchangeRate
        binding.tvNgnEquivalent.text = "≈ ₦${String.format("%,.2f", ngnAmount)} NGN"
    }

    private fun handleTopUp() {
        val amount = binding.etAmount.text.toString().toIntOrNull()

        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (!binding.rbCryptoWallet.isChecked) {
            Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        binding.btnTopUpNow.isEnabled = false
        binding.btnTopUpNow.text = "Processing..."

        // Simulate processing (replace with actual API call)
        binding.root.postDelayed({
            // Call success listener
            onTopUpSuccessListener?.invoke(amount)

            Toast.makeText(
                requireContext(),
                "Successfully topped up $${amount} USDC",
                Toast.LENGTH_LONG
            ).show()

            dismiss()
        }, 1500)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}