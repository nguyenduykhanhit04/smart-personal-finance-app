package com.example.personalfinance.firebase

import com.google.firebase.auth.FirebaseUser

fun interface FirebaseAuthCallback {
    fun onSuccess(user: FirebaseUser)
    fun onFailure(exception: Exception)
}
