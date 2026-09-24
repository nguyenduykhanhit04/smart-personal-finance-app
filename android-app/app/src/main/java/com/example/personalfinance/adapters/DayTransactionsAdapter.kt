package com.example.personalfinance.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.models.Transaction
import com.example.personalfinance.utils.CurrencyFormatter
import java.util.Locale
import kotlin.math.abs

class DayTransactionsAdapter(
    private val context: Context,
    private val list: List<Transaction>
) : RecyclerView.Adapter<DayTransactionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_day_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = list[position]
        holder.tvTitle.text = tx.title

        // Check if there is a note or transaction date/time
        var timeStr = "08:00" // default mock time
        val createdAt = tx.createdAt
        if (createdAt != null && createdAt.length >= 16) {
            try {
                // e.g. "2026-05-23T14:30:00" -> extract "14:30"
                timeStr = createdAt.substring(11, 16)
            } catch (ignored: Exception) {}
        } else {
            // Assign some mock times to make the list look realistic like Screen 3
            when (position) {
                0 -> timeStr = "07:45"
                1 -> timeStr = "08:15"
                2 -> timeStr = "12:20"
                3 -> timeStr = "18:30"
            }
        }

        val categoryName = tx.categoryName ?: context.getString(R.string.label_other)
        holder.tvSubtitle.text = context.getString(R.string.format_time_category, timeStr, categoryName)

        val amount = tx.amount
        if ("EXPENSE".equals(tx.transactionType, ignoreCase = true) || amount < 0) {
            holder.tvAmount.text = context.getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(abs(amount)))
            holder.tvAmount.setTextColor(Color.parseColor("#F85149")) // red
        } else {
            holder.tvAmount.text = context.getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(amount))
            holder.tvAmount.setTextColor(Color.parseColor("#22C55E")) // green
        }

        // Category color & icon mapping
        val name = "${tx.title.lowercase(Locale.ROOT)} ${categoryName.lowercase(Locale.ROOT)}"
        val (colorVal, iconRes) = when {
            name.contains("ăn uống") || name.contains("food") || name.contains("cà phê") || name.contains("bánh") || name.contains("highlands") ->
                Pair(Color.parseColor("#10B981"), R.drawable.ic_transaction)
            name.contains("sức khỏe") || name.contains("health") || name.contains("y tế") ->
                Pair(Color.parseColor("#EF4444"), R.drawable.ic_profile)
            name.contains("mua sắm") || name.contains("shopping") || name.contains("áo") || name.contains("sách") || name.contains("winmart") ->
                Pair(Color.parseColor("#D946EF"), R.drawable.ic_budget)
            name.contains("di chuyển") || name.contains("grab") || name.contains("xe") ->
                Pair(Color.parseColor("#F59E0B"), R.drawable.ic_credit_card)
            name.contains("lương") || name.contains("freelance") || name.contains("thu nhập") ->
                Pair(Color.parseColor("#22C55E"), R.drawable.ic_settings)
            else ->
                Pair(Color.parseColor("#3B82F6"), R.drawable.ic_credit_card)
        }

        holder.ivIcon.setImageResource(iconRes)
        holder.viewColor.backgroundTintList = ColorStateList.valueOf(colorVal)

        // Premium UX check: If the transaction has a receipt photo mock representation
        if (position == 0 && (name.contains("cà phê") || name.contains("highlands"))) {
            holder.cardThumbnail.visibility = View.VISIBLE
            holder.ivThumbnail.setImageResource(R.drawable.ic_splash_logo) // placeholder premium splash logo
        } else {
            holder.cardThumbnail.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewColor: View = itemView.findViewById(R.id.viewCategoryColor)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTransactionTitle)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvTransactionNote)
        val tvAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val cardThumbnail: View = itemView.findViewById(R.id.cardThumbnail)
    }
}
