package com.example.personalfinance.fragments.account

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinance.R
import com.example.personalfinance.adapters.TransactionAdapter
import com.example.personalfinance.databinding.FragmentAccountDetailsBinding
import com.example.personalfinance.models.domain.Transaction
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.TransactionViewModel
import java.util.Calendar

class AccountDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ACCOUNT_ID = "account_id"
        private const val ARG_ACCOUNT_NAME = "account_name"
        private const val ARG_ACCOUNT_BALANCE = "account_balance"

        fun newInstance(accountId: Int, accountName: String, balance: Double): AccountDetailsFragment {
            return AccountDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ACCOUNT_ID, accountId)
                    putString(ARG_ACCOUNT_NAME, accountName)
                    putDouble(ARG_ACCOUNT_BALANCE, balance)
                }
            }
        }
    }

    private var _binding: FragmentAccountDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel
    private var accountId: Int = 0
    private var accountName: String? = null
    private var accountBalance: Double = 0.0
    private var currentUser: User? = null

    private lateinit var adapter: TransactionAdapter
    private val fullTransactionList = ArrayList<Transaction>()
    private val filteredTransactionList = ArrayList<Transaction>()
    private var selectedTimeTab = "ALL" // "MONTH", "YEAR", "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            accountId = it.getInt(ARG_ACCOUNT_ID)
            var name = it.getString(ARG_ACCOUNT_NAME)
            if ("Ví chính".equals(name, ignoreCase = true)) {
                name = "Wallet"
            }
            accountName = name
            accountBalance = it.getDouble(ARG_ACCOUNT_BALANCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).user ?: return
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        setupUI()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        loadTransactions()
    }

    private fun setupUI() {
        binding.tvWalletTitle.text = accountName

        binding.tvWalletBalance.text = CurrencyFormatter.formatVND(accountBalance)
        val balanceColor = if (accountBalance < 0) {
            ContextCompat.getColor(requireContext(), R.color.expense_red)
        } else {
            ContextCompat.getColor(requireContext(), R.color.income_green)
        }
        binding.tvWalletBalance.setTextColor(balanceColor)

        selectTabVisuals(selectedTimeTab)
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(requireContext(), filteredTransactionList)
        binding.rvWalletTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWalletTransactions.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnEdit.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng chỉnh sửa tài khoản đang được phát triển!", Toast.LENGTH_SHORT).show()
        }

        binding.tvTabMonth.setOnClickListener {
            selectedTimeTab = "MONTH"
            selectTabVisuals(selectedTimeTab)
            filterAndDisplayTransactions()
        }

        binding.tvTabYear.setOnClickListener {
            selectedTimeTab = "YEAR"
            selectTabVisuals(selectedTimeTab)
            filterAndDisplayTransactions()
        }

        binding.tvTabAll.setOnClickListener {
            selectedTimeTab = "ALL"
            selectTabVisuals(selectedTimeTab)
            filterAndDisplayTransactions()
        }
    }

    private fun selectTabVisuals(tab: String) {
        binding.tvTabMonth.background = null
        binding.tvTabMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.tvTabYear.background = null
        binding.tvTabYear.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.tvTabAll.background = null
        binding.tvTabAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        val selectedView: TextView? = when (tab) {
            "MONTH" -> binding.tvTabMonth
            "YEAR" -> binding.tvTabYear
            "ALL" -> binding.tvTabAll
            else -> null
        }

        selectedView?.let {
            it.setBackgroundResource(R.drawable.bg_button_rounded)
            it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E2E33"))
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun loadTransactions() {
        val user = currentUser ?: return
        viewModel.loadTransactions(user.userId, "2000-01-01", "2100-12-31")
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { txs ->
            fullTransactionList.clear()
            if (txs != null) {
                fullTransactionList.addAll(txs.filter { it.accountId == accountId })
            }
            filterAndDisplayTransactions()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterAndDisplayTransactions() {
        filteredTransactionList.clear()

        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH) + 1 // 1-indexed
        val currentYear = now.get(Calendar.YEAR)

        var totalIncome = 0.0
        var totalExpense = 0.0

        for (t in fullTransactionList) {
            val keep = if ("ALL" == selectedTimeTab) {
                true
            } else {
                try {
                    val dateStr = t.transactionDate
                    if (dateStr != null) {
                        val date = DateUtils.parseApiDate(dateStr)
                        if (date != null) {
                            val txCal = Calendar.getInstance().apply { time = date }
                            val txMonth = txCal.get(Calendar.MONTH) + 1
                            val txYear = txCal.get(Calendar.YEAR)

                            when (selectedTimeTab) {
                                "MONTH" -> (txMonth == currentMonth && txYear == currentYear)
                                "YEAR" -> (txYear == currentYear)
                                else -> false
                            }
                        } else false
                    } else false
                } catch (e: Exception) {
                    "ALL" == selectedTimeTab
                }
            }

            if (keep) {
                filteredTransactionList.add(t)
                if ("EXPENSE".equals(t.transactionType, ignoreCase = true)) {
                    totalExpense += t.amount
                } else {
                    totalIncome += t.amount
                }
            }
        }

        adapter.notifyDataChanged()

        val subBalance = totalIncome - totalExpense
        binding.tvWalletBalance.text = CurrencyFormatter.formatVND(subBalance)
        val balanceColor = if (subBalance < 0) {
            ContextCompat.getColor(requireContext(), R.color.expense_red)
        } else {
            ContextCompat.getColor(requireContext(), R.color.income_green)
        }
        binding.tvWalletBalance.setTextColor(balanceColor)

        binding.tvTxCount.text = getString(R.string.format_transaction_count, filteredTransactionList.size)
        binding.tvWalletIncome.text = CurrencyFormatter.formatVND(totalIncome)
        binding.tvWalletExpense.text = CurrencyFormatter.formatVND(totalExpense)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
