package com.example.personalfinance.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.databinding.ItemTransactionBinding
import com.example.personalfinance.models.Transaction
import com.example.personalfinance.utils.Constants
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class TransactionAdapter(
    private val context: Context,
    private val transactions: MutableList<Transaction>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    private sealed class DisplayItem {
        data class Header(val dateStr: String, val netAmount: Double) : DisplayItem()
        data class Item(val transaction: Transaction) : DisplayItem()
    }

    private val displayItems = ArrayList<DisplayItem>()

    fun interface OnTransactionClickListener {
        fun onTransactionClick(transaction: Transaction)
    }

    private var clickListener: OnTransactionClickListener? = null

    init {
        processTransactions()
    }

    fun setOnTransactionClickListener(listener: OnTransactionClickListener) {
        this.clickListener = listener
    }

    fun notifyDataChanged() {
        processTransactions()
        notifyDataSetChanged()
    }

    private fun processTransactions() {
        displayItems.clear()
        if (transactions.isEmpty()) {
            return
        }

        // Sort transactions by date descending, then ID descending
        try {
            transactions.sortWith { t1, t2 ->
                val d1 = t1.transactionDate ?: ""
                val d2 = t2.transactionDate ?: ""
                val comp = d2.compareTo(d1)
                if (comp == 0) {
                    val id1 = t1.transactionId ?: 0
                    val id2 = t2.transactionId ?: 0
                    id2.compareTo(id1)
                } else {
                    comp
                }
            }
        } catch (ignored: Exception) {}

        var lastDate: String? = null
        val dayTransactions = ArrayList<Transaction>()

        for (t in transactions) {
            val currentDate = t.transactionDate?.split("T")?.getOrNull(0) ?: ""
            if (currentDate.isEmpty()) continue

            if (lastDate == null) {
                lastDate = currentDate
                dayTransactions.add(t)
            } else if (currentDate == lastDate) {
                dayTransactions.add(t)
            } else {
                addDayGroup(lastDate, dayTransactions)
                lastDate = currentDate
                dayTransactions.clear()
                dayTransactions.add(t)
            }
        }

        if (lastDate != null && dayTransactions.isNotEmpty()) {
            addDayGroup(lastDate, dayTransactions)
        }
    }

    private fun addDayGroup(dateStr: String, dayTransactions: List<Transaction>) {
        var netAmount = 0.0
        for (t in dayTransactions) {
            if (Constants.TYPE_INCOME == t.transactionType) {
                netAmount += t.amount
            } else {
                netAmount -= t.amount
            }
        }

        // Add Header
        displayItems.add(DisplayItem.Header(dateStr, netAmount))

        // Add Items
        for (t in dayTransactions) {
            displayItems.add(DisplayItem.Item(t))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is DisplayItem.Header -> TYPE_HEADER
            is DisplayItem.Item -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_transaction_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemTransactionBinding.inflate(LayoutInflater.from(context), parent, false)
            TransactionViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.Header -> (holder as HeaderViewHolder).bind(item)
            is DisplayItem.Item -> (holder as TransactionViewHolder).bind(item.transaction)
        }
    }

    override fun getItemCount(): Int = displayItems.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvHeaderDay)
        private val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvHeaderDayOfWeek)
        private val tvMonthYear: TextView = itemView.findViewById(R.id.tvHeaderMonthYear)
        private val tvNetAmount: TextView = itemView.findViewById(R.id.tvHeaderNetAmount)

        fun bind(header: DisplayItem.Header) {
            val dateStr = header.dateStr
            try {
                val date = DateUtils.parseApiDate(dateStr)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }

                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    tvDay.text = String.format(Locale.getDefault(), "%02d", day)

                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    val dayOfWeek = if (dow == Calendar.SUNDAY) "Chủ Nhật" else "Thứ $dow"
                    tvDayOfWeek.text = dayOfWeek

                    val month = cal.get(Calendar.MONTH) + 1
                    val year = cal.get(Calendar.YEAR)
                    tvMonthYear.text = itemView.context.getString(
                        R.string.format_month_year_lowercase,
                        String.format(Locale.getDefault(), "%02d", month),
                        year
                    )
                }
            } catch (e: Exception) {
                tvDay.text = "--"
                tvDayOfWeek.setText(R.string.label_day)
                tvMonthYear.text = dateStr
            }

            val amount = header.netAmount
            when {
                amount > 0 -> {
                    tvNetAmount.text = itemView.context.getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(amount))
                    tvNetAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.income_green))
                }
                amount < 0 -> {
                    tvNetAmount.text = itemView.context.getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(abs(amount)))
                    tvNetAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.expense_red))
                }
                else -> {
                    tvNetAmount.text = CurrencyFormatter.formatVND(0.0)
                    tvNetAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                }
            }
        }
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTransactionTitle.text = transaction.title
            binding.tvTransactionCategory.text = transaction.categoryName

            // Hide individual transaction date as grouped header already displays it
            binding.tvTransactionDate.visibility = View.GONE

            // Format Amount & Color based on Type
            if (Constants.TYPE_INCOME == transaction.transactionType) {
                binding.tvTransactionAmount.text = context.getString(
                    R.string.format_positive_amount,
                    CurrencyFormatter.formatVND(transaction.amount)
                )
                binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green))
            } else {
                binding.tvTransactionAmount.text = context.getString(
                    R.string.format_negative_amount,
                    CurrencyFormatter.formatVND(transaction.amount)
                )
                binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red))
            }

            // Dynamically color & iconify based on Category Name
            val catName = transaction.categoryName?.lowercase(Locale.ROOT) ?: ""
            val circleColor: Int
            val iconRes: Int

            when {
                catName.contains("ăn") || catName.contains("uống") || catName.contains("cà phê") ||
                catName.contains("cafe") || catName.contains("nhà hàng") || catName.contains("food") ||
                catName.contains("bún") || catName.contains("phở") -> {
                    circleColor = Color.parseColor("#F97316") // Coral Orange
                    iconRes = R.drawable.ic_transaction
                }
                catName.contains("di chuyển") || catName.contains("xăng") || catName.contains("xe") ||
                catName.contains("grab") || catName.contains("taxi") -> {
                    circleColor = Color.parseColor("#3B82F6") // Bright Blue
                    iconRes = R.drawable.ic_scan
                }
                catName.contains("lương") || catName.contains("thu nhập") || catName.contains("salary") ||
                catName.contains("thưởng") -> {
                    circleColor = Color.parseColor("#10B981") // Emerald Green
                    iconRes = R.drawable.ic_budget
                }
                catName.contains("nhà") || catName.contains("điện") || catName.contains("nước") ||
                catName.contains("thuê") || catName.contains("rent") -> {
                    circleColor = Color.parseColor("#6366F1") // Indigo Purple
                    iconRes = R.drawable.ic_home
                }
                catName.contains("mua sắm") || catName.contains("quần áo") || catName.contains("shopping") ||
                catName.contains("mỹ phẩm") -> {
                    circleColor = Color.parseColor("#EC4899") // Rose Pink
                    iconRes = R.drawable.ic_transaction
                }
                catName.contains("học") || catName.contains("sách") || catName.contains("study") ||
                catName.contains("edu") -> {
                    circleColor = Color.parseColor("#8B5CF6") // Deep Violet
                    iconRes = R.drawable.ic_profile
                }
                else -> {
                    if (Constants.TYPE_INCOME == transaction.transactionType) {
                        circleColor = ContextCompat.getColor(context, R.color.income_green)
                        iconRes = R.drawable.ic_budget
                    } else {
                        circleColor = ContextCompat.getColor(context, R.color.primary)
                        iconRes = R.drawable.ic_transaction
                    }
                }
            }

            val drawable = binding.viewCategoryColor.background as? GradientDrawable
            drawable?.setColor(circleColor)
            binding.ivCategoryIcon.setImageResource(iconRes)

            itemView.setOnClickListener {
                clickListener?.onTransactionClick(transaction)
            }
        }
    }
}
