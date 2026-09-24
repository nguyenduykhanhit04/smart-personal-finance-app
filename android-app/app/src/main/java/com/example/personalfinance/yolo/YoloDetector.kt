package com.example.personalfinance.yolo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class YoloDetector(context: Context, modelPath: String) {

    private val interpreter: Interpreter
    private var closed = false

    companion object {
        val CLASS_NAMES = arrayOf(
            "bottled_water", "bread", "clothes", "coffee_cup", "cosmetic",
            "electronic_item", "fastfood", "helmet", "medicine", "milk_tea",
            "motorbike", "noodle", "rice_meal", "shoes", "snack",
            "soft_drink", "taxi_car", "toy_game"
        )

        val LABELS_VIETNAMESE = arrayOf(
            "Nước đóng chai", "Bánh mì", "Quần áo", "Cà phê", "Mỹ phẩm",
            "Thiết bị điện tử", "Thức ăn nhanh", "Mũ bảo hiểm", "Thuốc y tế", "Trà sữa",
            "Xe máy", "Mì / Phở", "Cơm hộp / Suất ăn", "Giày dép", "Đồ ăn vặt",
            "Nước ngọt", "Taxi / Ô tô", "Đồ chơi / Game"
        )
    }

    data class YoloDetection(
        val classId: Int,
        val className: String,
        val labelVietnamese: String,
        val confidence: Float,
        val rect: RectF
    )

    init {
        val options = Interpreter.Options().apply {
            setNumThreads(4) // Use 4 threads for mobile CPU inference
        }
        interpreter = Interpreter(loadModelFile(context, modelPath), options)
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        context.assets.openFd(modelPath).use { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        }
    }

    @Synchronized
    fun detect(bitmap: Bitmap): List<YoloDetection> {
        val detections = ArrayList<YoloDetection>()
        if (closed) return detections

        // Resize bitmap to 640x640
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)

        // Preprocess image to ByteBuffer
        val inputBuffer = ByteBuffer.allocateDirect(1 * 640 * 640 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val intValues = IntArray(640 * 640)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        for (pixelValue in intValues) {
            val r = ((pixelValue shr 16) and 0xFF) / 255.0f
            val g = ((pixelValue shr 8) and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        // Output array structure for end-to-end YOLOv8 TFLite model: shape [1][300][6]
        val output = Array(1) { Array(300) { FloatArray(6) } }

        // Run inference
        interpreter.run(inputBuffer, output)

        // YOLOv8 TFLite with builtin postprocessing returns up to 300 boxes
        // output[0][box_index][4] is the confidence score
        // output[0][box_index][5] is the class index
        for (i in 0 until 300) {
            val confidence = output[0][i][4]
            if (confidence >= 0.30f) {
                val classId = output[0][i][5].toInt()
                if (classId in CLASS_NAMES.indices) {
                    var x1 = output[0][i][0]
                    var y1 = output[0][i][1]
                    var x2 = output[0][i][2]
                    var y2 = output[0][i][3]

                    // Constrain coordinates to [0, 1]
                    x1 = x1.coerceIn(0.0f, 1.0f)
                    y1 = y1.coerceIn(0.0f, 1.0f)
                    x2 = x2.coerceIn(0.0f, 1.0f)
                    y2 = y2.coerceIn(0.0f, 1.0f)

                    detections.add(
                        YoloDetection(
                            classId = classId,
                            className = CLASS_NAMES[classId],
                            labelVietnamese = LABELS_VIETNAMESE[classId],
                            confidence = confidence,
                            rect = RectF(x1, y1, x2, y2)
                        )
                    )
                }
            }
        }

        // Recycle the scaled bitmap to avoid memory leak
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }

        return detections
    }

    @Synchronized
    fun close() {
        if (!closed) {
            closed = true
            interpreter.close()
        }
    }
}
