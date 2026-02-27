package com.etibe.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.etibe.app.databinding.BottomSheetInviteMembersBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.util.Patterns
import androidx.lifecycle.lifecycleScope
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.InviteRequest
import kotlinx.coroutines.launch

class InviteMembersBottomSheet(
    private val circleId: String,
    private val circleName: String,
    private val inviteLink: String,
    private val inviteCode: String
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

        // Populate UI
        binding.tvCircleName.text = circleName
        binding.tvInviteLink.setText(inviteLink)  // Correct!
        binding.tvInviteCode.setText(inviteCode)  // Correct!


        // Copy link (using Material end icon click)
        binding.ivClose.setOnClickListener {
            copyToClipboard(inviteLink, "Invite link copied!")
        }

        // Copy code
        binding.ivInviteCopy.setOnClickListener {
            copyToClipboard(inviteCode, "Invite code copied!")
        }

        // Close
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        // Share
        binding.btnShare.setOnClickListener {
            shareInvite()
        }
        binding.btnInviteByEmail.setOnClickListener {
            inviteByEmail()
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

    private fun inviteByEmail() {
        val email = binding.etInviteEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilInviteEmail.error = "Email is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilInviteEmail.error = "Enter a valid email"
            return
        }

        binding.tilInviteEmail.error = null

        lifecycleScope.launch {
            binding.btnInviteByEmail.isEnabled = false
            binding.btnInviteByEmail.text = "Sending..."

            try {
                val response = RetrofitClient.instance(requireContext())
                    .inviteMember(
                        circleId,
                        InviteRequest(
                            inviteeEmail = email,
                            circleId = circleId
                        )
                    )

                if (response.isSuccessful && response.body()?.success == true) {

                    Toast.makeText(
                        requireContext(),
                        "Invite sent successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                    binding.etInviteEmail.text?.clear()

                } else {
                    val message = response.body()?.error?.message
                        ?: "Failed to send invite"

                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnInviteByEmail.isEnabled = true
                binding.btnInviteByEmail.text = "Send Invite"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}