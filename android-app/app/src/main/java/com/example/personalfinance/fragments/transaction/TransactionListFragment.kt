package com.example.personalfinance.fragments.transaction

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinance.R
import com.example.personalfinance.activities.ScanBillActivity
import com.example.personalfinance.activities.ScanProductActivity
import com.example.personalfinance.adapters.TransactionAdapter
import com.example.personalfinance.databinding.FragmentTransactionListBinding
import com.example.personalfinance.fragments.recurring.RecurringListFragment
import com.example.personalfinance.models.Transaction
import com.example.personalfinance.models.User
import com.example.personalfinance.utils.Constants
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.TransactionViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar
import java.util.Locale

class TransactionListFragment : Fragment() {

    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel
    private var currentUser: User? = null

    private lateinit var adapter: TransactionAdapter
    private val fullTransactionList = mutableListOf<Transaction>()
    private val filteredTransactionList = mutableListOf<Transaction>()

    private var selectedTypeTab = "ALL" // "ALL", "EXPENSE", "INCOME"
    private var searchQuery = ""
    private val currentCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser()
        if (currentUser == null) return

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        setupSearch()
        observeViewModel()

        updateMonthHeader()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(requireContext(), filteredTransactionList)
        adapter.setOnTransactionClickListener { transaction ->
            val editFragment = AddTransactionFragment.newInstance(
                transaction.transactionId ?: 0,
                transaction.amount,
                transaction.title ?: "",
                transaction.categoryId ?: 1,
                transaction.transactionDate ?: "",
                0,
                transaction.imageUrl
            )
            editFragment.setOnTransactionSavedListener { loadTransactions() }
            editFragment.show(parentFragmentManager, "AddTransactionFragment")
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.tabAll.setOnClickListener { selectTypeTab("ALL", binding.tabAll) }
        binding.tabExpense.setOnClickListener { selectTypeTab("EXPENSE", binding.tabExpense) }
        binding.tabIncome.setOnClickListener { selectTypeTab("INCOME", binding.tabIncome) }

        binding.btnSearch.setOnClickListener {
            if (binding.searchBarContainer.visibility == View.VISIBLE) {
                binding.searchBarContainer.visibility = View.GONE
                binding.edtSearchQuery.setText("")
                searchQuery = ""
                filterAndDisplayTransactions()
            } else {
                binding.searchBarContainer.visibility = View.VISIBLE
                binding.edtSearchQuery.requestFocus()
            }
        }

        binding.monthSelectorContainer.setOnClickListener {
            showMonthYearPickerDialog()
        }

        binding.fabAdd.setOnClickListener {
            showAddOptionsBottomSheet()
        }

        binding.btnRecurringScheduler.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecurringListFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showMonthYearPickerDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null)
        bottomSheetDialog.setContentView(view)

        val tvYear = view.findViewById<TextView>(R.id.tvYear)
        val btnPrevYear = view.findViewById<ImageView>(R.id.btnPrevYear)
        val btnNextYear = view.findViewById<ImageView>(R.id.btnNextYear)

        val monthResIds = intArrayOf(
            R.id.btnMonth1, R.id.btnMonth2, R.id.btnMonth3, R.id.btnMonth4,
            R.id.btnMonth5, R.id.btnMonth6, R.id.btnMonth7, R.id.btnMonth8,
            R.id.btnMonth9, R.id.btnMonth10, R.id.btnMonth11, R.id.btnMonth12
        )

        val monthButtons = Array(12) { i ->
            view.findViewById<TextView>(monthResIds[i])
        }

        val tempYear = intArrayOf(currentCalendar.get(Calendar.YEAR))
        val activeYear = currentCalendar.get(Calendar.YEAR)
        val activeMonth = currentCalendar.get(Calendar.MONTH) // 0-indexed

        val updateDialogUI = Runnable {
            tvYear.text = getString(R.string.format_year, tempYear[0])
            for (i in 0 until 12) {
                val btn = monthButtons[i]
                if (tempYear[0] == activeYear && i == activeMonth) {
                    btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                } else {
                    btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.surface))
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
            }
        }

        updateDialogUI.run()

        btnPrevYear.setOnClickListener {
            tempYear[0]--
            updateDialogUI.run()
        }

        btnNextYear.setOnClickListener {
            tempYear[0]++
            updateDialogUI.run()
        }

        for (i in 0 until 12) {
            val monthIndex = i
            monthButtons[i].setOnClickListener {
                currentCalendar.set(Calendar.YEAR, tempYear[0])
                currentCalendar.set(Calendar.MONTH, monthIndex)
                currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
                updateMonthHeader()
                filterAndDisplayTransactions()
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.show()
    }

    private fun selectTypeTab(type: String, tabView: TextView) {
        selectedTypeTab = type

        binding.tabAll.background = null
        binding.tabAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.tabExpense.background = null
        binding.tabExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.tabIncome.background = null
        binding.tabIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        tabView.setBackgroundResource(R.drawable.bg_button_rounded)
        tabView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E2E33"))
        tabView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        filterAndDisplayTransactions()
    }

    private fun setupSearch() {
        binding.edtSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim()?.lowercase(Locale.ROOT) ?: ""
                filterAndDisplayTransactions()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateMonthHeader() {
        val month = currentCalendar.get(Calendar.MONTH) + 1
        val year = currentCalendar.get(Calendar.YEAR)
        binding.tvMonthTitle.text = getString(R.string.format_month_year, month, year)
    }

    private fun loadTransactions() {
        val userId = currentUser?.userId ?: return
        viewModel.loadTransactions(userId, "2000-01-01", "2100-12-31")
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { txs ->
            fullTransactionList.clear()
            if (txs != null) {
                fullTransactionList.addAll(txs)
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

        val selectedMonth = currentCalendar.get(Calendar.MONTH) + 1
        val selectedYear = currentCalendar.get(Calendar.YEAR)

        var totalIncome = 0.0
        var totalExpense = 0.0

        for (t in fullTransactionList) {
            // 1. Filter by Month/Year
            var matchTime = false
            try {
                val dateStr = t.transactionDate
                if (dateStr != null) {
                    val date = DateUtils.parseApiDate(dateStr)
                    if (date != null) {
                        val txCal = Calendar.getInstance().apply { time = date }
                        val txMonth = txCal.get(Calendar.MONTH) + 1
                        val txYear = txCal.get(Calendar.YEAR)
                        matchTime = (txMonth == selectedMonth && txYear == selectedYear)
                    }
                }
            } catch (e: Exception) {
                matchTime = true
            }

            if (!matchTime) continue

            // 2. Filter by tab type
            val matchType = when (selectedTypeTab) {
                "ALL" -> true
                "EXPENSE" -> Constants.TYPE_EXPENSE.equals(t.transactionType, ignoreCase = true)
                "INCOME" -> Constants.TYPE_INCOME.equals(t.transactionType, ignoreCase = true)
                else -> true
            }

            if (!matchType) continue

            // 3. Filter by search query
            var matchSearch = true
            if (searchQuery.isNotEmpty()) {
                val title = t.title?.lowercase(Locale.ROOT) ?: ""
                val category = t.categoryName?.lowercase(Locale.ROOT) ?: ""
                val note = t.note?.lowercase(Locale.ROOT) ?: ""
                matchSearch = title.contains(searchQuery) || category.contains(searchQuery) || note.contains(searchQuery)
            }

            if (!matchSearch) continue

            filteredTransactionList.add(t)

            // Accumulate Chi/Thu
            if (Constants.TYPE_INCOME.equals(t.transactionType, ignoreCase = true)) {
                totalIncome += t.amount
            } else {
                totalExpense += t.amount
            }
        }

        // Setup overview totals
        binding.tvSummaryExpense.text = CurrencyFormatter.formatVND(totalExpense)
        binding.tvSummaryIncome.text = CurrencyFormatter.formatVND(totalIncome)

        // Setup empty state
        if (filteredTransactionList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvTransactions.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvTransactions.visibility = View.VISIBLE
        }

        adapter.notifyDataChanged()
    }

    private fun showAddOptionsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_add_options, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val btnManual = bottomSheetView.findViewById<View>(R.id.btnOptionManual)
        val btnOcr = bottomSheetView.findViewById<View>(R.id.btnOptionOcr)
        val btnYolo = bottomSheetView.findViewById<View>(R.id.btnOptionYolo)

        btnManual?.setOnClickListener {
            bottomSheetDialog.dismiss()
            val addFragment = AddTransactionFragment()
            addFragment.show(parentFragmentManager, "AddTransactionFragment")
        }

        btnOcr?.setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(requireContext(), ScanBillActivity::class.java))
        }

        btnYolo?.setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(requireContext(), ScanProductActivity::class.java))
        }

        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
