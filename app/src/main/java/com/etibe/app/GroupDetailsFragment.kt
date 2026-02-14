package com.etibe.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.etibe.app.databinding.FragmentGroupDetailsBinding
import com.etibe.app.models.RetrofitClient
import kotlinx.coroutines.launch

class GroupDetailsFragment : Fragment() {

    private var _binding: FragmentGroupDetailsBinding? = null
    private val binding get() = _binding!!

    private var circleId: String = ""
    private var circleName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get data from Bundle
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

        binding.tvGroupName.text = circleName
        loadGroupDashboard(circleId)
        setupListeners()
    }

    private fun loadGroupDashboard(circleId: String) = lifecycleScope.launch {


        try {
            val response =
                RetrofitClient.instance(requireContext()).getCircleDashboard(circleId)

            if (!response.isSuccessful) {
                Toast.makeText(
                    context,
                    "Server error: ${response.code()}",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val body = response.body()
            if (body?.success != true) {
                Toast.makeText(
                    context,
                    "Failed to load group: ${body?.error?.message}",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val dashboardDetails = body.data?.data
            if (dashboardDetails == null) {
                Toast.makeText(context, "No group data received", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Now safely bind
            binding.tvGroupName.text = dashboardDetails.circle?.name ?: "Unknown Group"
            binding.tvMembersCount.text =
                "${dashboardDetails.circle?.memberCount ?: 0} members"
            binding.tvStatus.text = dashboardDetails.circle?.status ?: "Unknown"

            // Add more bindings here...

        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }


    private fun setupListeners() {
        binding.btnAddMember.setOnClickListener {
            showInviteMembers(circleId, binding.tvGroupName.text.toString())
        }
    }

    private fun showInviteMembers(circleId: String, circleName: String) {
        InviteMembersBottomSheet(circleId, circleName).show(
            parentFragmentManager,
            "InviteBottomSheet"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}