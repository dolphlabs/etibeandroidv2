package com.etibe.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.etibe.app.databinding.DialogTopUpBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TopUpBottomSheetDialog(
    private val nearAccountId: String? = "" // fallback/default
) : BottomSheetDialogFragment() {

    private var _binding: DialogTopUpBinding? = null
    private val binding get() = _binding!!



    companion object {
        const val TAG = "TopUpBottomSheetDialog"

        fun newInstance(nearAccountId: String?): TopUpBottomSheetDialog {
            return TopUpBottomSheetDialog(nearAccountId)
        }
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

        // Hide everything except address + copy
        hideOldUiElements()

        // Show address
        binding.tvWalletAddress.text = nearAccountId ?: "No address available"

        // Copy button
        binding.ivCopyAddress.setOnClickListener {
            copyToClipboard(nearAccountId ?: "")
        }

        // Close
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun hideOldUiElements() {
        binding.apply {
            tvTitle.text = "Top Up Wallet"


            // Show new elements
            tvInstruction.isVisible = true
            tvWalletAddress.isVisible = true
            ivCopyAddress.isVisible = true
        }
    }

    private fun copyToClipboard(text: String) {
        if (text.isBlank()) {
            Toast.makeText(requireContext(), "No address to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("NEAR Address", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "Address copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}