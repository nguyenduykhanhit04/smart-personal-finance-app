package com.example.personalfinance.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.ItemCalendarDayBinding;
import com.example.personalfinance.models.CalendarDay;
import com.example.personalfinance.models.Transaction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.CalendarViewHolder> {

    private static final android.util.LruCache<String, android.graphics.Bitmap> imageCache = new android.util.LruCache<>(30);

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

    private void loadImage(String relativeUrl, com.google.android.material.imageview.ShapeableImageView imageView) {
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

    private final Context context;
    private final List<CalendarDay> calendarDays;
    private OnDayClickListener listener;
    private int selectedPosition = -1;
    private int displayYear;
    private int displayMonth;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day, boolean hasTransaction, int position);
    }

    public CalendarGridAdapter(Context context, List<CalendarDay> calendarDays) {
        this.context = context;
        this.calendarDays = calendarDays;
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setDisplayedMonth(int year, int month) {
        displayYear = year;
        displayMonth = month;
    }

    public void setSelectedPosition(int position) {
        int oldPos = selectedPosition;
        selectedPosition = position;
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (selectedPosition >= 0) notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void clearSelection() {
        int oldPos = selectedPosition;
        selectedPosition = -1;
        if (oldPos >= 0) notifyItemChanged(oldPos);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CalendarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = calendarDays.get(position);
        holder.bind(day, position);
    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        public CalendarViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CalendarDay day, int position) {
            if (day.isPlaceholder()) {
                // Invisible cell for grid alignment
                itemView.setVisibility(View.INVISIBLE);
                itemView.setClickable(false);
                itemView.setEnabled(false);
                return;
            }

            itemView.setVisibility(View.VISIBLE);
            itemView.setClickable(true);
            itemView.setEnabled(true);
            itemView.setAlpha(1f);
            binding.tvDayNumber.setText(String.valueOf(day.getDayNumber()));

            boolean isSelected = (position == selectedPosition);
            List<Transaction> transactions = day.getTransactions();
            boolean isFutureDay = isFutureDay(day.getDayNumber());

            if (isFutureDay) {
                binding.viewDashedBorder.setVisibility(View.GONE);
                binding.imgPlus.setVisibility(View.GONE);
                binding.imgThumbnail.setVisibility(View.GONE);
                binding.circleContainer.setBackgroundResource(R.drawable.bg_circle);
                binding.circleContainer.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.surface_variant)));
                binding.tvDayNumber.setTextColor(context.getColor(R.color.text_hint));
                itemView.setAlpha(0.55f);
                itemView.setEnabled(false);
                itemView.setClickable(false);
                itemView.setOnClickListener(null);
                return;
            }

            binding.circleContainer.setBackgroundTintList(null);

            if (transactions == null || transactions.isEmpty()) {
                // Empty day - show dashed border and plus sign
                binding.imgThumbnail.setVisibility(View.GONE);

                if (isSelected) {
                    binding.viewDashedBorder.setVisibility(View.GONE);
                    binding.imgPlus.setVisibility(View.VISIBLE);
                    binding.imgPlus.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    binding.circleContainer.setBackgroundResource(R.drawable.bg_circle_selected);
                    binding.tvDayNumber.setTextColor(Color.parseColor("#3B82F6")); // Blue highlight text
                } else {
                    binding.viewDashedBorder.setVisibility(View.VISIBLE);
                    binding.imgPlus.setVisibility(View.VISIBLE);
                    binding.imgPlus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.text_hint)));
                    binding.circleContainer.setBackground(null);
                    binding.tvDayNumber.setTextColor(context.getColor(R.color.text_secondary));
                }
            } else {
                // Has transactions
                binding.viewDashedBorder.setVisibility(View.GONE);
                binding.imgPlus.setVisibility(View.GONE);

                // Filter transactions that have valid images
                List<Transaction> imageTxs = new ArrayList<>();
                for (Transaction tx : transactions) {
                    if (tx.getImageUrl() != null && !tx.getImageUrl().trim().isEmpty()) {
                        imageTxs.add(tx);
                    }
                }

                if (imageTxs.isEmpty()) {
                    // Fallback to Category Icon
                    binding.imgThumbnail.setVisibility(View.VISIBLE);
                    binding.imgThumbnailBack.setVisibility(View.GONE);
                    binding.imgThumbnailFront.setVisibility(View.GONE);

                    // Shape is Circle
                    binding.imgThumbnail.setShapeAppearanceModel(
                        binding.imgThumbnail.getShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(new com.google.android.material.shape.RelativeCornerSize(0.5f))
                            .build()
                    );

                    Transaction firstTx = transactions.get(0);
                    if (firstTx.getCategoryName() != null
                            && firstTx.getCategoryName().toLowerCase(Locale.ROOT).contains("ăn uống")) {
                        binding.imgThumbnail.setImageResource(R.drawable.ic_transaction);
                        binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.expense_red));
                    } else if (firstTx.getCategoryName() != null
                            && (firstTx.getCategoryName().toLowerCase(Locale.ROOT).contains("sức khỏe")
                            || firstTx.getCategoryName().toLowerCase(Locale.ROOT).contains("y tế"))) {
                        binding.imgThumbnail.setImageResource(R.drawable.ic_transaction);
                        binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.expense_red));
                    } else {
                        binding.imgThumbnail.setImageResource(R.drawable.ic_budget);
                        binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.primary));
                    }
                } else if (imageTxs.size() == 1) {
                    // Single Image Thumbnail with Rounded Rect Shape
                    binding.imgThumbnail.setVisibility(View.VISIBLE);
                    binding.imgThumbnailBack.setVisibility(View.GONE);
                    binding.imgThumbnailFront.setVisibility(View.GONE);

                    // Set background to transparent first so we don't see the fallback background color
                    binding.imgThumbnail.setBackgroundColor(Color.TRANSPARENT);

                    // Shape is Rounded Rect
                    float r = android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics()
                    );
                    binding.imgThumbnail.setShapeAppearanceModel(
                        binding.imgThumbnail.getShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(r)
                            .build()
                    );

                    loadImage(imageTxs.get(0).getImageUrl(), binding.imgThumbnail);
                } else {
                    // Stacked Overlapping Image Thumbnails
                    binding.imgThumbnail.setVisibility(View.GONE);
                    binding.imgThumbnailBack.setVisibility(View.VISIBLE);
                    binding.imgThumbnailFront.setVisibility(View.VISIBLE);

                    loadImage(imageTxs.get(0).getImageUrl(), binding.imgThumbnailBack);
                    loadImage(imageTxs.get(1).getImageUrl(), binding.imgThumbnailFront);
                }

                if (isSelected) {
                    binding.circleContainer.setBackgroundResource(R.drawable.bg_circle_selected_ring);
                    binding.tvDayNumber.setTextColor(Color.parseColor("#3B82F6")); // Blue highlight text
                } else {
                    binding.circleContainer.setBackground(null);
                    binding.tvDayNumber.setTextColor(context.getColor(R.color.text_secondary));
                }
            }

            itemView.setOnClickListener(v -> {
                setSelectedPosition(position);
                if (listener != null) {
                    listener.onDayClick(day, transactions != null && !transactions.isEmpty(), position);
                }
            });
        }

        private boolean isFutureDay(int dayNumber) {
            Calendar cellDate = Calendar.getInstance();
            cellDate.set(displayYear, displayMonth, dayNumber, 0, 0, 0);
            cellDate.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return cellDate.after(today);
        }
    }
}
