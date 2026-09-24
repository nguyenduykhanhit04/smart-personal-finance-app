package com.example.personalfinance.yolo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BoundingBoxOverlay extends View {

    public static class Box {
        public final RectF rect;
        public final String label;
        public final float confidence;

        public Box(RectF rect, String label, float confidence) {
            this.rect = rect;
            this.label = label;
            this.confidence = confidence;
        }
    }

    private final List<Box> boxes = new ArrayList<>();
    private final Paint boxPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint textBackgroundPaint = new Paint();

    private boolean isFitCenter = false;
    private int frameWidth = 1;
    private int frameHeight = 1;

    public void setIsFitCenter(boolean isFitCenter) {
        this.isFitCenter = isFitCenter;
        postInvalidate();
    }

    public BoundingBoxOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Style for the bounding box
        boxPaint.setColor(Color.parseColor("#3B82F6")); // Royal blue
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6.0f);
        boxPaint.setAntiAlias(true);

        // Style for text
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36.0f);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);

        // Style for text background box
        textBackgroundPaint.setColor(Color.parseColor("#E63B82F6")); // opaque royal blue
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setAntiAlias(true);
    }

    public void setFrameSize(int width, int height) {
        this.frameWidth = width;
        this.frameHeight = height;
    }

    public void setBoxes(List<Box> newBoxes) {
        synchronized (boxes) {
            boxes.clear();
            if (newBoxes != null) {
                boxes.addAll(newBoxes);
            }
        }
        postInvalidate(); // Redraw on UI thread
    }

    public void clear() {
        synchronized (boxes) {
            boxes.clear();
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (boxes) {
            if (frameWidth <= 0 || frameHeight <= 0) return;

            float scaleX = (float) getWidth() / frameWidth;
            float scaleY = (float) getHeight() / frameHeight;

            float scale;
            float offsetX;
            float offsetY;
            float scaledW;
            float scaledH;

            if (isFitCenter) {
                scale = Math.min(scaleX, scaleY);
                scaledW = frameWidth * scale;
                scaledH = frameHeight * scale;
                offsetX = (getWidth() - scaledW) / 2.0f;
                offsetY = (getHeight() - scaledH) / 2.0f;
            } else {
                scale = Math.max(scaleX, scaleY);
                scaledW = frameWidth * scale;
                scaledH = frameHeight * scale;
                offsetX = (scaledW - getWidth()) / 2.0f;
                offsetY = (scaledH - getHeight()) / 2.0f;
            }

            for (Box box : boxes) {
                float left, right, top, bottom;
                if (isFitCenter) {
                    left = box.rect.left * scaledW + offsetX;
                    right = box.rect.right * scaledW + offsetX;
                    top = box.rect.top * scaledH + offsetY;
                    bottom = box.rect.bottom * scaledH + offsetY;
                } else {
                    left = box.rect.left * scaledW - offsetX;
                    right = box.rect.right * scaledW - offsetX;
                    top = box.rect.top * scaledH - offsetY;
                    bottom = box.rect.bottom * scaledH - offsetY;
                }

                // Draw bounding box
                canvas.drawRect(left, top, right, bottom, boxPaint);

                // Draw label text background
                String text = box.label + " " + (int)(box.confidence * 100) + "%";
                float textWidth = textPaint.measureText(text);
                float textHeight = textPaint.getTextSize();

                // Draw label box at the top left of bounding box
                canvas.drawRect(
                    left,
                    top - textHeight - 15,
                    left + textWidth + 20,
                    top,
                    textBackgroundPaint
                );

                // Draw label text
                canvas.drawText(text, left + 10, top - 10, textPaint);
            }
        }
    }
}
