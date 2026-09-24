package com.example.personalfinance.fragments.transaction

import android.content.Context
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
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinance.R
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.models.Transaction
import com.example.personalfinance.utils.CurrencyFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

class DayTransactionsBottomSheet : BottomSheetDialogFragment() {

    private var dayTitle: String = ""
    private var transactions: List<Transaction> = emptyList()
    private var totalAmount: Double = 0.0

    companion object {
        private val imageCache = LruCache<String, Bitmap>(20)

        @JvmStatic
        fun newInstance(dayTitle: String, transactions: List<Transaction>?, totalAmount: Double): DayTransactionsBottomSheet {
            val fragment = DayTransactionsBottomSheet()
            fragment.dayTitle = dayTitle
            fragment.transactions = transactions ?: emptyList()
            fragment.totalAmount = totalAmount
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_day_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvDayTitle = view.findViewById<TextView>(R.id.tvDayTitle)
        val tvTransactionCount = view.findViewById<TextView>(R.id.tvTransactionCount)
        val tvDayIncome = view.findViewById<TextView>(R.id.tvDayIncome)
        val tvDayExpense = view.findViewById<TextView>(R.id.tvDayExpense)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val rvDayTransactions = view.findViewById<RecyclerView>(R.id.rvDayTransactions)

        tvDayTitle.text = dayTitle

        // Filter transactions that have valid images
        val photoTxs = transactions.filter { !it.imageUrl.isNullOrBlank() }

        tvTransactionCount.text = getString(R.string.format_fraction, photoTxs.size, photoTxs.size)

        // Calculate dynamic income and expense totals for the day
        var totalIncome = 0.0
        var totalExpense = 0.0
        for (t in transactions) {
            val amt = t.amount
            val isExpense = "EXPENSE".equals(t.transactionType, ignoreCase = true) || amt < 0
            if (isExpense) {
                totalExpense += abs(amt)
            } else {
                totalIncome += abs(amt)
            }
        }

        if (totalIncome > 0) {
            tvDayIncome.text = getString(R.string.format_income_amount, CurrencyFormatter.formatVND(totalIncome))
            tvDayIncome.visibility = View.VISIBLE
        } else {
            tvDayIncome.visibility = View.GONE
        }

        if (totalExpense > 0) {
            tvDayExpense.text = getString(R.string.format_expense_amount, CurrencyFormatter.formatVND(totalExpense))
            tvDayExpense.visibility = View.VISIBLE
        } else {
            tvDayExpense.visibility = View.GONE
        }

        // Fallback if both are 0
        if (totalIncome == 0.0 && totalExpense == 0.0) {
            tvDayExpense.text = getString(R.string.format_expense_amount, CurrencyFormatter.formatVND(0.0))
            tvDayExpense.visibility = View.VISIBLE
        }

        btnClose.setOnClickListener { dismiss() }

        // Horizontal LinearLayoutManager for cards scroll matching Screen 6
        rvDayTransactions.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDayTransactions.adapter = DayTransactionsAdapter(requireContext(), photoTxs)
    }

    private class DayTransactionsAdapter(
        private val context: Context,
        private val list: List<Transaction>
    ) : RecyclerView.Adapter<DayTransactionsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_day_transaction_card, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tx = list[position]
            holder.tvCardCategory.text = tx.categoryName ?: context.getString(R.string.ui_an_uong)

            val amount = tx.amount
            if ("EXPENSE".equals(tx.transactionType, ignoreCase = true) || amount < 0) {
                holder.tvCardAmount.text = context.getString(R.string.format_negative_amount, CurrencyFormatter.formatVND(abs(amount)))
            } else {
                holder.tvCardAmount.text = context.getString(R.string.format_positive_amount, CurrencyFormatter.formatVND(amount))
            }

            // Load transaction image dynamically with cache
            loadImage(tx.imageUrl, holder.ivCardBackground)

            // Card click launches TransactionPhotoDetailDialog matching Screen 7
            holder.itemView.setOnClickListener {
                val dialog = TransactionPhotoDetailDialog.newInstance(tx, position, list.size)
                (context as? FragmentActivity)?.supportFragmentManager?.let { fm ->
                    dialog.show(fm, "TransactionPhotoDetailDialog")
                }
            }
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

            val baseUrl = RetrofitClient.getClient().baseUrl().toString()
            val path = if (relativeUrl.startsWith("/")) relativeUrl.substring(1) else relativeUrl
            val fullUrl = baseUrl + path

            val cached = imageCache.get(fullUrl)
            if (cached != null) {
                imageView.setImageBitmap(cached)
                return
            }

            imageView.tag = fullUrl
            Thread {
                try {
                    val url = URL(fullUrl)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        doInput = true
                        setRequestProperty("ngrok-skip-browser-warning", "true")
                        setRequestProperty("User-Agent", "Android-App")
                        connect()
                    }

                    if (conn.responseCode == 200) {
                        val inputStream: InputStream = conn.inputStream
                        val bos = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (inputStream.read(buffer).also { len = it } != -1) {
                            bos.write(buffer, 0, len)
                        }
                        val bytes = bos.toByteArray()

                        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            bitmap = rotateBitmapIfNeeded(bitmap, bytes)
                            imageCache.put(fullUrl, bitmap)
                            Handler(Looper.getMainLooper()).post {
                                if (fullUrl == imageView.tag) {
                                    imageView.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                } catch (ignored: Exception) {}
            }.start()
        }

        override fun getItemCount(): Int = list.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivCardBackground: ImageView = itemView.findViewById(R.id.ivCardBackground)
            val tvCardCategory: TextView = itemView.findViewById(R.id.tvCardCategory)
            val tvCardAmount: TextView = itemView.findViewById(R.id.tvCardAmount)
        }
    }
}
