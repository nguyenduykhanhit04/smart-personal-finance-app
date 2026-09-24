package com.example.personalfinance.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.personalfinance.databinding.ActivityRegisterBinding
import com.example.personalfinance.firebase.FirebaseAuthHelper
import com.example.personalfinance.utils.SharedPrefManager
import com.example.personalfinance.viewmodels.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var authHelper: FirebaseAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authHelper = FirebaseAuthHelper()
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.btnRegister.setOnClickListener { handleRegister() }
        binding.tvLoginLink.setOnClickListener { finish() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.syncedUser.observe(this) { syncedUser ->
            if (syncedUser != null) {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true

                SharedPrefManager.getInstance(this).saveUser(syncedUser)
                Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show()

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
                binding.btnRegister.isEnabled = true
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                authHelper.signOut() // clean session on sync failure
            }
        }
    }

    private fun handleRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        when {
            fullName.isEmpty() -> {
                binding.etFullName.error = "Vui lòng nhập họ và tên"
                binding.etFullName.requestFocus()
                return
            }
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
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Mật khẩu xác nhận không khớp"
                binding.etConfirmPassword.requestFocus()
                return
            }
        }

        binding.btnRegister.isEnabled = false
        viewModel.signUp(email, password, fullName, authHelper)
    }
}
