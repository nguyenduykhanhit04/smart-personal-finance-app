package com.example.personalfinance.fragments.home

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinance.R
import com.example.personalfinance.activities.ScanBillActivity
import com.example.personalfinance.activities.ScanProductActivity
import com.example.personalfinance.adapters.CalendarGridAdapter
import com.example.personalfinance.adapters.HorizontalAccountAdapter
import com.example.personalfinance.databinding.FragmentHomeBinding
import com.example.personalfinance.fragments.account.AccountDetailsFragment
import com.example.personalfinance.fragments.account.AddAccountFragment
import com.example.personalfinance.fragments.profile.ProfileFragment
import com.example.personalfinance.fragments.transaction.AddTransactionFragment
import com.example.personalfinance.fragments.transaction.DayDetailFragment
import com.example.personalfinance.fragments.transaction.DayTransactionsBottomSheet
import com.example.personalfinance.models.domain.Account
import com.example.personalfinance.models.domain.Budget
import com.example.personalfinance.models.domain.CalendarDay
import com.example.personalfinance.models.domain.Transaction
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.AccountViewModel
import com.example.personalfinance.viewmodels.BudgetViewModel
import com.example.personalfinance.viewmodels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar
import java.util.HashMap
import kotlin.math.abs
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var budgetViewModel: BudgetViewModel
    private var currentUser: User? = null

    private lateinit var calendarAdapter: CalendarGridAdapter
    private lateinit var horizontalAccountAdapter: HorizontalAccountAdapter
    private val accountList = ArrayList<Account>()
    private val calendarDays = ArrayList<CalendarDay>()
    private val allBudgets = ArrayList<Budget>()
    private val currentCalendar = Calendar.getInstance()
    private var isMonthMode = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).user ?: return

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        budgetViewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setupDynamicGreeting()
        setupCalendarRecyclerView()
        setupHorizontalWallets()
        setupClickListeners()
        observeViewModel()

        loadDataForSelectedMonth()
        updateTabStyles()
        accountViewModel.loadAccounts(currentUser!!.userId)
    }

    private fun setupDynamicGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour in 5..11 -> "Chào buổi sáng ☀️"
            hour in 12..17 -> "Chào buổi chiều ☀️"
            else -> "Chúc ngủ ngon 🌙"
        }
        binding.tvGreeting.text = greeting
        binding.tvUsername.text = currentUser?.fullName
    }

    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarGridAdapter(requireContext(), calendarDays)
        calendarAdapter.setOnDayClickListener { day, hasTransaction, position ->
            if (!hasTransaction) {
                val addFragment = AddTransactionFragment().apply {
                    arguments = Bundle().apply {
                        val selectCal = currentCalendar.clone() as Calendar
                        selectCal.set(Calendar.DAY_OF_MONTH, day.dayNumber)
                        putString("prefilled_date", DateUtils.formatApiDate(selectCal.time))
                    }
                    setOnTransactionSavedListener { loadDataForSelectedMonth() }
                }
                addFragment.show(parentFragmentManager, "AddTransactionFragment")
            } else {
                val dayTxs = day.transactions
                val hasImage = dayTxs?.any { !it.imageUrl.isNullOrBlank() } == true

                if (hasImage && dayTxs != null) {
                    var total = 0.0
                    for (t in dayTxs) {
                        if ("EXPENSE".equals(t.transactionType, ignoreCase = true) || t.amount < 0) {
                            total -= abs(t.amount)
                        } else {
                            total += t.amount
                        }
                    }
                    val selectCal = currentCalendar.clone() as Calendar
                    selectCal.set(Calendar.DAY_OF_MONTH, day.dayNumber)
                    val dayTitle = DateUtils.formatVietnameseDayTitle(selectCal.time)
                    val sheet = DayTransactionsBottomSheet.newInstance(dayTitle, dayTxs, total)
                    sheet.show(parentFragmentManager, "DayTransactionsBottomSheet")
                } else {
                    calendarAdapter.setSelectedPosition(position)
                    currentCalendar.set(Calendar.DAY_OF_MONTH, day.dayNumber)
                    updateTabStyles()

                    if (!isMonthMode) {
                        val dayDateStr = DateUtils.formatApiDate(currentCalendar.time)
                        viewModel.fetchDailyReport(currentUser!!.userId, dayDateStr)
                    }

                    val month = currentCalendar.get(Calendar.MONTH) + 1
                    val dayStr = if (day.dayNumber < 10) "0${day.dayNumber}" else "${day.dayNumber}"
                    val monthStr = if (month < 10) "0$month" else "$month"

                    binding.btnSelectedDayPill.text = getString(R.string.home_selected_day_format, dayStr, monthStr)
                    binding.btnSelectedDayPill.visibility = View.VISIBLE

                    binding.btnSelectedDayPill.setOnClickListener {
                        binding.btnSelectedDayPill.visibility = View.GONE
                        calendarAdapter.clearSelection()

                        val selectCal = currentCalendar.clone() as Calendar
                        selectCal.set(Calendar.DAY_OF_MONTH, day.dayNumber)

                        val selectedDateStr = DateUtils.formatApiDate(selectCal.time)
                        val dayTitle = DateUtils.formatVietnameseDayTitle(selectCal.time)

                        val detailFragment = DayDetailFragment.newInstance(selectedDateStr, dayTitle)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, detailFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        }

        binding.rvCalendarGrid.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvCalendarGrid.adapter = calendarAdapter
    }

    private fun setupHorizontalWallets() {
        horizontalAccountAdapter = HorizontalAccountAdapter(requireContext(), accountList)
        horizontalAccountAdapter.setOnAccountClickListener { account ->
            val detailsFragment = AccountDetailsFragment.newInstance(
                account.accountId ?: 0,
                account.accountName ?: "",
                account.balance
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailsFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.rvHorizontalWallets.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvHorizontalWallets.adapter = horizontalAccountAdapter

        binding.btnQuickAddAccount.setOnClickListener {
            val addFragment = AddAccountFragment()
            addFragment.setOnAccountSavedListener {
                accountViewModel.loadAccounts(currentUser!!.userId)
            }
            addFragment.show(parentFragmentManager, "AddAccountFragment")
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener { showAddOptionsBottomSheet(-1) }

        binding.btnPrevMonth.setOnClickListener {
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
            currentCalendar.add(Calendar.MONTH, -1)
            loadDataForSelectedMonth()
        }

        binding.btnNextMonth.setOnClickListener {
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
            currentCalendar.add(Calendar.MONTH, 1)
            loadDataForSelectedMonth()
        }

        binding.monthSelectorContainer.setOnClickListener {
            showMonthYearPickerDialog()
        }

        binding.btnResetMonth.setOnClickListener {
            currentCalendar.timeInMillis = System.currentTimeMillis()
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
            loadDataForSelectedMonth()
        }

        binding.ivAvatar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnDayTab.setOnClickListener {
            isMonthMode = false
            updateTabStyles()
            val dateStr = DateUtils.formatApiDate(currentCalendar.time)
            viewModel.fetchDailyReport(currentUser!!.userId, dateStr)
        }

        binding.btnMonthTab.setOnClickListener {
            isMonthMode = true
            updateTabStyles()
            val month = currentCalendar.get(Calendar.MONTH) + 1
            val year = currentCalendar.get(Calendar.YEAR)
            viewModel.fetchDashboardData(currentUser!!.userId, month, year)
        }
    }

    private fun showMonthYearPickerDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        bottomSheetDialog.setContentView(view)

        val tvYear = view.findViewById<TextView>(R.id.tvYear)
        val btnPrevYear = view.findViewById<ImageView>(R.id.btnPrevYear)
        val btnNextYear = view.findViewById<ImageView>(R.id.btnNextYear)

        val monthResIds = intArrayOf(
            R.id.btnMonth1, R.id.btnMonth2, R.id.btnMonth3, R.id.btnMonth4,
            R.id.btnMonth5, R.id.btnMonth6, R.id.btnMonth7, R.id.btnMonth8,
            R.id.btnMonth9, R.id.btnMonth10, R.id.btnMonth11, R.id.btnMonth12
        )

        val monthButtons = Array(12) { view.findViewById<TextView>(monthResIds[it]) }

        var tempYear = currentCalendar.get(Calendar.YEAR)
        val activeYear = currentCalendar.get(Calendar.YEAR)
        val activeMonth = currentCalendar.get(Calendar.MONTH)

        fun updateDialogUI() {
            tvYear.text = getString(R.string.home_year_format, tempYear)
            for (i in 0 until 12) {
                val btn = monthButtons[i]
                if (tempYear == activeYear && i == activeMonth) {
                    btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                } else {
                    btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.surface))
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
            }
        }

        updateDialogUI()

        btnPrevYear.setOnClickListener {
            tempYear--
            updateDialogUI()
        }

        btnNextYear.setOnClickListener {
            tempYear++
            updateDialogUI()
        }

        for (i in 0 until 12) {
            val monthIndex = i
            monthButtons[i].setOnClickListener {
                currentCalendar.set(Calendar.YEAR, tempYear)
                currentCalendar.set(Calendar.MONTH, monthIndex)
                currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
                loadDataForSelectedMonth()
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.show()
    }

    private fun loadDataForSelectedMonth() {
        val month = currentCalendar.get(Calendar.MONTH) + 1
        val year = currentCalendar.get(Calendar.YEAR)

        binding.tvMonthTitle.text = getString(R.string.home_month_title_format, month, year)

        calendarAdapter.clearSelection()
        binding.btnSelectedDayPill.visibility = View.GONE

        generateCalendarGrid(emptyList())
        renderBudgetSummary()
        updateTabStyles()

        val userId = currentUser?.userId ?: return
        viewModel.fetchDashboardData(userId, month, year)
        budgetViewModel.loadBudgets(userId)
        if (!isMonthMode) {
            val dateStr = DateUtils.formatApiDate(currentCalendar.time)
            viewModel.fetchDailyReport(userId, dateStr)
        }
    }

    private fun observeViewModel() {
        viewModel.monthlyReport.observe(viewLifecycleOwner) { report ->
            if (isMonthMode && report != null) {
                binding.tvExpenseAmount.text = CurrencyFormatter.formatVND(report.totalExpense)
                binding.tvIncomeAmount.text = CurrencyFormatter.formatVND(report.totalIncome)
                updateExpenseBadge(report.totalExpense, false)
            }
        }

        viewModel.dailyReport.observe(viewLifecycleOwner) { report ->
            if (!isMonthMode && report != null) {
                binding.tvExpenseAmount.text = CurrencyFormatter.formatVND(report.totalExpense)
                binding.tvIncomeAmount.text = CurrencyFormatter.formatVND(report.totalIncome)
                updateExpenseBadge(report.totalExpense, true)
            }
        }

        viewModel.monthlyTransactions.observe(viewLifecycleOwner) { transactions ->
            generateCalendarGrid(transactions ?: emptyList())
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        accountViewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            if (accounts != null) {
                accountList.clear()
                accountList.addAll(accounts)
                horizontalAccountAdapter.notifyDataSetChanged()
            }
        }

        budgetViewModel.budgets.observe(viewLifecycleOwner) { budgets ->
            allBudgets.clear()
            if (budgets != null) {
                allBudgets.addAll(budgets)
            }
            renderBudgetSummary()
        }
    }

    private fun updateExpenseBadge(totalExpense: Double, dayMode: Boolean) {
        if (dayMode) {
            val today = Calendar.getInstance()
            val isToday = today.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
            if (isToday) {
                binding.tvBadgeText.text = if (totalExpense > 0) {
                    getString(R.string.home_today_spent_format, CurrencyFormatter.formatVND(totalExpense))
                } else {
                    getString(R.string.home_today_no_expense)
                }
            } else {
                val day = currentCalendar.get(Calendar.DAY_OF_MONTH)
                val month = currentCalendar.get(Calendar.MONTH) + 1
                binding.tvBadgeText.text = if (totalExpense > 0) {
                    getString(R.string.home_day_spent_format, day, month, CurrencyFormatter.formatVND(totalExpense))
                } else {
                    getString(R.string.home_day_no_expense_format, day, month)
                }
            }
            return
        }

        val month = currentCalendar.get(Calendar.MONTH) + 1
        binding.tvBadgeText.text = if (totalExpense > 0) {
            getString(R.string.home_month_spent_format, month, CurrencyFormatter.formatVND(totalExpense))
        } else {
            getString(R.string.home_month_no_expense_format, month)
        }
    }

    private fun renderBudgetSummary() {
        if (_binding == null) return

        val month = currentCalendar.get(Calendar.MONTH) + 1
        val year = currentCalendar.get(Calendar.YEAR)
        binding.tvBudgetTitle.text = getString(R.string.home_budget_title_format, month, year)

        var totalLimit = 0.0
        var totalSpent = 0.0
        for (budget in allBudgets) {
            if (isBudgetInSelectedMonth(budget)) {
                totalLimit += budget.amountLimit
                totalSpent += budget.spentAmount
            }
        }

        binding.tvBudgetTotal.text = getString(R.string.home_budget_limit_format, CurrencyFormatter.formatVND(totalLimit))
        binding.tvBudgetSpent.text = getString(R.string.home_budget_spent_format, CurrencyFormatter.formatVND(totalSpent))

        val actualPercent = if (totalLimit > 0) ((totalSpent / totalLimit) * 100).roundToInt() else 0
        binding.tvBudPercent.text = getString(R.string.percentage_format, actualPercent)
        binding.progressBudget.progress = actualPercent.coerceIn(0, 100)
    }

    private fun isBudgetInSelectedMonth(budget: Budget): Boolean {
        val startDate = DateUtils.parseApiDate(budget.startDate)
        val endDate = DateUtils.parseApiDate(budget.endDate)
        if (startDate == null && endDate == null) return false

        val monthStart = (currentCalendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            resetTime(this, 0, 0, 0, 0)
        }

        val monthEnd = (monthStart.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            resetTime(this, 23, 59, 59, 999)
        }

        val budgetStart = startDate ?: endDate!!
        val budgetEnd = endDate ?: startDate!!
        return !budgetStart.after(monthEnd.time) && !budgetEnd.before(monthStart.time)
    }

    private fun resetTime(calendar: Calendar, hour: Int, minute: Int, second: Int, millisecond: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, millisecond)
    }

    private fun generateCalendarGrid(transactions: List<Transaction>) {
        calendarDays.clear()
        calendarAdapter.setDisplayedMonth(
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH)
        )

        // Group transactions by dayOfMonth
        val groupedTx = HashMap<Int, MutableList<Transaction>>()
        val selectedMonthVal = currentCalendar.get(Calendar.MONTH) + 1
        val selectedYearVal = currentCalendar.get(Calendar.YEAR)

        for (tx in transactions) {
            try {
                val date = DateUtils.parseApiDate(tx.transactionDate)
                if (date != null) {
                    val txCal = Calendar.getInstance().apply { time = date }
                    val txMonth = txCal.get(Calendar.MONTH) + 1
                    val txYear = txCal.get(Calendar.YEAR)

                    if (txMonth == selectedMonthVal && txYear == selectedYearVal) {
                        val day = txCal.get(Calendar.DAY_OF_MONTH)
                        groupedTx.getOrPut(day) { ArrayList() }.add(tx)
                    }
                }
            } catch (ignored: Exception) {}
        }

        // Get total days in month & first day of week
        val tempCal = currentCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        // Add empty placeholders for offset days
        for (i in 0 until offset) {
            calendarDays.add(CalendarDay(0, true))
        }

        // Add real days of month
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (dayNum in 1..maxDays) {
            val calDay = CalendarDay(dayNum, false)
            groupedTx[dayNum]?.let { calDay.transactions = it }
            calendarDays.add(calDay)
        }

        calendarAdapter.notifyDataSetChanged()
    }

    private fun showAddOptionsBottomSheet(prefilledDay: Int) {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            setContentView(R.layout.bottom_sheet_add_options)
        }

        val btnManual = bottomSheetDialog.findViewById<LinearLayout>(R.id.btnOptionManual)
        val btnOcr = bottomSheetDialog.findViewById<LinearLayout>(R.id.btnOptionOcr)
        val btnYolo = bottomSheetDialog.findViewById<LinearLayout>(R.id.btnOptionYolo)

        btnManual?.setOnClickListener {
            bottomSheetDialog.dismiss()
            val addFragment = AddTransactionFragment().apply {
                if (prefilledDay > 0) {
                    arguments = Bundle().apply {
                        val selectCal = currentCalendar.clone() as Calendar
                        selectCal.set(Calendar.DAY_OF_MONTH, prefilledDay)
                        putString("prefilled_date", DateUtils.formatApiDate(selectCal.time))
                    }
                }
                setOnTransactionSavedListener { loadDataForSelectedMonth() }
            }
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

    private fun updateTabStyles() {
        if (!isAdded || context == null) return

        val activeBgColor = Color.parseColor("#2E2E33")
        val inactiveBgColor = Color.TRANSPARENT
        val activeTextColor = requireContext().getColor(R.color.text_primary)
        val inactiveTextColor = requireContext().getColor(R.color.text_secondary)

        val day = currentCalendar.get(Calendar.DAY_OF_MONTH)
        val month = currentCalendar.get(Calendar.MONTH) + 1

        binding.tvDayBubbleText.text = day.toString()
        binding.tvMonthBubbleText.text = month.toString()

        if (isMonthMode) {
            binding.btnMonthTab.backgroundTintList = ColorStateList.valueOf(activeBgColor)
            binding.tvMonthLabel.setTextColor(activeTextColor)
            binding.ivMonthIcon.imageTintList = ColorStateList.valueOf(activeTextColor)
            binding.tvMonthBubbleText.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3B82F6"))

            binding.btnDayTab.backgroundTintList = ColorStateList.valueOf(inactiveBgColor)
            binding.tvDayLabel.setTextColor(inactiveTextColor)
            binding.tvDayBubbleText.backgroundTintList = ColorStateList.valueOf(activeBgColor)
        } else {
            binding.btnDayTab.backgroundTintList = ColorStateList.valueOf(activeBgColor)
            binding.tvDayLabel.setTextColor(activeTextColor)
            binding.tvDayBubbleText.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3B82F6"))

            binding.btnMonthTab.backgroundTintList = ColorStateList.valueOf(inactiveBgColor)
            binding.tvMonthLabel.setTextColor(inactiveTextColor)
            binding.ivMonthIcon.imageTintList = ColorStateList.valueOf(inactiveTextColor)
            binding.tvMonthBubbleText.backgroundTintList = ColorStateList.valueOf(activeBgColor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
