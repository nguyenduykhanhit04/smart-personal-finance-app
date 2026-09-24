package com.example.personalfinance.fragments.transaction;

import android.graphics.Color;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.personalfinance.R;
import com.example.personalfinance.adapters.CategoryStatsAdapter;
import com.example.personalfinance.databinding.FragmentTransactionBinding;
import com.example.personalfinance.models.ReportDTO;
import com.example.personalfinance.models.User;
import com.example.personalfinance.repositories.TransactionRepository;
import com.example.personalfinance.utils.Constants;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionFragment extends Fragment {

    private static final String PERIOD_MONTH = "month";
    private static final String PERIOD_YEAR = "year";
    private static final String PERIOD_ALL = "all";

    private FragmentTransactionBinding binding;
    private TransactionRepository repository;
    private User currentUser;

    private CategoryStatsAdapter statsAdapter;
    private final List<ReportDTO.CategoryBreakdown> breakdowns = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    private ReportDTO currentReport;
    private String selectedPeriod = PERIOD_MONTH;
    private String selectedType = Constants.TYPE_EXPENSE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        repository = new TransactionRepository();

        setupRecyclerView();
        setupClickListeners();
        updatePeriodTabs();
        updateTypeTabs();
        loadStatistics();
    }

    private void setupRecyclerView() {
        statsAdapter = new CategoryStatsAdapter(requireContext(), breakdowns);
        binding.rvCategoryStats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategoryStats.setAdapter(statsAdapter);
    }

    private void setupClickListeners() {
        binding.tabMonth.setOnClickListener(v -> {
            selectedPeriod = PERIOD_MONTH;
            updatePeriodTabs();
            loadStatistics();
        });
        binding.tabYear.setOnClickListener(v -> {
            selectedPeriod = PERIOD_YEAR;
            updatePeriodTabs();
            loadStatistics();
        });
        binding.tabAll.setOnClickListener(v -> {
            selectedPeriod = PERIOD_ALL;
            updatePeriodTabs();
            loadStatistics();
        });

        binding.tabExpense.setOnClickListener(v -> {
            selectedType = Constants.TYPE_EXPENSE;
            updateTypeTabs();
            applyReportToUi();
        });
        binding.cardExpense.setOnClickListener(v -> binding.tabExpense.performClick());

        binding.tabIncome.setOnClickListener(v -> {
            selectedType = Constants.TYPE_INCOME;
            updateTypeTabs();
            applyReportToUi();
        });
        binding.cardIncome.setOnClickListener(v -> binding.tabIncome.performClick());
    }

    private void updatePeriodTabs() {
        stylePeriodTab(binding.tabMonth, PERIOD_MONTH.equals(selectedPeriod));
        stylePeriodTab(binding.tabYear, PERIOD_YEAR.equals(selectedPeriod));
        stylePeriodTab(binding.tabAll, PERIOD_ALL.equals(selectedPeriod));
    }

    private void stylePeriodTab(TextView view, boolean selected) {
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_button_rounded);
            view.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            view.setBackground(null);
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void updateTypeTabs() {
        boolean expenseSelected = Constants.TYPE_EXPENSE.equals(selectedType);
        styleTypeTab(binding.tabExpense, expenseSelected, R.color.expense_red);
        styleTypeTab(binding.tabIncome, !expenseSelected, R.color.income_green);
    }

    private void styleTypeTab(TextView view, boolean selected, int selectedColorRes) {
        if (selected) {
            view.setBackgroundResource(R.drawable.bg_button_rounded);
            view.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), selectedColorRes));
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            view.setBackground(null);
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void loadStatistics() {
        DateRange range = resolveDateRange();
        repository.getCategoryReport(currentUser.getUserId(), range.startDate, range.endDate, new TransactionRepository.ApiCallback<ReportDTO>() {
            @Override
            public void onSuccess(ReportDTO result) {
                currentReport = result;
                if (binding != null) {
                    applyReportToUi();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi tải thống kê: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private DateRange resolveDateRange() {
        Calendar start = (Calendar) calendar.clone();
        Calendar end = (Calendar) calendar.clone();

        if (PERIOD_YEAR.equals(selectedPeriod)) {
            start.set(Calendar.MONTH, Calendar.JANUARY);
            start.set(Calendar.DAY_OF_MONTH, 1);
            end.set(Calendar.MONTH, Calendar.DECEMBER);
            end.set(Calendar.DAY_OF_MONTH, 31);
        } else if (PERIOD_ALL.equals(selectedPeriod)) {
            return new DateRange("1970-01-01", "2100-12-31");
        } else {
            start.set(Calendar.DAY_OF_MONTH, 1);
            end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        }

        return new DateRange(DateUtils.formatApiDate(start.getTime()), DateUtils.formatApiDate(end.getTime()));
    }

    private void applyReportToUi() {
        if (binding == null || currentReport == null) return;

        binding.tvTotalIncome.setText(CurrencyFormatter.formatVND(currentReport.getTotalIncome()));
        binding.tvTotalExpense.setText(CurrencyFormatter.formatVND(currentReport.getTotalExpense()));
        binding.tvNetBalance.setText(CurrencyFormatter.formatVND(currentReport.getNetAmount()));
        binding.tvNetBalance.setTextColor(ContextCompat.getColor(
                requireContext(),
                currentReport.getNetAmount() < 0 ? R.color.expense_red : R.color.income_green
        ));

        List<ReportDTO.CategoryBreakdown> filtered = filterBreakdownsByType(currentReport.getCategoryBreakdowns());
        recalculatePercentages(filtered);

        breakdowns.clear();
        breakdowns.addAll(filtered);
        statsAdapter.notifyDataSetChanged();

        setupPieChart(filtered);
    }

    private List<ReportDTO.CategoryBreakdown> filterBreakdownsByType(List<ReportDTO.CategoryBreakdown> source) {
        List<ReportDTO.CategoryBreakdown> filtered = new ArrayList<>();
        if (source == null) return filtered;

        for (ReportDTO.CategoryBreakdown item : source) {
            if (item != null && selectedType.equalsIgnoreCase(item.getCategoryType())) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private void recalculatePercentages(List<ReportDTO.CategoryBreakdown> list) {
        double total = 0;
        for (ReportDTO.CategoryBreakdown item : list) {
            total += item.getTotalAmount();
        }

        for (ReportDTO.CategoryBreakdown item : list) {
            double percentage = total > 0 ? item.getTotalAmount() * 100 / total : 0;
            item.setPercentage(percentage);
        }
    }

    private void setupPieChart(List<ReportDTO.CategoryBreakdown> list) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        double totalVal = 0;
        if (list.isEmpty()) {
            entries.add(new PieEntry(1f, ""));
            colors.add(Color.parseColor("#2E2E33"));
        } else {
            for (ReportDTO.CategoryBreakdown item : list) {
                totalVal += item.getTotalAmount();
                entries.add(new PieEntry((float) item.getTotalAmount(), item.getCategoryName()));
                colors.add(resolveCategoryColor(item.getCategoryName()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setDrawValues(!list.isEmpty());
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.45f);
        dataSet.setValueLinePart2Length(0.22f);
        dataSet.setValueLineColor(Color.parseColor("#6B7280"));
        dataSet.setValueLineWidth(1.2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.pieChart));
        data.setValueTextColor(Color.parseColor("#F3F4F6"));
        data.setValueTextSize(10f);

        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.setTransparentCircleRadius(0f);
        binding.pieChart.setHoleRadius(72f);
        binding.pieChart.setDrawEntryLabels(!list.isEmpty());
        binding.pieChart.setEntryLabelColor(Color.parseColor("#F3F4F6"));
        binding.pieChart.setEntryLabelTextSize(10f);
        binding.pieChart.setExtraOffsets(28f, 8f, 28f, 8f);
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.getDescription().setEnabled(false);

        String title = Constants.TYPE_EXPENSE.equals(selectedType) ? "Chi tiêu" : "Thu nhập";
        binding.pieChart.setDrawCenterText(true);
        binding.pieChart.setCenterText(title + "\n" + CurrencyFormatter.formatVND(totalVal) + "\n" + list.size() + " Danh mục");
        binding.pieChart.setCenterTextColor(Color.parseColor("#F3F4F6"));
        binding.pieChart.setCenterTextSize(12f);

        binding.pieChart.animateY(700, Easing.EaseInOutQuad);
        binding.pieChart.invalidate();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class DateRange {
        final String startDate;
        final String endDate;

        DateRange(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
