package com.example.personalfinance.fragments.transaction

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.DialogFragment
import com.example.personalfinance.R
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.models.domain.Transaction
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.abs

class TransactionPhotoDetailDialog : DialogFragment() {

    companion object {
        private val imageCache = LruCache<String, Bitmap>(20)

        fun newInstance(transaction: Transaction, position: Int, totalCount: Int): TransactionPhotoDetailDialog {
            return TransactionPhotoDetailDialog().apply {
                arguments = Bundle().apply {
                    putSerializable("transaction", transaction)
                    putInt("position", position)
                    putInt("totalCount", totalCount)
                }
            }
        }
    }

    private var transaction: Transaction? = null
    private var position = 0
    private var totalCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        arguments?.let {
            transaction = it.getSerializable("transaction") as? Transaction
            position = it.getInt("position", 0)
            totalCount = it.getInt("totalCount", 1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_transaction_photo_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivDetailPhoto = view.findViewById<ImageView>(R.id.ivDetailPhoto)
        val tvDetailDateTime = view.findViewById<TextView>(R.id.tvDetailDateTime)
        val tvCategoryTag = view.findViewById<TextView>(R.id.tvCategoryTag)
        val tvWalletTag = view.findViewById<TextView>(R.id.tvWalletTag)
        val tvDetailAmount = view.findViewById<TextView>(R.id.tvDetailAmount)
        val tvDetailNote = view.findViewById<TextView>(R.id.tvDetailNote)
        val tvDetailIndex = view.findViewById<TextView>(R.id.tvDetailIndex)
        val btnDetailClose = view.findViewById<ImageView>(R.id.btnDetailClose)
        val ivCategoryTagIcon = view.findViewById<ImageView>(R.id.ivCategoryTagIcon)

        btnDetailClose.setOnClickListener { dismiss() }

        val tx = transaction ?: return

        val categoryName = tx.categoryName ?: "Ăn uống"
        tvCategoryTag.text = categoryName
        val normalizedCategory = categoryName.lowercase(Locale.ROOT)
        if (normalizedCategory.contains("ăn uống") || normalizedCategory.contains("food")) {
            ivCategoryTagIcon.setImageResource(R.drawable.ic_transaction)
        } else {
            ivCategoryTagIcon.setImageResource(R.drawable.ic_credit_card)
        }

        var walletName = tx.accountName ?: "Wallet"
        if ("Ví chính".equals(walletName, ignoreCase = true)) {
            walletName = "Wallet"
        }
        tvWalletTag.text = walletName

        val amount = tx.amount
        val sign = if ("EXPENSE".equals(tx.transactionType, ignoreCase = true) || amount < 0) "- " else "+ "
        val formattedAmount = sign + CurrencyFormatter.formatVND(abs(amount)).replace("-", "").replace("+", "")
        tvDetailAmount.text = formattedAmount

        val noteText = if (!tx.note.isNullOrEmpty()) tx.note else tx.title
        tvDetailNote.text = noteText

        tvDetailIndex.text = getString(R.string.format_photo_index, position + 1, totalCount)

        var dateStr = tx.transactionDate ?: ""
        var timeStr = "18:38"
        try {
            if (dateStr.contains("T")) {
                val parts = dateStr.split("T")
                dateStr = parts[0]
                if (parts.size > 1 && parts[1].length >= 5) {
                    timeStr = parts[1].substring(0, 5)
                }
            }
            val date = DateUtils.parseApiDate(dateStr)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = cal.get(Calendar.MONTH) + 1
                val year = cal.get(Calendar.YEAR)
                val displayDateTime = "lúc $timeStr ngày $day tháng $month, $year"
                tvDetailDateTime.text = displayDateTime
            }
        } catch (ignored: Exception) {}

        loadImage(tx.imageUrl, ivDetailPhoto)
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

    private fun loadImage(relativeUrl: String?, imageView: ImageView) {
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
}
