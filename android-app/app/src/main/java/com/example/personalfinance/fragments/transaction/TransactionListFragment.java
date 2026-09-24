package com.example.personalfinance.fragments.transaction;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.personalfinance.R;
import com.example.personalfinance.adapters.TransactionAdapter;
import com.example.personalfinance.databinding.FragmentTransactionListBinding;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.Constants;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.fragments.recurring.RecurringListFragment;
import com.example.personalfinance.viewmodels.TransactionViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionListFragment extends Fragment {

    private FragmentTransactionListBinding binding;
    private TransactionViewModel viewModel;
    private User currentUser;

    private TransactionAdapter adapter;
    private final List<Transaction> fullTransactionList = new ArrayList<>();
    private final List<Transaction> filteredTransactionList = new ArrayList<>();
    
    private String selectedTypeTab = "ALL"; // "ALL", "EXPENSE", "INCOME", "TRANSFER"
    private String searchQuery = "";
    private final Calendar currentCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        setupSearch();
        observeViewModel();

        updateMonthHeader();
        loadTransactions();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(requireContext(), filteredTransactionList);
        adapter.setOnTransactionClickListener(transaction -> {
            // Open transaction details or edit fragment
            AddTransactionFragment editFragment = AddTransactionFragment.newInstance(
                    transaction.getTransactionId() != null ? transaction.getTransactionId() : 0,
                    transaction.getAmount(),
                    transaction.getTitle(),
                    transaction.getCategoryId() != null ? transaction.getCategoryId() : 1,
                    transaction.getTransactionDate(),
                    0,
                    transaction.getImageUrl()
            );
            editFragment.setOnTransactionSavedListener(this::loadTransactions);
            editFragment.show(getParentFragmentManager(), "AddTransactionFragment");
        });
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.tabAll.setOnClickListener(v -> selectTypeTab("ALL", binding.tabAll));
        binding.tabExpense.setOnClickListener(v -> selectTypeTab("EXPENSE", binding.tabExpense));
        binding.tabIncome.setOnClickListener(v -> selectTypeTab("INCOME", binding.tabIncome));
        
        binding.btnSearch.setOnClickListener(v -> {
            if (binding.searchBarContainer.getVisibility() == View.VISIBLE) {
                binding.searchBarContainer.setVisibility(View.GONE);
                binding.edtSearchQuery.setText("");
                searchQuery = "";
                filterAndDisplayTransactions();
            } else {
                binding.searchBarContainer.setVisibility(View.VISIBLE);
                binding.edtSearchQuery.requestFocus();
            }
        });


        binding.monthSelectorContainer.setOnClickListener(v -> {
            showMonthYearPickerDialog();
        });

        binding.fabAdd.setOnClickListener(v -> {
            // Direct launch sheet
            showAddOptionsBottomSheet();
        });

        binding.btnRecurringScheduler.setOnClickListener(v -> {
            // Open recurring scheduler fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new RecurringListFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void showMonthYearPickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null);
        bottomSheetDialog.setContentView(view);

        TextView tvYear = view.findViewById(R.id.tvYear);
        ImageView btnPrevYear = view.findViewById(R.id.btnPrevYear);
        ImageView btnNextYear = view.findViewById(R.id.btnNextYear);

        int[] monthResIds = {
                R.id.btnMonth1, R.id.btnMonth2, R.id.btnMonth3, R.id.btnMonth4,
                R.id.btnMonth5, R.id.btnMonth6, R.id.btnMonth7, R.id.btnMonth8,
                R.id.btnMonth9, R.id.btnMonth10, R.id.btnMonth11, R.id.btnMonth12
        };

        TextView[] monthButtons = new TextView[12];
        for (int i = 0; i < 12; i++) {
            monthButtons[i] = view.findViewById(monthResIds[i]);
        }

        final int[] tempYear = {currentCalendar.get(Calendar.YEAR)};
        final int activeYear = currentCalendar.get(Calendar.YEAR);
        final int activeMonth = currentCalendar.get(Calendar.MONTH); // 0-indexed

        Runnable updateDialogUI = new Runnable() {
            @Override
            public void run() {
                tvYear.setText(getString(R.string.format_year, tempYear[0]));
                for (int i = 0; i < 12; i++) {
                    TextView btn = monthButtons[i];
                    if (tempYear[0] == activeYear && i == activeMonth) {
                        // Active selected month
                        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary)));
                        btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                    } else {
                        // Regular month
                        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.surface)));
                        btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                    }
                }
            }
        };

        // Initial UI setup
        updateDialogUI.run();

        btnPrevYear.setOnClickListener(v -> {
            tempYear[0]--;
            updateDialogUI.run();
        });

        btnNextYear.setOnClickListener(v -> {
            tempYear[0]++;
            updateDialogUI.run();
        });

        for (int i = 0; i < 12; i++) {
            final int monthIndex = i;
            monthButtons[i].setOnClickListener(v -> {
                currentCalendar.set(Calendar.YEAR, tempYear[0]);
                currentCalendar.set(Calendar.MONTH, monthIndex);
                currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
                updateMonthHeader();
                filterAndDisplayTransactions();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }

    private void selectTypeTab(String type, TextView tabView) {
        selectedTypeTab = type;
        
        binding.tabAll.setBackground(null);
        binding.tabAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.tabExpense.setBackground(null);
        binding.tabExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.tabIncome.setBackground(null);
        binding.tabIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        tabView.setBackgroundResource(R.drawable.bg_button_rounded);
        tabView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E2E33")));
        tabView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        filterAndDisplayTransactions();
    }

    private void setupSearch() {
        binding.edtSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                filterAndDisplayTransactions();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateMonthHeader() {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        binding.tvMonthTitle.setText(getString(R.string.format_month_year, month, year));
    }

    private void loadTransactions() {
        // Load full transactions range
        viewModel.loadTransactions(currentUser.getUserId(), "2000-01-01", "2100-12-31");
    }

    private void observeViewModel() {
        viewModel.getTransactions().observe(getViewLifecycleOwner(), txs -> {
            fullTransactionList.clear();
            if (txs != null) {
                fullTransactionList.addAll(txs);
            }
            filterAndDisplayTransactions();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayTransactions() {
        filteredTransactionList.clear();

        int selectedMonth = currentCalendar.get(Calendar.MONTH) + 1;
        int selectedYear = currentCalendar.get(Calendar.YEAR);

        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : fullTransactionList) {
            // 1. Filter by Month/Year
            boolean matchTime = false;
            try {
                String dateStr = t.getTransactionDate();
                if (dateStr != null) {
                    java.util.Date date = DateUtils.parseApiDate(dateStr);
                    if (date != null) {
                        Calendar txCal = Calendar.getInstance();
                        txCal.setTime(date);
                        int txMonth = txCal.get(Calendar.MONTH) + 1;
                        int txYear = txCal.get(Calendar.YEAR);
                        matchTime = (txMonth == selectedMonth && txYear == selectedYear);
                    }
                }
            } catch (Exception e) {
                matchTime = true; // fallback
            }

            if (!matchTime) continue;

            // 2. Filter by tab type
            boolean matchType = false;
            if ("ALL".equals(selectedTypeTab)) {
                matchType = true;
            } else if ("EXPENSE".equals(selectedTypeTab)) {
                matchType = Constants.TYPE_EXPENSE.equalsIgnoreCase(t.getTransactionType());
            } else if ("INCOME".equals(selectedTypeTab)) {
                matchType = Constants.TYPE_INCOME.equalsIgnoreCase(t.getTransactionType());
            }

            if (!matchType) continue;

            // 3. Filter by search query
            boolean matchSearch = true;
            if (!searchQuery.isEmpty()) {
                String title = t.getTitle() != null ? t.getTitle().toLowerCase(Locale.ROOT) : "";
                String category = t.getCategoryName() != null ? t.getCategoryName().toLowerCase(Locale.ROOT) : "";
                String note = t.getNote() != null ? t.getNote().toLowerCase(Locale.ROOT) : "";
                matchSearch = title.contains(searchQuery) || category.contains(searchQuery) || note.contains(searchQuery);
            }

            if (!matchSearch) continue;

            filteredTransactionList.add(t);

            // Accumulate Chi/Thu
            if (Constants.TYPE_INCOME.equalsIgnoreCase(t.getTransactionType())) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
            }
        }

        // Setup overview totals
        binding.tvSummaryExpense.setText(CurrencyFormatter.formatVND(totalExpense));
        binding.tvSummaryIncome.setText(CurrencyFormatter.formatVND(totalIncome));

        // Setup empty state
        if (filteredTransactionList.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvTransactions.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.rvTransactions.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataChanged();
    }

    private void showAddOptionsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_add_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        View btnManual = bottomSheetView.findViewById(R.id.btnOptionManual);
        View btnOcr = bottomSheetView.findViewById(R.id.btnOptionOcr);
        View btnYolo = bottomSheetView.findViewById(R.id.btnOptionYolo);

        if (btnManual != null) {
            btnManual.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                AddTransactionFragment addFragment = new AddTransactionFragment();
                addFragment.show(getParentFragmentManager(), "AddTransactionFragment");
            });
        }

        if (btnOcr != null) {
            btnOcr.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(requireContext(), com.example.personalfinance.activities.ScanBillActivity.class));
            });
        }

        if (btnYolo != null) {
            btnYolo.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(requireContext(), com.example.personalfinance.activities.ScanProductActivity.class));
            });
        }

        bottomSheetDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
