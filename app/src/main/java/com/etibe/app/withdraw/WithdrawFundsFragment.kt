package com.etibe.app.withdraw

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.etibe.app.R
import com.etibe.app.adapter.CryptoOptionAdapter
import com.etibe.app.databinding.FragmentWithdrawFundsBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.ui.OtpVerificationDialog
import com.etibe.app.utils.WithdrawOtpRequest
import com.etibe.app.utils.WithdrawRequest
import kotlinx.coroutines.launch

class WithdrawFundsFragment : Fragment() {

    private var _binding: FragmentWithdrawFundsBinding? = null
    private val binding get() = _binding!!

    private lateinit var cryptoAdapter: CryptoOptionAdapter

    // Example balances (fetch from /wallet/balance in real app)
    private val cryptoOptions = listOf(
        CryptoOption("USDC", 1245.50, true),
        CryptoOption("USDT", 1250.00, false),
        CryptoOption("NEAR", 450.00, false)
    )

    private var selectedCrypto: CryptoOption = cryptoOptions.first()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWithdrawFundsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCryptoRecycler()
        setupListeners()
        updateReceiveAmount() // initial calculation
    }

    private fun setupCryptoRecycler() {
        cryptoAdapter = CryptoOptionAdapter(cryptoOptions) { selected ->
            selectedCrypto = selected
            updateReceiveAmount()
            binding.rvCryptoOptions.adapter?.notifyDataSetChanged()
        }
        binding.rvCryptoOptions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cryptoAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            etAmount.doAfterTextChanged { updateReceiveAmount() }

            btnNext.setOnClickListener {
                requestWithdrawalOtp()
            }
        }
    }

    private fun updateReceiveAmount() {
        val amountStr = binding.etAmount.text.toString()
        val amount = amountStr.toDoubleOrNull() ?: 0.0

        // Example fee calculation (replace with real logic or API)
        val fee = 2.50 // static for demo
        val receive = amount - fee

        binding.tvReceiveAmount.text = String.format("%.2f %s", receive, selectedCrypto.symbol)
        binding.tvNetworkFee.text = String.format("~$%.2f", fee)
    }

    private fun requestWithdrawalOtp() = lifecycleScope.launch {
        showLoading(true)

        try {
            val amountStr = binding.etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                showError("Please enter a valid amount")
                return@launch
            }

            val request = WithdrawOtpRequest(
                amount = amountStr,
                asset = selectedCrypto.symbol
            )

            val response = RetrofitClient.instance(requireContext()).requestWithdrawalOtp(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Toast.makeText(context, "OTP sent to your email", Toast.LENGTH_LONG).show()
                // Now show OTP input dialog or navigate to next step
                showOtpVerificationDialog(amountStr)
            } else {
                showError("Failed to request OTP")
            }
        } catch (e: Exception) {
            showError("Network error: ${e.message}")
        } finally {
            showLoading(false)
        }
    }

    private fun performWithdrawal(amount: String, otp: String) = lifecycleScope.launch {
        showLoading(true)

        try {
            val request = WithdrawRequest(
                amount = amount,
                asset = selectedCrypto.symbol,
                destinationAddress = binding.etWalletAddress.text.toString().trim(),
                otp = otp
            )

            val response = RetrofitClient.instance(requireContext()).withdraw(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val msg = response.body()?.data?.message ?: "Withdrawal submitted!"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                findNavController().popBackStack() // or go to success screen
            } else {
                showError("Withdrawal failed")
            }
        } catch (e: Exception) {
            showError("Error: ${e.message}")
        } finally {
            showLoading(false)
        }
    }

    private fun showOtpVerificationDialog(amount: String) {
        OtpVerificationDialog(amount) { otp ->
            performWithdrawal(amount, otp)
        }.show(parentFragmentManager, "OtpDialog")
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class CryptoOption(
        val symbol: String,
        val balance: Double,
        val isSelected: Boolean
    )
}