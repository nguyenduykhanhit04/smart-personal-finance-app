package com.example.personalfinance.fragments.account;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.personalfinance.R;
import com.example.personalfinance.adapters.TransactionAdapter;
import com.example.personalfinance.databinding.FragmentAccountDetailsBinding;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.TransactionViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AccountDetailsFragment extends Fragment {

    private static final String ARG_ACCOUNT_ID = "account_id";
    private static final String ARG_ACCOUNT_NAME = "account_name";
    private static final String ARG_ACCOUNT_BALANCE = "account_balance";

    private FragmentAccountDetailsBinding binding;
    private TransactionViewModel viewModel;
    private int accountId;
    private String accountName;
    private double accountBalance;
    private User currentUser;

    private TransactionAdapter adapter;
    private final List<Transaction> fullTransactionList = new ArrayList<>();
    private final List<Transaction> filteredTransactionList = new ArrayList<>();
    private String selectedTimeTab = "ALL"; // "MONTH", "YEAR", "ALL"

    public static AccountDetailsFragment newInstance(int accountId, String accountName, double balance) {
        AccountDetailsFragment fragment = new AccountDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        args.putString(ARG_ACCOUNT_NAME, accountName);
        args.putDouble(ARG_ACCOUNT_BALANCE, balance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accountId = getArguments().getInt(ARG_ACCOUNT_ID);
            accountName = getArguments().getString(ARG_ACCOUNT_NAME);
            if ("Ví chính".equalsIgnoreCase(accountName)) {
                accountName = "Wallet";
            }
            accountBalance = getArguments().getDouble(ARG_ACCOUNT_BALANCE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupUI();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        loadTransactions();
    }

    private void setupUI() {
        binding.tvWalletTitle.setText(accountName);
        
        // Initial setup for balance
        binding.tvWalletBalance.setText(CurrencyFormatter.formatVND(accountBalance));
        if (accountBalance < 0) {
            binding.tvWalletBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
        } else {
            binding.tvWalletBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green));
        }
        
        // Default time tab visually selected
        selectTabVisuals(selectedTimeTab);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(requireContext(), filteredTransactionList);
        binding.rvWalletTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWalletTransactions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnEdit.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Chức năng chỉnh sửa tài khoản đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        binding.tvTabMonth.setOnClickListener(v -> {
            selectedTimeTab = "MONTH";
            selectTabVisuals(selectedTimeTab);
            filterAndDisplayTransactions();
        });

        binding.tvTabYear.setOnClickListener(v -> {
            selectedTimeTab = "YEAR";
            selectTabVisuals(selectedTimeTab);
            filterAndDisplayTransactions();
        });

        binding.tvTabAll.setOnClickListener(v -> {
            selectedTimeTab = "ALL";
            selectTabVisuals(selectedTimeTab);
            filterAndDisplayTransactions();
        });
    }

    private void selectTabVisuals(String tab) {
        // Clear all visuals
        binding.tvTabMonth.setBackground(null);
        binding.tvTabMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.tvTabYear.setBackground(null);
        binding.tvTabYear.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.tvTabAll.setBackground(null);
        binding.tvTabAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        // Highlight selected
        TextView selectedView = null;
        if ("MONTH".equals(tab)) {
            selectedView = binding.tvTabMonth;
        } else if ("YEAR".equals(tab)) {
            selectedView = binding.tvTabYear;
        } else if ("ALL".equals(tab)) {
            selectedView = binding.tvTabAll;
        }

        if (selectedView != null) {
            selectedView.setBackgroundResource(R.drawable.bg_button_rounded);
            selectedView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E2E33")));
            selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private void loadTransactions() {
        viewModel.loadTransactions(currentUser.getUserId(), "2000-01-01", "2100-12-31");
    }

    private void observeViewModel() {
        viewModel.getTransactions().observe(getViewLifecycleOwner(), txs -> {
            fullTransactionList.clear();
            if (txs != null) {
                for (Transaction t : txs) {
                    if (t.getAccountId() != null && t.getAccountId() == accountId) {
                        fullTransactionList.add(t);
                    }
                }
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

        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH) + 1; // 1-indexed
        int currentYear = now.get(Calendar.YEAR);

        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : fullTransactionList) {
            boolean keep = false;
            
            if ("ALL".equals(selectedTimeTab)) {
                keep = true;
            } else {
                try {
                    String dateStr = t.getTransactionDate();
                    if (dateStr != null) {
                        java.util.Date date = DateUtils.parseApiDate(dateStr);
                        if (date != null) {
                            Calendar txCal = Calendar.getInstance();
                            txCal.setTime(date);
                            int txMonth = txCal.get(Calendar.MONTH) + 1;
                            int txYear = txCal.get(Calendar.YEAR);

                            if ("MONTH".equals(selectedTimeTab)) {
                                keep = (txMonth == currentMonth && txYear == currentYear);
                            } else if ("YEAR".equals(selectedTimeTab)) {
                                keep = (txYear == currentYear);
                            }
                        }
                    }
                } catch (Exception e) {
                    keep = "ALL".equals(selectedTimeTab); // fallback
                }
            }

            if (keep) {
                filteredTransactionList.add(t);
                if ("EXPENSE".equalsIgnoreCase(t.getTransactionType())) {
                    totalExpense += t.getAmount();
                } else {
                    totalIncome += t.getAmount();
                }
            }
        }

        // Set list and refresh RecyclerView
        adapter.notifyDataChanged();

        // Calculate dynamic sub-balance, transaction count, splits
        double subBalance = totalIncome - totalExpense;
        binding.tvWalletBalance.setText(CurrencyFormatter.formatVND(subBalance));
        if (subBalance < 0) {
            binding.tvWalletBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
        } else {
            binding.tvWalletBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green));
        }

        binding.tvTxCount.setText(getString(R.string.format_transaction_count, filteredTransactionList.size()));
        binding.tvWalletIncome.setText(CurrencyFormatter.formatVND(totalIncome));
        binding.tvWalletExpense.setText(CurrencyFormatter.formatVND(totalExpense));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
