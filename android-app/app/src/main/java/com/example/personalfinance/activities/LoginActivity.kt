package com.example.personalfinance.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.personalfinance.R
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.ActivityLoginBinding
import com.example.personalfinance.firebase.FirebaseAuthCallback
import com.example.personalfinance.firebase.FirebaseAuthHelper
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var authHelper: FirebaseAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authHelper = FirebaseAuthHelper()
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Load saved server IP and update base URL
        val savedIp = SharedPrefManager.getInstance(this).getServerIp()
        RetrofitClient.updateBaseUrl(savedIp)

        // Long click App Logo to change local Server IP
        binding.ivLogo.setOnLongClickListener {
            showIpConfigDialog()
            true
        }

        // Email/Password Login
        binding.btnLogin.setOnClickListener { handleLogin() }
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Google Sign-In
        authHelper.initGoogleSignIn(this, getString(R.string.default_web_client_id))
        binding.btnGoogleLogin.setOnClickListener { handleGoogleLogin() }

        // Facebook Login is not available yet.
        binding.btnFacebookLogin.setOnClickListener { showComingSoon() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.syncedUser.observe(this) { syncedUser ->
            if (syncedUser != null) {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                enableSocialButtons(true)

                SharedPrefManager.getInstance(this).saveUser(syncedUser)
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                enableSocialButtons(true)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                authHelper.signOut() // clean firebase session
            }
        }
    }

    private fun handleGoogleLogin() {
        binding.progressBar.visibility = View.VISIBLE
        enableSocialButtons(false)
        @Suppress("DEPRECATION")
        startActivityForResult(authHelper.getGoogleSignInIntent(), FirebaseAuthHelper.RC_GOOGLE_SIGN_IN)
    }

    private fun showComingSoon() {
        Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Use ActivityResultLauncher instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FirebaseAuthHelper.RC_GOOGLE_SIGN_IN) {
            authHelper.handleGoogleSignInResult(data, object : FirebaseAuthCallback {
                override fun onSuccess(user: FirebaseUser) {
                    Log.d(TAG, "Google → Firebase auth success: ${user.email}")
                    viewModel.syncUserWithBackend(user, null)
                }
                override fun onFailure(exception: Exception) {
                    binding.progressBar.visibility = View.GONE
                    enableSocialButtons(true)
                    Toast.makeText(this@LoginActivity, viewModel.getReadableAuthError(exception), Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun showIpConfigDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            val currentIp = SharedPrefManager.getInstance(this@LoginActivity).getServerIp()
            setText(currentIp)
            setSelection(currentIp.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Cấu hình IP Server")
            .setMessage("Nhập IP cục bộ của máy tính chạy Spring Boot:")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newIp = input.text.toString().trim()
                if (newIp.isNotEmpty()) {
                    SharedPrefManager.getInstance(this).saveServerIp(newIp)
                    RetrofitClient.updateBaseUrl(newIp)
                    Toast.makeText(this, "Đã cập nhật IP Server thành: $newIp", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "IP không được để trống!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.etEmail.error = "Vui lòng nhập Email"
                binding.etEmail.requestFocus()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Email không đúng định dạng"
                binding.etEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Vui lòng nhập mật khẩu"
                binding.etPassword.requestFocus()
                return
            }
            password.length < 6 -> {
                binding.etPassword.error = "Mật khẩu phải từ 6 ký tự trở lên"
                binding.etPassword.requestFocus()
                return
            }
        }

        binding.btnLogin.isEnabled = false
        viewModel.signIn(email, password, authHelper)
    }

    private fun enableSocialButtons(enabled: Boolean) {
        binding.btnGoogleLogin.isEnabled = enabled
        binding.btnFacebookLogin.isEnabled = enabled
    }
}
