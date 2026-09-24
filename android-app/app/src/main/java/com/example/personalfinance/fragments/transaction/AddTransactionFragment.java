package com.example.personalfinance.fragments.transaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.personalfinance.R;
import com.example.personalfinance.activities.MainActivity;
import com.example.personalfinance.databinding.FragmentAddTransactionBinding;
import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.models.User;
import com.example.personalfinance.repositories.BudgetRepository;
import com.example.personalfinance.repositories.TransactionRepository;
import com.example.personalfinance.utils.Constants;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.TransactionViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.personalfinance.api.RetrofitClient;

public class AddTransactionFragment extends DialogFragment {

    public static AddTransactionFragment newInstance(int transactionId, double amount, String title, int categoryId, String date, int aiScanLogId, String imageUrl) {
        AddTransactionFragment fragment = new AddTransactionFragment();
        Bundle args = new Bundle();
        args.putInt("transactionId", transactionId);
        args.putDouble("amount", amount);
        args.putString("title", title);
        args.putInt("categoryId", categoryId);
        args.putString("date", date);
        args.putInt("aiScanLogId", aiScanLogId);
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddTransactionFragment newInstance(double amount, String title, int categoryId, String date, int aiScanLogId) {
        return newInstance(0, amount, title, categoryId, date, aiScanLogId, null);
    }

    public static AddTransactionFragment newInstance(double amount, String title, int categoryId, String date, int aiScanLogId, String localImageUri) {
        AddTransactionFragment fragment = new AddTransactionFragment();
        Bundle args = new Bundle();
        args.putInt("transactionId", 0);
        args.putDouble("amount", amount);
        args.putString("title", title);
        args.putInt("categoryId", categoryId);
        args.putString("date", date);
        args.putInt("aiScanLogId", aiScanLogId);
        args.putString("imageUri", localImageUri);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddTransactionFragment newInstanceForProductScan(double amount, String title, int categoryId, String date, int aiProductLogId, String localImageUri) {
        AddTransactionFragment fragment = new AddTransactionFragment();
        Bundle args = new Bundle();
        args.putInt("transactionId", 0);
        args.putDouble("amount", amount);
        args.putString("title", title);
        args.putInt("categoryId", categoryId);
        args.putString("date", date);
        args.putInt("aiProductLogId", aiProductLogId);
        args.putString("imageUri", localImageUri);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnTransactionSavedListener {
        void onTransactionSaved();
    }
    private OnTransactionSavedListener onTransactionSavedListener;

    public void setOnTransactionSavedListener(OnTransactionSavedListener listener) {
        this.onTransactionSavedListener = listener;
    }

    private int transactionId = 0;
    private String existingImageUrl = null;
    private FragmentAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private User currentUser;
    private final Calendar calendar = Calendar.getInstance();

    private List<Account> allAccounts = new ArrayList<>();
    private List<Category> allCategories = new ArrayList<>();
    private List<Category> filteredCategories = new ArrayList<>();

    private String selectedDateStr;

    private static final int REQUEST_IMAGE_CAPTURE = 2001;
    private static final int REQUEST_IMAGE_PICK = 2002;
    private static final int PERMISSION_CAMERA_CODE = 2003;
    private android.net.Uri capturedImageUri;
    private android.net.Uri selectedImageUri;
    private byte[] selectedImageBytes;
    private String selectedImageMimeType;
    private Transaction savedTransactionForWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Set default date / read from arguments
        if (getArguments() != null) {
            transactionId = getArguments().getInt("transactionId", 0);
            existingImageUrl = getArguments().getString("imageUrl", null);
            double amount = getArguments().getDouble("amount", 0);
            String title = getArguments().getString("title", "");
            String dateArg = getArguments().getString("date");
            if (dateArg == null || dateArg.isEmpty()) {
                dateArg = getArguments().getString("prefilled_date");
            }
            selectedDateStr = (dateArg != null && !dateArg.isEmpty()) ? dateArg : DateUtils.getCurrentDateString();
            
            if (amount > 0) {
                binding.edtAmount.setText(String.valueOf(amount));
            }
            if (!title.isEmpty()) {
                binding.edtTitle.setText(title);
            }
            if (transactionId > 0) {
                binding.tvScreenTitle.setText(R.string.label_transaction_details);
                binding.btnSave.setText(R.string.label_update_transaction);
            }
            if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                String baseUrl = RetrofitClient.getClient().baseUrl().toString();
                String path = existingImageUrl.startsWith("/") ? existingImageUrl.substring(1) : existingImageUrl;
                loadNetworkImage(baseUrl + path);
            }
            String localImageUriStr = getArguments().getString("imageUri", null);
            if (localImageUriStr != null && !localImageUriStr.isEmpty()) {
                selectedImageUri = android.net.Uri.parse(localImageUriStr);
                cacheSelectedImageForUpload(selectedImageUri);
            }
        } else {
            selectedDateStr = DateUtils.getCurrentDateString();
        }
        binding.edtDate.setText(DateUtils.formatDateForDisplay(selectedDateStr));

        // Date selection
        binding.edtDate.setOnClickListener(v -> showDatePicker());

        // Cancel button
        binding.btnCancel.setOnClickListener(v -> dismiss());

        // Save button
        binding.btnSave.setOnClickListener(v -> saveTransaction());

        // Retake and Share buttons (Screen 7 actions)
        binding.btnRetake.setOnClickListener(v -> {
            dismiss();
            startActivity(new android.content.Intent(requireContext(), com.example.personalfinance.activities.ScanBillActivity.class));
        });
        binding.btnShare.setOnClickListener(v -> Toast.makeText(requireContext(), "Chia sẻ giao dịch này...", Toast.LENGTH_SHORT).show());

        // Focus and Keyboard handling
        binding.edtAmount.postDelayed(() -> {
            if (binding != null && isAdded()) {
                binding.edtAmount.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.edtAmount, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 300);

        // Dynamic VND formatting preview
        binding.edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String cleanString = s.toString().trim();
                if (!cleanString.isEmpty()) {
                    try {
                        double parsed = Double.parseDouble(cleanString);
                        binding.tvFormattedAmount.setText(CurrencyFormatter.formatVND(parsed));
                        binding.tvFormattedAmount.setVisibility(View.VISIBLE);
                    } catch (NumberFormatException e) {
                        binding.tvFormattedAmount.setVisibility(View.GONE);
                    }
                } else {
                    binding.tvFormattedAmount.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Trigger format if initial amount is present
        String initialAmount = binding.edtAmount.getText().toString().trim();
        if (!initialAmount.isEmpty()) {
            try {
                double parsed = Double.parseDouble(initialAmount);
                binding.tvFormattedAmount.setText(CurrencyFormatter.formatVND(parsed));
                binding.tvFormattedAmount.setVisibility(View.VISIBLE);
            } catch (NumberFormatException ignored) {}
        }

        // Optional attachment button handlers
        binding.btnAddImage.setOnClickListener(v -> showImageSourceSelector());
        binding.ivThumbnail.setOnClickListener(v -> showImageSourceSelector());
        binding.btnRemoveImage.setOnClickListener(v -> removeImage());

        // Determine if we are in Manual mode vs scan confirmation mode
        boolean isManualMode = true;
        if (getArguments() != null) {
            int aiScanLogId = getArguments().getInt("aiScanLogId", 0);
            if (aiScanLogId > 0) {
                isManualMode = false;
            }
        }

        if (isManualMode) {
            binding.btnRetake.setVisibility(View.GONE);
            binding.btnShare.setVisibility(View.GONE);
        } else {
            binding.btnRetake.setVisibility(View.VISIBLE);
            binding.btnShare.setVisibility(View.VISIBLE);
        }

        // Listen for type change to filter categories and update styles
        binding.rgType.setOnCheckedChangeListener((group, checkedId) -> {
            updateTypeSelectorStyles();
            filterCategories();
        });
        updateTypeSelectorStyles();

        // Register Observers
        observeViewModel();

        // Hide wallet spinner and expand category spinner to take full width
        if (binding.spAccount != null && binding.spCategory != null) {
            View accountParent = (View) binding.spAccount.getParent();
            accountParent.setVisibility(View.GONE);

            View categoryParent = (View) binding.spCategory.getParent();
            ViewGroup.LayoutParams layoutParams = categoryParent.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = 0;
            }
            if (layoutParams instanceof android.widget.LinearLayout.LayoutParams) {
                android.widget.LinearLayout.LayoutParams linearParams = (android.widget.LinearLayout.LayoutParams) layoutParams;
                linearParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                linearParams.weight = 0;
                categoryParent.setLayoutParams(linearParams);
            }
        }

        // Load account list and category list
        viewModel.loadFormData(currentUser.getUserId());
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            selectedDateStr = DateUtils.formatApiDate(calendar.getTime());
            binding.edtDate.setText(DateUtils.formatDateForDisplay(selectedDateStr));
        };

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void observeViewModel() {
        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) {
                allAccounts = accounts;
                populateAccountsSpinner();
            }
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                allCategories = categories;

                // Switch segment if pre-selected category is provided
                if (getArguments() != null && getArguments().containsKey("categoryId")) {
                    int preSelectedId = getArguments().getInt("categoryId");
                    for (Category cat : allCategories) {
                        if (cat.getCategoryId() == preSelectedId) {
                            if (Constants.TYPE_INCOME.equalsIgnoreCase(cat.getCategoryType())) {
                                binding.rbIncome.setChecked(true);
                            } else {
                                binding.rbExpense.setChecked(true);
                            }
                            break;
                        }
                    }
                }

                filterCategories();
            }
        });

        viewModel.getTransactionCreated().observe(getViewLifecycleOwner(), transaction -> {
            if (transaction != null) {
                savedTransactionForWarning = transaction;
                if (selectedImageUri != null) {
                    uploadImageAndFinish(transaction);
                } else {
                    completeSaveTransactionWithBudgetWarning();
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.btnSave.setEnabled(true);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateAccountsSpinner() {
        List<String> names = new ArrayList<>();
        for (Account acc : allAccounts) {
            names.add(acc.getAccountName() + " (" + CurrencyFormatter.formatVND(acc.getBalance()) + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_spinner_item, names);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        binding.spAccount.setAdapter(adapter);
    }

    private void updateTypeSelectorStyles() {
        if (getContext() == null) return;
        boolean isExpense = binding.rbExpense.isChecked();

        if (isExpense) {
            // Expense selected: red background, white text
            binding.rbExpense.setBackgroundResource(R.drawable.bg_button_rounded);
            binding.rbExpense.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.expense_red));
            binding.rbExpense.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white));

            binding.rbIncome.setBackground(null);
            binding.rbIncome.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary));
        } else {
            // Income selected: green background, white text
            binding.rbIncome.setBackgroundResource(R.drawable.bg_button_rounded);
            binding.rbIncome.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.income_green));
            binding.rbIncome.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white));

            binding.rbExpense.setBackground(null);
            binding.rbExpense.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void filterCategories() {
        String selectedType = binding.rbIncome.isChecked() ? Constants.TYPE_INCOME : Constants.TYPE_EXPENSE;

        filteredCategories.clear();
        List<String> names = new ArrayList<>();
        for (Category cat : allCategories) {
            if (selectedType.equalsIgnoreCase(cat.getCategoryType())) {
                filteredCategories.add(cat);
                names.add(cat.getCategoryName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_spinner_item, names);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        binding.spCategory.setAdapter(adapter);

        // Pre-select category if passed in arguments
        if (getArguments() != null && getArguments().containsKey("categoryId")) {
            int preSelectedId = getArguments().getInt("categoryId");
            for (int i = 0; i < filteredCategories.size(); i++) {
                if (filteredCategories.get(i).getCategoryId() == preSelectedId) {
                    binding.spCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveTransaction() {
        String title = binding.edtTitle.getText().toString().trim();
        String amountText = binding.edtAmount.getText().toString().trim();
        String note = binding.edtNote.getText().toString().trim();

        if (allAccounts.isEmpty()) {
            Toast.makeText(requireContext(), "Không có tài khoản nào được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (filteredCategories.isEmpty()) {
            Toast.makeText(requireContext(), "Không có danh mục nào được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            binding.edtTitle.setError("Vui lòng nhập tên giao dịch");
            binding.edtTitle.requestFocus();
            return;
        }

        if (amountText.isEmpty()) {
            binding.edtAmount.setError("Vui lòng nhập số tiền");
            binding.edtAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            binding.edtAmount.setError("Số tiền không hợp lệ");
            binding.edtAmount.requestFocus();
            return;
        }

        if (isSelectedDateInFuture()) {
            binding.edtDate.setError("Không thể tạo giao dịch cho ngày trong tương lai");
            Toast.makeText(requireContext(), "Không thể tạo giao dịch cho ngày trong tương lai", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryIdx = binding.spCategory.getSelectedItemPosition();

        if (categoryIdx < 0) return;

        Category selectedCategory = filteredCategories.get(categoryIdx);
        String selectedType = binding.rbIncome.isChecked() ? Constants.TYPE_INCOME : Constants.TYPE_EXPENSE;

        binding.btnSave.setEnabled(false);

        Transaction transaction = new Transaction();
        transaction.setUserId(currentUser.getUserId());
        transaction.setAccountId(null); // Omitted so backend assigns it automatically
        transaction.setCategoryId(selectedCategory.getCategoryId());
        transaction.setTitle(title);
        transaction.setAmount(amount);
        transaction.setTransactionType(selectedType);
        transaction.setTransactionDate(selectedDateStr);
        transaction.setNote(note);
        transaction.setStatus("completed");
        if (transactionId > 0) {
            transaction.setTransactionId(transactionId);
        }

        int aiScanLogId = 0;
        if (getArguments() != null && getArguments().containsKey("aiScanLogId")) {
            aiScanLogId = getArguments().getInt("aiScanLogId");
        }

        int aiProductLogId = 0;
        if (getArguments() != null && getArguments().containsKey("aiProductLogId")) {
            aiProductLogId = getArguments().getInt("aiProductLogId");
        }

        if (transactionId > 0) {
            viewModel.updateTransaction(transactionId, transaction);
        } else {
            viewModel.createTransaction(transaction, aiScanLogId, aiProductLogId, selectedCategory.getCategoryId());
        }
    }

    private boolean isSelectedDateInFuture() {
        try {
            java.util.Date selectedDate = DateUtils.parseApiDate(selectedDateStr);
            java.util.Date today = DateUtils.parseApiDate(DateUtils.getCurrentDateString());
            return selectedDate != null && today != null && selectedDate.after(today);
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
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

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(android.provider.MediaStore.Images.Media.TITLE, "New Picture");
        values.put(android.provider.MediaStore.Images.Media.DESCRIPTION, "From Camera");
        capturedImageUri = requireContext().getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (capturedImageUri == null) {
            Toast.makeText(requireContext(), "Không tạo được file ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.Intent intent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, capturedImageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void openGallery() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                | android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == android.app.Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (capturedImageUri != null) {
                    selectedImageUri = capturedImageUri;
                    cacheSelectedImageForUpload(selectedImageUri);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                persistGalleryReadPermission(selectedImageUri);
                cacheSelectedImageForUpload(selectedImageUri);
            }
        }
    }

    private void persistGalleryReadPermission(android.net.Uri uri) {
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some gallery providers grant temporary read access only; cached bytes still cover upload.
        }
    }

    private void cacheSelectedImageForUpload(android.net.Uri uri) {
        try {
            selectedImageBytes = readImageBytes(uri);
            if (selectedImageBytes.length == 0) {
                removeImage();
                Toast.makeText(requireContext(), "Ảnh không có dữ liệu, vui lòng chọn ảnh khác", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedImageMimeType = requireContext().getContentResolver().getType(uri);
            if (selectedImageMimeType == null || selectedImageMimeType.trim().isEmpty()) {
                selectedImageMimeType = "image/jpeg";
            }

            displayThumbnail(uri);
        } catch (Exception e) {
            removeImage();
            Toast.makeText(requireContext(), "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayThumbnail(android.net.Uri uri) {
        if (binding == null) return;
        binding.ivThumbnail.setImageURI(uri);
        binding.layoutImagePreview.setVisibility(View.VISIBLE);
        binding.btnAddImage.setVisibility(View.GONE);
    }

    private void removeImage() {
        selectedImageUri = null;
        capturedImageUri = null;
        selectedImageBytes = null;
        selectedImageMimeType = null;
        existingImageUrl = null;
        if (binding == null) return;
        binding.layoutImagePreview.setVisibility(View.GONE);
        binding.btnAddImage.setVisibility(View.VISIBLE);
        binding.ivThumbnail.setImageURI(null);
    }

    private void uploadImageAndFinish(Transaction transaction) {
        try {
            Integer transactionId = transaction != null ? transaction.getTransactionId() : null;
            if (transactionId == null || transactionId <= 0) {
                Toast.makeText(requireContext(), "Không lấy được mã giao dịch để lưu ảnh", Toast.LENGTH_SHORT).show();
                completeSaveTransactionWithBudgetWarning();
                return;
            }

            if (selectedImageBytes == null || selectedImageBytes.length == 0) {
                completeSaveTransactionWithBudgetWarning();
                return;
            }

            String mimeType = selectedImageMimeType;
            if (mimeType == null || mimeType.trim().isEmpty()) {
                mimeType = "image/jpeg";
            }

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse(mimeType),
                    selectedImageBytes
            );

            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData(
                    "file",
                    buildImageFileName(transactionId, mimeType),
                    requestFile
            );

            if (binding != null) {
                binding.btnSave.setEnabled(false);
            }
            Toast.makeText(requireContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

            RetrofitClient.getApiService().uploadTransactionImage(transactionId, body)
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (response.isSuccessful() && apiResponse != null && apiResponse.isSuccess()) {
                                Toast.makeText(requireContext(), "Đính kèm ảnh thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Lỗi tải ảnh lên server", Toast.LENGTH_SHORT).show();
                            }
                            completeSaveTransactionWithBudgetWarning();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(requireContext(), "Lỗi kết nối khi tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            completeSaveTransactionWithBudgetWarning();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi đọc file ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            completeSaveTransactionWithBudgetWarning();
        }
    }

    private int getOrientation(android.net.Uri uri) {
        // Method 1: Try ExifInterface
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            if (is != null) {
                androidx.exifinterface.media.ExifInterface exif =
                        new androidx.exifinterface.media.ExifInterface(is);
                int orientation = exif.getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                        return 90;
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                        return 180;
                    case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                        return 270;
                }
            }
        } catch (Exception ignored) {}

        // Method 2: Try querying MediaStore
        try {
            String[] projection = { android.provider.MediaStore.Images.ImageColumns.ORIENTATION };
            android.database.Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.ImageColumns.ORIENTATION);
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

    private byte[] readImageBytes(android.net.Uri uri) throws IOException {
        int rotation = getOrientation(uri);
        if (rotation == 0) {
            // No rotation needed, return raw bytes
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
            // Decode, rotate, compress, and return bytes
            try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                Bitmap originalBitmap = BitmapFactory.decodeStream(is);
                if (originalBitmap == null) {
                    return new byte[0];
                }
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postRotate(rotation);
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap, 0, 0,
                    originalBitmap.getWidth(), originalBitmap.getHeight(),
                    matrix, true
                );
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                // Compress to JPEG with 90% quality to maintain premium appearance
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                originalBitmap.recycle();
                rotatedBitmap.recycle();
                return bos.toByteArray();
            }
        }
    }

    private String buildImageFileName(Integer transactionId, String mimeType) {
        String extension = ".jpg";
        if ("image/png".equalsIgnoreCase(mimeType)) {
            extension = ".png";
        } else if ("image/webp".equalsIgnoreCase(mimeType)) {
            extension = ".webp";
        }
        return "transaction_" + transactionId + extension;
    }

    private void completeSaveTransactionWithBudgetWarning() {
        if (binding != null) {
            binding.btnSave.setEnabled(true);
        }
        Toast.makeText(requireContext(), "Lưu giao dịch thành công!", Toast.LENGTH_SHORT).show();

        if (!maybeShowBudgetWarning()) {
            finishSaveTransaction();
        }
    }

    private boolean maybeShowBudgetWarning() {
        Transaction transaction = savedTransactionForWarning;
        if (transaction == null
                || !Constants.TYPE_EXPENSE.equalsIgnoreCase(transaction.getTransactionType())
                || transaction.getCategoryId() == null
                || transaction.getTransactionDate() == null) {
            return false;
        }

        new BudgetRepository().getBudgets(currentUser.getUserId(), new BudgetRepository.ApiCallback<List<Budget>>() {
            @Override
            public void onSuccess(List<Budget> result) {
                Budget warningBudget = findWarningBudget(result, transaction);
                if (warningBudget == null) {
                    finishSaveTransaction();
                    return;
                }

                if (warningBudget.getPercentUsed() > 100) {
                    showBudgetExceededDialog(warningBudget);
                } else {
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.budget_approaching_limit_warning, Math.round(warningBudget.getPercentUsed()), getBudgetDisplayName(warningBudget)),
                            Toast.LENGTH_LONG
                    ).show();
                    finishSaveTransaction();
                }
            }

            @Override
            public void onError(String errorMessage) {
                finishSaveTransaction();
            }
        });
        return true;
    }

    private Budget findWarningBudget(List<Budget> budgets, Transaction transaction) {
        if (budgets == null) return null;

        Budget highest = null;
        for (Budget budget : budgets) {
            if (budget == null || budget.getAmountLimit() <= 0) continue;
            if (!isBudgetForTransaction(budget, transaction)) continue;
            if (budget.getPercentUsed() < 80) continue;

            if (highest == null || budget.getPercentUsed() > highest.getPercentUsed()) {
                highest = budget;
            }
        }
        return highest;
    }

    private boolean isBudgetForTransaction(Budget budget, Transaction transaction) {
        Integer budgetCategoryId = budget.getCategoryId();
        if (budgetCategoryId != null && !budgetCategoryId.equals(transaction.getCategoryId())) {
            return false;
        }

        String txDate = transaction.getTransactionDate();
        String startDate = normalizeDateOnly(budget.getStartDate());
        String endDate = normalizeDateOnly(budget.getEndDate());
        return (startDate == null || txDate.compareTo(startDate) >= 0)
                && (endDate == null || txDate.compareTo(endDate) <= 0);
    }

    private String normalizeDateOnly(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.contains("T") ? value.split("T")[0] : value;
    }

    private void showBudgetExceededDialog(Budget budget) {
        if (!isAdded() || getContext() == null) {
            finishSaveTransaction();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_limit_warning, null, false);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        dialog.setContentView(dialogView);

        double overAmount = Math.max(0, budget.getSpentAmount() - budget.getAmountLimit());
        ((TextView) dialogView.findViewById(R.id.tvCategoryAlertName)).setText(getBudgetDisplayName(budget));
        ((TextView) dialogView.findViewById(R.id.tvLimitVal)).setText(CurrencyFormatter.formatVND(budget.getAmountLimit()));
        ((TextView) dialogView.findViewById(R.id.tvSpentVal)).setText(CurrencyFormatter.formatVND(budget.getSpentAmount()));
        ((TextView) dialogView.findViewById(R.id.tvOverVal)).setText(CurrencyFormatter.formatVND(overAmount));
        ((TextView) dialogView.findViewById(R.id.tvOverPercentVal)).setText(
                getString(R.string.percentage_format, Math.round(budget.getPercentUsed())));

        dialogView.findViewById(R.id.btnDismiss).setOnClickListener(v -> {
            dialog.dismiss();
            finishSaveTransaction();
        });
        dialogView.findViewById(R.id.btnViewDetails).setOnClickListener(v -> {
            dialog.dismiss();
            if (onTransactionSavedListener != null) {
                onTransactionSavedListener.onTransactionSaved();
            }
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_budget);
            }
            dismiss();
        });
        dialog.setOnCancelListener(d -> finishSaveTransaction());
        dialog.show();
    }

    private String getBudgetDisplayName(Budget budget) {
        if (budget.getCategoryName() != null && !budget.getCategoryName().trim().isEmpty()) {
            return budget.getCategoryName();
        }
        if (budget.getBudgetName() != null && !budget.getBudgetName().trim().isEmpty()) {
            return budget.getBudgetName();
        }
        return getString(R.string.budget_fallback_display_name);
    }

    private void finishSaveTransaction() {
        if (onTransactionSavedListener != null) {
            onTransactionSavedListener.onTransactionSaved();
        } else {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_home);
            }
        }

        if (getActivity() != null && !(getActivity() instanceof MainActivity)) {
            getActivity().finish();
        }
        dismiss();
    }

    private void loadNetworkImage(String url) {
        new Thread(() -> {
            try {
                android.util.Log.d("AddTransactionFragment", "Loading image from URL: " + url);
                java.net.URL imageUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) imageUrl.openConnection();
                conn.setDoInput(true);
                conn.setRequestProperty("ngrok-skip-browser-warning", "true");
                conn.setRequestProperty("User-Agent", "Android-App");
                conn.connect();

                int responseCode = conn.getResponseCode();
                android.util.Log.d("AddTransactionFragment", "HTTP Response code: " + responseCode);

                if (responseCode >= 200 && responseCode < 300) {
                    java.io.InputStream is = conn.getInputStream();
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                    if (bitmap == null) {
                        android.util.Log.e("AddTransactionFragment", "Failed to decode bitmap from stream!");
                    } else {
                        android.util.Log.d("AddTransactionFragment", "Successfully decoded bitmap! Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (binding != null && bitmap != null) {
                                binding.ivThumbnail.setImageBitmap(bitmap);
                                binding.layoutImagePreview.setVisibility(View.VISIBLE);
                                binding.btnAddImage.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    android.util.Log.e("AddTransactionFragment", "Failed to load image, HTTP response: " + responseCode);
                }
            } catch (Exception e) {
                android.util.Log.e("AddTransactionFragment", "Error loading image: ", e);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
