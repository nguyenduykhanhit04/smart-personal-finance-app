package com.example.personalfinance.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalfinance.R;
import com.example.personalfinance.databinding.ItemReportCategoryBinding;
import com.example.personalfinance.models.ReportDTO;
import com.example.personalfinance.utils.CurrencyFormatter;

import java.util.List;
import java.util.Locale;

public class CategoryStatsAdapter extends RecyclerView.Adapter<CategoryStatsAdapter.StatsViewHolder> {

    private final Context context;
    private final List<ReportDTO.CategoryBreakdown> breakdowns;

    public CategoryStatsAdapter(Context context, List<ReportDTO.CategoryBreakdown> breakdowns) {
        this.context = context;
        this.breakdowns = breakdowns;
    }

    @NonNull
    @Override
    public StatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportCategoryBinding binding = ItemReportCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new StatsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
        holder.bind(breakdowns.get(position));
    }

    @Override
    public int getItemCount() {
        return breakdowns.size();
    }

    class StatsViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportCategoryBinding binding;

        StatsViewHolder(ItemReportCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReportDTO.CategoryBreakdown item) {
            binding.tvCategoryName.setText(item.getCategoryName());
            binding.tvCategoryAmount.setText(CurrencyFormatter.formatVND(item.getTotalAmount()));

            int percent = item.getPercentage() != null ? (int) Math.round(item.getPercentage()) : 0;
            binding.tvCategoryPercent.setText(context.getString(R.string.percentage_format, percent));
            binding.pbCategory.setProgress(percent);

            int color = resolveCategoryColor(item.getCategoryName());
            binding.imgCategoryIcon.setImageResource(resolveCategoryIcon(item.getCategoryName()));
            binding.viewCategoryBg.setBackgroundTintList(ColorStateList.valueOf(color));
            binding.pbCategory.setProgressTintList(ColorStateList.valueOf(color));
            binding.tvCategoryPercent.setTextColor(color);
        }

        private int resolveCategoryColor(String categoryName) {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return Color.parseColor("#9CA3AF");
            }

            // Premium Obsidian-Dark compatible palette
            String[] palette = {
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
            };

            int index = Math.abs(categoryName.hashCode()) % palette.length;
            return Color.parseColor(palette[index]);
        }

        private int resolveCategoryIcon(String categoryName) {
            String name = categoryName != null ? categoryName.toLowerCase(Locale.ROOT) : "";
            if (containsAny(name, "ăn uống", "food", "cà phê", "coffee")) {
                return R.drawable.ic_transaction;
            } else if (containsAny(name, "sức khỏe", "y tế", "health")) {
                return R.drawable.ic_profile;
            } else if (containsAny(name, "mua sắm", "shopping", "quần áo", "lương", "salary")) {
                return R.drawable.ic_credit_card;
            } else if (containsAny(name, "thưởng", "bonus")) {
                return R.drawable.ic_star;
            } else if (containsAny(name, "di chuyển", "travel", "xe")) {
                return R.drawable.ic_home;
            }
            return R.drawable.ic_budget;
        }

        private boolean containsAny(String text, String... keywords) {
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }
    }
}
