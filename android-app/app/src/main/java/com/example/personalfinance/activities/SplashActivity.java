package com.example.personalfinance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.personalfinance.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Retrofit base URL with the saved local IP from SharedPreferences
        String savedIp = com.example.personalfinance.utils.SharedPrefManager.getInstance(this).getServerIp();
        com.example.personalfinance.api.RetrofitClient.updateBaseUrl(savedIp);

        // Smooth delay to transition to core activities
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is signed in, go to main dashboard
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // User is not signed in, go to login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 1500);
    }
}
