package com.example.personalfinance.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.personalfinance.R
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.ActivityScanBillBinding
import com.example.personalfinance.fragments.transaction.AddTransactionFragment
import com.example.personalfinance.models.dto.AiScanResult
import com.example.personalfinance.models.dto.ApiResponse
import com.example.personalfinance.models.dto.OcrRequest
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanBillActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ScanBillActivity"
        private const val CAMERA_PERMISSION_CODE = 1001
        private const val GALLERY_PICK_CODE = 1002
    }

    private lateinit var binding: ActivityScanBillBinding
    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService? = null
    private var currentUser: User? = null
    private var scanResult: AiScanResult? = null
    private var currentImageUri: Uri? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = SharedPrefManager.getInstance(this).user
        if (currentUser == null) {
            finish()
            return
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCapture.setOnClickListener { captureImage() }
        binding.btnGallery.setOnClickListener { openGallery() }

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                bindPreview(provider)
            } catch (e: Exception) {
                when (e) {
                    is ExecutionException, is InterruptedException -> {
                        Toast.makeText(this, "Lỗi khởi động camera: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> throw e
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(provider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            provider.unbindAll()
            provider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi liên kết camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureImage() {
        val capture = imageCapture ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnCapture.isEnabled = false

        capture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                processImageProxy(imageProxy)
            }

            override fun onError(exception: ImageCaptureException) {
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true
                Toast.makeText(this@ScanBillActivity, "Lỗi chụp ảnh: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            binding.progressBar.visibility = View.GONE
            binding.btnCapture.isEnabled = true
            return
        }

        currentImageUri = saveImageProxyToCache(imageProxy)

        // Freeze preview and display captured photo
        runOnUiThread {
            currentImageUri?.let { uri ->
                binding.ivCapturedPreview.setImageURI(uri)
                binding.ivCapturedPreview.visibility = View.VISIBLE
                binding.previewView.visibility = View.GONE
                binding.btnCapture.visibility = View.GONE
                binding.btnGallery.visibility = View.GONE
                cameraProvider?.unbindAll()
            }
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                if (rawText.isBlank()) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true
                    Toast.makeText(this, "Không tìm thấy chữ trên hóa đơn. Vui lòng chụp lại!", Toast.LENGTH_LONG).show()
                } else {
                    classifyBillText(rawText)
                }
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true
                Toast.makeText(this, "Lỗi nhận diện chữ: ${e.message}", Toast.LENGTH_SHORT).show()
                imageProxy.close()
            }
    }

    private fun classifyBillText(rawText: String) {
        val userId = currentUser?.userId ?: return
        val request = OcrRequest(userId, rawText)
        Log.d(TAG, "Raw OCR text:\n$rawText")
        Log.d(TAG, "Sending OCR classify request. userId=$userId, textLength=${rawText.length}")

        RetrofitClient.apiService.classifyBill(request).enqueue(object : Callback<ApiResponse<AiScanResult>> {
            override fun onResponse(
                call: Call<ApiResponse<AiScanResult>>,
                response: Response<ApiResponse<AiScanResult>>
            ) {
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true

                val body = response.body()
                if (response.isSuccessful && body?.isSuccess == true) {
                    scanResult = body.data

                    val result = scanResult
                    if (result != null) {
                        Log.d(
                            TAG,
                            "OCR classify result: merchant=${result.detectedMerchant}, amount=${result.detectedAmount}, date=${result.detectedDate}, category=${result.suggestedCategoryName}, confidence=${result.confidenceScore}"
                        )
                        // Automatically open AddTransactionFragment sheet!
                        confirmAndOpenAddSheet()
                    } else {
                        Log.w(TAG, "OCR classify returned empty data")
                        Toast.makeText(this@ScanBillActivity, "Không trích xuất được thông tin từ hóa đơn", Toast.LENGTH_SHORT).show()
                        resetCameraAndPreview()
                    }
                } else {
                    val msg = getErrorMessage(response)
                    Log.w(TAG, "OCR classify failed. code=${response.code()}, message=$msg")
                    Toast.makeText(this@ScanBillActivity, msg, Toast.LENGTH_SHORT).show()
                    resetCameraAndPreview()
                }
            }

            override fun onFailure(call: Call<ApiResponse<AiScanResult>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true
                Log.e(TAG, "OCR classify request failed", t)
                Toast.makeText(this@ScanBillActivity, "Lỗi kết nối server: ${t.message}", Toast.LENGTH_SHORT).show()
                resetCameraAndPreview()
            }
        })
    }

    private fun getErrorMessage(response: Response<ApiResponse<AiScanResult>>): String {
        response.body()?.message?.let { return it }
        response.errorBody()?.let { errorBody ->
            try {
                val error = Gson().fromJson(errorBody.string(), ApiResponse::class.java)
                if (!error?.message.isNullOrBlank()) {
                    return error.message
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse OCR error response", e)
            }
        }
        return "Lỗi phân tích hóa đơn (HTTP ${response.code()})"
    }

    private fun confirmAndOpenAddSheet() {
        val result = scanResult ?: return

        val addFragment = AddTransactionFragment.newInstance(
            result.detectedAmount,
            result.detectedMerchant ?: "Hóa đơn quét",
            result.suggestedCategoryId ?: 0,
            result.detectedDate ?: DateUtils.getCurrentDateString(),
            result.aiScanLogId ?: 0,
            currentImageUri?.toString()
        )

        addFragment.show(supportFragmentManager, "AddTransactionFragment")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, GALLERY_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data?.data != null) {
            val imageUri = data.data!!
            currentImageUri = imageUri

            // Freeze preview and display gallery photo
            runOnUiThread {
                binding.ivCapturedPreview.setImageURI(imageUri)
                binding.ivCapturedPreview.visibility = View.VISIBLE
                binding.previewView.visibility = View.GONE
                binding.btnCapture.visibility = View.GONE
                binding.btnGallery.visibility = View.GONE
                cameraProvider?.unbindAll()
            }

            try {
                val image = InputImage.fromFilePath(this, imageUri)
                binding.progressBar.visibility = View.VISIBLE
                binding.btnCapture.isEnabled = false

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val rawText = visionText.text
                        if (rawText.isBlank()) {
                            binding.progressBar.visibility = View.GONE
                            binding.btnCapture.isEnabled = true
                            Toast.makeText(this, "Không tìm thấy chữ trên ảnh hóa đơn. Vui lòng chọn ảnh khác!", Toast.LENGTH_LONG).show()
                        } else {
                            classifyBillText(rawText)
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility = View.GONE
                        binding.btnCapture.isEnabled = true
                        Toast.makeText(this, "Lỗi nhận diện chữ: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi tải ảnh từ thư viện: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Quyền truy cập Camera bị từ chối. Không thể quét hóa đơn!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun saveImageProxyToCache(imageProxy: ImageProxy): Uri? {
        return try {
            val bitmap = imageProxyToBitmap(imageProxy) ?: return null
            val cacheFile = File(cacheDir, "captured_bill.jpg")
            FileOutputStream(cacheFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
            bitmap.recycle()
            Uri.fromFile(cacheFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving captured image to cache", e)
            null
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val planes = imageProxy.planes
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            return rotatedBitmap
        }
        return bitmap
    }

    private fun resetCameraAndPreview() {
        currentImageUri = null
        scanResult = null
        runOnUiThread {
            binding.ivCapturedPreview.setImageURI(null)
            binding.ivCapturedPreview.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE

            binding.btnCapture.visibility = View.VISIBLE
            binding.btnCapture.isEnabled = true
            binding.btnGallery.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE

            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}
