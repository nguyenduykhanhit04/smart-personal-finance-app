package com.example.personalfinance.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.databinding.ActivitySplashBinding
import com.example.personalfinance.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Retrofit base URL with the saved local IP from SharedPreferences
        val savedIp = SharedPrefManager.getInstance(this).getServerIp()
        RetrofitClient.updateBaseUrl(savedIp)

        // Smooth delay to transition to core activities
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User is signed in, go to main dashboard
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User is not signed in, go to login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1500)
    }
}
