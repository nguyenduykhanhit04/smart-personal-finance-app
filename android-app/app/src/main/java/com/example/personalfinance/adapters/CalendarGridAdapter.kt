package com.example.personalfinance.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.ItemCalendarDayBinding
import com.example.personalfinance.models.CalendarDay
import com.example.personalfinance.models.Transaction
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.RelativeCornerSize
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.concurrent.thread

class CalendarGridAdapter(
    private val context: Context,
    private val calendarDays: List<CalendarDay>
) : RecyclerView.Adapter<CalendarGridAdapter.CalendarViewHolder>() {

    companion object {
        private val imageCache = LruCache<String, Bitmap>(30)
    }

    fun interface OnDayClickListener {
        fun onDayClick(day: CalendarDay, hasTransaction: Boolean, position: Int)
    }

    private var listener: OnDayClickListener? = null
    var selectedPosition = -1
        private set
    private var displayYear = 0
    private var displayMonth = 0

    fun setOnDayClickListener(listener: OnDayClickListener) {
        this.listener = listener
    }

    fun setDisplayedMonth(year: Int, month: Int) {
        displayYear = year
        displayMonth = month
    }

    fun setSelectedPosition(position: Int) {
        val oldPos = selectedPosition
        selectedPosition = position
        if (oldPos >= 0) notifyItemChanged(oldPos)
        if (selectedPosition >= 0) notifyItemChanged(selectedPosition)
    }

    fun clearSelection() {
        val oldPos = selectedPosition
        selectedPosition = -1
        if (oldPos >= 0) notifyItemChanged(oldPos)
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, bytes: ByteArray): Bitmap {
        try {
            val bis = ByteArrayInputStream(bytes)
            val exif = ExifInterface(bis)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
            if (degrees != 0) {
                val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (ignored: Exception) {}
        return bitmap
    }

    private fun loadImage(relativeUrl: String?, imageView: ShapeableImageView) {
        if (relativeUrl.isNullOrBlank()) return

        val baseUrl = RetrofitClient.client.baseUrl().toString()
        val path = if (relativeUrl.startsWith("/")) relativeUrl.substring(1) else relativeUrl
        val fullUrl = baseUrl + path

        val cached = imageCache.get(fullUrl)
        if (cached != null) {
            imageView.setImageBitmap(cached)
            return
        }

        imageView.tag = fullUrl
        thread {
            try {
                val url = URL(fullUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    doInput = true
                    setRequestProperty("ngrok-skip-browser-warning", "true")
                    setRequestProperty("User-Agent", "Android-App")
                    connect()
                }

                if (conn.responseCode == 200) {
                    val bytes = conn.inputStream.use { inputStream ->
                        val bos = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (inputStream.read(buffer).also { len = it } != -1) {
                            bos.write(buffer, 0, len)
                        }
                        bos.toByteArray()
                    }

                    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        bitmap = rotateBitmapIfNeeded(bitmap, bytes)
                        imageCache.put(fullUrl, bitmap)
                        val finalBitmap = bitmap
                        Handler(Looper.getMainLooper()).post {
                            if (fullUrl == imageView.tag) {
                                imageView.setImageBitmap(finalBitmap)
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(context), parent, false)
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(calendarDays[position], position)
    }

    override fun getItemCount(): Int = calendarDays.size

    inner class CalendarViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay, position: Int) {
            if (day.isPlaceholder) {
                // Invisible cell for grid alignment
                itemView.visibility = View.INVISIBLE
                itemView.isClickable = false
                itemView.isEnabled = false
                return
            }

            itemView.visibility = View.VISIBLE
            itemView.isClickable = true
            itemView.isEnabled = true
            itemView.alpha = 1f
            binding.tvDayNumber.text = day.dayNumber.toString()

            val isSelected = (position == selectedPosition)
            val transactions = day.transactions
            val isFuture = isFutureDay(day.dayNumber)

            if (isFuture) {
                binding.viewDashedBorder.visibility = View.GONE
                binding.imgPlus.visibility = View.GONE
                binding.imgThumbnail.visibility = View.GONE
                binding.circleContainer.setBackgroundResource(R.drawable.bg_circle)
                binding.circleContainer.backgroundTintList =
                    ColorStateList.valueOf(context.getColor(R.color.surface_variant))
                binding.tvDayNumber.setTextColor(context.getColor(R.color.text_hint))
                itemView.alpha = 0.55f
                itemView.isEnabled = false
                itemView.isClickable = false
                itemView.setOnClickListener(null)
                return
            }

            binding.circleContainer.backgroundTintList = null

            if (transactions.isNullOrEmpty()) {
                // Empty day - show dashed border and plus sign
                binding.imgThumbnail.visibility = View.GONE

                if (isSelected) {
                    binding.viewDashedBorder.visibility = View.GONE
                    binding.imgPlus.visibility = View.VISIBLE
                    binding.imgPlus.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    binding.circleContainer.setBackgroundResource(R.drawable.bg_circle_selected)
                    binding.tvDayNumber.setTextColor(Color.parseColor("#3B82F6")) // Blue highlight text
                } else {
                    binding.viewDashedBorder.visibility = View.VISIBLE
                    binding.imgPlus.visibility = View.VISIBLE
                    binding.imgPlus.imageTintList = ColorStateList.valueOf(context.getColor(R.color.text_hint))
                    binding.circleContainer.background = null
                    binding.tvDayNumber.setTextColor(context.getColor(R.color.text_secondary))
                }
            } else {
                // Has transactions
                binding.viewDashedBorder.visibility = View.GONE
                binding.imgPlus.visibility = View.GONE

                // Filter transactions that have valid images
                val imageTxs = transactions.filter { !it.imageUrl.isNullOrBlank() }

                if (imageTxs.isEmpty()) {
                    // Fallback to Category Icon
                    binding.imgThumbnail.visibility = View.VISIBLE
                    binding.imgThumbnailBack.visibility = View.GONE
                    binding.imgThumbnailFront.visibility = View.GONE

                    // Shape is Circle
                    binding.imgThumbnail.shapeAppearanceModel = binding.imgThumbnail.shapeAppearanceModel.toBuilder()
                        .setAllCornerSizes(RelativeCornerSize(0.5f))
                        .build()

                    val firstTx = transactions[0]
                    val catName = firstTx.categoryName?.lowercase(Locale.ROOT) ?: ""
                    when {
                        catName.contains("ăn uống") || catName.contains("sức khỏe") || catName.contains("y tế") -> {
                            binding.imgThumbnail.setImageResource(R.drawable.ic_transaction)
                            binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.expense_red))
                        }
                        else -> {
                            binding.imgThumbnail.setImageResource(R.drawable.ic_budget)
                            binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.primary))
                        }
                    }
                } else if (imageTxs.size == 1) {
                    // Single Image Thumbnail with Rounded Rect Shape
                    binding.imgThumbnail.visibility = View.VISIBLE
                    binding.imgThumbnailBack.visibility = View.GONE
                    binding.imgThumbnailFront.visibility = View.GONE
                    binding.imgThumbnail.setBackgroundColor(Color.TRANSPARENT)

                    val r = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics
                    )
                    binding.imgThumbnail.shapeAppearanceModel = binding.imgThumbnail.shapeAppearanceModel.toBuilder()
                        .setAllCornerSizes(r)
                        .build()

                    loadImage(imageTxs[0].imageUrl, binding.imgThumbnail)
                } else {
                    // Stacked Overlapping Image Thumbnails
                    binding.imgThumbnail.visibility = View.GONE
                    binding.imgThumbnailBack.visibility = View.VISIBLE
                    binding.imgThumbnailFront.visibility = View.VISIBLE

                    loadImage(imageTxs[0].imageUrl, binding.imgThumbnailBack)
                    loadImage(imageTxs[1].imageUrl, binding.imgThumbnailFront)
                }

                if (isSelected) {
                    binding.circleContainer.setBackgroundResource(R.drawable.bg_circle_selected_ring)
                    binding.tvDayNumber.setTextColor(Color.parseColor("#3B82F6")) // Blue highlight text
                } else {
                    binding.circleContainer.background = null
                    binding.tvDayNumber.setTextColor(context.getColor(R.color.text_secondary))
                }
            }

            itemView.setOnClickListener {
                setSelectedPosition(position)
                listener?.onDayClick(day, !transactions.isNullOrEmpty(), position)
            }
        }

        private fun isFutureDay(dayNumber: Int): Boolean {
            val cellDate = Calendar.getInstance().apply {
                set(displayYear, displayMonth, dayNumber, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            return cellDate.after(today)
        }
    }
}
