package com.example.personalfinance.repositories;

import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountRepository {

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public void getAccounts(int userId, ApiCallback<List<Account>> callback) {
        RetrofitClient.getApiService().getAccounts(userId)
                .enqueue(new Callback<ApiResponse<List<Account>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Account>>> call, Response<ApiResponse<List<Account>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi tải danh sách ví");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Account>>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void createAccount(Account account, ApiCallback<Account> callback) {
        RetrofitClient.getApiService().createAccount(account)
                .enqueue(new Callback<ApiResponse<Account>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Account>> call, Response<ApiResponse<Account>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi thêm ví");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Account>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void updateAccount(int id, Account account, ApiCallback<Account> callback) {
        RetrofitClient.getApiService().updateAccount(id, account)
                .enqueue(new Callback<ApiResponse<Account>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Account>> call, Response<ApiResponse<Account>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi cập nhật ví");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Account>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    // Categories
    public void getCategories(int userId, ApiCallback<List<Category>> callback) {
        RetrofitClient.getApiService().getCategories(userId)
                .enqueue(new Callback<ApiResponse<List<Category>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải danh mục");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

}
