package com.example.personalfinance.fragments.transaction;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinance.R;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DayTransactionsBottomSheet extends BottomSheetDialogFragment {

    private String dayTitle = "";
    private List<Transaction> transactions = new ArrayList<>();
    private double totalAmount = 0;

    public static DayTransactionsBottomSheet newInstance(String dayTitle, List<Transaction> transactions, double totalAmount) {
        DayTransactionsBottomSheet fragment = new DayTransactionsBottomSheet();
        fragment.dayTitle = dayTitle;
        fragment.transactions = transactions != null ? transactions : new ArrayList<>();
        fragment.totalAmount = totalAmount;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_day_transactions, container, false);
    }

    private static final android.util.LruCache<String, android.graphics.Bitmap> imageCache = new android.util.LruCache<>(20);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvDayTitle = view.findViewById(R.id.tvDayTitle);
        TextView tvTransactionCount = view.findViewById(R.id.tvTransactionCount);
        TextView tvDayIncome = view.findViewById(R.id.tvDayIncome);
        TextView tvDayExpense = view.findViewById(R.id.tvDayExpense);
        ImageView btnClose = view.findViewById(R.id.btnClose);
        RecyclerView rvDayTransactions = view.findViewById(R.id.rvDayTransactions);

        tvDayTitle.setText(dayTitle);

        // Filter transactions that have valid images
        List<Transaction> photoTxs = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getImageUrl() != null && !t.getImageUrl().trim().isEmpty()) {
                photoTxs.add(t);
            }
        }

        tvTransactionCount.setText(getString(R.string.format_fraction, photoTxs.size(), photoTxs.size()));

        // Calculate dynamic income and expense totals for the day
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction t : transactions) {
            double amt = t.getAmount();
            boolean isExpense = "EXPENSE".equalsIgnoreCase(t.getTransactionType()) || amt < 0;
            if (isExpense) {
                totalExpense += Math.abs(amt);
            } else {
                totalIncome += Math.abs(amt);
            }
        }

        if (totalIncome > 0) {
            tvDayIncome.setText(getString(R.string.format_income_amount, CurrencyFormatter.formatVND(totalIncome)));
            tvDayIncome.setVisibility(View.VISIBLE);
        } else {
            tvDayIncome.setVisibility(View.GONE);
        }

        if (totalExpense > 0) {
            tvDayExpense.setText(getString(R.string.format_expense_amount, CurrencyFormatter.formatVND(totalExpense)));
            tvDayExpense.setVisibility(View.VISIBLE);
        } else {
            tvDayExpense.setVisibility(View.GONE);
        }

        // Fallback if both are 0
        if (totalIncome == 0 && totalExpense == 0) {
            tvDayExpense.setText(getString(R.string.format_expense_amount, CurrencyFormatter.formatVND(0)));
            tvDayExpense.setVisibility(View.VISIBLE);
        }

        btnClose.setOnClickListener(v -> dismiss());

        // Horizontal LinearLayoutManager for cards scroll matching Screen 6
        rvDayTransactions.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvDayTransactions.setAdapter(new DayTransactionsAdapter(requireContext(), photoTxs));
    }

    private static class DayTransactionsAdapter extends RecyclerView.Adapter<DayTransactionsAdapter.ViewHolder> {

        private final Context context;
        private final List<Transaction> list;

        public DayTransactionsAdapter(Context context, List<Transaction> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_day_transaction_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction tx = list.get(position);
            holder.tvCardCategory.setText(tx.getCategoryName() != null ? tx.getCategoryName() : context.getString(R.string.ui_an_uong));

            double amount = tx.getAmount();
            if ("EXPENSE".equalsIgnoreCase(tx.getTransactionType()) || amount < 0) {
                holder.tvCardAmount.setText(context.getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(Math.abs(amount))));
            } else {
                holder.tvCardAmount.setText(context.getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(amount)));
            }

            // Load transaction image dynamically with cache
            loadImage(tx.getImageUrl(), holder.ivCardBackground);

            // Card click launches TransactionPhotoDetailDialog matching Screen 7
            holder.itemView.setOnClickListener(v -> {
                TransactionPhotoDetailDialog dialog = TransactionPhotoDetailDialog.newInstance(tx, position, list.size());
                dialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "TransactionPhotoDetailDialog");
            });
        }

        private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, byte[] bytes) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ExifInterface exif = new ExifInterface(bis);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int degrees = 0;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degrees = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degrees = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degrees = 270;
                        break;
                }
                if (degrees != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(degrees);
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
            } catch (Exception ignored) {}
            return bitmap;
        }

        private void loadImage(String relativeUrl, ImageView imageView) {
            if (relativeUrl == null || relativeUrl.trim().isEmpty()) return;

            String baseUrl = com.example.personalfinance.api.RetrofitClient.getClient().baseUrl().toString();
            String path = relativeUrl.startsWith("/") ? relativeUrl.substring(1) : relativeUrl;
            String fullUrl = baseUrl + path;

            Bitmap cached = imageCache.get(fullUrl);
            if (cached != null) {
                imageView.setImageBitmap(cached);
                return;
            }

            imageView.setTag(fullUrl);
            new Thread(() -> {
                try {
                    URL url = new URL(fullUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setRequestProperty("ngrok-skip-browser-warning", "true");
                    conn.setRequestProperty("User-Agent", "Android-App");
                    conn.connect();

                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }
                        byte[] bytes = bos.toByteArray();

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bitmap != null) {
                            bitmap = rotateBitmapIfNeeded(bitmap, bytes);
                            imageCache.put(fullUrl, bitmap);
                            final Bitmap finalBitmap = bitmap;
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                if (fullUrl.equals(imageView.getTag())) {
                                    imageView.setImageBitmap(finalBitmap);
                                }
                            });
                        }
                    }
                } catch (Exception ignored) {}
            }).start();
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCardBackground;
            TextView tvCardCategory;
            TextView tvCardAmount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCardBackground = itemView.findViewById(R.id.ivCardBackground);
                tvCardCategory = itemView.findViewById(R.id.tvCardCategory);
                tvCardAmount = itemView.findViewById(R.id.tvCardAmount);
            }
        }
    }
}
