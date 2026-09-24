package com.example.personalfinance.repositories;

import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.LoginRequest;
import com.example.personalfinance.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public void firebaseLogin(LoginRequest loginRequest, ApiCallback<User> callback) {
        RetrofitClient.getApiService().firebaseLogin(loginRequest)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            String errorMsg = "Lỗi không xác định khi đăng nhập";
                            try {
                                if (response.errorBody() != null) {
                                    String errJson = response.errorBody().string();
                                    com.google.gson.Gson gson = new com.google.gson.Gson();
                                    ApiResponse<?> errResponse = gson.fromJson(errJson, ApiResponse.class);
                                    if (errResponse != null && errResponse.getMessage() != null) {
                                        errorMsg = errResponse.getMessage();
                                    }
                                } else if (response.body() != null && response.body().getMessage() != null) {
                                    errorMsg = response.body().getMessage();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            callback.onError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }
}
