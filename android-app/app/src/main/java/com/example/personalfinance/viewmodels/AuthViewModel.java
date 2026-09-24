package com.example.personalfinance.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.personalfinance.firebase.FirebaseAuthCallback;
import com.example.personalfinance.firebase.FirebaseAuthHelper;
import com.example.personalfinance.models.LoginRequest;
import com.example.personalfinance.models.User;
import com.example.personalfinance.repositories.AuthRepository;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;

    private final MutableLiveData<User> syncedUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel() {
        this.repository = new AuthRepository();
    }

    public LiveData<User> getSyncedUser() {
        return syncedUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void signIn(String email, String password, FirebaseAuthHelper authHelper) {
        isLoading.setValue(true);
        authHelper.signIn(email, password, new FirebaseAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                syncUserWithBackend(firebaseUser, null);
            }

            @Override
            public void onFailure(Exception exception) {
                errorMessage.postValue(getReadableAuthError(exception));
                isLoading.postValue(false);
            }
        });
    }

    public void signUp(String email, String password, final String fullName, FirebaseAuthHelper authHelper) {
        isLoading.setValue(true);
        authHelper.signUp(email, password, fullName, new FirebaseAuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                syncUserWithBackend(firebaseUser, fullName);
            }

            @Override
            public void onFailure(Exception exception) {
                errorMessage.postValue(getReadableAuthError(exception));
                isLoading.postValue(false);
            }
        });
    }

    public void syncUserWithBackend(FirebaseUser firebaseUser, String customFullName) {
        isLoading.setValue(true);
        String fullName = customFullName;
        if (fullName == null || fullName.isEmpty()) {
            fullName = firebaseUser.getDisplayName();
        }
        if (fullName == null || fullName.isEmpty()) {
            fullName = firebaseUser.getEmail() != null ? firebaseUser.getEmail().split("@")[0] : "User";
        }

        LoginRequest request = new LoginRequest(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                fullName,
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : ""
        );

        repository.firebaseLogin(request, new AuthRepository.ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                syncedUser.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Đồng bộ server thất bại: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public String getReadableAuthError(Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            return "Không kết nối được tới Firebase. Kiểm tra Internet trên điện thoại rồi thử lại.";
        }

        String message = exception != null ? exception.getMessage() : null;
        if (message == null || message.trim().isEmpty()) {
            return "Đăng nhập thất bại. Vui lòng thử lại.";
        }

        String lowerMessage = message.toLowerCase(Locale.ROOT);
        if (lowerMessage.contains("network error")
                || lowerMessage.contains("timeout")
                || lowerMessage.contains("interrupted connection")
                || lowerMessage.contains("unreachable host")) {
            return "Không kết nối được tới Firebase. Kiểm tra Internet trên điện thoại rồi thử lại.";
        }

        return message;
    }
}
