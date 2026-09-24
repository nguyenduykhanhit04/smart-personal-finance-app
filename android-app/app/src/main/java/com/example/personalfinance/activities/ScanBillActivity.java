package com.example.personalfinance.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.personalfinance.R;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.ActivityScanBillBinding;
import com.example.personalfinance.fragments.transaction.AddTransactionFragment;
import com.example.personalfinance.models.AiScanResult;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.OcrRequest;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanBillActivity extends AppCompatActivity {

    private static final String TAG = "ScanBillActivity";
    private static final int CAMERA_PERMISSION_CODE = 1001;
    private static final int GALLERY_PICK_CODE = 1002;
    private ActivityScanBillBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private User currentUser;
    private AiScanResult scanResult;
    private Uri currentImageUri;
    private ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBillBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = SharedPrefManager.getInstance(this).getUser();
        if (currentUser == null) {
            finish();
            return;
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCapture.setOnClickListener(v -> captureImage());
        binding.btnGallery.setOnClickListener(v -> openGallery());

        // Check and request camera permission
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
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Lỗi khởi động camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi liên kết camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void captureImage() {
        if (imageCapture == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCapture.setEnabled(false);

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                processImageProxy(imageProxy);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCapture.setEnabled(true);
                Toast.makeText(ScanBillActivity.this, "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnCapture.setEnabled(true);
            return;
        }

        currentImageUri = saveImageProxyToCache(imageProxy);

        // Freeze preview and display captured photo
        runOnUiThread(() -> {
            if (currentImageUri != null) {
                binding.ivCapturedPreview.setImageURI(currentImageUri);
                binding.ivCapturedPreview.setVisibility(View.VISIBLE);
                binding.previewView.setVisibility(View.GONE);
                binding.btnCapture.setVisibility(View.GONE);
                binding.btnGallery.setVisibility(View.GONE);
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
            }
        });

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String rawText = visionText.getText();

                    if (rawText.trim().isEmpty()) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCapture.setEnabled(true);
                        Toast.makeText(ScanBillActivity.this, "Không tìm thấy chữ trên hóa đơn. Vui lòng chụp lại!", Toast.LENGTH_LONG).show();
                    } else {
                        classifyBillText(rawText);
                    }
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCapture.setEnabled(true);
                    Toast.makeText(ScanBillActivity.this, "Lỗi nhận diện chữ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    imageProxy.close();
                });
    }

    private void classifyBillText(String rawText) {
        OcrRequest request = new OcrRequest(currentUser.getUserId(), rawText);
        Log.d(TAG, "Raw OCR text:\n" + rawText);
        Log.d(TAG, "Sending OCR classify request. userId=" + currentUser.getUserId() + ", textLength=" + rawText.length());

        RetrofitClient.getApiService().classifyBill(request).enqueue(new Callback<ApiResponse<AiScanResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiScanResult>> call, Response<ApiResponse<AiScanResult>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCapture.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    scanResult = response.body().getData();

                    Log.d("ddd", "ssss");
                    if (scanResult != null) {
                        Log.d(TAG, "OCR classify result: merchant=" + scanResult.getDetectedMerchant()
                                + ", amount=" + scanResult.getDetectedAmount()
                                + ", date=" + scanResult.getDetectedDate()
                                + ", category=" + scanResult.getSuggestedCategoryName()
                                + ", confidence=" + scanResult.getConfidenceScore());

                        // Automatically open AddTransactionFragment sheet!
                        confirmAndOpenAddSheet();
                    } else {
                        Log.w(TAG, "OCR classify returned empty data");
                        Toast.makeText(ScanBillActivity.this, "Không trích xuất được thông tin từ hóa đơn", Toast.LENGTH_SHORT).show();
                        resetCameraAndPreview();
                    }
                } else {
                    String msg = getErrorMessage(response);
                    Log.w(TAG, "OCR classify failed. code=" + response.code() + ", message=" + msg);
                    Toast.makeText(ScanBillActivity.this, msg, Toast.LENGTH_SHORT).show();
                    resetCameraAndPreview();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiScanResult>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCapture.setEnabled(true);
                Log.e(TAG, "OCR classify request failed", t);
                Toast.makeText(ScanBillActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetCameraAndPreview();
            }
        });
    }

    private String getErrorMessage(Response<ApiResponse<AiScanResult>> response) {
        if (response.body() != null && response.body().getMessage() != null) {
            return response.body().getMessage();
        }
        if (response.errorBody() != null) {
            try {
                ApiResponse<?> error = new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
                if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
                    return error.getMessage();
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not parse OCR error response", e);
            }
        }
        return "Lỗi phân tích hóa đơn (HTTP " + response.code() + ")";
    }

    private void confirmAndOpenAddSheet() {
        if (scanResult == null) return;

        AddTransactionFragment addFragment = AddTransactionFragment.newInstance(
                scanResult.getDetectedAmount(),
                scanResult.getDetectedMerchant() != null ? scanResult.getDetectedMerchant() : "Hóa đơn quét",
                scanResult.getSuggestedCategoryId() != null ? scanResult.getSuggestedCategoryId() : 0,
                scanResult.getDetectedDate() != null ? scanResult.getDetectedDate() : DateUtils.getCurrentDateString(),
                scanResult.getAiScanLogId() != null ? scanResult.getAiScanLogId() : 0,
                currentImageUri != null ? currentImageUri.toString() : null
        );

        addFragment.show(getSupportFragmentManager(), "AddTransactionFragment");
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
            currentImageUri = imageUri;

            // Freeze preview and display gallery photo
            runOnUiThread(() -> {
                binding.ivCapturedPreview.setImageURI(imageUri);
                binding.ivCapturedPreview.setVisibility(View.VISIBLE);
                binding.previewView.setVisibility(View.GONE);
                binding.btnCapture.setVisibility(View.GONE);
                binding.btnGallery.setVisibility(View.GONE);
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
            });

            try {
                InputImage image = InputImage.fromFilePath(this, imageUri);
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnCapture.setEnabled(false);

                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                recognizer.process(image)
                        .addOnSuccessListener(visionText -> {
                            String rawText = visionText.getText();
                            if (rawText.trim().isEmpty()) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.btnCapture.setEnabled(true);
                                Toast.makeText(ScanBillActivity.this, "Không tìm thấy chữ trên ảnh hóa đơn. Vui lòng chọn ảnh khác!", Toast.LENGTH_LONG).show();
                            } else {
                                classifyBillText(rawText);
                            }
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnCapture.setEnabled(true);
                            Toast.makeText(ScanBillActivity.this, "Lỗi nhận diện chữ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi tải ảnh từ thư viện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Quyền truy cập Camera bị từ chối. Không thể quét hóa đơn!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private Uri saveImageProxyToCache(ImageProxy imageProxy) {
        try {
            Bitmap bitmap = imageProxyToBitmap(imageProxy);
            if (bitmap == null) return null;

            java.io.File cacheFile = new java.io.File(getCacheDir(), "captured_bill.jpg");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(cacheFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }
            bitmap.recycle();
            return Uri.fromFile(cacheFile);
        } catch (Exception e) {
            Log.e(TAG, "Error saving captured image to cache", e);
            return null;
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        java.nio.ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
        if (rotationDegrees != 0) {
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(rotationDegrees);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotatedBitmap;
        }
        return bitmap;
    }

    private void resetCameraAndPreview() {
        currentImageUri = null;
        scanResult = null;
        runOnUiThread(() -> {
            binding.ivCapturedPreview.setImageURI(null);
            binding.ivCapturedPreview.setVisibility(View.GONE);
            binding.previewView.setVisibility(View.VISIBLE);

            binding.btnCapture.setVisibility(View.VISIBLE);
            binding.btnCapture.setEnabled(true);
            binding.btnGallery.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);

            startCamera();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
