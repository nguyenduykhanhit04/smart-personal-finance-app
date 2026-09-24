package com.example.personalfinance.fragments.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.FragmentAddAccountBinding;
import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.AccountViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddAccountFragment extends BottomSheetDialogFragment {

    private FragmentAddAccountBinding binding;
    private AccountViewModel viewModel;
    private User currentUser;
    private OnAccountSavedListener savedListener;

    private final String[] typeDisplayNames = {"Tiền mặt", "Ngân hàng", "Ví điện tử", "Thẻ tín dụng"};
    private final String[] typeServerKeys = {"CASH", "BANK", "EWALLET", "CREDIT"};

    public interface OnAccountSavedListener {
        void onAccountSaved();
    }

    public void setOnAccountSavedListener(OnAccountSavedListener listener) {
        this.savedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        // Set up Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_spinner_item, typeDisplayNames);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        binding.spAccountType.setAdapter(adapter);

        // Cancel
        binding.btnCancel.setOnClickListener(v -> dismiss());

        // Save
        binding.btnSave.setOnClickListener(v -> saveAccount());

        // Register Observers
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getAccountCreated().observe(getViewLifecycleOwner(), account -> {
            if (account != null) {
                binding.btnSave.setEnabled(true);
                Toast.makeText(requireContext(), "Tạo ví thành công!", Toast.LENGTH_SHORT).show();
                if (savedListener != null) {
                    savedListener.onAccountSaved();
                }
                dismiss();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.btnSave.setEnabled(true);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAccount() {
        String name = binding.edtAccountName.getText().toString().trim();
        String balanceText = binding.edtBalance.getText().toString().trim();

        if (name.isEmpty()) {
            binding.edtAccountName.setError("Vui lòng nhập tên ví");
            binding.edtAccountName.requestFocus();
            return;
        }

        if (balanceText.isEmpty()) {
            binding.edtBalance.setError("Vui lòng nhập số dư ban đầu");
            binding.edtBalance.requestFocus();
            return;
        }

        double balance;
        try {
            balance = Double.parseDouble(balanceText);
        } catch (NumberFormatException e) {
            binding.edtBalance.setError("Số dư không hợp lệ");
            binding.edtBalance.requestFocus();
            return;
        }

        int typeIdx = binding.spAccountType.getSelectedItemPosition();
        if (typeIdx < 0) return;

        String serverType = typeServerKeys[typeIdx];

        binding.btnSave.setEnabled(false);

        Account account = new Account();
        account.setUserId(currentUser.getUserId());
        account.setAccountName(name);
        account.setAccountType(serverType);
        account.setBalance(balance);
        account.setCurrency("VND");

        viewModel.createAccount(account);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
