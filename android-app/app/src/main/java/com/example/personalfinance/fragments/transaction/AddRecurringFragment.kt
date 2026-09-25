package com.example.personalfinance.fragments.transaction

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.personalfinance.R
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.FragmentAddRecurringBinding
import com.example.personalfinance.models.domain.Account
import com.example.personalfinance.models.dto.ApiResponse
import com.example.personalfinance.models.domain.Category
import com.example.personalfinance.models.domain.RecurringTransaction
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.Constants
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddRecurringFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(item: RecurringTransaction): AddRecurringFragment {
            return AddRecurringFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("editing_item", item)
                }
            }
        }
    }

    fun interface OnSavedListener {
        fun onSaved()
    }

    private var _binding: FragmentAddRecurringBinding? = null
    private val binding get() = _binding!!

    private var currentUser: User? = null
    private var editingItem: RecurringTransaction? = null
    private var selectedType = Constants.TYPE_EXPENSE
    private var selectedFreq = "MONTHLY"
    private var selectedDayNum = 20
    private var selectedStartDate = ""

    private var selectedCategoryId = 1
    private var selectedCategoryName = "Sinh hoạt"
    private var selectedCategoryColor = "#6366F1"
    private val allCategories = ArrayList<Category>()

    private var selectedAccountId = -1
    private val allAccounts = ArrayList<Account>()
    private var savedListener: OnSavedListener? = null

    fun setOnSavedListener(listener: OnSavedListener) {
        this.savedListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddRecurringBinding.inflate(inflater, container, false)
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

        arguments?.let {
            if (it.containsKey("editing_item")) {
                editingItem = it.getSerializable("editing_item") as? RecurringTransaction
            }
        }

        setupInitialData()
        setupClickListeners()
    }

    private fun setupInitialData() {
        val cal = Calendar.getInstance()
        selectedStartDate = DateUtils.formatApiDate(cal.time)
        binding.tvStartDate.text = DateUtils.formatDisplayDate(cal.time)

        fetchCategories()
        fetchAccounts()

        val item = editingItem
        if (item != null) {
            binding.tvFormTitle.setText(R.string.label_edit_recurring)
            binding.edtAmount.setText((item.amount ?: 0.0).toString())
            binding.edtNote.setText(item.note)
            selectedStartDate = item.startDate ?: ""

            try {
                val date = DateUtils.parseApiDate(item.startDate)
                if (date != null) {
                    binding.tvStartDate.text = DateUtils.formatDisplayDate(date)
                }
            } catch (ignored: Exception) {}

            selectedType = item.transactionType ?: Constants.TYPE_EXPENSE
            selectTypeTab(selectedType)

            selectedFreq = item.repeatType ?: "MONTHLY"
            selectFreqTab(selectedFreq)

            val interval = item.repeatInterval ?: 1
            binding.edtRepeatInterval.setText(interval.toString())
            selectedDayNum = 1
            updateFreqDependentViews()

            if (item.categoryId != null) {
                selectedCategoryId = item.categoryId!!
                selectedCategoryName = item.categoryName ?: "Sinh hoạt"
                selectedCategoryColor = item.categoryColor ?: "#6366F1"
                binding.tvCategoryName.text = selectedCategoryName
            }
        } else {
            selectTypeTab(Constants.TYPE_EXPENSE)
            selectFreqTab("MONTHLY")
            binding.edtRepeatInterval.setText("1")
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener { dismiss() }

        binding.toggleExpense.setOnClickListener { selectTypeTab(Constants.TYPE_EXPENSE) }
        binding.toggleIncome.setOnClickListener { selectTypeTab(Constants.TYPE_INCOME) }

        binding.freqDaily.setOnClickListener { selectFreqTab("DAILY") }
        binding.freqWeekly.setOnClickListener { selectFreqTab("WEEKLY") }
        binding.freqMonthly.setOnClickListener { selectFreqTab("MONTHLY") }
        binding.freqYearly.setOnClickListener { selectFreqTab("YEARLY") }

        binding.cardCategorySelect.setOnClickListener { showCategorySelectorDialog() }
        binding.cardDayOfMonthSelect.setOnClickListener { showDayOfMonthSelectorDialog() }

        binding.cardStartDate.setOnClickListener { showStartDatePickerDialog() }

        binding.btnSave.setOnClickListener { saveRecurringTransaction() }
    }

    private fun selectTypeTab(type: String) {
        selectedType = type

        binding.toggleExpense.background = null
        binding.toggleExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.toggleIncome.background = null
        binding.toggleIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        val selected = if (Constants.TYPE_INCOME.equals(type, ignoreCase = true)) binding.toggleIncome else binding.toggleExpense
        val color = if (Constants.TYPE_INCOME.equals(type, ignoreCase = true)) R.color.income_green else R.color.expense_red

        selected.setBackgroundResource(R.drawable.bg_button_rounded)
        selected.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        setupDefaultCategoryForType()
    }

    private fun selectFreqTab(freq: String) {
        selectedFreq = freq

        binding.freqDaily.background = null
        binding.freqDaily.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.freqWeekly.background = null
        binding.freqWeekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.freqMonthly.background = null
        binding.freqMonthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        binding.freqYearly.background = null
        binding.freqYearly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))

        val selected = when {
            "DAILY".equals(freq, ignoreCase = true) -> binding.freqDaily
            "WEEKLY".equals(freq, ignoreCase = true) -> binding.freqWeekly
            "YEARLY".equals(freq, ignoreCase = true) -> binding.freqYearly
            else -> binding.freqMonthly
        }

        selected.setBackgroundResource(R.drawable.bg_button_rounded)
        selected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E2E33"))
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        updateFreqDependentViews()
    }

    private fun fetchCategories() {
        val user = currentUser ?: return
        RetrofitClient.apiService.getCategories(user.userId).enqueue(object : Callback<ApiResponse<List<Category>>> {
            override fun onResponse(call: Call<ApiResponse<List<Category>>>, response: Response<ApiResponse<List<Category>>>) {
                val body = response.body()
                if (response.isSuccessful && body?.isSuccess == true) {
                    allCategories.clear()
                    body.data?.let { allCategories.addAll(it) }
                    setupDefaultCategoryForType()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Category>>>, t: Throwable) {}
        })
    }

    private fun fetchAccounts() {
        val user = currentUser ?: return
        RetrofitClient.apiService.getAccounts(user.userId).enqueue(object : Callback<ApiResponse<List<Account>>> {
            override fun onResponse(call: Call<ApiResponse<List<Account>>>, response: Response<ApiResponse<List<Account>>>) {
                val body = response.body()
                if (response.isSuccessful && body?.isSuccess == true) {
                    allAccounts.clear()
                    body.data?.let { allAccounts.addAll(it) }
                    setupDefaultAccount()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Account>>>, t: Throwable) {}
        })
    }

    private fun setupDefaultAccount() {
        if (allAccounts.isNotEmpty()) {
            val mainWallet = allAccounts.firstOrNull { "Ví chính".equals(it.accountName, ignoreCase = true) }
            selectedAccountId = mainWallet?.accountId ?: allAccounts[0].accountId ?: 1
        } else {
            selectedAccountId = 1
        }
    }

    private fun setupDefaultCategoryForType() {
        if (allCategories.isEmpty()) return
        for (cat in allCategories) {
            if (selectedType.equals(cat.categoryType, ignoreCase = true)) {
                selectedCategoryId = cat.categoryId
                selectedCategoryName = cat.categoryName
                selectedCategoryColor = cat.color ?: "#6366F1"
                binding.tvCategoryName.text = selectedCategoryName
                break
            }
        }
    }

    private fun updateFreqDependentViews() {
        if (_binding == null || context == null) return

        binding.tvDayLabel.visibility = View.GONE
        binding.cardDayOfMonthSelect.visibility = View.GONE

        when {
            "DAILY".equals(selectedFreq, ignoreCase = true) -> binding.tvIntervalSuffix.setText(R.string.label_day_interval)
            "WEEKLY".equals(selectedFreq, ignoreCase = true) -> binding.tvIntervalSuffix.setText(R.string.label_week_interval)
            "MONTHLY".equals(selectedFreq, ignoreCase = true) -> binding.tvIntervalSuffix.setText(R.string.label_month_interval)
        }
    }

    private fun getWeeklyDayName(dayNum: Int): String {
        val weeks = arrayOf("Mỗi thứ Hai", "Mỗi thứ Ba", "Mỗi thứ Tư", "Mỗi thứ Năm", "Mỗi thứ Sáu", "Mỗi thứ Bảy", "Mỗi Chủ Nhật")
        return if (dayNum in 1..7) weeks[dayNum - 1] else "Mỗi thứ Hai"
    }

    private fun showCategorySelectorDialog() {
        val filtered = allCategories.filter { selectedType.equals(it.categoryType, ignoreCase = true) }
        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "Không có danh mục nào thuộc nhóm này!", Toast.LENGTH_SHORT).show()
            return
        }

        val names = filtered.map { it.categoryName }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Chọn danh mục")
            .setItems(names) { _, which ->
                val selectedCat = filtered[which]
                selectedCategoryId = selectedCat.categoryId
                selectedCategoryName = selectedCat.categoryName
                selectedCategoryColor = selectedCat.color ?: "#6366F1"
                binding.tvCategoryName.text = selectedCategoryName
            }
            .show()
    }

    private fun showDayOfMonthSelectorDialog() {
        if ("WEEKLY".equals(selectedFreq, ignoreCase = true)) {
            val weeks = arrayOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn thứ thực hiện")
                .setItems(weeks) { _, which ->
                    selectedDayNum = which + 1
                    binding.tvDayOfMonth.text = getWeeklyDayName(selectedDayNum)
                }
                .show()
        } else {
            val days = Array(28) { "Mỗi ngày ${it + 1}" }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn ngày thực hiện")
                .setItems(days) { _, which ->
                    selectedDayNum = which + 1
                    if ("MONTHLY".equals(selectedFreq, ignoreCase = true)) {
                        binding.tvDayOfMonth.text = getString(R.string.format_every_day, selectedDayNum)
                    } else {
                        binding.tvDayOfMonth.text = getString(R.string.format_annual_day, selectedDayNum)
                    }
                }
                .show()
        }
    }

    private fun showStartDatePickerDialog() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            selectedStartDate = DateUtils.formatApiDate(selectCal.time)
            binding.tvStartDate.text = DateUtils.formatDisplayDate(selectCal.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveRecurringTransaction() {
        val amountStr = binding.edtAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            binding.edtAmount.error = "Vui lòng nhập số tiền!"
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: Exception) {
            binding.edtAmount.error = "Số tiền không hợp lệ!"
            return
        }

        val intervalStr = binding.edtRepeatInterval.text.toString().trim()
        var interval = 1
        if (intervalStr.isNotEmpty()) {
            try {
                interval = intervalStr.toInt()
            } catch (e: Exception) {
                binding.edtRepeatInterval.error = "Số không hợp lệ!"
                binding.edtRepeatInterval.requestFocus()
                return
            }
        }
        if (interval < 1) {
            binding.edtRepeatInterval.error = "Tần suất phải lớn hơn hoặc bằng 1!"
            binding.edtRepeatInterval.requestFocus()
            return
        }

        val note = binding.edtNote.text.toString().trim()
        var title = if (note.isEmpty()) selectedCategoryName else note
        if (title.length > 50) title = title.substring(0, 47) + "..."

        val item = editingItem ?: RecurringTransaction()
        item.userId = currentUser?.userId
        item.accountId = if (editingItem?.accountId != null) {
            editingItem!!.accountId
        } else {
            if (selectedAccountId > 0) selectedAccountId else 1
        }
        item.categoryId = selectedCategoryId
        item.categoryName = selectedCategoryName
        item.categoryColor = selectedCategoryColor
        item.title = title
        item.amount = amount
        item.transactionType = selectedType
        item.repeatType = selectedFreq
        item.repeatInterval = interval
        item.startDate = selectedStartDate
        item.note = note
        item.isActive = true

        val callback = object : Callback<ApiResponse<RecurringTransaction>> {
            override fun onResponse(call: Call<ApiResponse<RecurringTransaction>>, response: Response<ApiResponse<RecurringTransaction>>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã lưu giao dịch định kỳ thành công!", Toast.LENGTH_SHORT).show()
                    savedListener?.onSaved()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi lưu giao dịch định kỳ!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<RecurringTransaction>>, t: Throwable) {
                Toast.makeText(requireContext(), "Không thể lưu giao dịch định kỳ: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val editId = editingItem?.recurringId
        if (editId != null) {
            RetrofitClient.apiService.updateRecurringTransaction(editId, item).enqueue(callback)
        } else {
            RetrofitClient.apiService.createRecurringTransaction(item).enqueue(callback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
