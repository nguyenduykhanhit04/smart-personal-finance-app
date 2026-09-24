package com.example.personalfinance.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class TokenInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // Add ngrok bypass header to prevent browser warning on dynamic/API hosts
        val requestBuilder = chain.request().newBuilder()
            .header("ngrok-skip-browser-warning", "true")

        val user = FirebaseAuth.getInstance().currentUser
            ?: return chain.proceed(requestBuilder.build())

        try {
            // Synchronously block until the Firebase ID Token resolves (runs on OkHttp background thread)
            val result = Tasks.await(user.getIdToken(false))
            result.token?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chain.proceed(requestBuilder.build())
    }
}
