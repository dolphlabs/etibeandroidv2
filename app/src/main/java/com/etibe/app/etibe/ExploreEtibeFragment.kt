package com.etibe.app.etibe


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.etibe.app.R
import com.etibe.app.adapters.CirclesAdapter
import com.etibe.app.data.models.Circle
import com.etibe.app.databinding.FragmentExploreEtibeBinding
import com.etibe.app.utils.Circle
import com.etibe.app.viewmodels.ExploreEtibeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExploreEtibeFragment : Fragment() {

    private var _binding: FragmentExploreEtibeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreEtibeViewModel by viewModels()
    private lateinit var adapter: CirclesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreEtibeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CirclesAdapter(
            onCircleClick = { circle ->
                navigateToCircleDetails(circle)
            },
            onJoinClick = { circle ->
                handleJoinCircle(circle)
            }
        )
        binding.rvCircles.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString()
            viewModel.searchCircles(query)
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString()
            if (query.isNotBlank()) {
                viewModel.searchCircles(query)
            }
        }

        // Handle search action on keyboard
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString()
            if (query.isNotBlank()) {
                viewModel.searchCircles(query)
            }
            true
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadCircles(forceRefresh = true)
        }

        // Set refresh color
        binding.swipeRefresh.setColorSchemeResources(R.color.primary_green)
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.ivNotification.setOnClickListener {
            // Navigate to notifications
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

        binding.ivProfile.setOnClickListener {
            // Navigate to profile
            Toast.makeText(requireContext(), "Profile", Toast.LENGTH_SHORT).show()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun observeViewModel() {
        viewModel.circles.observe(viewLifecycleOwner) { circles ->
            adapter.submitList(circles)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading && adapter.currentList.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showError(error)
            } else {
                binding.layoutError.visibility = View.GONE
            }
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.layoutEmpty.visibility = if (isEmpty && viewModel.error.value == null) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.rvCircles.visibility = if (!isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun showError(message: String) {
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
        binding.rvCircles.visibility = View.GONE
    }

    private fun navigateToCircleDetails(circle: Circle) {
        // Navigate to circle details screen
        // You can pass the circle ID or entire circle object
        Toast.makeText(
            requireContext(),
            "View details: ${circle.name}",
            Toast.LENGTH_SHORT
        ).show()

        // Example navigation with Safe Args:
        // val action = ExploreEtibeFragmentDirections
        //     .actionExploreToCircleDetails(circle.id)
        // findNavController().navigate(action)
    }

    private fun handleJoinCircle(circle: Circle) {
        when {
            circle.isFull -> {
                Toast.makeText(
                    requireContext(),
                    "This circle is full",
                    Toast.LENGTH_SHORT
                ).show()
            }
            circle.status == "COMPLETED" -> {
                Toast.makeText(
                    requireContext(),
                    "This circle has been completed",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                // Navigate to join circle screen or show join dialog
                showJoinDialog(circle)
            }
        }
    }

    private fun showJoinDialog(circle: Circle) {
        // Show a dialog or navigate to join screen
        Toast.makeText(
            requireContext(),
            "Joining ${circle.name}...",
            Toast.LENGTH_SHORT
        ).show()

        // Example: Show bottom sheet or dialog
        // val dialog = JoinCircleBottomSheet(circle)
        // dialog.show(childFragmentManager, "JoinCircle")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}