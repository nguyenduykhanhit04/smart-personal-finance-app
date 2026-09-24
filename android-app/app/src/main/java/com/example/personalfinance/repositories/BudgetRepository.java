package com.example.personalfinance.repositories;

import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.Budget;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetRepository {

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public void getBudgets(int userId, ApiCallback<List<Budget>> callback) {
        RetrofitClient.getApiService().getBudgets(userId)
                .enqueue(new Callback<ApiResponse<List<Budget>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Budget>>> call, Response<ApiResponse<List<Budget>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi tải ngân sách");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Budget>>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void createBudget(Budget budget, ApiCallback<Budget> callback) {
        RetrofitClient.getApiService().createBudget(budget)
                .enqueue(new Callback<ApiResponse<Budget>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Budget>> call, Response<ApiResponse<Budget>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi thêm ngân sách");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Budget>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void updateBudget(int id, Budget budget, ApiCallback<Budget> callback) {
        RetrofitClient.getApiService().updateBudget(id, budget)
                .enqueue(new Callback<ApiResponse<Budget>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Budget>> call, Response<ApiResponse<Budget>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi sửa ngân sách");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Budget>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

}
