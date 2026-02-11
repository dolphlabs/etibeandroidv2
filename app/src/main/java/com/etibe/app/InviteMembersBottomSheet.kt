package com.etibe.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.etibe.app.databinding.BottomSheetInviteMembersBinding
import com.etibe.app.models.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class InviteMembersBottomSheet(
    private val circleId: String,
    private val circleName: String
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetInviteMembersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetInviteMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCircleName.text = circleName

        // Load invite code/link from API or pass as argument
        loadInviteCode()

        binding.ivCopyLink.setOnClickListener {
            copyToClipboard(binding.tvInviteLink.text.toString())
        }

        binding.ivCopyCode.setOnClickListener {
            copyToClipboard(binding.tvInviteCode.text.toString())
        }

        binding.btnShare.setOnClickListener {
            shareInvite()
        }
    }

    private fun loadInviteCode() = lifecycleScope.launch {
        // If invite code is not passed, fetch from circle details
        // For simplicity, assume it's already available or hardcode
        binding.tvInviteLink.text = "https://etibe.app/join/XXXXXX"
        binding.tvInviteCode.text = "XXXXXX"
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Invite", text))
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun shareInvite() {
        val shareText = "Join my Etibé group: ${binding.tvCircleName.text}\nLink: ${binding.tvInviteLink.text}\nCode: ${binding.tvInviteCode.text}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}