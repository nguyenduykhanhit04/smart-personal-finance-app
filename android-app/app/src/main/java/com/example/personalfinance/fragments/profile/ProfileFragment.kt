package com.example.personalfinance.fragments.profile

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.personalfinance.R
import com.example.personalfinance.activities.LoginActivity
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.FragmentProfileBinding
import com.example.personalfinance.models.dto.ApiResponse
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.utils.CurrencyFormatter
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import kotlin.concurrent.thread

class ProfileFragment : Fragment() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val PERMISSION_CAMERA_CODE = 100
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private var currentUser: User? = null
    private var capturedImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = SharedPrefManager.getInstance(requireContext()).user ?: return

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Set User details
        binding.tvUserName.text = currentUser?.fullName
        binding.tvUserEmail.text = currentUser?.email

        // Load Avatar
        loadAvatar(currentUser?.avatarUrl)

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        binding.tvUserDate.text = getString(R.string.format_profile_month_year, month, year)

        // Setup Option Click Listeners
        binding.btnEditProfile.setOnClickListener { showImageSourceSelector() }
        binding.flAvatarContainer.setOnClickListener { showImageSourceSelector() }
        binding.tvUserName.setOnClickListener { showEditNameDialog() }
        binding.btnChangePassword.setOnClickListener { showChangePasswordDialog() }
        binding.btnLogoutRow.setOnClickListener { handleLogout() }
        binding.btnLogout.setOnClickListener { handleLogout() }

        observeViewModel()
        loadMetrics(month, year)
    }

    private fun loadAvatar(relativeUrl: String?) {
        if (relativeUrl.isNullOrBlank()) {
            binding.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
            return
        }

        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            loadImageFromUrl(relativeUrl)
            return
        }

        val baseUrl = RetrofitClient.client.baseUrl().toString()
        val path = if (relativeUrl.startsWith("/")) relativeUrl.substring(1) else relativeUrl
        val fullUrl = baseUrl + path
        loadImageFromUrl(fullUrl)
    }

    private fun loadImageFromUrl(fullUrl: String) {
        binding.imgAvatar.tag = fullUrl
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

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        Handler(Looper.getMainLooper()).post {
                            if (fullUrl == binding.imgAvatar.tag && _binding != null) {
                                binding.imgAvatar.setImageBitmap(bitmap)
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {}
        }
    }

    private fun showImageSourceSelector() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            setContentView(R.layout.bottom_sheet_image_picker)
        }

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
            put(MediaStore.Images.Media.TITLE, "New Profile Picture")
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
        val intent = Intent(Intent.ACTION_PICK).apply {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            val selectedUri: Uri? = when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> capturedImageUri
                REQUEST_IMAGE_PICK -> data?.data
                else -> null
            }

            selectedUri?.let { uploadAvatarImage(it) }
        }
    }

    private fun uploadAvatarImage(uri: Uri) {
        val user = currentUser ?: return
        try {
            val imageBytes = readImageBytes(uri)
            if (imageBytes.isEmpty()) {
                Toast.makeText(requireContext(), "Lỗi đọc file ảnh!", Toast.LENGTH_SHORT).show()
                return
            }

            val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "avatar_${user.userId}.jpg", requestFile)

            Toast.makeText(requireContext(), "Đang tải ảnh đại diện lên...", Toast.LENGTH_SHORT).show()

            RetrofitClient.apiService.uploadAvatar(user.userId, body)
                .enqueue(object : Callback<ApiResponse<User>> {
                    override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                        val apiResponse = response.body()
                        if (response.isSuccessful && apiResponse?.isSuccess == true) {
                            val updatedUser = apiResponse.data
                            if (updatedUser != null) {
                                SharedPrefManager.getInstance(requireContext()).saveUser(updatedUser)
                                currentUser = updatedUser
                                loadAvatar(updatedUser.avatarUrl)
                                Toast.makeText(requireContext(), "Đổi ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Lỗi tải ảnh lên server!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                        Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun readImageBytes(uri: Uri): ByteArray {
        val rotation = getOrientation(uri)
        return if (rotation == 0) {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val byteBuffer = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    byteBuffer.write(buffer, 0, len)
                }
                byteBuffer.toByteArray()
            } ?: ByteArray(0)
        } else {
            requireContext().contentResolver.openInputStream(uri)?.use { isStream ->
                val originalBitmap = BitmapFactory.decodeStream(isStream) ?: return ByteArray(0)
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
                bos.toByteArray()
            } ?: ByteArray(0)
        }
    }

    private fun getOrientation(uri: Uri): Int {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { isStream ->
                val exif = ExifInterface(isStream)
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
            val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
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

    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null)
        builder.setView(view)

        val edtNewName = view.findViewById<EditText>(R.id.edtNewName)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val btnSave = view.findViewById<View>(R.id.btnSave)

        edtNewName.setText(currentUser?.fullName)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val newName = edtNewName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Tên không được để trống!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            updateNameOnServer(newName)
        }

        dialog.show()
    }

    private fun updateNameOnServer(newName: String) {
        val user = currentUser ?: return
        val userUpdate = User(fullName = newName)

        Toast.makeText(requireContext(), "Đang cập nhật tên...", Toast.LENGTH_SHORT).show()

        RetrofitClient.apiService.updateUser(user.userId, userUpdate)
            .enqueue(object : Callback<ApiResponse<User>> {
                override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                    val apiResponse = response.body()
                    if (response.isSuccessful && apiResponse?.isSuccess == true) {
                        val updatedUser = apiResponse.data
                        if (updatedUser != null) {
                            SharedPrefManager.getInstance(requireContext()).saveUser(updatedUser)
                            currentUser = updatedUser
                            binding.tvUserName.text = updatedUser.fullName
                            Toast.makeText(requireContext(), "Cập nhật tên thành công!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Cập nhật tên thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        builder.setView(view)

        val edtCurrentPassword = view.findViewById<EditText>(R.id.edtCurrentPassword)
        val edtNewPassword = view.findViewById<EditText>(R.id.edtNewPassword)
        val edtConfirmPassword = view.findViewById<EditText>(R.id.edtConfirmPassword)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val btnUpdate = view.findViewById<View>(R.id.btnUpdate)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnUpdate.setOnClickListener {
            val currentPw = edtCurrentPassword.text.toString().trim()
            val newPw = edtNewPassword.text.toString().trim()
            val confirmPw = edtConfirmPassword.text.toString().trim()

            if (currentPw.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu hiện tại!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPw.length < 6) {
                Toast.makeText(requireContext(), "Mật khẩu mới phải chứa ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPw != confirmPw) {
                Toast.makeText(requireContext(), "Xác nhận mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            updateFirebasePassword(currentPw, newPw)
        }

        dialog.show()
    }

    private fun updateFirebasePassword(currentPassword: String, newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (user != null && email != null) {
            Toast.makeText(requireContext(), "Đang xác thực...", Toast.LENGTH_SHORT).show()
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Đang cập nhật mật khẩu...", Toast.LENGTH_SHORT).show()
                    user.updatePassword(newPassword).addOnCompleteListener { pwTask ->
                        if (pwTask.isSuccessful) {
                            Toast.makeText(requireContext(), "Cập nhật mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            val error = pwTask.exception?.message ?: "Lỗi không xác định"
                            Toast.makeText(requireContext(), "Cập nhật thất bại: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Mật khẩu hiện tại không chính xác!", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy phiên đăng nhập!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMetrics(month: Int, year: Int) {
        val user = currentUser ?: return
        homeViewModel.fetchDashboardData(user.userId, month, year)
    }

    private fun observeViewModel() {
        homeViewModel.monthlyReport.observe(viewLifecycleOwner) { report ->
            if (report != null && _binding != null) {
                binding.tvTotalIncome.text = CurrencyFormatter.formatVND(report.totalIncome)
                binding.tvTotalExpense.text = CurrencyFormatter.formatVND(report.totalExpense)
                binding.tvNetAmount.text = CurrencyFormatter.formatVND(report.netAmount)

                val color = if (report.netAmount < 0) {
                    ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_error)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.income_green)
                }
                binding.tvNetAmount.setTextColor(color)
            }
        }

        homeViewModel.monthlyTransactions.observe(viewLifecycleOwner) { list ->
            if (_binding != null) {
                binding.tvTransactionCount.text = (list?.size ?: 0).toString()
            }
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLogout() {
        FirebaseAuth.getInstance().signOut()
        SharedPrefManager.getInstance(requireContext()).clear()

        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
