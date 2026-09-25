package com.example.personalfinance.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.databinding.ItemReportCategoryBinding
import com.example.personalfinance.models.dto.ReportDTO
import com.example.personalfinance.utils.CurrencyFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class CategoryStatsAdapter(
    private val context: Context,
    private val breakdowns: List<ReportDTO.CategoryBreakdown>
) : RecyclerView.Adapter<CategoryStatsAdapter.StatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val binding = ItemReportCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return StatsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(breakdowns[position])
    }

    override fun getItemCount(): Int = breakdowns.size

    inner class StatsViewHolder(private val binding: ItemReportCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportDTO.CategoryBreakdown) {
            binding.tvCategoryName.text = item.categoryName
            binding.tvCategoryAmount.text = CurrencyFormatter.formatVND(item.totalAmount)

            val percent = item.percentage?.roundToInt() ?: 0
            binding.tvCategoryPercent.text = context.getString(R.string.percentage_format, percent)
            binding.pbCategory.progress = percent

            val color = resolveCategoryColor(item.categoryName)
            binding.imgCategoryIcon.setImageResource(resolveCategoryIcon(item.categoryName))
            binding.viewCategoryBg.backgroundTintList = ColorStateList.valueOf(color)
            binding.pbCategory.progressTintList = ColorStateList.valueOf(color)
            binding.tvCategoryPercent.setTextColor(color)
        }

        private fun resolveCategoryColor(categoryName: String?): Int {
            if (categoryName.isNullOrBlank()) {
                return Color.parseColor("#9CA3AF")
            }

            // Premium Obsidian-Dark compatible palette
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

        private fun resolveCategoryIcon(categoryName: String?): Int {
            val name = categoryName?.lowercase(Locale.ROOT) ?: ""
            return when {
                containsAny(name, "ăn uống", "food", "cà phê", "coffee") -> R.drawable.ic_transaction
                containsAny(name, "sức khỏe", "y tế", "health") -> R.drawable.ic_profile
                containsAny(name, "mua sắm", "shopping", "quần áo", "lương", "salary") -> R.drawable.ic_credit_card
                containsAny(name, "thưởng", "bonus") -> R.drawable.ic_star
                containsAny(name, "di chuyển", "travel", "xe") -> R.drawable.ic_home
                else -> R.drawable.ic_budget
            }
        }

        private fun containsAny(text: String, vararg keywords: String): Boolean {
            return keywords.any { text.contains(it) }
        }
    }
}
