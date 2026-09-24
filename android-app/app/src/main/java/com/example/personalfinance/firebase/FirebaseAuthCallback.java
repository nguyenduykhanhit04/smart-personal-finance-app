package com.example.personalfinance.firebase;

import com.google.firebase.auth.FirebaseUser;

public interface FirebaseAuthCallback {
    void onSuccess(FirebaseUser user);
    void onFailure(Exception exception);
}
