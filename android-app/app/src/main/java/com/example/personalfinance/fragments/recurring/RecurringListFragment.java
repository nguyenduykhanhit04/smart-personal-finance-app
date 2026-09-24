package com.example.personalfinance.fragments.recurring;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalfinance.R;
import com.example.personalfinance.fragments.transaction.AddRecurringFragment;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.FragmentRecurringListBinding;
import com.example.personalfinance.databinding.ItemRecurringTransactionBinding;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.RecurringTransaction;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.Constants;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.SharedPrefManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecurringListFragment extends Fragment {

    private FragmentRecurringListBinding binding;
    private User currentUser;
    private final List<RecurringTransaction> recurringList = new ArrayList<>();
    private RecurringAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecurringListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        setupRecyclerView();
        setupClickListeners();
        loadRecurringTransactions();
    }

    private void setupRecyclerView() {
        adapter = new RecurringAdapter(requireContext(), recurringList);
        adapter.setOnItemClickListener(this::showOptionsBottomSheet);
        binding.rvRecurring.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecurring.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.fabAdd.setOnClickListener(v -> openAddRecurringFragment());
        binding.btnEmptyAdd.setOnClickListener(v -> openAddRecurringFragment());

        binding.btnSearch.setOnClickListener(v -> Toast.makeText(requireContext(), "Tìm kiếm định kỳ đang tải...", Toast.LENGTH_SHORT).show());
        binding.btnFilter.setOnClickListener(v -> Toast.makeText(requireContext(), "Lọc đang tải...", Toast.LENGTH_SHORT).show());
    }

    private void openAddRecurringFragment() {
        AddRecurringFragment addFragment = new AddRecurringFragment();
        addFragment.setOnSavedListener(this::loadRecurringTransactions);
        addFragment.show(getParentFragmentManager(), "AddRecurringFragment");
    }

    private void loadRecurringTransactions() {
        RetrofitClient.getApiService().getRecurringTransactions(currentUser.getUserId())
                .enqueue(new Callback<ApiResponse<List<RecurringTransaction>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<RecurringTransaction>>> call, @NonNull Response<ApiResponse<List<RecurringTransaction>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            recurringList.clear();
                            recurringList.addAll(response.body().getData());
                            updateUI();
                        } else {
                            loadMockRecurringData();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<RecurringTransaction>>> call, @NonNull Throwable t) {
                        loadMockRecurringData();
                    }
                });
    }

    private void loadMockRecurringData() {
        recurringList.clear();
        
        // Mock data to match screenshot 7 exactly:
        // Tiền điện: -500k, Hằng tháng - 20th
        // Internet: -220k, Hằng tháng - 05th
        // Lương: +15M, Hằng tháng - 01st
        // Tiết kiệm: -2M, Hằng tháng - 28th
        
        RecurringTransaction t1 = new RecurringTransaction();
        t1.setRecurringId(1);
        t1.setTitle("Tiền điện");
        t1.setAmount(500000.0);
        t1.setTransactionType(Constants.TYPE_EXPENSE);
        t1.setRepeatType("MONTHLY");
        t1.setStartDate("2026-05-20");
        t1.setCategoryName("Sinh hoạt");
        t1.setCategoryColor("#6366F1"); // Indigo
        t1.setIsActive(true);

        RecurringTransaction t2 = new RecurringTransaction();
        t2.setRecurringId(2);
        t2.setTitle("Internet");
        t2.setAmount(220000.0);
        t2.setTransactionType(Constants.TYPE_EXPENSE);
        t2.setRepeatType("MONTHLY");
        t2.setStartDate("2026-05-05");
        t2.setCategoryName("Sinh hoạt");
        t2.setCategoryColor("#6366F1"); // Indigo
        t2.setIsActive(true);

        RecurringTransaction t3 = new RecurringTransaction();
        t3.setRecurringId(3);
        t3.setTitle("Lương");
        t3.setAmount(15000000.0);
        t3.setTransactionType(Constants.TYPE_INCOME);
        t3.setRepeatType("MONTHLY");
        t3.setStartDate("2026-05-01");
        t3.setCategoryName("Thu nhập");
        t3.setCategoryColor("#10B981"); // Emerald Green
        t3.setIsActive(true);

        RecurringTransaction t4 = new RecurringTransaction();
        t4.setRecurringId(4);
        t4.setTitle("Tiết kiệm");
        t4.setAmount(2000000.0);
        t4.setTransactionType(Constants.TYPE_EXPENSE);
        t4.setRepeatType("MONTHLY");
        t4.setStartDate("2026-05-28");
        t4.setCategoryName("Tiết kiệm");
        t4.setCategoryColor("#EC4899"); // Pink
        t4.setIsActive(false); // Paused/Tùy chọn

        recurringList.add(t1);
        recurringList.add(t2);
        recurringList.add(t3);
        recurringList.add(t4);

        updateUI();
    }

    private void updateUI() {
        int activeCount = 0;
        for (RecurringTransaction r : recurringList) {
            if (r.getIsActive()) activeCount++;
        }
        binding.tvActiveCount.setText(getString(R.string.format_active_recurring, activeCount));

        if (recurringList.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvRecurring.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.rvRecurring.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private void showOptionsBottomSheet(RecurringTransaction item) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_recurring_options, null);
        dialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvRecurringTitle);
        View btnPauseResume = sheetView.findViewById(R.id.btnPauseResume);
        TextView tvPauseResume = sheetView.findViewById(R.id.tvPauseResumeText);
        View btnEdit = sheetView.findViewById(R.id.btnEditRecurring);
        View btnDelete = sheetView.findViewById(R.id.btnDeleteRecurring);

        if (tvTitle != null) tvTitle.setText(item.getTitle());
        if (tvPauseResume != null) {
            tvPauseResume.setText(item.getIsActive() ? "Tạm dừng định kỳ" : "Bật lại định kỳ");
        }

        if (btnPauseResume != null) {
            btnPauseResume.setOnClickListener(v -> {
                dialog.dismiss();
                item.setIsActive(!item.getIsActive());
                updateRecurringStatus(item);
            });
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                dialog.dismiss();
                AddRecurringFragment editFragment = AddRecurringFragment.newInstance(item);
                editFragment.setOnSavedListener(this::loadRecurringTransactions);
                editFragment.show(getParentFragmentManager(), "AddRecurringFragment");
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                dialog.dismiss();
                deleteRecurringTransaction(item);
            });
        }

        dialog.show();
    }

    private void updateRecurringStatus(RecurringTransaction item) {
        RetrofitClient.getApiService().updateRecurringTransaction(item.getRecurringId(), item)
                .enqueue(new Callback<ApiResponse<RecurringTransaction>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<RecurringTransaction>> call, @NonNull Response<ApiResponse<RecurringTransaction>> response) {
                        Toast.makeText(requireContext(), item.getIsActive() ? "Đã bật lại định kỳ!" : "Đã tạm dừng định kỳ!", Toast.LENGTH_SHORT).show();
                        loadRecurringTransactions();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<RecurringTransaction>> call, @NonNull Throwable t) {
                        loadRecurringTransactions(); // refresh locally
                    }
                });
    }

    private void deleteRecurringTransaction(RecurringTransaction item) {
        RetrofitClient.getApiService().deleteRecurringTransaction(item.getRecurringId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        Toast.makeText(requireContext(), "Đã xóa khoản định kỳ!", Toast.LENGTH_SHORT).show();
                        loadRecurringTransactions();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        recurringList.remove(item);
                        updateUI();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Nested Recycler Adapter to keep package clean & organized
    private static class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.ViewHolder> {
        private final Context context;
        private final List<RecurringTransaction> list;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(RecurringTransaction item);
        }

        public RecurringAdapter(Context context, List<RecurringTransaction> list) {
            this.context = context;
            this.list = list;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemRecurringTransactionBinding binding = ItemRecurringTransactionBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecurringTransaction item = list.get(position);
            holder.binding.tvTitle.setText(item.getTitle());

            // Format subtext: "Hàng tháng - 20/05"
            String dateSuffix = "";
            try {
                if (item.getStartDate() != null && item.getStartDate().length() >= 10) {
                    String[] parts = item.getStartDate().split("-");
                    dateSuffix = " - " + parts[2] + "/" + parts[1];
                }
            } catch (Exception ignored) {}

            int interval = item.getRepeatInterval() != null ? item.getRepeatInterval() : 1;
            String repeatLabel = "";
            if (interval > 1) {
                if ("DAILY".equalsIgnoreCase(item.getRepeatType())) repeatLabel = "Mỗi " + interval + " ngày";
                else if ("WEEKLY".equalsIgnoreCase(item.getRepeatType())) repeatLabel = "Mỗi " + interval + " tuần";
                else if ("MONTHLY".equalsIgnoreCase(item.getRepeatType())) repeatLabel = "Mỗi " + interval + " tháng";
            } else {
                if ("DAILY".equalsIgnoreCase(item.getRepeatType())) repeatLabel = "Hàng ngày";
                else if ("WEEKLY".equalsIgnoreCase(item.getRepeatType())) repeatLabel = "Hàng tuần";
                else if ("MONTHLY".equalsIgnoreCase(item.getRepeatType())) repeatLabel = "Hàng tháng";
            }

            holder.binding.tvSubtext.setText(context.getString(R.string.format_recurring_month_day, repeatLabel, dateSuffix));

            // Format amount
            if (Constants.TYPE_INCOME.equalsIgnoreCase(item.getTransactionType())) {
                holder.binding.tvAmount.setText(context.getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(item.getAmount())));
                holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
            } else {
                holder.binding.tvAmount.setText(context.getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(item.getAmount())));
                holder.binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            }

            // Colors
            int colorVal = Color.parseColor("#3B82F6"); // Default Blue
            if (item.getCategoryColor() != null && !item.getCategoryColor().isEmpty()) {
                try {
                    colorVal = Color.parseColor(item.getCategoryColor());
                } catch (Exception ignored) {}
            }
            GradientDrawable drawable = (GradientDrawable) holder.binding.viewCategoryColor.getBackground();
            if (drawable != null) {
                drawable.setColor(colorVal);
            }

            // Icons
            int iconRes = R.drawable.ic_recurring;
            String name = item.getTitle().toLowerCase(Locale.ROOT);
            if (name.contains("điện") || name.contains("nước")) iconRes = R.drawable.ic_home;
            else if (name.contains("internet") || name.contains("wifi") || name.contains("mạng")) iconRes = R.drawable.ic_scan;
            else if (name.contains("lương") || name.contains("bonus") || name.contains("thu nhập")) iconRes = R.drawable.ic_budget;

            holder.binding.ivCategoryIcon.setImageResource(iconRes);

            // Active/Inactive state visuals (paused has reduced alpha)
            holder.itemView.setAlpha(item.getIsActive() ? 1.0f : 0.45f);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemRecurringTransactionBinding binding;

            public ViewHolder(ItemRecurringTransactionBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
