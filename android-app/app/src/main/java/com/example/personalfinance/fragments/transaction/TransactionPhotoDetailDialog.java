package com.example.personalfinance.fragments.transaction;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.DialogFragment;
import com.example.personalfinance.R;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TransactionPhotoDetailDialog extends DialogFragment {

    private static final LruCache<String, Bitmap> imageCache = new LruCache<>(20);

    private Transaction transaction;
    private int position = 0;
    private int totalCount = 1;

    public static TransactionPhotoDetailDialog newInstance(Transaction transaction, int position, int totalCount) {
        TransactionPhotoDetailDialog dialog = new TransactionPhotoDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable("transaction", transaction);
        args.putInt("position", position);
        args.putInt("totalCount", totalCount);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        if (getArguments() != null) {
            transaction = (Transaction) getArguments().getSerializable("transaction");
            position = getArguments().getInt("position", 0);
            totalCount = getArguments().getInt("totalCount", 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_transaction_photo_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivDetailPhoto = view.findViewById(R.id.ivDetailPhoto);
        TextView tvDetailDateTime = view.findViewById(R.id.tvDetailDateTime);
        TextView tvCategoryTag = view.findViewById(R.id.tvCategoryTag);
        TextView tvWalletTag = view.findViewById(R.id.tvWalletTag);
        TextView tvDetailAmount = view.findViewById(R.id.tvDetailAmount);
        TextView tvDetailNote = view.findViewById(R.id.tvDetailNote);
        TextView tvDetailIndex = view.findViewById(R.id.tvDetailIndex);
        ImageView btnDetailClose = view.findViewById(R.id.btnDetailClose);
        ImageView ivCategoryTagIcon = view.findViewById(R.id.ivCategoryTagIcon);

        // Close button action
        btnDetailClose.setOnClickListener(v -> dismiss());

        if (transaction == null) return;

        // Display category name and map tag icon
        String categoryName = transaction.getCategoryName() != null ? transaction.getCategoryName() : "Ăn uống";
        tvCategoryTag.setText(categoryName);
        String normalizedCategory = categoryName.toLowerCase(Locale.ROOT);
        if (normalizedCategory.contains("ăn uống") || normalizedCategory.contains("food")) {
            ivCategoryTagIcon.setImageResource(R.drawable.ic_transaction);
        } else {
            ivCategoryTagIcon.setImageResource(R.drawable.ic_credit_card);
        }

        // Display wallet name
        String walletName = transaction.getAccountName() != null ? transaction.getAccountName() : "Wallet";
        if ("Ví chính".equalsIgnoreCase(walletName)) {
            walletName = "Wallet";
        }
        tvWalletTag.setText(walletName);

        // Format and display amount with proper sign/spacing matching high fidelity (e.g. "- 87,000đ")
        double amount = transaction.getAmount();
        String sign = "EXPENSE".equalsIgnoreCase(transaction.getTransactionType()) || amount < 0 ? "- " : "+ ";
        String formattedAmount = sign + CurrencyFormatter.formatVND(Math.abs(amount)).replace("-", "").replace("+", "");
        tvDetailAmount.setText(formattedAmount);

        // Display title/note
        String noteText = transaction.getNote() != null && !transaction.getNote().isEmpty() ? transaction.getNote() : transaction.getTitle();
        tvDetailNote.setText(noteText);

        // Display index indicator (e.g. "12 / 14")
        tvDetailIndex.setText(getString(R.string.format_photo_index, position + 1, totalCount));

        // Format and display date/time matching Screen 7 (e.g. "lúc 18:38 ngày 3 tháng 4, 2026")
        String dateStr = transaction.getTransactionDate();
        String timeStr = "18:38"; // Mock default time
        try {
            if (dateStr.contains("T")) {
                String[] parts = dateStr.split("T");
                dateStr = parts[0];
                if (parts.length > 1 && parts[1].length() >= 5) {
                    timeStr = parts[1].substring(0, 5);
                }
            }
            Date date = DateUtils.parseApiDate(dateStr);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH) + 1;
                int year = cal.get(Calendar.YEAR);

                String displayDateTime = "lúc " + timeStr + " ngày " + day + " tháng " + month + ", " + year;
                tvDetailDateTime.setText(displayDateTime);
            }
        } catch (Exception ignored) {}

        // Load background transaction image
        loadImage(transaction.getImageUrl(), ivDetailPhoto);
    }

    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, byte[] bytes) {
        try {
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes);
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
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postRotate(degrees);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception ignored) {}
        return bitmap;
    }

    private void loadImage(String relativeUrl, ImageView imageView) {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) return;

        String baseUrl = RetrofitClient.getClient().baseUrl().toString();
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
                    java.io.InputStream is = conn.getInputStream();
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
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
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (fullUrl.equals(imageView.getTag())) {
                                imageView.setImageBitmap(finalBitmap);
                            }
                        });
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }
}
