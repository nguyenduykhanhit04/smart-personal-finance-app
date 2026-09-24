package com.example.personalfinance.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.ActivityScanProductBinding;
import com.example.personalfinance.fragments.transaction.AddTransactionFragment;
import com.example.personalfinance.models.AiProductResult;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.ProductClassificationRequest;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.yolo.BoundingBoxOverlay;
import com.example.personalfinance.yolo.YoloDetector;
import com.google.common.util.concurrent.ListenableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanProductActivity extends AppCompatActivity {

    private static final String YOLO_LOG_TAG = "YOLO_CAPTURE";
    private static final int CAMERA_PERMISSION_CODE = 1002;
    private static final int GALLERY_PICK_CODE = 1003;

    private ActivityScanProductBinding binding;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private ExecutorService cameraExecutor;
    private User currentUser;
    private YoloDetector yoloDetector;

    private String detectedProductName = "";
    private double detectedConfidence = 0.0;
    private volatile boolean isRealtimeAnalyzing = false;
    private volatile boolean isCapturing = false;
    private long lastAnalysisTime = 0;
    private List<YoloDetector.YoloDetection> latestDetections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = SharedPrefManager.getInstance(this).getUser();
        if (currentUser == null) {
            finish();
            return;
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        try {
            yoloDetector = new YoloDetector(this, "yolo_product.tflite");
        } catch (IOException e) {
            Toast.makeText(this, "Loi tai mo hinh YOLO: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCapture.setOnClickListener(v -> captureProduct());
        binding.btnGallery.setOnClickListener(v -> openGallery());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Loi khoi dong camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Use same 16:9 aspect ratio for all use cases to ensure
        // captured image matches what user sees in preview
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            long now = System.currentTimeMillis();
            if (isCapturing || isRealtimeAnalyzing || yoloDetector == null
                    || (now - lastAnalysisTime) < 250) {
                imageProxy.close();
                return;
            }
            lastAnalysisTime = now;
            isRealtimeAnalyzing = true;

            Bitmap bitmap = yuvToBitmap(imageProxy);
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            imageProxy.close();

            if (bitmap != null) {
                Bitmap rotatedBitmap = rotateBitmap(bitmap, rotationDegrees);
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle();
                }
                List<YoloDetector.YoloDetection> detections = yoloDetector.detect(rotatedBitmap);
                int frameW = rotatedBitmap.getWidth();
                int frameH = rotatedBitmap.getHeight();
                rotatedBitmap.recycle();
                runOnUiThread(() -> showRealtimeDetections(frameW, frameH, detections));
            }
            isRealtimeAnalyzing = false;
        });

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, imageAnalysis);
        } catch (Exception e) {
            Toast.makeText(this, "Loi lien ket camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void captureProduct() {
        if (imageCapture == null) {
            return;
        }

        resetDetectedState();
        isCapturing = true;
        binding.ivFrozenPhoto.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCapture.setEnabled(false);

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                Bitmap bitmap = imageProxyToBitmap(imageProxy);
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                imageProxy.close();

                if (bitmap == null) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCapture.setEnabled(true);
                    isCapturing = false;
                    Toast.makeText(ScanProductActivity.this, "Khong the xu ly anh chup", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap rotatedBitmap = rotateBitmap(bitmap, rotationDegrees);
                String imageUri = saveBitmapToCache(rotatedBitmap);
                binding.ivFrozenPhoto.setImageBitmap(rotatedBitmap);
                binding.ivFrozenPhoto.setVisibility(View.VISIBLE);
                binding.overlayView.setIsFitCenter(true);
                runYoloDetection(rotatedBitmap, imageUri);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCapture.setEnabled(true);
                isCapturing = false;
                Toast.makeText(ScanProductActivity.this, "Loi chup anh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void runYoloDetection(Bitmap bitmap, String imageUri) {
        if (yoloDetector == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnCapture.setEnabled(true);
            openTransactionForm(0, "", imageUri, 0);
            return;
        }

        cameraExecutor.execute(() -> {
            List<YoloDetector.YoloDetection> detections = yoloDetector.detect(bitmap);
            runOnUiThread(() -> showDetectionResult(bitmap, detections, imageUri));
        });
    }

    private void showDetectionResult(Bitmap bitmap, List<YoloDetector.YoloDetection> detections, String imageUri) {
        List<YoloDetector.YoloDetection> safeDetections = new ArrayList<>();
        if (detections != null) {
            for (YoloDetector.YoloDetection detection : detections) {
                if (detection != null) {
                    safeDetections.add(detection);
                }
            }
        }
        binding.overlayView.setFrameSize(bitmap.getWidth(), bitmap.getHeight());
        List<BoundingBoxOverlay.Box> overlayBoxes = new ArrayList<>();
        for (YoloDetector.YoloDetection detection : safeDetections) {
            overlayBoxes.add(new BoundingBoxOverlay.Box(detection.rect, detection.className, detection.confidence));
        }
        binding.overlayView.setBoxes(overlayBoxes);
        latestDetections = new ArrayList<>(safeDetections);
        Log.d(YOLO_LOG_TAG, "detections=" + safeDetections.size());
        for (YoloDetector.YoloDetection detection : safeDetections) {
            Log.d(YOLO_LOG_TAG, "classId=" + detection.classId
                    + ", className=" + detection.className
                    + ", confidence=" + detection.confidence
                    + ", bbox=[" + detection.rect.left
                    + "," + detection.rect.top
                    + "," + detection.rect.right
                    + "," + detection.rect.bottom + "]");
        }

        if (safeDetections.isEmpty()) {
            detectedProductName = "";
            detectedConfidence = 0.0;
            openTransactionForm(0, "", imageUri, 0);
            return;
        }

        YoloDetector.YoloDetection best = safeDetections.get(0);
        for (YoloDetector.YoloDetection detection : safeDetections) {
            if (detection.confidence > best.confidence) {
                best = detection;
            }
        }

        detectedProductName = best.className;
        detectedConfidence = best.confidence;

        // Call backend API to classify product detections using ONNX Random Forest
        User currentUser = SharedPrefManager.getInstance(this).getUser();
        int userId = currentUser != null ? currentUser.getUserId() : 0;

        List<ProductClassificationRequest.YoloDetectionDTO> dtoList = new ArrayList<>();
        for (YoloDetector.YoloDetection d : latestDetections) {
            dtoList.add(new ProductClassificationRequest.YoloDetectionDTO(d.className, d.confidence));
        }

        ProductClassificationRequest request = new ProductClassificationRequest(userId, dtoList);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCapture.setEnabled(false);

        RetrofitClient.getApiService().classifyProduct(request)
                .enqueue(new Callback<ApiResponse<AiProductResult>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AiProductResult>> call, Response<ApiResponse<AiProductResult>> response) {
                        int categoryId = 7; // Default to 'other' (Khác)
                        int aiProductLogId = 0;
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AiProductResult result = response.body().getData();
                            if (result != null) {
                                categoryId = result.getSuggestedCategoryId() != null ? result.getSuggestedCategoryId() : 7;
                                aiProductLogId = result.getAiProductLogId() != null ? result.getAiProductLogId() : 0;
                            }
                        } else {
                            Log.e("ScanProductActivity", "Classification failed: " + (response.body() != null ? response.body().getMessage() : "unknown error"));
                        }
                        
                        openTransactionForm(categoryId, detectedProductName, imageUri, aiProductLogId);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AiProductResult>> call, Throwable t) {
                        Log.e("ScanProductActivity", "Classification network failure", t);
                        // Fallback category classification on network failure
                        openTransactionForm(7, detectedProductName, imageUri, 0);
                    }
                });
    }

    private void showRealtimeDetections(int frameWidth, int frameHeight, List<YoloDetector.YoloDetection> detections) {
        if (isCapturing) {
            return;
        }

        binding.overlayView.setFrameSize(frameWidth, frameHeight);
        List<BoundingBoxOverlay.Box> overlayBoxes = new ArrayList<>();
        for (YoloDetector.YoloDetection detection : detections) {
            overlayBoxes.add(new BoundingBoxOverlay.Box(detection.rect, detection.className, detection.confidence));
        }
        binding.overlayView.setBoxes(overlayBoxes);
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        try {
            ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap yuvToBitmap(ImageProxy image) {
        try {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 90, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        if (rotationDegrees == 0) {
            return bitmap;
        }

        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void openTransactionForm(int categoryId, String title, String imageUri, int aiProductLogId) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnCapture.setEnabled(true);
        isCapturing = false;
        AddTransactionFragment addFragment = AddTransactionFragment.newInstanceForProductScan(
                0,
                title != null ? title : "",
                categoryId,
                DateUtils.getCurrentDateString(),
                aiProductLogId,
                imageUri
        );
        addFragment.show(getSupportFragmentManager(), "AddTransactionFragment");
    }

    private String saveBitmapToCache(Bitmap bitmap) {
        try {
            File file = File.createTempFile("product_scan_", ".jpg", getCacheDir());
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream);
            }
            return Uri.fromFile(file).toString();
        } catch (IOException e) {
            return null;
        }
    }

    private void resetDetectedState() {
        detectedProductName = "";
        detectedConfidence = 0.0;
        latestDetections = new ArrayList<>();
        binding.overlayView.clear();
        binding.overlayView.setIsFitCenter(false);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnCapture.setEnabled(false);
            resetDetectedState();

            try {
                Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                if (bitmap == null) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCapture.setEnabled(true);
                    Toast.makeText(this, "Khong the tai anh tu thu vien", Toast.LENGTH_SHORT).show();
                    return;
                }

                int rotationDegrees = getOrientation(imageUri);
                if (rotationDegrees != 0) {
                    Bitmap rotated = rotateBitmap(bitmap, rotationDegrees);
                    bitmap.recycle();
                    bitmap = rotated;
                }

                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
                cameraProvider.unbindAll();

                binding.ivFrozenPhoto.setImageBitmap(bitmap);
                binding.ivFrozenPhoto.setVisibility(View.VISIBLE);
                binding.overlayView.setIsFitCenter(true);
                runYoloDetection(bitmap, imageUri.toString());
            } catch (Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCapture.setEnabled(true);
                Toast.makeText(this, "Loi doc anh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getOrientation(Uri uri) {
        try (java.io.InputStream is = getContentResolver().openInputStream(uri)) {
            if (is != null) {
                androidx.exifinterface.media.ExifInterface exif =
                        new androidx.exifinterface.media.ExifInterface(is);
                int orientation = exif.getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                        return 90;
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                        return 180;
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                        return 270;
                }
            }
        } catch (Exception ignored) {}

        try {
            String[] projection = { android.provider.MediaStore.Images.ImageColumns.ORIENTATION };
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.ImageColumns.ORIENTATION);
                    if (colIndex != -1) {
                        int rotation = cursor.getInt(colIndex);
                        cursor.close();
                        return rotation;
                    }
                }
                cursor.close();
            }
        } catch (Exception ignored) {}

        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Quyen camera bi tu choi", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (imageAnalysis != null) {
            imageAnalysis.clearAnalyzer();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdownNow();
        }
        if (yoloDetector != null) {
            yoloDetector.close();
        }
        super.onDestroy();
    }
}
