package com.example.personalfinance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.personalfinance.R;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.ActivityLoginBinding;
import com.example.personalfinance.firebase.FirebaseAuthCallback;
import com.example.personalfinance.firebase.FirebaseAuthHelper;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private FirebaseAuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new FirebaseAuthHelper();
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Load saved server IP and update base URL
        String savedIp = SharedPrefManager.getInstance(this).getServerIp();
        RetrofitClient.updateBaseUrl(savedIp);

        // Long click App Logo to change local Server IP
        binding.ivLogo.setOnLongClickListener(v -> {
            showIpConfigDialog();
            return true;
        });

        // Email/Password Login
        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Google Sign-In
        authHelper.initGoogleSignIn(this, getString(R.string.default_web_client_id));
        binding.btnGoogleLogin.setOnClickListener(v -> handleGoogleLogin());

        // Facebook Login is not available yet.
        binding.btnFacebookLogin.setOnClickListener(v -> showComingSoon());

        // Register LiveData Observers
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getSyncedUser().observe(this, syncedUser -> {
            if (syncedUser != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                enableSocialButtons(true);

                // Save user in SharedPref
                SharedPrefManager.getInstance(LoginActivity.this).saveUser(syncedUser);
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                
                // Navigate to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                enableSocialButtons(true);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                authHelper.signOut(); // clean firebase session
            }
        });
    }

    private void handleGoogleLogin() {
        binding.progressBar.setVisibility(View.VISIBLE);
        enableSocialButtons(false);
        Intent signInIntent = authHelper.getGoogleSignInIntent();
        startActivityForResult(signInIntent, FirebaseAuthHelper.RC_GOOGLE_SIGN_IN);
    }

    private void showComingSoon() {
        Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FirebaseAuthHelper.RC_GOOGLE_SIGN_IN) {
            authHelper.handleGoogleSignInResult(data, new FirebaseAuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Log.d(TAG, "Google → Firebase auth success: " + user.getEmail());
                    viewModel.syncUserWithBackend(user, null);
                }

                @Override
                public void onFailure(Exception exception) {
                    binding.progressBar.setVisibility(View.GONE);
                    enableSocialButtons(true);
                    Toast.makeText(LoginActivity.this, viewModel.getReadableAuthError(exception), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showIpConfigDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Cấu hình IP Server");
        builder.setMessage("Nhập IP cục bộ của máy tính chạy Spring Boot:");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        String currentIp = SharedPrefManager.getInstance(this).getServerIp();
        input.setText(currentIp);
        input.setSelection(currentIp.length());
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newIp = input.getText().toString().trim();
            if (!newIp.isEmpty()) {
                SharedPrefManager.getInstance(this).saveServerIp(newIp);
                RetrofitClient.updateBaseUrl(newIp);
                Toast.makeText(this, "Đã cập nhật IP Server thành: " + newIp, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "IP không được để trống!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Vui lòng nhập Email");
            binding.etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Email không đúng định dạng");
            binding.etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Vui lòng nhập mật khẩu");
            binding.etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            binding.etPassword.requestFocus();
            return;
        }

        binding.btnLogin.setEnabled(false);
        viewModel.signIn(email, password, authHelper);
    }

    private void enableSocialButtons(boolean enabled) {
        binding.btnGoogleLogin.setEnabled(enabled);
        binding.btnFacebookLogin.setEnabled(enabled);
    }
}
