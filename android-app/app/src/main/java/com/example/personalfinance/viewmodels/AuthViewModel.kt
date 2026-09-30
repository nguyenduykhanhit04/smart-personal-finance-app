package com.example.personalfinance.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalfinance.firebase.FirebaseAuthHelper
import com.example.personalfinance.models.dto.LoginRequest
import com.example.personalfinance.models.domain.User
import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.repositories.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _syncedUser = MutableLiveData<User>()
    val syncedUser: LiveData<User> = _syncedUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun signIn(email: String, password: String, authHelper: FirebaseAuthHelper) {
        _isLoading.value = true
        authHelper.signIn(email, password, object : com.example.personalfinance.firebase.FirebaseAuthCallback {
            override fun onSuccess(user: FirebaseUser) {
                syncUserWithBackend(user, null)
            }
            override fun onFailure(exception: Exception) {
                _errorMessage.postValue(getReadableAuthError(exception))
                _isLoading.postValue(false)
            }
        })
    }

    fun signUp(email: String, password: String, fullName: String, authHelper: FirebaseAuthHelper) {
        _isLoading.value = true
        authHelper.signUp(email, password, fullName, object : com.example.personalfinance.firebase.FirebaseAuthCallback {
            override fun onSuccess(user: FirebaseUser) {
                syncUserWithBackend(user, fullName)
            }
            override fun onFailure(exception: Exception) {
                _errorMessage.postValue(getReadableAuthError(exception))
                _isLoading.postValue(false)
            }
        })
    }

    fun syncUserWithBackend(firebaseUser: FirebaseUser, customFullName: String?) {
        _isLoading.value = true
        val fullName = customFullName?.takeIf { it.isNotEmpty() }
            ?: firebaseUser.displayName?.takeIf { it.isNotEmpty() }
            ?: firebaseUser.email?.split("@")?.firstOrNull()
            ?: "User"

        val request = LoginRequest(
            firebaseUid = firebaseUser.uid,
            email = firebaseUser.email,
            fullName = fullName,
            avatarUrl = firebaseUser.photoUrl?.toString() ?: ""
        )

        repository.firebaseLogin(request, object : ApiCallback<User?> {
            override fun onSuccess(result: User?) {
                _syncedUser.postValue(result)
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Đồng bộ server thất bại: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }

    fun getReadableAuthError(exception: Exception?): String {
        if (exception is FirebaseNetworkException) {
            return "Không kết nối được tới Firebase. Kiểm tra Internet trên điện thoại rồi thử lại."
        }
        val message = exception?.message?.takeIf { it.isNotBlank() }
            ?: return "Đăng nhập thất bại. Vui lòng thử lại."

        val lower = message.lowercase(Locale.ROOT)
        return if (lower.contains("network error") || lower.contains("timeout") ||
            lower.contains("interrupted connection") || lower.contains("unreachable host")) {
            "Không kết nối được tới Firebase. Kiểm tra Internet trên điện thoại rồi thử lại."
        } else {
            message
        }
    }
}
