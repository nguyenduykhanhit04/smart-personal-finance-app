package com.example.personalfinance.fragments.transaction

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinance.R
import com.example.personalfinance.adapters.CategoryStatsAdapter
import com.example.personalfinance.databinding.FragmentTransactionBinding
import com.example.personalfinance.models.ReportDTO
import com.example.personalfinance.models.User
import com.example.personalfinance.repositories.TransactionRepository
import com.example.personalfinance.utils.Constants
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.Calendar
import kotlin.math.abs

class TransactionFragment : Fragment() {

    companion object {
        private const val PERIOD_MONTH = "month"
        private const val PERIOD_YEAR = "year"
        private const val PERIOD_ALL = "all"
    }

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: TransactionRepository
    private var currentUser: User? = null

    private lateinit var statsAdapter: CategoryStatsAdapter
    private val breakdowns = mutableListOf<ReportDTO.CategoryBreakdown>()
    private val calendar: Calendar = Calendar.getInstance()
    private var currentReport: ReportDTO? = null
    private var selectedPeriod = PERIOD_MONTH
    private var selectedType = Constants.TYPE_EXPENSE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser()
        if (currentUser == null) return

        repository = TransactionRepository()

        setupRecyclerView()
        setupClickListeners()
        updatePeriodTabs()
        updateTypeTabs()
        loadStatistics()
    }

    private fun setupRecyclerView() {
        statsAdapter = CategoryStatsAdapter(requireContext(), breakdowns)
        binding.rvCategoryStats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryStats.adapter = statsAdapter
    }

    private fun setupClickListeners() {
        binding.tabMonth.setOnClickListener {
            selectedPeriod = PERIOD_MONTH
            updatePeriodTabs()
            loadStatistics()
        }
        binding.tabYear.setOnClickListener {
            selectedPeriod = PERIOD_YEAR
            updatePeriodTabs()
            loadStatistics()
        }
        binding.tabAll.setOnClickListener {
            selectedPeriod = PERIOD_ALL
            updatePeriodTabs()
            loadStatistics()
        }

        binding.tabExpense.setOnClickListener {
            selectedType = Constants.TYPE_EXPENSE
            updateTypeTabs()
            applyReportToUi()
        }
        binding.cardExpense.setOnClickListener { binding.tabExpense.performClick() }

        binding.tabIncome.setOnClickListener {
            selectedType = Constants.TYPE_INCOME
            updateTypeTabs()
            applyReportToUi()
        }
        binding.cardIncome.setOnClickListener { binding.tabIncome.performClick() }
    }

    private fun updatePeriodTabs() {
        stylePeriodTab(binding.tabMonth, PERIOD_MONTH == selectedPeriod)
        stylePeriodTab(binding.tabYear, PERIOD_YEAR == selectedPeriod)
        stylePeriodTab(binding.tabAll, PERIOD_ALL == selectedPeriod)
    }

    private fun stylePeriodTab(view: TextView, selected: Boolean) {
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_button_rounded)
            view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            view.background = null
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun updateTypeTabs() {
        val expenseSelected = Constants.TYPE_EXPENSE == selectedType
        styleTypeTab(binding.tabExpense, expenseSelected, R.color.expense_red)
        styleTypeTab(binding.tabIncome, !expenseSelected, R.color.income_green)
    }

    private fun styleTypeTab(view: TextView, selected: Boolean, selectedColorRes: Int) {
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_button_rounded)
            view.backgroundTintList = ContextCompat.getColorStateList(requireContext(), selectedColorRes)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            view.background = null
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun loadStatistics() {
        val userId = currentUser?.userId ?: return
        val range = resolveDateRange()
        repository.getCategoryReport(userId, range.startDate, range.endDate, object : TransactionRepository.ApiCallback<ReportDTO> {
            override fun onSuccess(result: ReportDTO?) {
                currentReport = result
                if (_binding != null) {
                    applyReportToUi()
                }
            }

            override fun onError(errorMessage: String) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Lỗi tải thống kê: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun resolveDateRange(): DateRange {
        val start = calendar.clone() as Calendar
        val end = calendar.clone() as Calendar

        return when (selectedPeriod) {
            PERIOD_YEAR -> {
                start.set(Calendar.MONTH, Calendar.JANUARY)
                start.set(Calendar.DAY_OF_MONTH, 1)
                end.set(Calendar.MONTH, Calendar.DECEMBER)
                end.set(Calendar.DAY_OF_MONTH, 31)
                DateRange(DateUtils.formatApiDate(start.time), DateUtils.formatApiDate(end.time))
            }
            PERIOD_ALL -> {
                DateRange("1970-01-01", "2100-12-31")
            }
            else -> {
                start.set(Calendar.DAY_OF_MONTH, 1)
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
                DateRange(DateUtils.formatApiDate(start.time), DateUtils.formatApiDate(end.time))
            }
        }
    }

    private fun applyReportToUi() {
        val report = currentReport ?: return
        val b = _binding ?: return

        b.tvTotalIncome.text = CurrencyFormatter.formatVND(report.totalIncome)
        b.tvTotalExpense.text = CurrencyFormatter.formatVND(report.totalExpense)
        b.tvNetBalance.text = CurrencyFormatter.formatVND(report.netAmount)
        b.tvNetBalance.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (report.netAmount < 0) R.color.expense_red else R.color.income_green
            )
        )

        val filtered = filterBreakdownsByType(report.categoryBreakdowns)
        recalculatePercentages(filtered)

        breakdowns.clear()
        breakdowns.addAll(filtered)
        statsAdapter.notifyDataSetChanged()

        setupPieChart(filtered)
    }

    private fun filterBreakdownsByType(source: List<ReportDTO.CategoryBreakdown>?): MutableList<ReportDTO.CategoryBreakdown> {
        val filtered = mutableListOf<ReportDTO.CategoryBreakdown>()
        if (source == null) return filtered

        for (item in source) {
            if (selectedType.equals(item.categoryType, ignoreCase = true)) {
                filtered.add(item)
            }
        }
        return filtered
    }

    private fun recalculatePercentages(list: List<ReportDTO.CategoryBreakdown>) {
        var total = 0.0
        for (item in list) {
            total += item.totalAmount
        }

        for (item in list) {
            val percentage = if (total > 0) item.totalAmount * 100.0 / total else 0.0
            item.percentage = percentage
        }
    }

    private fun setupPieChart(list: List<ReportDTO.CategoryBreakdown>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        var totalVal = 0.0
        if (list.isEmpty()) {
            entries.add(PieEntry(1f, ""))
            colors.add(Color.parseColor("#2E2E33"))
        } else {
            for (item in list) {
                totalVal += item.totalAmount
                entries.add(PieEntry(item.totalAmount.toFloat(), item.categoryName))
                colors.add(resolveCategoryColor(item.categoryName))
            }
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 3f
            selectionShift = 5f
            isHighlightEnabled = true
            setDrawValues(list.isNotEmpty())
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.45f
            valueLinePart2Length = 0.22f
            valueLineColor = Color.parseColor("#6B7280")
            valueLineWidth = 1.2f
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextColor(Color.parseColor("#F3F4F6"))
            setValueTextSize(10f)
        }

        binding.pieChart.apply {
            this.data = data
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            transparentCircleRadius = 0f
            holeRadius = 72f
            setDrawEntryLabels(list.isNotEmpty())
            setEntryLabelColor(Color.parseColor("#F3F4F6"))
            setEntryLabelTextSize(10f)
            setExtraOffsets(28f, 8f, 28f, 8f)
            legend.isEnabled = false
            description.isEnabled = false

            val title = if (Constants.TYPE_EXPENSE == selectedType) "Chi tiêu" else "Thu nhập"
            isCenterTextScaled = false
            centerText = "$title\n${CurrencyFormatter.formatVND(totalVal)}\n${list.size} Danh mục"
            setCenterTextColor(Color.parseColor("#F3F4F6"))
            setCenterTextSize(12f)

            animateY(700, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun resolveCategoryColor(categoryName: String?): Int {
        if (categoryName.isNullOrBlank()) {
            return Color.parseColor("#9CA3AF")
        }

        val palette = arrayOf(
            "#3B82F6", // Blue
            "#10B981", // Green
            "#D946EF", // Pink
            "#F59E0B", // Yellow
            "#EF4444", // Red
            "#8B5CF6", // Purple
            "#06B6D4", // Cyan
            "#F97316", // Orange
            "#EC4899", // Rose
            "#14B8A6"  // Teal
        )

        val index = abs(categoryName.hashCode()) % palette.size
        return Color.parseColor(palette[index])
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class DateRange(val startDate: String, val endDate: String)
}
