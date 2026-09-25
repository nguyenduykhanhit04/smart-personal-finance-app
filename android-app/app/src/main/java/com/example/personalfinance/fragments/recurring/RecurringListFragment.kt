package com.example.personalfinance.fragments.recurring

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.FragmentRecurringListBinding
import com.example.personalfinance.databinding.ItemRecurringTransactionBinding
import com.example.personalfinance.fragments.transaction.AddRecurringFragment
import com.example.personalfinance.models.dto.ApiResponse
import com.example.personalfinance.models.domain.RecurringTransaction
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.Constants
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.SharedPrefManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class RecurringListFragment : Fragment() {

    private var _binding: FragmentRecurringListBinding? = null
    private val binding get() = _binding!!

    private var currentUser: User? = null
    private val recurringList = ArrayList<RecurringTransaction>()
    private lateinit var adapter: RecurringAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecurringListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).user ?: return

        setupRecyclerView()
        setupClickListeners()
        loadRecurringTransactions()
    }

    private fun setupRecyclerView() {
        adapter = RecurringAdapter(requireContext(), recurringList)
        adapter.setOnItemClickListener { showOptionsBottomSheet(it) }
        binding.rvRecurring.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecurring.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.fabAdd.setOnClickListener { openAddRecurringFragment() }
        binding.btnEmptyAdd.setOnClickListener { openAddRecurringFragment() }

        binding.btnSearch.setOnClickListener {
            Toast.makeText(requireContext(), "Tìm kiếm định kỳ đang tải...", Toast.LENGTH_SHORT).show()
        }
        binding.btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Lọc đang tải...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAddRecurringFragment() {
        val addFragment = AddRecurringFragment()
        addFragment.setOnSavedListener { loadRecurringTransactions() }
        addFragment.show(parentFragmentManager, "AddRecurringFragment")
    }

    private fun loadRecurringTransactions() {
        val user = currentUser ?: return
        RetrofitClient.apiService.getRecurringTransactions(user.userId)
            .enqueue(object : Callback<ApiResponse<List<RecurringTransaction>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<RecurringTransaction>>>,
                    response: Response<ApiResponse<List<RecurringTransaction>>>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.data != null) {
                        recurringList.clear()
                        recurringList.addAll(body.data!!)
                        updateUI()
                    } else {
                        loadMockRecurringData()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<RecurringTransaction>>>, t: Throwable) {
                    loadMockRecurringData()
                }
            })
    }

    private fun loadMockRecurringData() {
        recurringList.clear()

        recurringList.add(
            RecurringTransaction(
                recurringId = 1,
                title = "Tiền điện",
                amount = 500000.0,
                transactionType = Constants.TYPE_EXPENSE,
                repeatType = "MONTHLY",
                startDate = "2026-05-20",
                categoryName = "Sinh hoạt",
                categoryColor = "#6366F1",
                isActive = true
            )
        )
        recurringList.add(
            RecurringTransaction(
                recurringId = 2,
                title = "Internet",
                amount = 220000.0,
                transactionType = Constants.TYPE_EXPENSE,
                repeatType = "MONTHLY",
                startDate = "2026-05-05",
                categoryName = "Sinh hoạt",
                categoryColor = "#6366F1",
                isActive = true
            )
        )
        recurringList.add(
            RecurringTransaction(
                recurringId = 3,
                title = "Lương",
                amount = 15000000.0,
                transactionType = Constants.TYPE_INCOME,
                repeatType = "MONTHLY",
                startDate = "2026-05-01",
                categoryName = "Thu nhập",
                categoryColor = "#10B981",
                isActive = true
            )
        )
        recurringList.add(
            RecurringTransaction(
                recurringId = 4,
                title = "Tiết kiệm",
                amount = 2000000.0,
                transactionType = Constants.TYPE_EXPENSE,
                repeatType = "MONTHLY",
                startDate = "2026-05-28",
                categoryName = "Tiết kiệm",
                categoryColor = "#EC4899",
                isActive = false
            )
        )

        updateUI()
    }

    private fun updateUI() {
        val activeCount = recurringList.count { it.isActive ?: false }
        binding.tvActiveCount.text = getString(R.string.format_active_recurring, activeCount)

        if (recurringList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvRecurring.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvRecurring.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()
    }

    private fun showOptionsBottomSheet(item: RecurringTransaction) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_recurring_options, null)
        dialog.setContentView(sheetView)

        val tvTitle = sheetView.findViewById<TextView>(R.id.tvRecurringTitle)
        val btnPauseResume = sheetView.findViewById<View>(R.id.btnPauseResume)
        val tvPauseResume = sheetView.findViewById<TextView>(R.id.tvPauseResumeText)
        val btnEdit = sheetView.findViewById<View>(R.id.btnEditRecurring)
        val btnDelete = sheetView.findViewById<View>(R.id.btnDeleteRecurring)

        tvTitle?.text = item.title
        val active = item.isActive ?: false
        tvPauseResume?.text = if (active) "Tạm dừng định kỳ" else "Bật lại định kỳ"

        btnPauseResume?.setOnClickListener {
            dialog.dismiss()
            item.isActive = !active
            updateRecurringStatus(item)
        }

        btnEdit?.setOnClickListener {
            dialog.dismiss()
            val editFragment = AddRecurringFragment.newInstance(item)
            editFragment.setOnSavedListener { loadRecurringTransactions() }
            editFragment.show(parentFragmentManager, "AddRecurringFragment")
        }

        btnDelete?.setOnClickListener {
            dialog.dismiss()
            deleteRecurringTransaction(item)
        }

        dialog.show()
    }

    private fun updateRecurringStatus(item: RecurringTransaction) {
        val id = item.recurringId ?: return
        RetrofitClient.apiService.updateRecurringTransaction(id, item)
            .enqueue(object : Callback<ApiResponse<RecurringTransaction>> {
                override fun onResponse(call: Call<ApiResponse<RecurringTransaction>>, response: Response<ApiResponse<RecurringTransaction>>) {
                    val isActive = item.isActive ?: false
                    Toast.makeText(requireContext(), if (isActive) "Đã bật lại định kỳ!" else "Đã tạm dừng định kỳ!", Toast.LENGTH_SHORT).show()
                    loadRecurringTransactions()
                }

                override fun onFailure(call: Call<ApiResponse<RecurringTransaction>>, t: Throwable) {
                    loadRecurringTransactions()
                }
            })
    }

    private fun deleteRecurringTransaction(item: RecurringTransaction) {
        val id = item.recurringId ?: return
        RetrofitClient.apiService.deleteRecurringTransaction(id)
            .enqueue(object : Callback<ApiResponse<Void>> {
                override fun onResponse(call: Call<ApiResponse<Void>>, response: Response<ApiResponse<Void>>) {
                    Toast.makeText(requireContext(), "Đã xóa khoản định kỳ!", Toast.LENGTH_SHORT).show()
                    loadRecurringTransactions()
                }

                override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                    recurringList.remove(item)
                    updateUI()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private static class RecurringAdapter(
        private val context: Context,
        private val list: List<RecurringTransaction>
    ) : RecyclerView.Adapter<RecurringAdapter.ViewHolder>() {

        fun interface OnItemClickListener {
            fun onItemClick(item: RecurringTransaction)
        }

        private var listener: OnItemClickListener? = null

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.listener = listener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRecurringTransactionBinding.inflate(LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvTitle.text = item.title

            var dateSuffix = ""
            try {
                val start = item.startDate
                if (start != null && start.length >= 10) {
                    val parts = start.split("-")
                    dateSuffix = " - ${parts[2]}/${parts[1]}"
                }
            } catch (ignored: Exception) {}

            val interval = item.repeatInterval ?: 1
            val repeatLabel = if (interval > 1) {
                when {
                    "DAILY".equals(item.repeatType, ignoreCase = true) -> "Mỗi $interval ngày"
                    "WEEKLY".equals(item.repeatType, ignoreCase = true) -> "Mỗi $interval tuần"
                    "MONTHLY".equals(item.repeatType, ignoreCase = true) -> "Mỗi $interval tháng"
                    else -> "Mỗi $interval kỳ"
                }
            } else {
                when {
                    "DAILY".equals(item.repeatType, ignoreCase = true) -> "Hàng ngày"
                    "WEEKLY".equals(item.repeatType, ignoreCase = true) -> "Hàng tuần"
                    "MONTHLY".equals(item.repeatType, ignoreCase = true) -> "Hàng tháng"
                    else -> "Định kỳ"
                }
            }

            holder.binding.tvSubtext.text = context.getString(R.string.format_recurring_month_day, repeatLabel, dateSuffix)

            val amount = item.amount ?: 0.0
            if (Constants.TYPE_INCOME.equals(item.transactionType, ignoreCase = true)) {
                holder.binding.tvAmount.text = context.getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(amount))
                holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green))
            } else {
                holder.binding.tvAmount.text = context.getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(amount))
                holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red))
            }

            var colorVal = Color.parseColor("#3B82F6")
            val colorStr = item.categoryColor
            if (!colorStr.isNullOrEmpty()) {
                try {
                    colorVal = Color.parseColor(colorStr)
                } catch (ignored: Exception) {}
            }
            val drawable = holder.binding.viewCategoryColor.background as? GradientDrawable
            drawable?.setColor(colorVal)

            val name = (item.title ?: "").lowercase(Locale.ROOT)
            val iconRes = when {
                name.contains("điện") || name.contains("nước") -> R.drawable.ic_home
                name.contains("internet") || name.contains("wifi") || name.contains("mạng") -> R.drawable.ic_scan
                name.contains("lương") || name.contains("bonus") || name.contains("thu nhập") -> R.drawable.ic_budget
                else -> R.drawable.ic_recurring
            }

            holder.binding.ivCategoryIcon.setImageResource(iconRes)
            holder.itemView.alpha = if (item.isActive == true) 1.0f else 0.45f

            holder.itemView.setOnClickListener {
                listener?.onItemClick(item)
            }
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(val binding: ItemRecurringTransactionBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
