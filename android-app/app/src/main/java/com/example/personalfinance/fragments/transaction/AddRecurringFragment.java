package com.example.personalfinance.fragments.transaction;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.personalfinance.R;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.FragmentAddRecurringBinding;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.RecurringTransaction;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.Constants;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRecurringFragment extends BottomSheetDialogFragment {

    private FragmentAddRecurringBinding binding;
    private User currentUser;

    private RecurringTransaction editingItem = null;
    private String selectedType = Constants.TYPE_EXPENSE; // Constants.TYPE_EXPENSE or INCOME
    private String selectedFreq = "MONTHLY"; // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    private int selectedDayNum = 20; // Default execution day
    private String selectedStartDate = ""; // "yyyy-MM-dd"

    private int selectedCategoryId = 1;
    private String selectedCategoryName = "Sinh hoạt";
    private String selectedCategoryColor = "#6366F1";
    private java.util.List<com.example.personalfinance.models.Category> allCategories = new java.util.ArrayList<>();

    private int selectedAccountId = -1;
    private java.util.List<Account> allAccounts = new java.util.ArrayList<>();

    private OnSavedListener savedListener;

    public interface OnSavedListener {
        void onSaved();
    }

    public static AddRecurringFragment newInstance(RecurringTransaction item) {
        AddRecurringFragment fragment = new AddRecurringFragment();
        Bundle args = new Bundle();
        args.putSerializable("editing_item", item);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnSavedListener(OnSavedListener listener) {
        this.savedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddRecurringBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        // Check if editing
        if (getArguments() != null && getArguments().containsKey("editing_item")) {
            editingItem = (RecurringTransaction) getArguments().getSerializable("editing_item");
        }

        setupInitialData();
        setupClickListeners();
    }

    private void setupInitialData() {
        Calendar cal = Calendar.getInstance();
        selectedStartDate = DateUtils.formatApiDate(cal.getTime());
        binding.tvStartDate.setText(DateUtils.formatDisplayDate(cal.getTime()));

        fetchCategories();
        fetchAccounts();

        if (editingItem != null) {
            binding.tvFormTitle.setText(R.string.label_edit_recurring);
            binding.edtAmount.setText(String.valueOf(editingItem.getAmount()));
            binding.edtNote.setText(editingItem.getNote());
            selectedStartDate = editingItem.getStartDate();
            
            try {
                java.util.Date date = DateUtils.parseApiDate(editingItem.getStartDate());
                if (date != null) {
                    binding.tvStartDate.setText(DateUtils.formatDisplayDate(date));
                }
            } catch (Exception ignored) {}

            selectedType = editingItem.getTransactionType();
            selectTypeTab(selectedType);

            selectedFreq = editingItem.getRepeatType();
            selectFreqTab(selectedFreq);

            int interval = editingItem.getRepeatInterval() != null ? editingItem.getRepeatInterval() : 1;
            binding.edtRepeatInterval.setText(String.valueOf(interval));
            selectedDayNum = 1;
            updateFreqDependentViews();

            if (editingItem.getCategoryId() != null) {
                selectedCategoryId = editingItem.getCategoryId();
                selectedCategoryName = editingItem.getCategoryName();
                selectedCategoryColor = editingItem.getCategoryColor();
                binding.tvCategoryName.setText(selectedCategoryName);
            }
        } else {
            selectTypeTab(Constants.TYPE_EXPENSE);
            selectFreqTab("MONTHLY");
            binding.edtRepeatInterval.setText("1");
        }
    }

    private void setupClickListeners() {
        binding.btnClose.setOnClickListener(v -> dismiss());

        binding.toggleExpense.setOnClickListener(v -> selectTypeTab(Constants.TYPE_EXPENSE));
        binding.toggleIncome.setOnClickListener(v -> selectTypeTab(Constants.TYPE_INCOME));

        binding.freqDaily.setOnClickListener(v -> selectFreqTab("DAILY"));
        binding.freqWeekly.setOnClickListener(v -> selectFreqTab("WEEKLY"));
        binding.freqMonthly.setOnClickListener(v -> selectFreqTab("MONTHLY"));
        binding.freqYearly.setOnClickListener(v -> selectFreqTab("YEARLY"));

        binding.cardCategorySelect.setOnClickListener(v -> showCategorySelectorDialog());
        binding.cardDayOfMonthSelect.setOnClickListener(v -> showDayOfMonthSelectorDialog());
        
        binding.cardStartDate.setOnClickListener(v -> showStartDatePickerDialog());

        binding.btnSave.setOnClickListener(v -> saveRecurringTransaction());
    }

    private void selectTypeTab(String type) {
        selectedType = type;
        
        binding.toggleExpense.setBackground(null);
        binding.toggleExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.toggleIncome.setBackground(null);
        binding.toggleIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        TextView selected = Constants.TYPE_INCOME.equalsIgnoreCase(type) ? binding.toggleIncome : binding.toggleExpense;
        int color = Constants.TYPE_INCOME.equalsIgnoreCase(type) ? R.color.income_green : R.color.expense_red;

        selected.setBackgroundResource(R.drawable.bg_button_rounded);
        selected.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color)));
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        setupDefaultCategoryForType();
    }

    private void selectFreqTab(String freq) {
        selectedFreq = freq;

        binding.freqDaily.setBackground(null);
        binding.freqDaily.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.freqWeekly.setBackground(null);
        binding.freqWeekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.freqMonthly.setBackground(null);
        binding.freqMonthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.freqYearly.setBackground(null);
        binding.freqYearly.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        TextView selected = binding.freqMonthly;
        if ("DAILY".equalsIgnoreCase(freq)) selected = binding.freqDaily;
        else if ("WEEKLY".equalsIgnoreCase(freq)) selected = binding.freqWeekly;
        else if ("YEARLY".equalsIgnoreCase(freq)) selected = binding.freqYearly;

        selected.setBackgroundResource(R.drawable.bg_button_rounded);
        selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E2E33")));
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        updateFreqDependentViews();
    }

    private void fetchCategories() {
        com.example.personalfinance.api.RetrofitClient.getApiService().getCategories(currentUser.getUserId()).enqueue(new retrofit2.Callback<com.example.personalfinance.models.ApiResponse<java.util.List<com.example.personalfinance.models.Category>>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.example.personalfinance.models.ApiResponse<java.util.List<com.example.personalfinance.models.Category>>> call, @NonNull retrofit2.Response<com.example.personalfinance.models.ApiResponse<java.util.List<com.example.personalfinance.models.Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allCategories.clear();
                    if (response.body().getData() != null) {
                        allCategories.addAll(response.body().getData());
                    }
                    setupDefaultCategoryForType();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.example.personalfinance.models.ApiResponse<java.util.List<com.example.personalfinance.models.Category>>> call, @NonNull Throwable t) {
                // Fallback logging
            }
        });
    }

    private void fetchAccounts() {
        com.example.personalfinance.api.RetrofitClient.getApiService().getAccounts(currentUser.getUserId()).enqueue(new retrofit2.Callback<com.example.personalfinance.models.ApiResponse<java.util.List<Account>>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.example.personalfinance.models.ApiResponse<java.util.List<Account>>> call, @NonNull retrofit2.Response<com.example.personalfinance.models.ApiResponse<java.util.List<Account>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allAccounts.clear();
                    if (response.body().getData() != null) {
                        allAccounts.addAll(response.body().getData());
                    }
                    setupDefaultAccount();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.example.personalfinance.models.ApiResponse<java.util.List<Account>>> call, @NonNull Throwable t) {
                // Fallback logging
            }
        });
    }

    private void setupDefaultAccount() {
        if (!allAccounts.isEmpty()) {
            for (Account acc : allAccounts) {
                if ("Ví chính".equalsIgnoreCase(acc.getAccountName())) {
                    selectedAccountId = acc.getAccountId();
                    return;
                }
            }
            selectedAccountId = allAccounts.get(0).getAccountId();
        } else {
            selectedAccountId = 1; // Fallback
        }
    }

    private void setupDefaultCategoryForType() {
        if (allCategories.isEmpty()) return;
        for (com.example.personalfinance.models.Category cat : allCategories) {
            if (selectedType.equalsIgnoreCase(cat.getCategoryType())) {
                selectedCategoryId = cat.getCategoryId();
                selectedCategoryName = cat.getCategoryName();
                selectedCategoryColor = cat.getColor() != null ? cat.getColor() : "#6366F1";
                binding.tvCategoryName.setText(selectedCategoryName);
                break;
            }
        }
    }

    private void updateFreqDependentViews() {
        if (binding == null || getContext() == null) return;
        
        // Hide specific execution day card completely since start_date natively controls it
        binding.tvDayLabel.setVisibility(View.GONE);
        binding.cardDayOfMonthSelect.setVisibility(View.GONE);
        
        // Update repeat every interval suffix
        if ("DAILY".equalsIgnoreCase(selectedFreq)) {
            binding.tvIntervalSuffix.setText(R.string.label_day_interval);
        } else if ("WEEKLY".equalsIgnoreCase(selectedFreq)) {
            binding.tvIntervalSuffix.setText(R.string.label_week_interval);
        } else if ("MONTHLY".equalsIgnoreCase(selectedFreq)) {
            binding.tvIntervalSuffix.setText(R.string.label_month_interval);
        }
    }

    private String getWeeklyDayName(int dayNum) {
        String[] weeks = {"Mỗi thứ Hai", "Mỗi thứ Ba", "Mỗi thứ Tư", "Mỗi thứ Năm", "Mỗi thứ Sáu", "Mỗi thứ Bảy", "Mỗi Chủ Nhật"};
        if (dayNum >= 1 && dayNum <= 7) {
            return weeks[dayNum - 1];
        }
        return "Mỗi thứ Hai";
    }

    private void showCategorySelectorDialog() {
        java.util.List<com.example.personalfinance.models.Category> filtered = new java.util.ArrayList<>();
        for (com.example.personalfinance.models.Category cat : allCategories) {
            if (selectedType.equalsIgnoreCase(cat.getCategoryType())) {
                filtered.add(cat);
            }
        }

        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "Không có danh mục nào thuộc nhóm này!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[filtered.size()];
        for (int i = 0; i < filtered.size(); i++) {
            names[i] = filtered.get(i).getCategoryName();
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Chọn danh mục");
        builder.setItems(names, (dialog, which) -> {
            com.example.personalfinance.models.Category selectedCat = filtered.get(which);
            selectedCategoryId = selectedCat.getCategoryId();
            selectedCategoryName = selectedCat.getCategoryName();
            selectedCategoryColor = selectedCat.getColor() != null ? selectedCat.getColor() : "#6366F1";
            binding.tvCategoryName.setText(selectedCategoryName);
        });
        builder.show();
    }

    private void showDayOfMonthSelectorDialog() {
        if ("WEEKLY".equalsIgnoreCase(selectedFreq)) {
            String[] weeks = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
            com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext());
            builder.setTitle("Chọn thứ thực hiện");
            builder.setItems(weeks, (dialog, which) -> {
                selectedDayNum = which + 1;
                binding.tvDayOfMonth.setText(getWeeklyDayName(selectedDayNum));
            });
            builder.show();
        } else {
            String[] days = new String[28];
            for (int i = 0; i < 28; i++) {
                days[i] = "Mỗi ngày " + (i + 1);
            }

            com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext());
            builder.setTitle("Chọn ngày thực hiện");
            builder.setItems(days, (dialog, which) -> {
                selectedDayNum = which + 1;
                if ("MONTHLY".equalsIgnoreCase(selectedFreq)) {
                    binding.tvDayOfMonth.setText(getString(R.string.format_every_day, selectedDayNum));
                } else {
                    binding.tvDayOfMonth.setText(getString(R.string.format_annual_day, selectedDayNum));
                }
            });
            builder.show();
        }
    }

    private void showStartDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar selectCal = Calendar.getInstance();
            selectCal.set(Calendar.YEAR, year);
            selectCal.set(Calendar.MONTH, month);
            selectCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            selectedStartDate = DateUtils.formatApiDate(selectCal.getTime());
            binding.tvStartDate.setText(DateUtils.formatDisplayDate(selectCal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveRecurringTransaction() {
        String amountStr = binding.edtAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            binding.edtAmount.setError("Vui lòng nhập số tiền!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) {
            binding.edtAmount.setError("Số tiền không hợp lệ!");
            return;
        }

        String intervalStr = binding.edtRepeatInterval.getText().toString().trim();
        int interval = 1;
        if (!intervalStr.isEmpty()) {
            try {
                interval = Integer.parseInt(intervalStr);
            } catch (Exception e) {
                binding.edtRepeatInterval.setError("Số không hợp lệ!");
                binding.edtRepeatInterval.requestFocus();
                return;
            }
        }
        if (interval < 1) {
            binding.edtRepeatInterval.setError("Tần suất phải lớn hơn hoặc bằng 1!");
            binding.edtRepeatInterval.requestFocus();
            return;
        }

        String note = binding.edtNote.getText().toString().trim();
        String title = note.isEmpty() ? selectedCategoryName : note;
        if (title.length() > 50) title = title.substring(0, 47) + "...";

        RecurringTransaction item = editingItem != null ? editingItem : new RecurringTransaction();
        item.setUserId(currentUser.getUserId());
        if (editingItem != null && editingItem.getAccountId() != null) {
            item.setAccountId(editingItem.getAccountId());
        } else {
            item.setAccountId(selectedAccountId > 0 ? selectedAccountId : 1);
        }
        item.setCategoryId(selectedCategoryId);
        item.setCategoryName(selectedCategoryName);
        item.setCategoryColor(selectedCategoryColor);
        item.setTitle(title);
        item.setAmount(amount);
        item.setTransactionType(selectedType);
        item.setRepeatType(selectedFreq);
        item.setRepeatInterval(interval);
        item.setStartDate(selectedStartDate);
        item.setNote(note);
        item.setIsActive(true);

        Callback<ApiResponse<RecurringTransaction>> callback = new Callback<ApiResponse<RecurringTransaction>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<RecurringTransaction>> call, @NonNull Response<ApiResponse<RecurringTransaction>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã lưu giao dịch định kỳ thành công!", Toast.LENGTH_SHORT).show();
                    if (savedListener != null) savedListener.onSaved();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi lưu giao dịch định kỳ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<RecurringTransaction>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Không thể lưu giao dịch định kỳ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (editingItem != null) {
            RetrofitClient.getApiService().updateRecurringTransaction(editingItem.getRecurringId(), item).enqueue(callback);
        } else {
            RetrofitClient.getApiService().createRecurringTransaction(item).enqueue(callback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
