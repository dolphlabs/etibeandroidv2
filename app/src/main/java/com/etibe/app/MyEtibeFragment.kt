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
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        adapter = MyEtibeAdapter { circle ->
            val action = MyEtibeFragmentDirections.actionMyEtibeToGroupDetails(circle.id)
            findNavController().navigate(action)
        }
        binding.rvMyEtibeGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyEtibeFragment.adapter
        }
    }

    private fun loadMyCircles() = lifecycleScope.launch {
        try {
            val response = RetrofitClient.instance(requireContext()).getMyCircles()

            if (response.isSuccessful && response.body()?.success == true) {
                val circles = response.body()?.data?.data ?: emptyList()
                adapter.submitList(circles)

                // Update stats (example - customize as needed)
                binding.tvActiveGroups.text = circles.size.toString()
            } else {
                Toast.makeText(context, "Failed to load groups", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> findNavController().navigate(R.id.action_myEtibe_to_home)
                R.id.nav_explore -> findNavController().navigate(R.id.action_myEtibe_to_explore)
                R.id.nav_settings -> findNavController().navigate(R.id.action_myEtibe_to_settings)
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}