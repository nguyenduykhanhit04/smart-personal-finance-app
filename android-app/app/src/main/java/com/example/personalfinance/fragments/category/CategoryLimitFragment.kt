package com.example.personalfinance.fragments.category

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.databinding.FragmentCategoryLimitBinding
import com.example.personalfinance.databinding.ItemCategoryLimitBinding
import com.example.personalfinance.fragments.budget.AddBudgetFragment
import com.example.personalfinance.models.domain.Budget
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.BudgetViewModel
import java.util.Calendar
import java.util.Date
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.math.roundToInt

class CategoryLimitFragment : Fragment() {

    private enum class PeriodMode {
        MONTH,
        YEAR,
        ALL
    }

    private var _binding: FragmentCategoryLimitBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BudgetViewModel
    private var currentUser: User? = null
    private lateinit var adapter: LimitAdapter

    private val allBudgetList = ArrayList<Budget>()
    private val displayedBudgetList = ArrayList<Budget>()
    private var selectedMode = PeriodMode.MONTH
    private var selectedYear = 0
    private var selectedMonth = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryLimitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).user ?: return

        val now = Calendar.getInstance()
        selectedYear = now.get(Calendar.YEAR)
        selectedMonth = now.get(Calendar.MONTH)

        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        updatePeriodControls()

        loadBudgets()
    }

    private fun setupRecyclerView() {
        adapter = LimitAdapter(requireContext(), displayedBudgetList)
        binding.rvCategoryLimits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryLimits.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnAddCategory.setOnClickListener {
            val addFragment = AddBudgetFragment()
            addFragment.configureBudgetPeriod(selectedYear, selectedMonth, getBudgetsForSelectedMonth())
            addFragment.setOnBudgetSavedListener { loadBudgets() }
            addFragment.show(parentFragmentManager, "AddBudgetFragment")
        }

        binding.tabMonth.setOnClickListener {
            selectedMode = PeriodMode.MONTH
            updatePeriodControls()
            filterAndDisplayBudgets()
        }

        binding.tabYear.setOnClickListener {
            selectedMode = PeriodMode.YEAR
            updatePeriodControls()
            filterAndDisplayBudgets()
        }

        binding.tabAll.setOnClickListener {
            selectedMode = PeriodMode.ALL
            updatePeriodControls()
            filterAndDisplayBudgets()
        }

        binding.btnYear.setOnClickListener { showYearPicker() }
        binding.btnMonth.setOnClickListener { showMonthPicker() }
    }

    private fun loadBudgets() {
        val user = currentUser ?: return
        viewModel.loadBudgets(user.userId)
    }

    private fun observeViewModel() {
        viewModel.budgets.observe(viewLifecycleOwner) { list ->
            allBudgetList.clear()
            if (list != null) {
                allBudgetList.addAll(list)
            }
            filterAndDisplayBudgets()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterAndDisplayBudgets() {
        displayedBudgetList.clear()

        for (budget in allBudgetList) {
            if (selectedMode == PeriodMode.ALL || isBudgetInSelectedPeriod(budget)) {
                displayedBudgetList.add(budget)
            }
        }

        calculateOverallOverview()
        updateSectionHeader()
        updateEmptyState()
        updateBudgetActionText()
        adapter.notifyDataSetChanged()
    }

    private fun isBudgetInSelectedPeriod(budget: Budget): Boolean {
        val start = parseDate(budget.startDate)
        val end = parseDate(budget.endDate)
        if (start == null && end == null) return true

        val periodStart = Calendar.getInstance()
        val periodEnd = Calendar.getInstance()

        if (selectedMode == PeriodMode.YEAR) {
            periodStart.set(selectedYear, Calendar.JANUARY, 1, 0, 0, 0)
            periodEnd.set(selectedYear, Calendar.DECEMBER, 31, 23, 59, 59)
        } else {
            periodStart.set(selectedYear, selectedMonth, 1, 0, 0, 0)
            periodEnd.set(selectedYear, selectedMonth, periodStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        }

        val budgetStart = start ?: end ?: return true
        val budgetEnd = end ?: start ?: return true
        return !budgetStart.after(periodEnd.time) && !budgetEnd.before(periodStart.time)
    }

    private fun updatePeriodControls() {
        selectPeriodTab(binding.tabMonth, selectedMode == PeriodMode.MONTH)
        selectPeriodTab(binding.tabYear, selectedMode == PeriodMode.YEAR)
        selectPeriodTab(binding.tabAll, selectedMode == PeriodMode.ALL)

        binding.btnYear.text = selectedYear.toString()
        binding.btnMonth.text = String.format(Locale.getDefault(), "%02d", selectedMonth + 1)
        binding.btnYear.visibility = if (selectedMode == PeriodMode.ALL) View.GONE else View.VISIBLE
        binding.btnMonth.visibility = if (selectedMode == PeriodMode.MONTH) View.VISIBLE else View.GONE

        when (selectedMode) {
            PeriodMode.ALL -> binding.tvPeriodTitle.setText(R.string.label_all_budgets)
            PeriodMode.YEAR -> binding.tvPeriodTitle.text = getString(R.string.format_year, selectedYear)
            PeriodMode.MONTH -> {
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, 1)
                }
                binding.tvPeriodTitle.text = DateUtils.formatMonthTitle(selectedDate.time)
            }
        }
    }

    private fun selectPeriodTab(tabView: TextView, selected: Boolean) {
        tabView.background = if (selected) ContextCompat.getDrawable(requireContext(), R.drawable.bg_button_rounded) else null
        if (selected) {
            tabView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
            tabView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            tabView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun updateSectionHeader() {
        when (selectedMode) {
            PeriodMode.ALL -> binding.tvSectionHeader.setText(R.string.label_all_categories)
            PeriodMode.YEAR -> binding.tvSectionHeader.setText(R.string.label_categories_in_year)
            PeriodMode.MONTH -> binding.tvSectionHeader.setText(R.string.label_categories_in_month)
        }
    }

    private fun updateEmptyState() {
        val empty = displayedBudgetList.isEmpty()
        binding.tvEmptyState.visibility = if (empty) View.VISIBLE else View.GONE
        binding.rvCategoryLimits.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun updateBudgetActionText() {
        val hasSelectedMonthBudget = getBudgetsForSelectedMonth().isNotEmpty()
        binding.btnAddCategory.setText(if (hasSelectedMonthBudget) R.string.label_update_budget else R.string.ui_them_ngan_sach)
    }

    private fun calculateOverallOverview() {
        var totalLimit = 0.0
        var totalSpent = 0.0
        for (budget in displayedBudgetList) {
            totalLimit += budget.amountLimit
            totalSpent += budget.spentAmount
        }

        val remaining = totalLimit - totalSpent
        val percent = if (totalLimit > 0) ((totalSpent / totalLimit) * 100).roundToInt() else 0
        val progress = percent.coerceIn(0, 100)

        binding.tvOverallLimit.text = CurrencyFormatter.formatVND(totalLimit)
        binding.tvOverallSpent.text = CurrencyFormatter.formatVND(totalSpent)
        binding.tvOverallRemaining.text = CurrencyFormatter.formatVND(remaining)
        binding.tvOverallRemaining.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (remaining < 0) R.color.expense_red else R.color.income_green
            )
        )
        binding.tvOverallPercent.text = getString(R.string.format_percent_used, percent)
        binding.progressOverall.progress = progress
        binding.progressOverall.setIndicatorColor(getProgressColor(percent))
    }

    private fun showYearPicker() {
        val years = getAvailableYears()
        val items = Array(years.size) { years[it].toString() }
        var checkedIndex = 0

        for (i in years.indices) {
            if (years[i] == selectedYear) {
                checkedIndex = i
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn năm")
            .setSingleChoiceItems(items, checkedIndex) { dialog, which ->
                selectedYear = years[which]
                updatePeriodControls()
                filterAndDisplayBudgets()
                dialog.dismiss()
            }
            .show()
    }

    private fun showMonthPicker() {
        val items = Array(12) { "Tháng " + String.format(Locale.getDefault(), "%02d", it + 1) }

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn tháng")
            .setSingleChoiceItems(items, selectedMonth) { dialog, which ->
                selectedMonth = which
                updatePeriodControls()
                filterAndDisplayBudgets()
                dialog.dismiss()
            }
            .show()
    }

    private fun getAvailableYears(): List<Int> {
        val years = LinkedHashSet<Int>()
        years.add(selectedYear)

        for (budget in allBudgetList) {
            addYearIfPresent(years, budget.startDate)
            addYearIfPresent(years, budget.endDate)
        }

        return ArrayList(years)
    }

    private fun getBudgetsForSelectedMonth(): List<Budget> {
        return allBudgetList.filter { isBudgetInMonth(it, selectedYear, selectedMonth) }
    }

    private fun isBudgetInMonth(budget: Budget, year: Int, month: Int): Boolean {
        val start = parseDate(budget.startDate)
        val end = parseDate(budget.endDate)
        if (start == null && end == null) return false

        val periodStart = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val periodEnd = Calendar.getInstance().apply {
            set(year, month, periodStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val budgetStart = start ?: end ?: return false
        val budgetEnd = end ?: start ?: return false
        return !budgetStart.after(periodEnd.time) && !budgetEnd.before(periodStart.time)
    }

    private fun addYearIfPresent(years: MutableSet<Int>, dateValue: String?) {
        val date = parseDate(dateValue) ?: return
        val cal = Calendar.getInstance().apply { time = date }
        years.add(cal.get(Calendar.YEAR))
    }

    private fun parseDate(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        return try {
            DateUtils.parseApiDate(value)
        } catch (ignored: Exception) {
            null
        }
    }

    private fun getProgressColor(percent: Int): Int {
        if (percent >= 100) {
            return ContextCompat.getColor(requireContext(), R.color.expense_red)
        }
        if (percent >= 80) {
            return ContextCompat.getColor(requireContext(), R.color.warning_yellow)
        }
        return ContextCompat.getColor(requireContext(), R.color.primary)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class LimitAdapter(
        private val context: Context,
        private val list: List<Budget>
    ) : RecyclerView.Adapter<LimitAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCategoryLimitBinding.inflate(LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val budget = list[position]
            val spent = budget.spentAmount
            val limit = budget.amountLimit
            val remaining = limit - spent
            val percent = if (limit > 0) ((spent / limit) * 100).roundToInt() else 0
            val progress = percent.coerceIn(0, 100)
            val progressColor = getProgressColor(context, percent)

            holder.binding.tvCategoryName.text = getDisplayName(budget)
            holder.binding.tvBudgetPeriod.text = getPeriodLabel(budget)
            holder.binding.tvBudgetAmount.text = CurrencyFormatter.formatVND(limit)
            holder.binding.tvSpentAmount.text = CurrencyFormatter.formatVND(spent)
            holder.binding.tvRemainingAmount.text = CurrencyFormatter.formatVND(remaining)
            holder.binding.tvRemainingAmount.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (remaining < 0) R.color.expense_red else R.color.income_green
                )
            )
            holder.binding.tvPercentage.text = context.getString(R.string.percentage_format, percent)
            holder.binding.tvPercentage.setTextColor(progressColor)
            holder.binding.progressLimit.progress = progress
            holder.binding.progressLimit.setIndicatorColor(progressColor)

            applyCategoryVisual(holder.binding, budget)
            holder.itemView.setOnClickListener(null)
        }

        private fun getProgressColor(context: Context, percent: Int): Int {
            if (percent >= 100) {
                return ContextCompat.getColor(context, R.color.expense_red)
            }
            if (percent >= 80) {
                return ContextCompat.getColor(context, R.color.warning_yellow)
            }
            return ContextCompat.getColor(context, R.color.primary)
        }

        private fun getDisplayName(budget: Budget): String {
            if (!budget.categoryName.isNullOrBlank()) {
                return budget.categoryName!!
            }

            val name = budget.budgetName
            if (name.isNullOrBlank()) return "Danh mục"
            return name.replaceFirst("(?i)^ngân sách\\s+".toRegex(), "")
                .replaceFirst("\\s+\\d{2}/\\d{4}$".toRegex(), "")
                .trim()
        }

        private fun getPeriodLabel(budget: Budget): String {
            val startDate = budget.startDate
            if (startDate == null || startDate.length < 7) return "Chưa có thời gian"
            return try {
                val parts = startDate.substring(0, 7).split("-")
                "Tháng ${parts[1]}/${parts[0]}"
            } catch (ignored: Exception) {
                "Chưa có thời gian"
            }
        }

        private fun applyCategoryVisual(binding: ItemCategoryLimitBinding, budget: Budget) {
            val name = getDisplayName(budget).lowercase(Locale.getDefault())

            val (circleColor, iconRes) = when {
                name.contains("ăn") || name.contains("uống") || name.contains("cà phê") || name.contains("food") ->
                    Pair(Color.parseColor("#EAB308"), R.drawable.ic_transaction)
                name.contains("di chuyển") || name.contains("xăng") || name.contains("xe") || name.contains("grab") ->
                    Pair(Color.parseColor("#2563EB"), R.drawable.ic_scan)
                name.contains("mua sắm") || name.contains("quần áo") || name.contains("shopping") ->
                    Pair(Color.parseColor("#A855F7"), R.drawable.ic_budget)
                name.contains("giải trí") || name.contains("chơi") || name.contains("phim") ->
                    Pair(Color.parseColor("#F43F5E"), R.drawable.ic_recurring)
                name.contains("sức khỏe") || name.contains("thuốc") || name.contains("y tế") ->
                    Pair(Color.parseColor("#22C55E"), R.drawable.ic_profile)
                name.contains("hóa đơn") || name.contains("điện") || name.contains("nước") || name.contains("thuê") ->
                    Pair(Color.parseColor("#F97316"), R.drawable.ic_home)
                else ->
                    Pair(Color.parseColor("#64748B"), R.drawable.ic_categories)
            }

            val drawable = binding.viewCategoryColor.background as? GradientDrawable
            drawable?.setColor(circleColor)
            binding.ivCategoryIcon.setImageResource(iconRes)
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(val binding: ItemCategoryLimitBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
