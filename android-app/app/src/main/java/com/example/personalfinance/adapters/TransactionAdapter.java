package com.example.personalfinance.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.ItemTransactionBinding;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.utils.Constants;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    // Helper model to store flat list items
    private static class DisplayItem {
        final int type;
        final String dateStr; // for header
        final double netAmount; // for header sum
        final Transaction transaction; // for transaction item

        DisplayItem(int type, String dateStr, double netAmount) {
            this.type = type;
            this.dateStr = dateStr;
            this.netAmount = netAmount;
            this.transaction = null;
        }

        DisplayItem(int type, Transaction transaction) {
            this.type = type;
            this.transaction = transaction;
            this.dateStr = null;
            this.netAmount = 0;
        }
    }

    private final List<Transaction> transactions;
    private final List<DisplayItem> displayItems = new ArrayList<>();
    private final Context context;
    private OnTransactionClickListener clickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        processTransactions();
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }

    public void notifyDataChanged() {
        processTransactions();
        super.notifyDataSetChanged();
    }

    private void processTransactions() {
        displayItems.clear();
        if (transactions == null || transactions.isEmpty()) {
            return;
        }

        // Sort transactions by date descending
        try {
            transactions.sort((t1, t2) -> {
                String d1 = t1.getTransactionDate() != null ? t1.getTransactionDate() : "";
                String d2 = t2.getTransactionDate() != null ? t2.getTransactionDate() : "";
                int comp = d2.compareTo(d1);
                if (comp == 0) {
                    Integer id1 = t1.getTransactionId() != null ? t1.getTransactionId() : 0;
                    Integer id2 = t2.getTransactionId() != null ? t2.getTransactionId() : 0;
                    return id2.compareTo(id1);
                }
                return comp;
            });
        } catch (Exception ignored) {}

        String lastDate = null;
        List<Transaction> dayTransactions = new ArrayList<>();

        for (Transaction t : transactions) {
            String currentDate = t.getTransactionDate() != null ? t.getTransactionDate().split("T")[0] : "";
            if (currentDate.isEmpty()) continue;

            if (lastDate == null) {
                lastDate = currentDate;
                dayTransactions.add(t);
            } else if (currentDate.equals(lastDate)) {
                dayTransactions.add(t);
            } else {
                addDayGroup(lastDate, dayTransactions);
                lastDate = currentDate;
                dayTransactions = new ArrayList<>();
                dayTransactions.add(t);
            }
        }

        if (lastDate != null && !dayTransactions.isEmpty()) {
            addDayGroup(lastDate, dayTransactions);
        }
    }

    private void addDayGroup(String dateStr, List<Transaction> dayTransactions) {
        double netAmount = 0;
        for (Transaction t : dayTransactions) {
            if (Constants.TYPE_INCOME.equals(t.getTransactionType())) {
                netAmount += t.getAmount();
            } else {
                netAmount -= t.getAmount();
            }
        }

        // Add Header
        displayItems.add(new DisplayItem(TYPE_HEADER, dateStr, netAmount));

        // Add Items
        for (Transaction t : dayTransactions) {
            displayItems.add(new DisplayItem(TYPE_TRANSACTION, t));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            ItemTransactionBinding binding = ItemTransactionBinding.inflate(LayoutInflater.from(context), parent, false);
            return new TransactionViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = displayItems.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item);
        } else if (holder instanceof TransactionViewHolder) {
            ((TransactionViewHolder) holder).bind(item.transaction);
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDay;
        private final TextView tvDayOfWeek;
        private final TextView tvMonthYear;
        private final TextView tvNetAmount;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvHeaderDay);
            tvDayOfWeek = itemView.findViewById(R.id.tvHeaderDayOfWeek);
            tvMonthYear = itemView.findViewById(R.id.tvHeaderMonthYear);
            tvNetAmount = itemView.findViewById(R.id.tvHeaderNetAmount);
        }

        public void bind(DisplayItem item) {
            String dateStr = item.dateStr;
            try {
                java.util.Date date = DateUtils.parseApiDate(dateStr);
                if (date != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);

                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    tvDay.setText(String.format(java.util.Locale.getDefault(), "%02d", day));

                    int dow = cal.get(Calendar.DAY_OF_WEEK);
                    String dayOfWeek;
                    if (dow == Calendar.SUNDAY) {
                        dayOfWeek = "Chủ Nhật";
                    } else {
                        dayOfWeek = "Thứ " + dow;
                    }
                    tvDayOfWeek.setText(dayOfWeek);

                    int month = cal.get(Calendar.MONTH) + 1;
                    int year = cal.get(Calendar.YEAR);
                    tvMonthYear.setText(itemView.getContext().getString(
                            R.string.format_month_year_lowercase,
                            String.format(java.util.Locale.getDefault(), "%02d", month),
                            year));
                }
            } catch (Exception e) {
                tvDay.setText("--");
                tvDayOfWeek.setText(R.string.label_day);
                tvMonthYear.setText(dateStr);
            }

            double amount = item.netAmount;
            if (amount > 0) {
                tvNetAmount.setText(itemView.getContext().getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(amount)));
                tvNetAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.income_green));
            } else if (amount < 0) {
                tvNetAmount.setText(itemView.getContext().getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(Math.abs(amount))));
                tvNetAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.expense_red));
            } else {
                tvNetAmount.setText(CurrencyFormatter.formatVND(0));
                tvNetAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }
        }
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        public TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Transaction transaction) {
            binding.tvTransactionTitle.setText(transaction.getTitle());
            binding.tvTransactionCategory.setText(transaction.getCategoryName());

            // Hide the individual transaction date, as the grouped daily header already displays it!
            binding.tvTransactionDate.setVisibility(View.GONE);

            // Format Amount & Color based on Type
            if (Constants.TYPE_INCOME.equals(transaction.getTransactionType())) {
                binding.tvTransactionAmount.setText(context.getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(transaction.getAmount())));
                binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
            } else {
                binding.tvTransactionAmount.setText(context.getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(transaction.getAmount())));
                binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            }

            // Dynamically color & iconify based on Category Name (Clone CapMoney premium feel)
            String catName = transaction.getCategoryName() != null
                    ? transaction.getCategoryName().toLowerCase(Locale.ROOT)
                    : "";
            int circleColor;
            int iconRes;

            if (catName.contains("ăn") || catName.contains("uống") || catName.contains("cà phê") || catName.contains("cafe") || catName.contains("nhà hàng") || catName.contains("food") || catName.contains("bún") || catName.contains("phở")) {
                circleColor = Color.parseColor("#F97316"); // Coral Orange
                iconRes = R.drawable.ic_transaction; // bills/transactions icon
            } else if (catName.contains("di chuyển") || catName.contains("xăng") || catName.contains("xe") || catName.contains("grab") || catName.contains("taxi")) {
                circleColor = Color.parseColor("#3B82F6"); // Bright Blue
                iconRes = R.drawable.ic_scan; // scanning focus icon representing path tracking
            } else if (catName.contains("lương") || catName.contains("thu nhập") || catName.contains("salary") || catName.contains("thưởng")) {
                circleColor = Color.parseColor("#10B981"); // Emerald Green
                iconRes = R.drawable.ic_budget; // budget cash icon
            } else if (catName.contains("nhà") || catName.contains("điện") || catName.contains("nước") || catName.contains("thuê") || catName.contains("rent")) {
                circleColor = Color.parseColor("#6366F1"); // Indigo Purple
                iconRes = R.drawable.ic_home; // home icon
            } else if (catName.contains("mua sắm") || catName.contains("quần áo") || catName.contains("shopping") || catName.contains("mỹ phẩm")) {
                circleColor = Color.parseColor("#EC4899"); // Rose Pink
                iconRes = R.drawable.ic_transaction;
            } else if (catName.contains("học") || catName.contains("sách") || catName.contains("study") || catName.contains("edu")) {
                circleColor = Color.parseColor("#8B5CF6"); // Deep Violet
                iconRes = R.drawable.ic_profile; // student profile
            } else {
                // Fallback style based on type
                if (Constants.TYPE_INCOME.equals(transaction.getTransactionType())) {
                    circleColor = ContextCompat.getColor(context, R.color.income_green);
                    iconRes = R.drawable.ic_budget;
                } else {
                    circleColor = ContextCompat.getColor(context, R.color.primary);
                    iconRes = R.drawable.ic_transaction;
                }
            }

            GradientDrawable drawable = (GradientDrawable) binding.viewCategoryColor.getBackground();
            if (drawable != null) {
                drawable.setColor(circleColor);
            }
            binding.ivCategoryIcon.setImageResource(iconRes);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTransactionClick(transaction);
                }
            });
        }
    }
}
