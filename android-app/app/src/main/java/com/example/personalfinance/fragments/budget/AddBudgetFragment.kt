package com.example.personalfinance.fragments.budget

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.personalfinance.R
import com.example.personalfinance.databinding.FragmentAddBudgetBinding
import com.example.personalfinance.models.Budget
import com.example.personalfinance.models.Category
import com.example.personalfinance.models.User
import com.example.personalfinance.utils.Constants
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.BudgetViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

class AddBudgetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val MAX_CATEGORY_COUNT = 7
    }

    fun interface OnBudgetSavedListener {
        fun onBudgetSaved()
    }

    private var _binding: FragmentAddBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BudgetViewModel
    private var currentUser: User? = null
    private val calendar = Calendar.getInstance()
    private val categoryRows = ArrayList<CategoryBudgetRow>()
    private val existingBudgets = ArrayList<Budget>()
    private val amountFormatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMANY))

    private var startDateStr: String? = null
    private var endDateStr: String? = null
    private var updatingTotal = false
    private var distributingTotal = false
    private var savedListener: OnBudgetSavedListener? = null

    fun setOnBudgetSavedListener(listener: OnBudgetSavedListener) {
        this.savedListener = listener
    }

    fun configureBudgetPeriod(year: Int, zeroBasedMonth: Int, budgets: List<Budget>?) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, zeroBasedMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        existingBudgets.clear()
        if (budgets != null) {
            existingBudgets.addAll(budgets)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).user ?: return
        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]

        startDateStr = formatMonthBoundary(true)
        endDateStr = formatMonthBoundary(false)

        bindInitialUi()
        observeViewModel()
        viewModel.loadCategories(currentUser!!.userId)
    }

    private fun bindInitialUi() {
        val editMode = existingBudgets.isNotEmpty()
        binding.tvSheetTitle.setText(if (editMode) R.string.label_update_monthly_budget else R.string.ui_them_ngan_sach_thang)
        binding.btnSave.setText(if (editMode) R.string.label_update_budget else R.string.label_save_budget)
        binding.tvMonth.text = DateUtils.formatMonthTitle(calendar.time)
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveBudgets() }
        binding.btnReset.setOnClickListener { resetAmounts() }
        binding.edtTotalBudget.hint = "0 đ"
        binding.edtTotalBudget.setSelectAllOnFocus(true)
        binding.edtTotalBudget.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                distributeTotalToCategories(parseAmount(binding.edtTotalBudget.text.toString()))
                true
            } else false
        }

        binding.edtTotalBudget.doAfterTextChanged { s ->
            if (updatingTotal || distributingTotal) return@doAfterTextChanged
            val total = parseAmount(s?.toString())
            setFormattedText(binding.edtTotalBudget, total)
            distributeTotalToCategories(total)
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                renderCategoryRows(list)
            }
        }

        viewModel.budgetsCreated.observe(viewLifecycleOwner) { count ->
            if (count != null) {
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Lưu ngân sách thành công!", Toast.LENGTH_SHORT).show()
                savedListener?.onBudgetSaved()
                dismiss()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderCategoryRows(categories: List<Category>) {
        binding.categoryContainer.removeAllViews()
        categoryRows.clear()

        val existingBudgetByCategory = HashMap<Int, Budget>()
        for (budget in existingBudgets) {
            budget.categoryId?.let { existingBudgetByCategory[it] = budget }
        }

        for (category in categories) {
            if (!Constants.TYPE_EXPENSE.equals(category.categoryType, ignoreCase = true)) {
                continue
            }
            if (categoryRows.size >= MAX_CATEGORY_COUNT) {
                break
            }
            addCategoryRow(category, existingBudgetByCategory[category.categoryId])
        }

        if (categoryRows.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                setText(R.string.label_add_expense_categories_first)
                setTextColor(getColor(R.color.text_secondary))
                textSize = 14f
            }
            binding.categoryContainer.addView(emptyView)
        } else {
            updateTotalFromRows()
        }
    }

    private fun addCategoryRow(category: Category, existingBudget: Budget?) {
        val row = layoutInflater.inflate(R.layout.item_add_budget_row, binding.categoryContainer, false)
        binding.categoryContainer.addView(row)

        val badge = row.findViewById<TextView>(R.id.tvCategoryBadge)
        val nameView = row.findViewById<TextView>(R.id.tvCategoryName)
        val amountInput = row.findViewById<EditText>(R.id.edtAmountInput)
        val dailyLimitInput = row.findViewById<EditText>(R.id.edtDailyLimitInput)

        badge.text = getCategoryInitial(category)
        badge.background = makeCircleDrawable(category.color)
        nameView.text = category.categoryName

        if (existingBudget != null && existingBudget.amountLimit > 0) {
            setFormattedText(amountInput, existingBudget.amountLimit)
        }
        if (existingBudget != null && existingBudget.dailyAmountLimit > 0) {
            setFormattedText(dailyLimitInput, existingBudget.dailyAmountLimit)
        }

        val categoryBudgetRow = CategoryBudgetRow(category, existingBudget, amountInput, dailyLimitInput)
        categoryRows.add(categoryBudgetRow)

        amountInput.doAfterTextChanged { s ->
            if (distributingTotal) return@doAfterTextChanged
            setFormattedText(amountInput, parseAmount(s?.toString()))
            updateTotalFromRows()
        }

        dailyLimitInput.doAfterTextChanged { s ->
            setFormattedText(dailyLimitInput, parseAmount(s?.toString()))
        }
    }

    private fun distributeTotalToCategories(total: Double) {
        if (categoryRows.isEmpty()) return

        distributingTotal = true
        val perCategory = total / categoryRows.size
        for (row in categoryRows) {
            setFormattedText(row.amountInput, perCategory)
        }
        distributingTotal = false
    }

    private fun updateTotalFromRows() {
        var total = 0.0
        for (row in categoryRows) {
            total += parseAmount(row.amountInput.text.toString())
        }
        updatingTotal = true
        setFormattedText(binding.edtTotalBudget, total)
        updatingTotal = false
    }

    private fun resetAmounts() {
        distributingTotal = true
        for (row in categoryRows) {
            row.amountInput.setText("")
        }
        distributingTotal = false

        updatingTotal = true
        binding.edtTotalBudget.setText("")
        updatingTotal = false
    }

    private fun saveBudgets() {
        if (categoryRows.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng thêm danh mục chi tiêu trước", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetsToCreate = ArrayList<Budget>()
        val budgetsToUpdate = ArrayList<Budget>()
        val monthLabel = DateUtils.formatMonthYear(calendar.time)

        for (row in categoryRows) {
            val amount = parseAmount(row.amountInput.text.toString())
            if (amount <= 0) continue

            val budget = row.existingBudget ?: Budget()
            budget.userId = currentUser?.userId
            budget.categoryId = row.category.categoryId
            budget.budgetName = "Ngân sách ${row.category.categoryName} $monthLabel"
            budget.amountLimit = amount
            budget.dailyAmountLimit = parseAmount(row.dailyLimitInput.text.toString())
            budget.startDate = startDateStr
            budget.endDate = endDateStr

            if (row.existingBudget != null) {
                budgetsToUpdate.add(budget)
            } else {
                budgetsToCreate.add(budget)
            }
        }

        if (budgetsToCreate.isEmpty() && budgetsToUpdate.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập ngân sách cho ít nhất một danh mục", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false
        viewModel.saveBudgets(budgetsToCreate, budgetsToUpdate)
    }

    private fun formatMonthBoundary(firstDay: Boolean): String {
        val selected = calendar.clone() as Calendar
        selected.set(Calendar.DAY_OF_MONTH, if (firstDay) 1 else selected.getActualMaximum(Calendar.DAY_OF_MONTH))
        return DateUtils.formatApiDate(selected.time)
    }

    private fun setFormattedText(editText: EditText, value: Double) {
        val formatted = if (value > 0) amountFormatter.format(value.roundToLong()) else ""
        if (formatted == editText.text.toString()) return
        editText.setText(formatted)
        editText.setSelection(editText.text.length)
    }

    private fun parseAmount(value: String?): Double {
        if (value.isNullOrBlank()) return 0.0
        val digits = value.replace("[^0-9]".toRegex(), "")
        if (digits.isEmpty()) return 0.0
        return digits.toDoubleOrNull() ?: 0.0
    }

    private fun getCategoryInitial(category: Category): String {
        val name = category.categoryName
        if (name.isNullOrBlank()) return "?"
        return name.trim().substring(0, 1).uppercase(Locale.getDefault())
    }

    private fun makeCircleDrawable(colorValue: String?): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(parseColor(colorValue))
        }
    }

    private fun parseColor(colorValue: String?): Int {
        if (colorValue.isNullOrBlank()) {
            return getColor(R.color.primary)
        }
        return try {
            Color.parseColor(colorValue)
        } catch (e: IllegalArgumentException) {
            getColor(R.color.primary)
        }
    }

    private fun getColor(resId: Int): Int {
        return androidx.core.content.ContextCompat.getColor(requireContext(), resId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class CategoryBudgetRow(
        val category: Category,
        val existingBudget: Budget?,
        val amountInput: EditText,
        val dailyLimitInput: EditText
    )
}
