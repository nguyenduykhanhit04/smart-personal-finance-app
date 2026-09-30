package com.example.personalfinance.fragments.transaction

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinance.R
import com.example.personalfinance.adapters.DayTransactionsAdapter
import com.example.personalfinance.databinding.FragmentDayDetailBinding
import com.example.personalfinance.models.domain.Transaction
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.repositories.TransactionRepository
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.SharedPrefManager
import kotlin.math.abs

class DayDetailFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(selectedDate: String, dateTitle: String): DayDetailFragment {
            val fragment = DayDetailFragment()
            val args = Bundle().apply {
                putString("selected_date", selectedDate)
                putString("date_title", dateTitle)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentDayDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: TransactionRepository
    private var currentUser: User? = null
    private var selectedDate: String = ""
    private var dateTitle: String = ""

    private val allTransactions = mutableListOf<Transaction>()
    private val filteredTransactions = mutableListOf<Transaction>()
    private lateinit var adapter: DayTransactionsAdapter
    private var currentFilter = "ALL" // ALL, EXPENSE, INCOME

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDayDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser()
        if (currentUser == null) return

        repository = TransactionRepository()

        arguments?.let {
            selectedDate = it.getString("selected_date", "")
            dateTitle = it.getString("date_title", "Chi tiết ngày")
        }

        binding.tvDayDetailDate.text = dateTitle

        setupRecyclerView()
        setupClickListeners()
        loadDayTransactions()
    }

    private fun setupRecyclerView() {
        adapter = DayTransactionsAdapter(requireContext(), filteredTransactions)
        binding.rvDayTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDayTransactions.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnMonthCalendar.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnFilterAll.setOnClickListener {
            selectFilterTab("ALL")
            filterTransactions()
        }

        binding.btnFilterExpense.setOnClickListener {
            selectFilterTab("EXPENSE")
            filterTransactions()
        }

        binding.btnFilterIncome.setOnClickListener {
            selectFilterTab("INCOME")
            filterTransactions()
        }

        binding.fabAddTransaction.setOnClickListener { openAddTransactionDialog() }
        binding.btnEmptyAddTransaction.setOnClickListener { openAddTransactionDialog() }
    }

    private fun selectFilterTab(filter: String) {
        currentFilter = filter

        // Reset backgrounds
        binding.btnFilterAll.background = null
        binding.btnFilterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.btnFilterExpense.background = null
        binding.btnFilterExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.btnFilterIncome.background = null
        binding.btnFilterIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        // Active state background shape
        val activeColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.white)

        when (filter.uppercase()) {
            "ALL" -> {
                binding.btnFilterAll.setBackgroundResource(R.drawable.bg_button_rounded)
                binding.btnFilterAll.backgroundTintList = ColorStateList.valueOf(activeColor)
                binding.btnFilterAll.setTextColor(activeTextColor)
            }
            "EXPENSE" -> {
                binding.btnFilterExpense.setBackgroundResource(R.drawable.bg_button_rounded)
                binding.btnFilterExpense.backgroundTintList = ColorStateList.valueOf(activeColor)
                binding.btnFilterExpense.setTextColor(activeTextColor)
            }
            "INCOME" -> {
                binding.btnFilterIncome.setBackgroundResource(R.drawable.bg_button_rounded)
                binding.btnFilterIncome.backgroundTintList = ColorStateList.valueOf(activeColor)
                binding.btnFilterIncome.setTextColor(activeTextColor)
            }
        }
    }

    private fun loadDayTransactions() {
        val userId = currentUser?.userId ?: return
        repository.getTransactions(userId, selectedDate, selectedDate, object : TransactionRepository.ApiCallback<List<Transaction>> {
            override fun onSuccess(result: List<Transaction>?) {
                if (isAdded) {
                    allTransactions.clear()
                    if (result != null) {
                        allTransactions.addAll(result)
                    }
                    calculateDailySummary()
                    filterTransactions()
                }
            }

            override fun onError(errorMessage: String) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Lỗi tải giao dịch: $errorMessage", Toast.LENGTH_SHORT).show()
                    calculateDailySummary()
                    filterTransactions()
                }
            }
        })
    }

    private fun calculateDailySummary() {
        var expense = 0.0
        var income = 0.0

        for (tx in allTransactions) {
            val amt = tx.amount
            if ("EXPENSE".equals(tx.transactionType, ignoreCase = true) || amt < 0) {
                expense += abs(amt)
            } else {
                income += amt
            }
        }

        binding.tvExpenseAmount.text = CurrencyFormatter.formatVND(expense)
        binding.tvIncomeAmount.text = CurrencyFormatter.formatVND(income)

        if (expense > 0) {
            binding.tvExpensePercent.setText(R.string.label_comparison_expense_down)
            binding.tvExpensePercent.setTextColor(Color.parseColor("#10B981"))
        } else {
            binding.tvExpensePercent.setText(R.string.label_comparison_unchanged)
            binding.tvExpensePercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }

        if (income > 0) {
            binding.tvIncomePercent.setText(R.string.label_comparison_income_up)
            binding.tvIncomePercent.setTextColor(Color.parseColor("#10B981"))
        } else {
            binding.tvIncomePercent.setText(R.string.label_comparison_unchanged)
            binding.tvIncomePercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun filterTransactions() {
        filteredTransactions.clear()

        for (tx in allTransactions) {
            val amt = tx.amount
            val isExpense = "EXPENSE".equals(tx.transactionType, ignoreCase = true) || amt < 0

            when (currentFilter.uppercase()) {
                "ALL" -> filteredTransactions.add(tx)
                "EXPENSE" -> if (isExpense) filteredTransactions.add(tx)
                "INCOME" -> if (!isExpense) filteredTransactions.add(tx)
            }
        }

        if (filteredTransactions.isEmpty()) {
            binding.rvDayTransactions.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvDayTransactions.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }

        adapter.notifyDataSetChanged()
    }

    private fun openAddTransactionDialog() {
        val addFragment = AddTransactionFragment()
        val bundle = Bundle().apply {
            putString("date", selectedDate)
        }
        addFragment.arguments = bundle
        addFragment.setOnTransactionSavedListener { loadDayTransactions() }
        addFragment.show(parentFragmentManager, "AddTransactionFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
