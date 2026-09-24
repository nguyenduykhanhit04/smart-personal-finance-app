package com.example.personalfinance.yolo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class YoloDetector {

    private final Interpreter interpreter;
    private boolean closed;

    private static final String[] CLASS_NAMES = {
        "bottled_water", "bread", "clothes", "coffee_cup", "cosmetic",
        "electronic_item", "fastfood", "helmet", "medicine", "milk_tea",
        "motorbike", "noodle", "rice_meal", "shoes", "snack",
        "soft_drink", "taxi_car", "toy_game"
    };

    private static final String[] LABELS_VIETNAMESE = {
        "Nước đóng chai", "Bánh mì", "Quần áo", "Cà phê", "Mỹ phẩm",
        "Thiết bị điện tử", "Thức ăn nhanh", "Mũ bảo hiểm", "Thuốc y tế", "Trà sữa",
        "Xe máy", "Mì / Phở", "Cơm hộp / Suất ăn", "Giày dép", "Đồ ăn vặt",
        "Nước ngọt", "Taxi / Ô tô", "Đồ chơi / Game"
    };

    public static class YoloDetection {
        public final int classId;
        public final String className;
        public final String labelVietnamese;
        public final float confidence;
        public final RectF rect;

        public YoloDetection(int classId, String className, String labelVietnamese, float confidence, RectF rect) {
            this.classId = classId;
            this.className = className;
            this.labelVietnamese = labelVietnamese;
            this.confidence = confidence;
            this.rect = rect;
        }
    }

    public YoloDetector(Context context, String modelPath) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4); // Use 4 threads for mobile CPU inference
        interpreter = new Interpreter(loadModelFile(context, modelPath), options);
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public synchronized List<YoloDetection> detect(Bitmap bitmap) {
        List<YoloDetection> detections = new ArrayList<>();
        if (closed) return detections;

        // Resize bitmap to 640x640
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);

        // Preprocess image to ByteBuffer
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * 640 * 640 * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        inputBuffer.rewind();

        int[] intValues = new int[640 * 640];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        for (int pixelValue : intValues) {
            float r = ((pixelValue >> 16) & 0xFF) / 255.0f;
            float g = ((pixelValue >> 8) & 0xFF) / 255.0f;
            float b = (pixelValue & 0xFF) / 255.0f;
            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        // Output array structure for end-to-end YOLOv8 TFLite model: shape [1][300][6]
        float[][][] output = new float[1][300][6];

        // Run inference
        interpreter.run(inputBuffer, output);

        // YOLOv8 TFLite with builtin postprocessing returns up to 300 boxes
        // output[0][box_index][4] is the confidence score
        // output[0][box_index][5] is the class index
        for (int i = 0; i < 300; i++) {
            float confidence = output[0][i][4];
            if (confidence >= 0.30f) {
                int classId = (int) output[0][i][5];
                if (classId >= 0 && classId < CLASS_NAMES.length) {
                    float x1 = output[0][i][0];
                    float y1 = output[0][i][1];
                    float x2 = output[0][i][2];
                    float y2 = output[0][i][3];

                    // Constrain coordinates to [0, 1]
                    x1 = Math.max(0.0f, Math.min(1.0f, x1));
                    y1 = Math.max(0.0f, Math.min(1.0f, y1));
                    x2 = Math.max(0.0f, Math.min(1.0f, x2));
                    y2 = Math.max(0.0f, Math.min(1.0f, y2));

                    detections.add(new YoloDetection(
                        classId,
                        CLASS_NAMES[classId],
                        LABELS_VIETNAMESE[classId],
                        confidence,
                        new RectF(x1, y1, x2, y2)
                    ));
                }
            }
        }

        // Recycle the scaled bitmap to avoid memory leak
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle();
        }

        return detections;
    }

    public synchronized void close() {
        if (!closed) {
            closed = true;
            interpreter.close();
        }
    }
}
