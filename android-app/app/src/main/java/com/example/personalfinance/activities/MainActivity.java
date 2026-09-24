package com.example.personalfinance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.ActivityMainBinding;
import com.example.personalfinance.fragments.home.HomeFragment;
import com.example.personalfinance.fragments.transaction.TransactionFragment;
import com.example.personalfinance.fragments.transaction.AddTransactionFragment;
import com.example.personalfinance.fragments.transaction.TransactionListFragment;
import com.example.personalfinance.fragments.category.CategoryLimitFragment;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.SharedPrefManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = SharedPrefManager.getInstance(this).getUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Set default fragment
        loadFragment(new HomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_transactions) {
                loadFragment(new TransactionListFragment());
                return true;
            } else if (itemId == R.id.nav_scan) {
                showAddOptionsBottomSheet();
                return false; // keep previous highlight selected
            } else if (itemId == R.id.nav_budget) {
                loadFragment(new CategoryLimitFragment());
                return true;
            } else if (itemId == R.id.nav_statistics) {
                loadFragment(new TransactionFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void setSelectedTab(int itemId) {
        binding.bottomNavigation.setSelectedItemId(itemId);
    }

    private void showAddOptionsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_options);

        LinearLayout btnManual = bottomSheetDialog.findViewById(R.id.btnOptionManual);
        LinearLayout btnOcr = bottomSheetDialog.findViewById(R.id.btnOptionOcr);
        LinearLayout btnYolo = bottomSheetDialog.findViewById(R.id.btnOptionYolo);

        if (btnManual != null) {
            btnManual.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                // Open add transaction bottom sheet dialog
                AddTransactionFragment addFragment = new AddTransactionFragment();
                addFragment.show(getSupportFragmentManager(), "AddTransactionFragment");
            });
        }

        if (btnOcr != null) {
            btnOcr.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(MainActivity.this, ScanBillActivity.class));
            });
        }

        if (btnYolo != null) {
            btnYolo.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(MainActivity.this, ScanProductActivity.class));
            });
        }

        bottomSheetDialog.show();
    }
}
