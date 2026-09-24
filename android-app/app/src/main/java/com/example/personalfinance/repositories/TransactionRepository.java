package com.example.personalfinance.repositories;

import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.ReportDTO;
import com.example.personalfinance.models.ScanFeedbackRequest;
import com.example.personalfinance.models.Transaction;
import com.google.android.gms.common.api.Api;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionRepository {

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public void getTransactions(int userId, String startDate, String endDate, ApiCallback<List<Transaction>> callback) {
        RetrofitClient.getApiService().getTransactions(userId, startDate, endDate)
                .enqueue(new Callback<ApiResponse<List<Transaction>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi tải giao dịch");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void createTransaction(Transaction transaction, ApiCallback<Transaction> callback) {

        RetrofitClient.getApiService().createTransaction(transaction)
                .enqueue(new Callback<ApiResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Transaction>> call, Response<ApiResponse<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi thêm giao dịch");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Transaction>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void updateTransaction(int id, Transaction transaction, ApiCallback<Transaction> callback) {
        RetrofitClient.getApiService().updateTransaction(id, transaction)
                .enqueue(new Callback<ApiResponse<Transaction>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Transaction>> call, Response<ApiResponse<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi sửa giao dịch");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Transaction>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void deleteTransaction(int id, ApiCallback<Void> callback) {
        RetrofitClient.getApiService().deleteTransaction(id)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi không xác định khi xóa giao dịch");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getMonthlyReport(int userId, int year, int month, ApiCallback<ReportDTO> callback) {
        RetrofitClient.getApiService().getMonthlyReport(userId, year, month)
                .enqueue(new Callback<ApiResponse<ReportDTO>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportDTO>> call, Response<ApiResponse<ReportDTO>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải báo cáo tháng");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportDTO>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getDailyReport(int userId, String date, ApiCallback<ReportDTO> callback) {
        RetrofitClient.getApiService().getDailyReport(userId, date)
                .enqueue(new Callback<ApiResponse<ReportDTO>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportDTO>> call, Response<ApiResponse<ReportDTO>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải báo cáo ngày");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportDTO>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getCategoryReport(int userId, String startDate, String endDate, ApiCallback<ReportDTO> callback) {
        RetrofitClient.getApiService().getCategoryReport(userId, startDate, endDate)
                .enqueue(new Callback<ApiResponse<ReportDTO>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportDTO>> call, Response<ApiResponse<ReportDTO>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi tải báo cáo danh mục");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportDTO>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void submitScanFeedback(ScanFeedbackRequest request, ApiCallback<String> callback) {
        RetrofitClient.getApiService().submitScanFeedback(request)
                .enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi gửi phản hồi hóa đơn");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void submitProductFeedback(com.example.personalfinance.models.ProductFeedbackRequest request, ApiCallback<String> callback) {
        RetrofitClient.getApiService().submitProductFeedback(request)
                .enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError(response.body() != null ? response.body().getMessage() : "Lỗi gửi phản hồi sản phẩm");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

}

