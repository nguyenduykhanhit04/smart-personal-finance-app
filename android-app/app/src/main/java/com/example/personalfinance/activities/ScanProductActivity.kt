package com.example.personalfinance.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.ActivityScanProductBinding
import com.example.personalfinance.fragments.transaction.AddTransactionFragment
import com.example.personalfinance.models.AiProductResult
import com.example.personalfinance.models.ApiResponse
import com.example.personalfinance.models.ProductClassificationRequest
import com.example.personalfinance.models.User
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.ml.yolo.BoundingBoxOverlay
import com.example.personalfinance.ml.yolo.YoloDetector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanProductActivity : AppCompatActivity() {

    companion object {
        private const val YOLO_LOG_TAG = "YOLO_CAPTURE"
        private const val CAMERA_PERMISSION_CODE = 1002
        private const val GALLERY_PICK_CODE = 1003
    }

    private lateinit var binding: ActivityScanProductBinding
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService? = null
    private var currentUser: User? = null
    private var yoloDetector: YoloDetector? = null

    private var detectedProductName = ""
    private var detectedConfidence = 0.0
    @Volatile
    private var isRealtimeAnalyzing = false
    @Volatile
    private var isCapturing = false
    private var lastAnalysisTime: Long = 0
    private var latestDetections: List<YoloDetector.YoloDetection> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = SharedPrefManager.getInstance(this).user
        if (currentUser == null) {
            finish()
            return
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        try {
            yoloDetector = YoloDetector(this, "yolo_product.tflite")
        } catch (e: IOException) {
            Toast.makeText(this, "Lỗi tải mô hình YOLO: ${e.message}", Toast.LENGTH_LONG).show()
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCapture.setOnClickListener { captureProduct() }
        binding.btnGallery.setOnClickListener { openGallery() }

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
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
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

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()

        val executor = cameraExecutor ?: return
        imageAnalysis?.setAnalyzer(executor) { imageProxy ->
            val now = System.currentTimeMillis()
            if (isCapturing || isRealtimeAnalyzing || yoloDetector == null || (now - lastAnalysisTime) < 250) {
                imageProxy.close()
                return@setAnalyzer
            }
            lastAnalysisTime = now
            isRealtimeAnalyzing = true

            val bitmap = yuvToBitmap(imageProxy)
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            imageProxy.close()

            if (bitmap != null) {
                val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees)
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle()
                }
                val detections = yoloDetector?.detect(rotatedBitmap) ?: emptyList()
                val frameW = rotatedBitmap.width
                val frameH = rotatedBitmap.height
                rotatedBitmap.recycle()
                runOnUiThread { showRealtimeDetections(frameW, frameH, detections) }
            }
            isRealtimeAnalyzing = false
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                imageAnalysis
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi liên kết camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureProduct() {
        val capture = imageCapture ?: return

        resetDetectedState()
        isCapturing = true
        binding.ivFrozenPhoto.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCapture.isEnabled = false

        capture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val bitmap = imageProxyToBitmap(imageProxy)
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                imageProxy.close()

                if (bitmap == null) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true
                    isCapturing = false
                    Toast.makeText(this@ScanProductActivity, "Không thể xử lý ảnh chụp", Toast.LENGTH_SHORT).show()
                    return
                }

                val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees)
                val imageUri = saveBitmapToCache(rotatedBitmap)
                binding.ivFrozenPhoto.setImageBitmap(rotatedBitmap)
                binding.ivFrozenPhoto.visibility = View.VISIBLE
                binding.overlayView.isFitCenter = true
                runYoloDetection(rotatedBitmap, imageUri)
            }

            override fun onError(exception: ImageCaptureException) {
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true
                isCapturing = false
                Toast.makeText(this@ScanProductActivity, "Lỗi chụp ảnh: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun runYoloDetection(bitmap: Bitmap, imageUri: String?) {
        val detector = yoloDetector
        if (detector == null) {
            binding.progressBar.visibility = View.GONE
            binding.btnCapture.isEnabled = true
            openTransactionForm(0, "", imageUri, 0)
            return
        }

        cameraExecutor?.execute {
            val detections = detector.detect(bitmap)
            runOnUiThread { showDetectionResult(bitmap, detections, imageUri) }
        }
    }

    private fun showDetectionResult(bitmap: Bitmap, detections: List<YoloDetector.YoloDetection>?, imageUri: String?) {
        val safeDetections = detections?.filterNotNull() ?: emptyList()

        binding.overlayView.setFrameSize(bitmap.width, bitmap.height)
        val overlayBoxes = safeDetections.map {
            BoundingBoxOverlay.Box(it.rect, it.className, it.confidence)
        }
        binding.overlayView.setBoxes(overlayBoxes)
        latestDetections = safeDetections

        Log.d(YOLO_LOG_TAG, "detections=${safeDetections.size}")
        for (detection in safeDetections) {
            Log.d(
                YOLO_LOG_TAG,
                "classId=${detection.classId}, className=${detection.className}, confidence=${detection.confidence}, bbox=[${detection.rect.left},${detection.rect.top},${detection.rect.right},${detection.rect.bottom}]"
            )
        }

        if (safeDetections.isEmpty()) {
            detectedProductName = ""
            detectedConfidence = 0.0
            openTransactionForm(0, "", imageUri, 0)
            return
        }

        val best = safeDetections.maxByOrNull { it.confidence } ?: safeDetections[0]
        detectedProductName = best.className
        detectedConfidence = best.confidence.toDouble()

        // Call backend API to classify product detections using ONNX Random Forest
        val userId = currentUser?.userId ?: 0
        val dtoList = latestDetections.map {
            ProductClassificationRequest.YoloDetectionDTO(it.className, it.confidence.toDouble())
        }

        val request = ProductClassificationRequest(userId, dtoList)

        binding.progressBar.visibility = View.VISIBLE
        binding.btnCapture.isEnabled = false

        RetrofitClient.apiService.classifyProduct(request)
            .enqueue(object : Callback<ApiResponse<AiProductResult>> {
                override fun onResponse(
                    call: Call<ApiResponse<AiProductResult>>,
                    response: Response<ApiResponse<AiProductResult>>
                ) {
                    var categoryId = 7 // Default to 'other' (Khác)
                    var aiProductLogId = 0

                    val body = response.body()
                    if (response.isSuccessful && body?.isSuccess == true) {
                        val result = body.data
                        if (result != null) {
                            categoryId = result.suggestedCategoryId ?: 7
                            aiProductLogId = result.aiProductLogId ?: 0
                        }
                    } else {
                        Log.e("ScanProductActivity", "Classification failed: ${body?.message ?: "unknown error"}")
                    }

                    openTransactionForm(categoryId, detectedProductName, imageUri, aiProductLogId)
                }

                override fun onFailure(call: Call<ApiResponse<AiProductResult>>, t: Throwable) {
                    Log.e("ScanProductActivity", "Classification network failure", t)
                    openTransactionForm(7, detectedProductName, imageUri, 0)
                }
            })
    }

    private fun showRealtimeDetections(frameWidth: Int, frameHeight: Int, detections: List<YoloDetector.YoloDetection>) {
        if (isCapturing) return

        binding.overlayView.setFrameSize(frameWidth, frameHeight)
        val overlayBoxes = detections.map {
            BoundingBoxOverlay.Box(it.rect, it.className, it.confidence)
        }
        binding.overlayView.setBoxes(overlayBoxes)
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun yuvToBitmap(image: ImageProxy): Bitmap? {
        return try {
            val planes = image.planes
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val outputStream = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 90, outputStream)
            val imageBytes = outputStream.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap

        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun openTransactionForm(categoryId: Int, title: String?, imageUri: String?, aiProductLogId: Int) {
        binding.progressBar.visibility = View.GONE
        binding.btnCapture.isEnabled = true
        isCapturing = false
        val addFragment = AddTransactionFragment.newInstanceForProductScan(
            0.0,
            title ?: "",
            categoryId,
            DateUtils.getCurrentDateString(),
            aiProductLogId,
            imageUri
        )
        addFragment.show(supportFragmentManager, "AddTransactionFragment")
    }

    private fun saveBitmapToCache(bitmap: Bitmap): String? {
        return try {
            val file = File.createTempFile("product_scan_", ".jpg", cacheDir)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
            }
            Uri.fromFile(file).toString()
        } catch (e: IOException) {
            null
        }
    }

    private fun resetDetectedState() {
        detectedProductName = ""
        detectedConfidence = 0.0
        latestDetections = ArrayList()
        binding.overlayView.clear()
        binding.overlayView.isFitCenter = false
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
            binding.progressBar.visibility = View.VISIBLE
            binding.btnCapture.isEnabled = false
            resetDetectedState()

            try {
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                if (bitmap == null) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCapture.isEnabled = true
                    Toast.makeText(this, "Không thể tải ảnh từ thư viện", Toast.LENGTH_SHORT).show()
                    return
                }

                val rotationDegrees = getOrientation(imageUri)
                if (rotationDegrees != 0) {
                    val rotated = rotateBitmap(bitmap, rotationDegrees)
                    bitmap.recycle()
                    bitmap = rotated
                }

                val cameraProvider = ProcessCameraProvider.getInstance(this).get()
                cameraProvider.unbindAll()

                binding.ivFrozenPhoto.setImageBitmap(bitmap)
                binding.ivFrozenPhoto.visibility = View.VISIBLE
                binding.overlayView.isFitCenter = true
                runYoloDetection(bitmap, imageUri.toString())
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnCapture.isEnabled = true
                Toast.makeText(this, "Lỗi đọc ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getOrientation(uri: Uri): Int {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> return 270
                }
            }
        } catch (ignored: Exception) {}

        try {
            val projection = arrayOf(MediaStore.Images.ImageColumns.ORIENTATION)
            val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val colIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
                    if (colIndex != -1) {
                        return it.getInt(colIndex)
                    }
                }
            }
        } catch (ignored: Exception) {}

        return 0
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Quyền camera bị từ chối", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        imageAnalysis?.clearAnalyzer()
        cameraExecutor?.shutdownNow()
        yoloDetector?.close()
        super.onDestroy()
    }
}
