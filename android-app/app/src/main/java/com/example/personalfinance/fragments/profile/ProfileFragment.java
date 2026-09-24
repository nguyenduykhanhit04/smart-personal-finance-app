package com.example.personalfinance.fragments.profile;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.personalfinance.activities.LoginActivity;
import com.example.personalfinance.R;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.FragmentProfileBinding;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_CAMERA_CODE = 100;

    private FragmentProfileBinding binding;
    private HomeViewModel homeViewModel;
    private User currentUser;
    private Uri capturedImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        // Initialize HomeViewModel to fetch monthly stats
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Set User details
        binding.tvUserName.setText(currentUser.getFullName());
        binding.tvUserEmail.setText(currentUser.getEmail());

        // Load Avatar
        loadAvatar(currentUser.getAvatarUrl());

        // Set dynamic date placeholder based on registration if any, or static current month
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        binding.tvUserDate.setText(getString(R.string.format_profile_month_year, month, year));

        // Setup Option Click Listeners
        // Avatar edit triggers
        binding.btnEditProfile.setOnClickListener(v -> showImageSourceSelector());
        binding.flAvatarContainer.setOnClickListener(v -> showImageSourceSelector());

        // Edit Name by clicking on the name text
        binding.tvUserName.setOnClickListener(v -> showEditNameDialog());

        // Change Password
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Logout hooks
        binding.btnLogoutRow.setOnClickListener(v -> handleLogout());
        binding.btnLogout.setOnClickListener(v -> handleLogout());

        // Register Observers for overview metrics
        observeViewModel();

        // Load data
        loadMetrics(month, year);
    }

    private void loadAvatar(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) {
            binding.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
            return;
        }

        // If it is a full web URL (e.g. from Google or Facebook login)
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            loadImageFromUrl(relativeUrl);
            return;
        }

        // Relative path from our server
        String baseUrl = RetrofitClient.getClient().baseUrl().toString();
        String path = relativeUrl.startsWith("/") ? relativeUrl.substring(1) : relativeUrl;
        String fullUrl = baseUrl + path;
        loadImageFromUrl(fullUrl);
    }

    private void loadImageFromUrl(String fullUrl) {
        binding.imgAvatar.setTag(fullUrl);
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(fullUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setRequestProperty("ngrok-skip-browser-warning", "true");
                conn.setRequestProperty("User-Agent", "Android-App");
                conn.connect();

                if (conn.getResponseCode() == 200) {
                    InputStream is = conn.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                    byte[] bytes = bos.toByteArray();

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (fullUrl.equals(binding.imgAvatar.getTag()) && binding != null) {
                                binding.imgAvatar.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void showImageSourceSelector() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_image_picker);

        View btnCamera = bottomSheetDialog.findViewById(R.id.btnPickerCamera);
        View btnGallery = bottomSheetDialog.findViewById(R.id.btnPickerGallery);

        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                openCamera();
            });
        }

        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                openGallery();
            });
        }

        bottomSheetDialog.show();
    }

    private void openCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, PERMISSION_CAMERA_CODE);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Profile Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        capturedImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (capturedImageUri == null) {
            Toast.makeText(requireContext(), "Không tạo được file ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Quyền Camera bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == android.app.Activity.RESULT_OK) {
            Uri selectedUri = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                selectedUri = capturedImageUri;
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                selectedUri = data.getData();
            }

            if (selectedUri != null) {
                uploadAvatarImage(selectedUri);
            }
        }
    }

    private void uploadAvatarImage(Uri uri) {
        try {
            byte[] imageBytes = readImageBytes(uri);
            if (imageBytes == null || imageBytes.length == 0) {
                Toast.makeText(requireContext(), "Lỗi đọc file ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/jpeg"),
                    imageBytes
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    "avatar_" + currentUser.getUserId() + ".jpg",
                    requestFile
            );

            Toast.makeText(requireContext(), "Đang tải ảnh đại diện lên...", Toast.LENGTH_SHORT).show();

            RetrofitClient.getApiService().uploadAvatar(currentUser.getUserId(), body)
                    .enqueue(new Callback<ApiResponse<User>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                            ApiResponse<User> apiResponse = response.body();
                            if (response.isSuccessful() && apiResponse != null && apiResponse.isSuccess()) {
                                User updatedUser = apiResponse.getData();
                                if (updatedUser != null) {
                                    SharedPrefManager.getInstance(requireContext()).saveUser(updatedUser);
                                    currentUser = updatedUser;
                                    loadAvatar(currentUser.getAvatarUrl());
                                    Toast.makeText(requireContext(), "Đổi ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(requireContext(), "Lỗi tải ảnh lên server!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                            Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] readImageBytes(Uri uri) throws IOException {
        int rotation = getOrientation(uri);
        if (rotation == 0) {
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
                if (inputStream == null) {
                    return new byte[0];
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                return byteBuffer.toByteArray();
            }
        } else {
            try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                Bitmap originalBitmap = BitmapFactory.decodeStream(is);
                if (originalBitmap == null) {
                    return new byte[0];
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap, 0, 0,
                    originalBitmap.getWidth(), originalBitmap.getHeight(),
                    matrix, true
                );
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                originalBitmap.recycle();
                rotatedBitmap.recycle();
                return bos.toByteArray();
            }
        }
    }

    private int getOrientation(Uri uri) {
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            if (is != null) {
                ExifInterface exif = new ExifInterface(is);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        return 270;
                }
            }
        } catch (Exception ignored) {}

        try {
            String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
            android.database.Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
                    if (colIndex != -1) {
                        int rotation = cursor.getInt(colIndex);
                        cursor.close();
                        return rotation;
                    }
                }
                cursor.close();
            }
        } catch (Exception ignored) {}

        return 0;
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null);
        builder.setView(view);

        EditText edtNewName = view.findViewById(R.id.edtNewName);
        View btnCancel = view.findViewById(R.id.btnCancel);
        View btnSave = view.findViewById(R.id.btnSave);

        edtNewName.setText(currentUser.getFullName());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName = edtNewName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            updateNameOnServer(newName);
        });

        dialog.show();
    }

    private void updateNameOnServer(String newName) {
        User userUpdate = new User();
        userUpdate.setFullName(newName);

        Toast.makeText(requireContext(), "Đang cập nhật tên...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApiService().updateUser(currentUser.getUserId(), userUpdate)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        ApiResponse<User> apiResponse = response.body();
                        if (response.isSuccessful() && apiResponse != null && apiResponse.isSuccess()) {
                            User updatedUser = apiResponse.getData();
                            if (updatedUser != null) {
                                SharedPrefManager.getInstance(requireContext()).saveUser(updatedUser);
                                currentUser = updatedUser;
                                binding.tvUserName.setText(currentUser.getFullName());
                                Toast.makeText(requireContext(), "Cập nhật tên thành công!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Cập nhật tên thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword);
        EditText edtNewPassword = view.findViewById(R.id.edtNewPassword);
        EditText edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        View btnCancel = view.findViewById(R.id.btnCancel);
        View btnUpdate = view.findViewById(R.id.btnUpdate);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            String currentPw = edtCurrentPassword.getText().toString().trim();
            String newPw = edtNewPassword.getText().toString().trim();
            String confirmPw = edtConfirmPassword.getText().toString().trim();

            if (currentPw.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu hiện tại!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPw.length() < 6) {
                Toast.makeText(requireContext(), "Mật khẩu mới phải chứa ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPw.equals(confirmPw)) {
                Toast.makeText(requireContext(), "Xác nhận mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            updateFirebasePassword(currentPw, newPw);
        });

        dialog.show();
    }

    private void updateFirebasePassword(String currentPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            Toast.makeText(requireContext(), "Đang xác thực...", Toast.LENGTH_SHORT).show();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Đang cập nhật mật khẩu...", Toast.LENGTH_SHORT).show();
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(pwTask -> {
                                        if (pwTask.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Cập nhật mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String error = pwTask.getException() != null ? pwTask.getException().getMessage() : "Lỗi không xác định";
                                            Toast.makeText(requireContext(), "Cập nhật thất bại: " + error, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Mật khẩu hiện tại không chính xác!", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy phiên đăng nhập!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMetrics(int month, int year) {
        homeViewModel.fetchDashboardData(currentUser.getUserId(), month, year);
    }

    private void observeViewModel() {
        // Observe monthly report totals
        homeViewModel.getMonthlyReport().observe(getViewLifecycleOwner(), report -> {
            if (report != null && binding != null) {
                binding.tvTotalIncome.setText(CurrencyFormatter.formatVND(report.getTotalIncome()));
                binding.tvTotalExpense.setText(CurrencyFormatter.formatVND(report.getTotalExpense()));
                binding.tvNetAmount.setText(CurrencyFormatter.formatVND(report.getNetAmount()));

                // Style the balance amount based on positive/negative
                if (report.getNetAmount() < 0) {
                    binding.tvNetAmount.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_error));
                } else {
                    binding.tvNetAmount.setTextColor(getResources().getColor(com.example.personalfinance.R.color.income_green));
                }
            }
        });

        // Observe monthly transaction list to calculate count
        homeViewModel.getMonthlyTransactions().observe(getViewLifecycleOwner(), list -> {
            if (list != null && binding != null) {
                binding.tvTransactionCount.setText(String.valueOf(list.size()));
            } else if (binding != null) {
                binding.tvTransactionCount.setText("0");
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        SharedPrefManager.getInstance(requireContext()).clear();

        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
