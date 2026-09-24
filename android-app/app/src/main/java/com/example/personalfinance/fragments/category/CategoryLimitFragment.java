package com.example.personalfinance.fragments.category;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalfinance.R;
import com.example.personalfinance.databinding.FragmentCategoryLimitBinding;
import com.example.personalfinance.databinding.ItemCategoryLimitBinding;
import com.example.personalfinance.fragments.budget.AddBudgetFragment;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.BudgetViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CategoryLimitFragment extends Fragment {

    private enum PeriodMode {
        MONTH,
        YEAR,
        ALL
    }

    private FragmentCategoryLimitBinding binding;
    private BudgetViewModel viewModel;
    private User currentUser;
    private LimitAdapter adapter;

    private final List<Budget> allBudgetList = new ArrayList<>();
    private final List<Budget> displayedBudgetList = new ArrayList<>();
    private PeriodMode selectedMode = PeriodMode.MONTH;
    private int selectedYear;
    private int selectedMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryLimitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        Calendar now = Calendar.getInstance();
        selectedYear = now.get(Calendar.YEAR);
        selectedMonth = now.get(Calendar.MONTH);

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        updatePeriodControls();

        loadBudgets();
    }

    private void setupRecyclerView() {
        adapter = new LimitAdapter(requireContext(), displayedBudgetList);
        binding.rvCategoryLimits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategoryLimits.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnAddCategory.setOnClickListener(v -> {
            AddBudgetFragment addFragment = new AddBudgetFragment();
            addFragment.configureBudgetPeriod(selectedYear, selectedMonth, getBudgetsForSelectedMonth());
            addFragment.setOnBudgetSavedListener(this::loadBudgets);
            addFragment.show(getParentFragmentManager(), "AddBudgetFragment");
        });

        binding.tabMonth.setOnClickListener(v -> {
            selectedMode = PeriodMode.MONTH;
            updatePeriodControls();
            filterAndDisplayBudgets();
        });

        binding.tabYear.setOnClickListener(v -> {
            selectedMode = PeriodMode.YEAR;
            updatePeriodControls();
            filterAndDisplayBudgets();
        });

        binding.tabAll.setOnClickListener(v -> {
            selectedMode = PeriodMode.ALL;
            updatePeriodControls();
            filterAndDisplayBudgets();
        });

        binding.btnYear.setOnClickListener(v -> showYearPicker());
        binding.btnMonth.setOnClickListener(v -> showMonthPicker());
    }

    private void loadBudgets() {
        viewModel.loadBudgets(currentUser.getUserId());
    }

    private void observeViewModel() {
        viewModel.getBudgets().observe(getViewLifecycleOwner(), list -> {
            allBudgetList.clear();
            if (list != null) {
                allBudgetList.addAll(list);
            }
            filterAndDisplayBudgets();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayBudgets() {
        displayedBudgetList.clear();

        for (Budget budget : allBudgetList) {
            if (selectedMode == PeriodMode.ALL || isBudgetInSelectedPeriod(budget)) {
                displayedBudgetList.add(budget);
            }
        }

        calculateOverallOverview();
        updateSectionHeader();
        updateEmptyState();
        updateBudgetActionText();
        adapter.notifyDataSetChanged();
    }

    private boolean isBudgetInSelectedPeriod(Budget budget) {
        Date start = parseDate(budget.getStartDate());
        Date end = parseDate(budget.getEndDate());
        if (start == null && end == null) return true;

        Calendar periodStart = Calendar.getInstance();
        Calendar periodEnd = Calendar.getInstance();

        if (selectedMode == PeriodMode.YEAR) {
            periodStart.set(selectedYear, Calendar.JANUARY, 1, 0, 0, 0);
            periodEnd.set(selectedYear, Calendar.DECEMBER, 31, 23, 59, 59);
        } else {
            periodStart.set(selectedYear, selectedMonth, 1, 0, 0, 0);
            periodEnd.set(selectedYear, selectedMonth, periodStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        }

        Date budgetStart = start != null ? start : end;
        Date budgetEnd = end != null ? end : start;
        return !budgetStart.after(periodEnd.getTime()) && !budgetEnd.before(periodStart.getTime());
    }

    private void updatePeriodControls() {
        selectPeriodTab(binding.tabMonth, selectedMode == PeriodMode.MONTH);
        selectPeriodTab(binding.tabYear, selectedMode == PeriodMode.YEAR);
        selectPeriodTab(binding.tabAll, selectedMode == PeriodMode.ALL);

        binding.btnYear.setText(String.valueOf(selectedYear));
        binding.btnMonth.setText(String.format(Locale.getDefault(), "%02d", selectedMonth + 1));
        binding.btnYear.setVisibility(selectedMode == PeriodMode.ALL ? View.GONE : View.VISIBLE);
        binding.btnMonth.setVisibility(selectedMode == PeriodMode.MONTH ? View.VISIBLE : View.GONE);

        if (selectedMode == PeriodMode.ALL) {
            binding.tvPeriodTitle.setText(R.string.label_all_budgets);
        } else if (selectedMode == PeriodMode.YEAR) {
            binding.tvPeriodTitle.setText(getString(R.string.format_year, selectedYear));
        } else {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, 1);
            binding.tvPeriodTitle.setText(DateUtils.formatMonthTitle(selectedDate.getTime()));
        }
    }

    private void selectPeriodTab(TextView tabView, boolean selected) {
        tabView.setBackground(selected ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_button_rounded) : null);
        if (selected) {
            tabView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary)));
            tabView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            tabView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void updateSectionHeader() {
        if (selectedMode == PeriodMode.ALL) {
            binding.tvSectionHeader.setText(R.string.label_all_categories);
        } else if (selectedMode == PeriodMode.YEAR) {
            binding.tvSectionHeader.setText(R.string.label_categories_in_year);
        } else {
            binding.tvSectionHeader.setText(R.string.label_categories_in_month);
        }
    }

    private void updateEmptyState() {
        boolean empty = displayedBudgetList.isEmpty();
        binding.tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvCategoryLimits.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateBudgetActionText() {
        boolean hasSelectedMonthBudget = !getBudgetsForSelectedMonth().isEmpty();
        binding.btnAddCategory.setText(hasSelectedMonthBudget ? R.string.label_update_budget : R.string.ui_them_ngan_sach);
    }

    private void calculateOverallOverview() {
        double totalLimit = 0;
        double totalSpent = 0;
        for (Budget budget : displayedBudgetList) {
            totalLimit += budget.getAmountLimit();
            totalSpent += budget.getSpentAmount();
        }

        double remaining = totalLimit - totalSpent;
        int percent = totalLimit > 0 ? (int) Math.round((totalSpent / totalLimit) * 100) : 0;
        int progress = Math.min(100, Math.max(0, percent));

        binding.tvOverallLimit.setText(CurrencyFormatter.formatVND(totalLimit));
        binding.tvOverallSpent.setText(CurrencyFormatter.formatVND(totalSpent));
        binding.tvOverallRemaining.setText(CurrencyFormatter.formatVND(remaining));
        binding.tvOverallRemaining.setTextColor(ContextCompat.getColor(
                requireContext(),
                remaining < 0 ? R.color.expense_red : R.color.income_green
        ));
        binding.tvOverallPercent.setText(getString(R.string.format_percent_used, percent));
        binding.progressOverall.setProgress(progress);
        binding.progressOverall.setIndicatorColor(getProgressColor(percent));
    }

    private void showYearPicker() {
        List<Integer> years = getAvailableYears();
        String[] items = new String[years.size()];
        int checkedIndex = 0;

        for (int i = 0; i < years.size(); i++) {
            int year = years.get(i);
            items[i] = String.valueOf(year);
            if (year == selectedYear) {
                checkedIndex = i;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn năm")
                .setSingleChoiceItems(items, checkedIndex, (dialog, which) -> {
                    selectedYear = years.get(which);
                    updatePeriodControls();
                    filterAndDisplayBudgets();
                    dialog.dismiss();
                })
                .show();
    }

    private void showMonthPicker() {
        String[] items = new String[12];
        for (int i = 0; i < 12; i++) {
            items[i] = "Tháng " + String.format(Locale.getDefault(), "%02d", i + 1);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn tháng")
                .setSingleChoiceItems(items, selectedMonth, (dialog, which) -> {
                    selectedMonth = which;
                    updatePeriodControls();
                    filterAndDisplayBudgets();
                    dialog.dismiss();
                })
                .show();
    }

    private List<Integer> getAvailableYears() {
        Set<Integer> years = new LinkedHashSet<>();
        years.add(selectedYear);

        for (Budget budget : allBudgetList) {
            addYearIfPresent(years, budget.getStartDate());
            addYearIfPresent(years, budget.getEndDate());
        }

        return new ArrayList<>(years);
    }

    private List<Budget> getBudgetsForSelectedMonth() {
        List<Budget> monthBudgets = new ArrayList<>();
        for (Budget budget : allBudgetList) {
            if (isBudgetInMonth(budget, selectedYear, selectedMonth)) {
                monthBudgets.add(budget);
            }
        }
        return monthBudgets;
    }

    private boolean isBudgetInMonth(Budget budget, int year, int month) {
        Date start = parseDate(budget.getStartDate());
        Date end = parseDate(budget.getEndDate());
        if (start == null && end == null) return false;

        Calendar periodStart = Calendar.getInstance();
        periodStart.set(year, month, 1, 0, 0, 0);
        periodStart.set(Calendar.MILLISECOND, 0);

        Calendar periodEnd = Calendar.getInstance();
        periodEnd.set(year, month, periodStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        periodEnd.set(Calendar.MILLISECOND, 999);

        Date budgetStart = start != null ? start : end;
        Date budgetEnd = end != null ? end : start;
        return !budgetStart.after(periodEnd.getTime()) && !budgetEnd.before(periodStart.getTime());
    }

    private void addYearIfPresent(Set<Integer> years, String dateValue) {
        Date date = parseDate(dateValue);
        if (date == null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        years.add(calendar.get(Calendar.YEAR));
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return DateUtils.parseApiDate(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int getProgressColor(int percent) {
        if (percent >= 100) {
            return ContextCompat.getColor(requireContext(), R.color.expense_red);
        }
        if (percent >= 80) {
            return ContextCompat.getColor(requireContext(), R.color.warning_yellow);
        }
        return ContextCompat.getColor(requireContext(), R.color.primary);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class LimitAdapter extends RecyclerView.Adapter<LimitAdapter.ViewHolder> {
        private final Context context;
        private final List<Budget> list;

        public LimitAdapter(Context context, List<Budget> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCategoryLimitBinding binding = ItemCategoryLimitBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Budget budget = list.get(position);
            double spent = budget.getSpentAmount();
            double limit = budget.getAmountLimit();
            double remaining = limit - spent;
            int percent = limit > 0 ? (int) Math.round((spent / limit) * 100) : 0;
            int progress = Math.min(100, Math.max(0, percent));
            int progressColor = getProgressColor(context, percent);

            holder.binding.tvCategoryName.setText(getDisplayName(budget));
            holder.binding.tvBudgetPeriod.setText(getPeriodLabel(budget));
            holder.binding.tvBudgetAmount.setText(CurrencyFormatter.formatVND(limit));
            holder.binding.tvSpentAmount.setText(CurrencyFormatter.formatVND(spent));
            holder.binding.tvRemainingAmount.setText(CurrencyFormatter.formatVND(remaining));
            holder.binding.tvRemainingAmount.setTextColor(ContextCompat.getColor(
                    context,
                    remaining < 0 ? R.color.expense_red : R.color.income_green
            ));
            holder.binding.tvPercentage.setText(context.getString(R.string.percentage_format, percent));
            holder.binding.tvPercentage.setTextColor(progressColor);
            holder.binding.progressLimit.setProgress(progress);
            holder.binding.progressLimit.setIndicatorColor(progressColor);

            applyCategoryVisual(holder.binding, budget);

            holder.itemView.setOnClickListener(null);
        }

        private static int getProgressColor(Context context, int percent) {
            if (percent >= 100) {
                return ContextCompat.getColor(context, R.color.expense_red);
            }
            if (percent >= 80) {
                return ContextCompat.getColor(context, R.color.warning_yellow);
            }
            return ContextCompat.getColor(context, R.color.primary);
        }

        private static String getDisplayName(Budget budget) {
            if (budget.getCategoryName() != null && !budget.getCategoryName().trim().isEmpty()) {
                return budget.getCategoryName();
            }

            String name = budget.getBudgetName();
            if (name == null || name.trim().isEmpty()) return "Danh mục";
            return name.replaceFirst("(?i)^ngân sách\\s+", "")
                    .replaceFirst("\\s+\\d{2}/\\d{4}$", "")
                    .trim();
        }

        private static String getPeriodLabel(Budget budget) {
            String startDate = budget.getStartDate();
            if (startDate == null || startDate.length() < 7) return "Chưa có thời gian";
            try {
                String[] parts = startDate.substring(0, 7).split("-");
                return "Tháng " + parts[1] + "/" + parts[0];
            } catch (Exception ignored) {
                return "Chưa có thời gian";
            }
        }

        private void applyCategoryVisual(ItemCategoryLimitBinding binding, Budget budget) {
            int circleColor;
            int iconRes;
            String name = getDisplayName(budget).toLowerCase(Locale.getDefault());

            if (name.contains("ăn") || name.contains("uống") || name.contains("cà phê") || name.contains("food")) {
                circleColor = Color.parseColor("#EAB308");
                iconRes = R.drawable.ic_transaction;
            } else if (name.contains("di chuyển") || name.contains("xăng") || name.contains("xe") || name.contains("grab")) {
                circleColor = Color.parseColor("#2563EB");
                iconRes = R.drawable.ic_scan;
            } else if (name.contains("mua sắm") || name.contains("quần áo") || name.contains("shopping")) {
                circleColor = Color.parseColor("#A855F7");
                iconRes = R.drawable.ic_budget;
            } else if (name.contains("giải trí") || name.contains("chơi") || name.contains("phim")) {
                circleColor = Color.parseColor("#F43F5E");
                iconRes = R.drawable.ic_recurring;
            } else if (name.contains("sức khỏe") || name.contains("thuốc") || name.contains("y tế")) {
                circleColor = Color.parseColor("#22C55E");
                iconRes = R.drawable.ic_profile;
            } else if (name.contains("hóa đơn") || name.contains("điện") || name.contains("nước") || name.contains("thuê")) {
                circleColor = Color.parseColor("#F97316");
                iconRes = R.drawable.ic_home;
            } else {
                circleColor = Color.parseColor("#64748B");
                iconRes = R.drawable.ic_categories;
            }

            GradientDrawable drawable = (GradientDrawable) binding.viewCategoryColor.getBackground();
            if (drawable != null) {
                drawable.setColor(circleColor);
            }
            binding.ivCategoryIcon.setImageResource(iconRes);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemCategoryLimitBinding binding;

            public ViewHolder(ItemCategoryLimitBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
