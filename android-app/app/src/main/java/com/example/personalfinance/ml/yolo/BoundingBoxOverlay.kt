package com.example.personalfinance.ml.yolo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class BoundingBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Box(
        val rect: RectF,
        val label: String,
        val confidence: Float
    )

    private val boxes = ArrayList<Box>()
    private val boxPaint = Paint().apply {
        color = Color.parseColor("#3B82F6") // Royal blue
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36.0f
        isFakeBoldText = true
        isAntiAlias = true
    }

    private val textBackgroundPaint = Paint().apply {
        color = Color.parseColor("#E63B82F6") // opaque royal blue
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    var isFitCenter: Boolean = false
        set(value) {
            field = value
            postInvalidate()
        }

    private var frameWidth = 1
    private var frameHeight = 1

    fun setFrameSize(width: Int, height: Int) {
        frameWidth = width
        frameHeight = height
    }

    fun setBoxes(newBoxes: List<Box>?) {
        synchronized(boxes) {
            boxes.clear()
            if (newBoxes != null) {
                boxes.addAll(newBoxes)
            }
        }
        postInvalidate()
    }

    fun clear() {
        synchronized(boxes) {
            boxes.clear()
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        synchronized(boxes) {
            if (frameWidth <= 0 || frameHeight <= 0) return

            val scaleX = width.toFloat() / frameWidth
            val scaleY = height.toFloat() / frameHeight

            val scale: Float
            val offsetX: Float
            val offsetY: Float
            val scaledW: Float
            val scaledH: Float

            if (isFitCenter) {
                scale = min(scaleX, scaleY)
                scaledW = frameWidth * scale
                scaledH = frameHeight * scale
                offsetX = (width - scaledW) / 2.0f
                offsetY = (height - scaledH) / 2.0f
            } else {
                scale = max(scaleX, scaleY)
                scaledW = frameWidth * scale
                scaledH = frameHeight * scale
                offsetX = (scaledW - width) / 2.0f
                offsetY = (scaledH - height) / 2.0f
            }

            for (box in boxes) {
                val left: Float
                val right: Float
                val top: Float
                val bottom: Float

                if (isFitCenter) {
                    left = box.rect.left * scaledW + offsetX
                    right = box.rect.right * scaledW + offsetX
                    top = box.rect.top * scaledH + offsetY
                    bottom = box.rect.bottom * scaledH + offsetY
                } else {
                    left = box.rect.left * scaledW - offsetX
                    right = box.rect.right * scaledW - offsetX
                    top = box.rect.top * scaledH - offsetY
                    bottom = box.rect.bottom * scaledH - offsetY
                }

                // Draw bounding box
                canvas.drawRect(left, top, right, bottom, boxPaint)

                // Draw label text background
                val text = "${box.label} ${(box.confidence * 100).toInt()}%"
                val textWidth = textPaint.measureText(text)
                val textHeight = textPaint.textSize

                // Draw label box at the top left of bounding box
                canvas.drawRect(
                    left,
                    top - textHeight - 15,
                    left + textWidth + 20,
                    top,
                    textBackgroundPaint
                )

                // Draw label text
                canvas.drawText(text, left + 10, top - 10, textPaint)
            }
        }
    }
}
