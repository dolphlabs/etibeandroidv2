package com.etibe.app.etibe

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.R
import com.etibe.app.databinding.FragmentGroupDetailsBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.ui.InviteMembersBottomSheet
import kotlinx.coroutines.launch

class GroupDetailsFragment : Fragment() {

    private var _binding: FragmentGroupDetailsBinding? = null
    private val binding get() = _binding!!

    private var circleId: String = ""
    private var circleName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        circleId = arguments?.getString("circleId") ?: ""
        circleName = arguments?.getString("circleName") ?: "Group"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (circleId.isBlank()) {
            Toast.makeText(context, "Invalid group", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        // Set initial values
        binding.tvGroupName.text = circleName

        loadGroupDashboard()
        setupListeners()
    }

    private fun loadGroupDashboard() = lifecycleScope.launch {
        showLoading(true)

        try {
            val response = RetrofitClient.instance(requireContext()).getCircleDashboard(circleId)

            if (!response.isSuccessful) {
                showError("Server error: ${response.code()}")
                return@launch
            }

            val body = response.body() ?: run {
                showError("Empty response from server")
                return@launch
            }

            if (!body.success) {
                showError(body.error?.message ?: "Failed to load group details")
                return@launch
            }

            val details = body.data?.data ?: run {
                showError("No group data received")
                return@launch
            }

            // Bind circle info
            binding.tvGroupName.text = details.circle?.name ?: circleName
            binding.tvMembersCount.text = "${details.circle?.memberCount ?: 0} members"
            binding.tvStatus.text = details.circle?.status ?: "Unknown"
            binding.tvStatus.isVisible = !details.circle?.status.isNullOrBlank()

            // Next Payout section (from stats or circle)
            binding.tvNextPayoutAmount.text = details.circle?.payoutAmount ?: "$0"
            binding.tvNextPayoutDate.text =
                details.circle?.nextPayoutDate?.substringBefore("T") ?: "N/A"

            // You Received (placeholder - add real logic later)
            binding.tvYouReceivedAmount.text = "$0"
            binding.tvYouReceivedCycle.text = "This Cycle"

            // Your Turn (placeholder - calculate from payoutOrder)
            binding.tvYourTurnNumber.text = "#?"
            binding.tvYourTurnOf.text = "of ${details.circle?.totalRounds ?: "?"}"

            // Next Recipient
            val recipient = details.nextRecipient
            binding.tvRecipientName.text =
                recipient?.let { "${it.firstName} ${it.lastName}" } ?: "N/A"
            binding.tvRecipientDate.text =
                details.circle?.nextPayoutDate?.substringBefore("T") ?: "N/A"
            binding.tvRecipientAmount.text = details.circle?.payoutAmount ?: "$0"

            // Progress bar (example)
            val progress = details.stats?.progress ?: 0
            binding.progressContributions.progress = progress
            binding.tvContributionsProgress.text = "$progress/10"

            // Contribution Status
            binding.tvTotalContributions.text = "8" // placeholder - calculate from real data
            binding.tvNextDueDate.text = "Dec 13, 2025" // placeholder

            // Show success state
            binding.layoutStatusMessage.isVisible = true

        } catch (e: Exception) {
            showError("Error loading group: ${e.message}")
        } finally {
            showLoading(false)
        }
    }

    private fun setupListeners() {
        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            btnAddMember.setOnClickListener {
                showInviteMembers(circleId, tvGroupName.text.toString())
            }

            // Optional: View All members
            tvViewAll.setOnClickListener {
                Toast.makeText(context, "View all members coming soon", Toast.LENGTH_SHORT).show()
            }
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
    private fun showInviteMembers(circleId: String, circleName: String) {

        val inviteCode = circleId.takeLast(6).uppercase()     // example code
        val inviteLink = "https://etibe.app/join/$inviteCode"   // example


        InviteMembersBottomSheet(
            circleId,
            circleName,
            inviteLink,
            inviteCode
        ).show(parentFragmentManager, "InviteBottomSheet")
    }


    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}