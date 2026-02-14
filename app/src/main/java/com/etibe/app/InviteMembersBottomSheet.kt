package com.etibe.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.etibe.app.databinding.BottomSheetInviteMembersBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InviteMembersBottomSheet(
    private val circleId: String,
    private val circleName: String,
    private val inviteLink: String = "https://etibe.app/join/family-circle-XXXX",
    private val inviteCode: String = "FAMILY2025"
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetInviteMembersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetInviteMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set circle name
        binding.tvCircleName.text = circleName

        // Set invite link & code (can be passed or fetched)
        binding.tvInviteLink.text = inviteLink
        binding.tvInviteCode.text = inviteCode

        // Copy link
        binding.tilInviteLink.setEndIconOnClickListener {
            copyToClipboard(inviteLink, "Invite link copied!")
        }

        // Copy code
        binding.tilInviteCode.setEndIconOnClickListener {
            copyToClipboard(inviteCode, "Invite code copied!")
        }

        // Close button
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        // Share button
        binding.btnShare.setOnClickListener {
            shareInvite()
        }
    }

    private fun copyToClipboard(text: String, toastMessage: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun shareInvite() {
        val shareText = """
            Join my Etibé group: $circleName
            Link: $inviteLink
            Code: $inviteCode
        """.trimIndent()

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