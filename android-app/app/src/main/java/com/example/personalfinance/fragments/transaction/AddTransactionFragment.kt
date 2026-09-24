package com.example.personalfinance.fragments.transaction

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.personalfinance.R
import com.example.personalfinance.activities.MainActivity
import com.example.personalfinance.activities.ScanBillActivity
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.FragmentAddTransactionBinding
import com.example.personalfinance.models.Account
import com.example.personalfinance.models.ApiResponse
import com.example.personalfinance.models.Budget
import com.example.personalfinance.models.Category
import com.example.personalfinance.models.Transaction
import com.example.personalfinance.models.User
import com.example.personalfinance.repositories.BudgetRepository
import com.example.personalfinance.utils.Constants
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.DateUtils
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.TransactionViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import kotlin.math.roundToLong

class AddTransactionFragment : DialogFragment() {

    fun interface OnTransactionSavedListener {
        fun onTransactionSaved()
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 2001
        private const val REQUEST_IMAGE_PICK = 2002
        private const val PERMISSION_CAMERA_CODE = 2003

        @JvmStatic
        fun newInstance(
            transactionId: Int,
            amount: Double,
            title: String,
            categoryId: Int,
            date: String,
            aiScanLogId: Int,
            imageUrl: String?
        ): AddTransactionFragment {
            val fragment = AddTransactionFragment()
            val args = Bundle().apply {
                putInt("transactionId", transactionId)
                putDouble("amount", amount)
                putString("title", title)
                putInt("categoryId", categoryId)
                putString("date", date)
                putInt("aiScanLogId", aiScanLogId)
                putString("imageUrl", imageUrl)
            }
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(
            amount: Double,
            title: String,
            categoryId: Int,
            date: String,
            aiScanLogId: Int
        ): AddTransactionFragment {
            return newInstance(0, amount, title, categoryId, date, aiScanLogId, null)
        }

        @JvmStatic
        fun newInstance(
            amount: Double,
            title: String,
            categoryId: Int,
            date: String,
            aiScanLogId: Int,
            localImageUri: String?
        ): AddTransactionFragment {
            val fragment = AddTransactionFragment()
            val args = Bundle().apply {
                putInt("transactionId", 0)
                putDouble("amount", amount)
                putString("title", title)
                putInt("categoryId", categoryId)
                putString("date", date)
                putInt("aiScanLogId", aiScanLogId)
                putString("imageUri", localImageUri)
            }
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstanceForProductScan(
            amount: Double,
            title: String,
            categoryId: Int,
            date: String,
            aiProductLogId: Int,
            localImageUri: String?
        ): AddTransactionFragment {
            val fragment = AddTransactionFragment()
            val args = Bundle().apply {
                putInt("transactionId", 0)
                putDouble("amount", amount)
                putString("title", title)
                putInt("categoryId", categoryId)
                putString("date", date)
                putInt("aiProductLogId", aiProductLogId)
                putString("imageUri", localImageUri)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var onTransactionSavedListener: OnTransactionSavedListener? = null

    fun setOnTransactionSavedListener(listener: OnTransactionSavedListener) {
        this.onTransactionSavedListener = listener
    }

    private var transactionId = 0
    private var existingImageUrl: String? = null
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel
    private var currentUser: User? = null
    private val calendar: Calendar = Calendar.getInstance()

    private var allAccounts = mutableListOf<Account>()
    private var allCategories = mutableListOf<Category>()
    private val filteredCategories = mutableListOf<Category>()

    private var selectedDateStr: String = ""

    private var capturedImageUri: Uri? = null
    private var selectedImageUri: Uri? = null
    private var selectedImageBytes: ByteArray? = null
    private var selectedImageMimeType: String? = null
    private var savedTransactionForWarning: Transaction? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser()
        if (currentUser == null) return

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        // Set default date / read from arguments
        val args = arguments
        if (args != null) {
            transactionId = args.getInt("transactionId", 0)
            existingImageUrl = args.getString("imageUrl", null)
            val amount = args.getDouble("amount", 0.0)
            val title = args.getString("title", "")
            var dateArg = args.getString("date")
            if (dateArg.isNullOrEmpty()) {
                dateArg = args.getString("prefilled_date")
            }
            selectedDateStr = if (!dateArg.isNullOrEmpty()) dateArg else DateUtils.getCurrentDateString()

            if (amount > 0) {
                binding.edtAmount.setText(amount.toString())
            }
            if (!title.isNullOrEmpty()) {
                binding.edtTitle.setText(title)
            }
            if (transactionId > 0) {
                binding.tvScreenTitle.setText(R.string.label_transaction_details)
                binding.btnSave.setText(R.string.label_update_transaction)
            }
            if (!existingImageUrl.isNullOrEmpty()) {
                val baseUrl = RetrofitClient.getClient().baseUrl().toString()
                val path = if (existingImageUrl!!.startsWith("/")) existingImageUrl!!.substring(1) else existingImageUrl!!
                loadNetworkImage(baseUrl + path)
            }
            val localImageUriStr = args.getString("imageUri", null)
            if (!localImageUriStr.isNullOrEmpty()) {
                val uri = Uri.parse(localImageUriStr)
                selectedImageUri = uri
                cacheSelectedImageForUpload(uri)
            }
        } else {
            selectedDateStr = DateUtils.getCurrentDateString()
        }
        binding.edtDate.setText(DateUtils.formatDateForDisplay(selectedDateStr))

        // Date selection
        binding.edtDate.setOnClickListener { showDatePicker() }

        // Cancel button
        binding.btnCancel.setOnClickListener { dismiss() }

        // Save button
        binding.btnSave.setOnClickListener { saveTransaction() }

        // Retake and Share buttons (Screen 7 actions)
        binding.btnRetake.setOnClickListener {
            dismiss()
            startActivity(Intent(requireContext(), ScanBillActivity::class.java))
        }
        binding.btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "Chia sẻ giao dịch này...", Toast.LENGTH_SHORT).show()
        }

        // Focus and Keyboard handling
        binding.edtAmount.postDelayed({
            if (_binding != null && isAdded) {
                binding.edtAmount.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(binding.edtAmount, InputMethodManager.SHOW_IMPLICIT)
            }
        }, 300)

        // Dynamic VND formatting preview
        binding.edtAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val cleanString = s?.toString()?.trim() ?: ""
                if (cleanString.isNotEmpty()) {
                    try {
                        val parsed = cleanString.toDouble()
                        binding.tvFormattedAmount.text = CurrencyFormatter.formatVND(parsed)
                        binding.tvFormattedAmount.visibility = View.VISIBLE
                    } catch (e: NumberFormatException) {
                        binding.tvFormattedAmount.visibility = View.GONE
                    }
                } else {
                    binding.tvFormattedAmount.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val initialAmount = binding.edtAmount.text.toString().trim()
        if (initialAmount.isNotEmpty()) {
            try {
                val parsed = initialAmount.toDouble()
                binding.tvFormattedAmount.text = CurrencyFormatter.formatVND(parsed)
                binding.tvFormattedAmount.visibility = View.VISIBLE
            } catch (ignored: NumberFormatException) {}
        }

        // Image attachment handlers
        binding.btnAddImage.setOnClickListener { showImageSourceSelector() }
        binding.ivThumbnail.setOnClickListener { showImageSourceSelector() }
        binding.btnRemoveImage.setOnClickListener { removeImage() }

        // Determine if we are in Manual mode vs scan confirmation mode
        var isManualMode = true
        if (args != null) {
            val aiScanLogId = args.getInt("aiScanLogId", 0)
            if (aiScanLogId > 0) {
                isManualMode = false
            }
        }

        if (isManualMode) {
            binding.btnRetake.visibility = View.GONE
            binding.btnShare.visibility = View.GONE
        } else {
            binding.btnRetake.visibility = View.VISIBLE
            binding.btnShare.visibility = View.VISIBLE
        }

        binding.rgType.setOnCheckedChangeListener { _, _ ->
            updateTypeSelectorStyles()
            filterCategories()
        }
        updateTypeSelectorStyles()

        observeViewModel()

        // Hide wallet spinner and expand category spinner to take full width
        binding.spAccount?.let { spAccount ->
            val accountParent = spAccount.parent as? View
            accountParent?.visibility = View.GONE
        }
        binding.spCategory?.let { spCategory ->
            val categoryParent = spCategory.parent as? View
            categoryParent?.let { parent ->
                val layoutParams = parent.layoutParams
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.rightMargin = 0
                }
                if (layoutParams is android.widget.LinearLayout.LayoutParams) {
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.weight = 0f
                    parent.layoutParams = layoutParams
                }
            }
        }

        val userId = currentUser?.userId ?: return
        viewModel.loadFormData(userId)
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            selectedDateStr = DateUtils.formatApiDate(calendar.time)
            binding.edtDate.setText(DateUtils.formatDateForDisplay(selectedDateStr))
        }

        val dialog = DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            if (accounts != null) {
                allAccounts.clear()
                allAccounts.addAll(accounts)
                populateAccountsSpinner()
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (categories != null) {
                allCategories.clear()
                allCategories.addAll(categories)

                if (arguments != null && requireArguments().containsKey("categoryId")) {
                    val preSelectedId = requireArguments().getInt("categoryId")
                    for (cat in allCategories) {
                        if (cat.categoryId == preSelectedId) {
                            if (Constants.TYPE_INCOME.equals(cat.categoryType, ignoreCase = true)) {
                                binding.rbIncome.isChecked = true
                            } else {
                                binding.rbExpense.isChecked = true
                            }
                            break
                        }
                    }
                }

                filterCategories()
            }
        }

        viewModel.transactionCreated.observe(viewLifecycleOwner) { transaction ->
            if (transaction != null) {
                savedTransactionForWarning = transaction
                if (selectedImageUri != null) {
                    uploadImageAndFinish(transaction)
                } else {
                    completeSaveTransactionWithBudgetWarning()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateAccountsSpinner() {
        val names = allAccounts.map { "${it.accountName} (${CurrencyFormatter.formatVND(it.balance)})" }
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, names)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.spAccount.adapter = adapter
    }

    private fun updateTypeSelectorStyles() {
        if (context == null) return
        val isExpense = binding.rbExpense.isChecked

        if (isExpense) {
            binding.rbExpense.setBackgroundResource(R.drawable.bg_button_rounded)
            binding.rbExpense.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.expense_red)
            binding.rbExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.rbIncome.background = null
            binding.rbIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        } else {
            binding.rbIncome.setBackgroundResource(R.drawable.bg_button_rounded)
            binding.rbIncome.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.income_green)
            binding.rbIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.rbExpense.background = null
            binding.rbExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun filterCategories() {
        val selectedType = if (binding.rbIncome.isChecked) Constants.TYPE_INCOME else Constants.TYPE_EXPENSE

        filteredCategories.clear()
        val names = mutableListOf<String>()
        for (cat in allCategories) {
            if (selectedType.equals(cat.categoryType, ignoreCase = true)) {
                filteredCategories.add(cat)
                names.add(cat.categoryName ?: "")
            }
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, names)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.spCategory.adapter = adapter

        if (arguments != null && requireArguments().containsKey("categoryId")) {
            val preSelectedId = requireArguments().getInt("categoryId")
            for (i in filteredCategories.indices) {
                if (filteredCategories[i].categoryId == preSelectedId) {
                    binding.spCategory.setSelection(i)
                    break
                }
            }
        }
    }

    private fun saveTransaction() {
        val title = binding.edtTitle.text.toString().trim()
        val amountText = binding.edtAmount.text.toString().trim()
        val note = binding.edtNote.text.toString().trim()

        if (allAccounts.isEmpty()) {
            Toast.makeText(requireContext(), "Không có tài khoản nào được chọn", Toast.LENGTH_SHORT).show()
            return
        }

        if (filteredCategories.isEmpty()) {
            Toast.makeText(requireContext(), "Không có danh mục nào được chọn", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty()) {
            binding.edtTitle.error = "Vui lòng nhập tên giao dịch"
            binding.edtTitle.requestFocus()
            return
        }

        if (amountText.isEmpty()) {
            binding.edtAmount.error = "Vui lòng nhập số tiền"
            binding.edtAmount.requestFocus()
            return
        }

        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            binding.edtAmount.error = "Số tiền không hợp lệ"
            binding.edtAmount.requestFocus()
            return
        }

        if (isSelectedDateInFuture()) {
            binding.edtDate.error = "Không thể tạo giao dịch cho ngày trong tương lai"
            Toast.makeText(requireContext(), "Không thể tạo giao dịch cho ngày trong tương lai", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryIdx = binding.spCategory.selectedItemPosition
        if (categoryIdx < 0 || categoryIdx >= filteredCategories.size) return

        val selectedCategory = filteredCategories[categoryIdx]
        val selectedType = if (binding.rbIncome.isChecked) Constants.TYPE_INCOME else Constants.TYPE_EXPENSE

        binding.btnSave.isEnabled = false

        val transaction = Transaction().apply {
            this.userId = currentUser?.userId
            this.accountId = null
            this.categoryId = selectedCategory.categoryId
            this.title = title
            this.amount = amount
            this.transactionType = selectedType
            this.transactionDate = selectedDateStr
            this.note = note
            this.status = "completed"
            if (this@AddTransactionFragment.transactionId > 0) {
                this.transactionId = this@AddTransactionFragment.transactionId
            }
        }

        var aiScanLogId = 0
        if (arguments != null && requireArguments().containsKey("aiScanLogId")) {
            aiScanLogId = requireArguments().getInt("aiScanLogId")
        }

        var aiProductLogId = 0
        if (arguments != null && requireArguments().containsKey("aiProductLogId")) {
            aiProductLogId = requireArguments().getInt("aiProductLogId")
        }

        val selectedCategoryId = selectedCategory.categoryId ?: 0
        if (transactionId > 0) {
            viewModel.updateTransaction(transactionId, transaction)
        } else {
            viewModel.createTransaction(transaction, aiScanLogId, aiProductLogId, selectedCategoryId)
        }
    }

    private fun isSelectedDateInFuture(): Boolean {
        return try {
            val selectedDate = DateUtils.parseApiDate(selectedDateStr)
            val today = DateUtils.parseApiDate(DateUtils.getCurrentDateString())
            selectedDate != null && today != null && selectedDate.after(today)
        } catch (e: Exception) {
            true
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun showImageSourceSelector() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_image_picker)

        val btnCamera = bottomSheetDialog.findViewById<View>(R.id.btnPickerCamera)
        val btnGallery = bottomSheetDialog.findViewById<View>(R.id.btnPickerGallery)

        btnCamera?.setOnClickListener {
            bottomSheetDialog.dismiss()
            openCamera()
        }

        btnGallery?.setOnClickListener {
            bottomSheetDialog.dismiss()
            openGallery()
        }

        bottomSheetDialog.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), PERMISSION_CAMERA_CODE)
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        capturedImageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (capturedImageUri == null) {
            Toast.makeText(requireContext(), "Không tạo được file ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)
        }
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Quyền Camera bị từ chối!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                capturedImageUri?.let { uri ->
                    selectedImageUri = uri
                    cacheSelectedImageForUpload(uri)
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data?.data != null) {
                val uri = data.data!!
                selectedImageUri = uri
                persistGalleryReadPermission(uri)
                cacheSelectedImageForUpload(uri)
            }
        }
    }

    private fun persistGalleryReadPermission(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (ignored: SecurityException) {}
    }

    private fun cacheSelectedImageForUpload(uri: Uri) {
        try {
            val bytes = readImageBytes(uri)
            if (bytes.isEmpty()) {
                removeImage()
                Toast.makeText(requireContext(), "Ảnh không có dữ liệu, vui lòng chọn ảnh khác", Toast.LENGTH_SHORT).show()
                return
            }
            selectedImageBytes = bytes

            var mime = requireContext().contentResolver.getType(uri)
            if (mime.isNullOrBlank()) {
                mime = "image/jpeg"
            }
            selectedImageMimeType = mime

            displayThumbnail(uri)
        } catch (e: Exception) {
            removeImage()
            Toast.makeText(requireContext(), "Lỗi đọc ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayThumbnail(uri: Uri) {
        val b = _binding ?: return
        b.ivThumbnail.setImageURI(uri)
        b.layoutImagePreview.visibility = View.VISIBLE
        b.btnAddImage.visibility = View.GONE
    }

    private fun removeImage() {
        selectedImageUri = null
        capturedImageUri = null
        selectedImageBytes = null
        selectedImageMimeType = null
        existingImageUrl = null
        val b = _binding ?: return
        b.layoutImagePreview.visibility = View.GONE
        b.btnAddImage.visibility = View.VISIBLE
        b.ivThumbnail.setImageURI(null)
    }

    private fun uploadImageAndFinish(transaction: Transaction) {
        try {
            val txId = transaction.transactionId
            if (txId == null || txId <= 0) {
                Toast.makeText(requireContext(), "Không lấy được mã giao dịch để lưu ảnh", Toast.LENGTH_SHORT).show()
                completeSaveTransactionWithBudgetWarning()
                return
            }

            val bytes = selectedImageBytes
            if (bytes == null || bytes.isEmpty()) {
                completeSaveTransactionWithBudgetWarning()
                return
            }

            val mimeType = if (!selectedImageMimeType.isNullOrBlank()) selectedImageMimeType!! else "image/jpeg"
            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData(
                "file",
                buildImageFileName(txId, mimeType),
                requestFile
            )

            binding.btnSave.isEnabled = false
            Toast.makeText(requireContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()

            RetrofitClient.getApiService().uploadTransactionImage(txId, body)
                .enqueue(object : Callback<ApiResponse<Void>> {
                    override fun onResponse(call: Call<ApiResponse<Void>>, response: Response<ApiResponse<Void>>) {
                        val apiResponse = response.body()
                        if (response.isSuccessful && apiResponse != null && apiResponse.isSuccess) {
                            Toast.makeText(requireContext(), "Đính kèm ảnh thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Lỗi tải ảnh lên server", Toast.LENGTH_SHORT).show()
                        }
                        completeSaveTransactionWithBudgetWarning()
                    }

                    override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                        Toast.makeText(requireContext(), "Lỗi kết nối khi tải ảnh: ${t.message}", Toast.LENGTH_SHORT).show()
                        completeSaveTransactionWithBudgetWarning()
                    }
                })

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi đọc file ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            completeSaveTransactionWithBudgetWarning()
        }
    }

    private fun getOrientation(uri: Uri): Int {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        } catch (ignored: Exception) {}

        try {
            val projection = arrayOf(MediaStore.Images.ImageColumns.ORIENTATION)
            requireContext().contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val colIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
                    if (colIndex != -1) {
                        return cursor.getInt(colIndex)
                    }
                }
            }
        } catch (ignored: Exception) {}

        return 0
    }

    private fun readImageBytes(uri: Uri): ByteArray {
        val rotation = getOrientation(uri)
        if (rotation == 0) {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val byteBuffer = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    byteBuffer.write(buffer, 0, len)
                }
                return byteBuffer.toByteArray()
            } ?: return ByteArray(0)
        } else {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return ByteArray(0)
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap, 0, 0,
                    originalBitmap.width, originalBitmap.height,
                    matrix, true
                )
                val bos = ByteArrayOutputStream()
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
                originalBitmap.recycle()
                rotatedBitmap.recycle()
                return bos.toByteArray()
            } ?: return ByteArray(0)
        }
    }

    private fun buildImageFileName(transactionId: Int, mimeType: String): String {
        val extension = when {
            "image/png".equals(mimeType, ignoreCase = true) -> ".png"
            "image/webp".equals(mimeType, ignoreCase = true) -> ".webp"
            else -> ".jpg"
        }
        return "transaction_${transactionId}$extension"
    }

    private fun completeSaveTransactionWithBudgetWarning() {
        if (_binding != null) {
            binding.btnSave.isEnabled = true
        }
        Toast.makeText(requireContext(), "Lưu giao dịch thành công!", Toast.LENGTH_SHORT).show()

        if (!maybeShowBudgetWarning()) {
            finishSaveTransaction()
        }
    }

    private fun maybeShowBudgetWarning(): Boolean {
        val transaction = savedTransactionForWarning
        if (transaction == null
            || !Constants.TYPE_EXPENSE.equals(transaction.transactionType, ignoreCase = true)
            || transaction.categoryId == null
            || transaction.transactionDate == null
        ) {
            return false
        }

        val userId = currentUser?.userId ?: return false
        BudgetRepository().getBudgets(userId, object : BudgetRepository.ApiCallback<List<Budget>> {
            override fun onSuccess(result: List<Budget>?) {
                val warningBudget = findWarningBudget(result, transaction)
                if (warningBudget == null) {
                    finishSaveTransaction()
                    return
                }

                if (warningBudget.percentUsed > 100) {
                    showBudgetExceededDialog(warningBudget)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.budget_approaching_limit_warning, warningBudget.percentUsed.roundToLong(), getBudgetDisplayName(warningBudget)),
                        Toast.LENGTH_LONG
                    ).show()
                    finishSaveTransaction()
                }
            }

            override fun onError(errorMessage: String) {
                finishSaveTransaction()
            }
        })
        return true
    }

    private fun findWarningBudget(budgets: List<Budget>?, transaction: Transaction): Budget? {
        if (budgets == null) return null

        var highest: Budget? = null
        for (budget in budgets) {
            if (budget.amountLimit <= 0) continue
            if (!isBudgetForTransaction(budget, transaction)) continue
            if (budget.percentUsed < 80) continue

            if (highest == null || budget.percentUsed > highest.percentUsed) {
                highest = budget
            }
        }
        return highest
    }

    private fun isBudgetForTransaction(budget: Budget, transaction: Transaction): Boolean {
        val budgetCategoryId = budget.categoryId
        if (budgetCategoryId != null && budgetCategoryId != transaction.categoryId) {
            return false
        }

        val txDate = transaction.transactionDate ?: return false
        val startDate = normalizeDateOnly(budget.startDate)
        val endDate = normalizeDateOnly(budget.endDate)
        return (startDate == null || txDate >= startDate) && (endDate == null || txDate <= endDate)
    }

    private fun normalizeDateOnly(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return if (value.contains("T")) value.split("T")[0] else value
    }

    private fun showBudgetExceededDialog(budget: Budget) {
        if (!isAdded || context == null) {
            finishSaveTransaction()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_limit_warning, null, false)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)

        val overAmount = (budget.spentAmount - budget.amountLimit).coerceAtLeast(0.0)
        dialogView.findViewById<TextView>(R.id.tvCategoryAlertName).text = getBudgetDisplayName(budget)
        dialogView.findViewById<TextView>(R.id.tvLimitVal).text = CurrencyFormatter.formatVND(budget.amountLimit)
        dialogView.findViewById<TextView>(R.id.tvSpentVal).text = CurrencyFormatter.formatVND(budget.spentAmount)
        dialogView.findViewById<TextView>(R.id.tvOverVal).text = CurrencyFormatter.formatVND(overAmount)
        dialogView.findViewById<TextView>(R.id.tvOverPercentVal).text =
            getString(R.string.percentage_format, budget.percentUsed.roundToLong())

        dialogView.findViewById<View>(R.id.btnDismiss).setOnClickListener {
            dialog.dismiss()
            finishSaveTransaction()
        }
        dialogView.findViewById<View>(R.id.btnViewDetails).setOnClickListener {
            dialog.dismiss()
            onTransactionSavedListener?.onTransactionSaved()
            (activity as? MainActivity)?.setSelectedTab(R.id.nav_budget)
            dismiss()
        }
        dialog.setOnCancelListener { finishSaveTransaction() }
        dialog.show()
    }

    private fun getBudgetDisplayName(budget: Budget): String {
        if (!budget.categoryName.isNullOrBlank()) {
            return budget.categoryName!!
        }
        if (!budget.budgetName.isNullOrBlank()) {
            return budget.budgetName!!
        }
        return getString(R.string.budget_fallback_display_name)
    }

    private fun finishSaveTransaction() {
        if (onTransactionSavedListener != null) {
            onTransactionSavedListener?.onTransactionSaved()
        } else {
            (activity as? MainActivity)?.setSelectedTab(R.id.nav_home)
        }

        val act = activity
        if (act != null && act !is MainActivity) {
            act.finish()
        }
        dismiss()
    }

    private fun loadNetworkImage(url: String) {
        Thread {
            try {
                Log.d("AddTransactionFragment", "Loading image from URL: $url")
                val imageUrl = URL(url)
                val conn = (imageUrl.openConnection() as HttpURLConnection).apply {
                    doInput = true
                    setRequestProperty("ngrok-skip-browser-warning", "true")
                    setRequestProperty("User-Agent", "Android-App")
                    connect()
                }

                val responseCode = conn.responseCode
                Log.d("AddTransactionFragment", "HTTP Response code: $responseCode")

                if (responseCode in 200..299) {
                    val inputStream: InputStream = conn.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap == null) {
                        Log.e("AddTransactionFragment", "Failed to decode bitmap from stream!")
                    } else {
                        Log.d("AddTransactionFragment", "Successfully decoded bitmap! Size: ${bitmap.width}x${bitmap.height}")
                    }
                    activity?.runOnUiThread {
                        if (_binding != null && bitmap != null) {
                            binding.ivThumbnail.setImageBitmap(bitmap)
                            binding.layoutImagePreview.visibility = View.VISIBLE
                            binding.btnAddImage.visibility = View.GONE
                        }
                    }
                } else {
                    Log.e("AddTransactionFragment", "Failed to load image, HTTP response: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("AddTransactionFragment", "Error loading image: ", e)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
