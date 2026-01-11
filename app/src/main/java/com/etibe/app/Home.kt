package com.etibe.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.etibe.app.databinding.FragmentHomeBinding


class Home : Fragment() {
   private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var myEtibeAdapter: MyEtibeAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
        loadData()
    }

    private fun setupViews() {
        // Setup Recent Activity RecyclerView
        recentActivityAdapter = RecentActivityAdapter { activity ->
            onActivityClicked(activity)
        }
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentActivityAdapter
            setHasFixedSize(true)
        }

        // Setup My Etibe RecyclerView
        myEtibeAdapter = MyEtibeAdapter { etibe ->
            onEtibeClicked(etibe)
        }
        binding.rvMyEtibe.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myEtibeAdapter
            setHasFixedSize(true)
        }

        // Set user balance
        updateBalance(1245.50, 5000.0)
    }

    private fun setupClickListeners() {
        binding.apply {
            // Top-up button - Show bottom sheet dialog
            btnTopUp.setOnClickListener {
                showTopUpDialog()
            }
            currencySelector.setOnClickListener {
                SelectTokenBottomSheet(
                    selectedToken = binding.tvCurrency.text.toString()
                ) { token ->
                    binding.tvCurrency.text = token
                    updateBalanceForToken(token)
                }.show(parentFragmentManager, "SelectTokenSheet")
            }

            // Withdraw button
            btnWithdraw.setOnClickListener {
                handleWithdraw()
            }

            // Eye icon - Toggle balance visibility
            ivEyeIcon.setOnClickListener {
                toggleBalanceVisibility()
            }

            // Currency dropdown
            tvCurrency.setOnClickListener {
                showCurrencyOptions()
            }

            // Quick action buttons
            btnCreateEtibe.setOnClickListener {
                navigateToCreateEtibe()
            }

            btnJoinEtibe.setOnClickListener {
                navigateToJoinEtibe()
            }

            btnExploreEtibe.setOnClickListener {
                navigateToExploreEtibe()
            }

            btnMyEtibe.setOnClickListener {
                navigateToMyEtibe()
            }

            // View All buttons
            tvViewAllActivity.setOnClickListener {
                navigateToAllActivities()
            }

            tvViewAllEtibe.setOnClickListener {
                navigateToAllEtibe()
            }

            // Notification icon
            ivNotification.setOnClickListener {
                navigateToNotifications()
            }

            // Profile icon
            ivProfile.setOnClickListener {
                navigateToProfile()
            }
        }
    }

    private fun showTopUpDialog() {
        val dialog = TopUpBottomSheetDialog()

        // Optional: Set a listener for when top-up is successful
        dialog.setOnTopUpSuccessListener { amount ->
            onTopUpSuccess(amount)
        }

        dialog.show(childFragmentManager, TopUpBottomSheetDialog.TAG)
    }

    private fun onTopUpSuccess(amount: Int) {
        // Refresh balance
        loadBalance()

        // Add new activity to the list
        addNewActivity(
            RecentActivity(
                id = System.currentTimeMillis().toString(),
                title = "Top Up",
                date = getCurrentDate(),
                amount = "+₦${formatAmount(amount * 850)}",
                status = "success",
                iconRes = R.drawable.ic_top_up,
                type = ActivityType.TOP_UP
            )
        )
    }

    private fun handleWithdraw() {
        // Navigate to withdraw screen or show withdraw dialog
        // findNavController().navigate(R.id.action_home_to_withdraw)
    }

    private var isBalanceVisible = true

    private fun toggleBalanceVisibility() {
        isBalanceVisible = !isBalanceVisible
        binding.apply {
            if (isBalanceVisible) {
                tvBalance.text = "$1,245.50"
                tvApprox.text = "~ $5000"
                ivEyeIcon.setImageResource(R.drawable.ic_eye)
            } else {
                tvBalance.text = "****"
                tvApprox.text = "****"
                ivEyeIcon.setImageResource(R.drawable.ic_eye_off)
            }
        }
    }

    private fun showCurrencyOptions() {
        // Show currency selection dialog (USDC, NGN, etc.)
    }

    private fun updateBalance(usdcAmount: Double, usdAmount: Double) {
        binding.apply {
            tvBalance.text = "$${"%.2f".format(usdcAmount)}"
            tvApprox.text = "~ $${formatAmount(usdAmount.toInt())}"
        }
    }

    private fun loadBalance() {
        // TODO: Load balance from API/ViewModel
        updateBalance(1245.50, 5000.0)
    }

    private fun loadData() {
        // Load recent activity data
        val recentActivities = listOf(
            RecentActivity(
                id = "1",
                title = "Top Up",
                date = "Dec 20, 2025",
                amount = "+₦50,000",
                status = "success",
                iconRes = R.drawable.ic_top_up,
                type = ActivityType.TOP_UP
            ),
            RecentActivity(
                id = "2",
                title = "Contribution",
                date = "Dec 18, 2025",
                amount = "+₦50,000",
                status = "success",
                iconRes = R.drawable.ic_top_up,
                type = ActivityType.CONTRIBUTION
            ),
            RecentActivity(
                id = "3",
                title = "Payout",
                date = "Dec 15, 2025",
                amount = "+₦50,000",
                status = "pending",
                iconRes = R.drawable.ic_top_up,
                type = ActivityType.PAYOUT
            )
        )
        recentActivityAdapter.submitList(recentActivities)

        // Load my etibe data
        val myEtibeList = listOf(
            MyEtibe(
                id = "1",
                name = "Family Circle",
                amount = "₦10,000",
                frequency = "week",
                status = "Active",
                membersCount = 8,
                nextPayoutDate = "Jan 15, 2026"
            )
        )
        myEtibeAdapter.submitList(myEtibeList)
    }

    private fun addNewActivity(activity: RecentActivity) {
        val currentList = recentActivityAdapter.currentList.toMutableList()
        currentList.add(0, activity) // Add to top
        recentActivityAdapter.submitList(currentList)
    }

    private fun onActivityClicked(activity: RecentActivity) {
        // Navigate to activity details
        // val action = HomeFragmentDirections.actionHomeToActivityDetails(activity.id)
        // findNavController().navigate(action)
    }

    private fun onEtibeClicked(etibe: MyEtibe) {
        // Navigate to etibe details
        // val action = HomeFragmentDirections.actionHomeToEtibeDetails(etibe.id)
        // findNavController().navigate(action)
    }

    // Navigation methods
    private fun navigateToCreateEtibe() {
        // findNavController().navigate(R.id.action_home_to_create_etibe)
    }

    private fun navigateToJoinEtibe() {
        // findNavController().navigate(R.id.action_home_to_join_etibe)
    }

    private fun navigateToExploreEtibe() {
        // findNavController().navigate(R.id.action_home_to_explore_etibe)
    }

    private fun navigateToMyEtibe() {
        // findNavController().navigate(R.id.action_home_to_my_etibe)
    }

    private fun navigateToAllActivities() {
        // findNavController().navigate(R.id.action_home_to_all_activities)
    }

    private fun navigateToAllEtibe() {
        // findNavController().navigate(R.id.action_home_to_all_etibe)
    }

    private fun navigateToNotifications() {
        // findNavController().navigate(R.id.action_home_to_notifications)
    }

    private fun navigateToProfile() {
        // findNavController().navigate(R.id.action_home_to_profile)
    }

    // Helper methods
    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
    private fun updateBalanceForToken(token: String) {
        when (token) {
            "USDC" -> updateBalance(1245.50, 5000.00)
            "USDT" -> updateBalance(980.25, 4200.00)
            "NEAR" -> updateBalance(312.40, 1800.00)
        }

    }


    private fun formatAmount(amount: Int): String {
        return String.format("%,d", amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

