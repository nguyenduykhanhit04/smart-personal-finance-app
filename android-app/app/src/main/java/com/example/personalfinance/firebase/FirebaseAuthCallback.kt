package com.example.personalfinance.firebase

import com.google.firebase.auth.FirebaseUser

interface FirebaseAuthCallback {
    fun onSuccess(user: FirebaseUser)
    fun onFailure(exception: Exception)
}
