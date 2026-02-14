package com.etibe.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.etibe.app.adapter.MyEtibeAdapter
import com.etibe.app.databinding.FragmentMyEtibeBinding
import com.etibe.app.models.RetrofitClient
import kotlinx.coroutines.launch

class MyEtibeFragment : Fragment() {

    private var _binding: FragmentMyEtibeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MyEtibeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyEtibeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadMyCircles()
    }

    private fun setupRecyclerView() {
        adapter = MyEtibeAdapter { circle ->
            // Pass circle ID using Bundle
            val bundle = Bundle().apply {
                putString("circleId", circle.id)
                putString("circleName", circle.name) // optional
            }
            findNavController().navigate(R.id.action_myEtibeFragment_to_groupDetailsFragment, bundle)
        }

        binding.rvMyEtibeGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyEtibeFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadMyCircles() = lifecycleScope.launch {
        try {
            val response = RetrofitClient.instance(requireContext()).getMyCircles()

            if (response.isSuccessful && response.body()?.success == true) {
                val circles = response.body()?.data?.data ?: emptyList()
                adapter.submitList(circles)

                // Update stats
                binding.tvActiveGroups.text = circles.size.toString()
                // You can calculate other stats (next payout, gift received) here if needed
            } else {
                Toast.makeText(context, "Failed to load your Etibés", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}