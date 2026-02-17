package com.etibe.app.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.etibe.app.databinding.DialogOtpVerificationBinding

class OtpVerificationDialog(
    private val amount: String,
    private val onOtpEntered: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogOtpVerificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (otp.length == 6 && otp.all { it.isDigit() }) {
                onOtpEntered(otp)
                dismiss()
            } else {
                Toast.makeText(context, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}