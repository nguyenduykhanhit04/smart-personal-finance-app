package com.example.personalfinance.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.ItemHorizontalWalletBinding;
import com.example.personalfinance.models.Account;
import com.example.personalfinance.utils.CurrencyFormatter;
import java.util.List;
import java.util.Locale;

public class HorizontalAccountAdapter extends RecyclerView.Adapter<HorizontalAccountAdapter.ViewHolder> {

    private final Context context;
    private final List<Account> accounts;
    private OnAccountClickListener clickListener;

    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    public HorizontalAccountAdapter(Context context, List<Account> accounts) {
        this.context = context;
        this.accounts = accounts;
    }

    public void setOnAccountClickListener(OnAccountClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHorizontalWalletBinding binding = ItemHorizontalWalletBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.bind(account);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHorizontalWalletBinding binding;

        public ViewHolder(ItemHorizontalWalletBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Account account) {
            String name = account.getAccountName();
            if ("Ví chính".equalsIgnoreCase(name)) name = "Wallet";
            binding.tvAccountName.setText(name);
            binding.tvBalance.setText(CurrencyFormatter.formatVND(account.getBalance()));

            if (account.getBalance() < 0) {
                binding.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            } else {
                binding.tvBalance.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            }

            // Only show star for first account
            if (getAdapterPosition() == 0) {
                binding.txtStarBadge.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.txtStarBadge.setVisibility(android.view.View.GONE);
            }

            // Setup colors and icons based on Account Type
            String type = account.getAccountType() != null
                    ? account.getAccountType().toUpperCase(Locale.ROOT)
                    : "CASH";
            GradientDrawable background = (GradientDrawable) binding.viewTypeColor.getBackground();

            switch (type) {
                case "BANK":
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_transaction);
                    if (background != null) {
                        background.setColor(ContextCompat.getColor(context, R.color.primary));
                    }
                    break;
                case "EWALLET":
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_transaction);
                    if (background != null) {
                        background.setColor(ContextCompat.getColor(context, R.color.status_active));
                    }
                    break;
                case "CREDIT":
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_credit_card);
                    if (background != null) {
                        background.setColor(ContextCompat.getColor(context, R.color.expense_red));
                    }
                    break;
                case "CASH":
                default:
                    binding.imgAccountIcon.setImageResource(R.drawable.ic_home);
                    if (background != null) {
                        background.setColor(ContextCompat.getColor(context, R.color.income_green));
                    }
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onAccountClick(account);
                }
            });
        }
    }
}
