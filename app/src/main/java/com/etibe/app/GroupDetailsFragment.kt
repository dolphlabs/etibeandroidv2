package com.etibe.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.etibe.app.databinding.FragmentGroupDetailsBinding
import com.etibe.app.models.RetrofitClient
import kotlinx.coroutines.launch

class GroupDetailsFragment : Fragment() {

    private var _binding: FragmentGroupDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: GroupDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadGroupDashboard(args.circleId)
        setupListeners()
    }

    private fun loadGroupDashboard(circleId: String) = lifecycleScope.launch {
        try {
            val response = RetrofitClient.instance(requireContext()).getCircleDashboard(circleId)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data?.data
                // Bind data to UI
                binding.tvGroupName.text = data?.circle?.name
                binding.tvMembersCount.text = "${data?.circle?.memberCount} members"
                // ... bind other fields (next payout, progress, etc.)
            } else {
                Toast.makeText(context, "Failed to load group", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnAddMember.setOnClickListener {
            // Open Invite Members bottom sheet or screen
            showInviteMembers(args.circleId)
        }
    }

    private fun showInviteMembers(circleId: String) {
        // Implement invite bottom sheet or navigate to invite screen
        // Use InviteMembersBottomSheet(circleId = circleId).show(...)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}