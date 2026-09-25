package com.example.personalfinance.repositories

import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.api.enqueueCallback
import com.example.personalfinance.models.*

class TransactionRepository {

    fun getTransactions(
        userId: Int, startDate: String, endDate: String,
        callback: ApiCallback<List<Transaction>?>
    ) {
        RetrofitClient.getApiService().getTransactions(userId, startDate, endDate)
            .enqueueCallback("Lỗi không xác định khi tải giao dịch", callback)
    }

    fun createTransaction(transaction: Transaction, callback: ApiCallback<Transaction?>) {
        RetrofitClient.getApiService().createTransaction(transaction)
            .enqueueCallback("Lỗi không xác định khi thêm giao dịch", callback)
    }

    fun updateTransaction(id: Int, transaction: Transaction, callback: ApiCallback<Transaction?>) {
        RetrofitClient.getApiService().updateTransaction(id, transaction)
            .enqueueCallback("Lỗi không xác định khi sửa giao dịch", callback)
    }

    fun deleteTransaction(id: Int, callback: ApiCallback<Void?>) {
        RetrofitClient.getApiService().deleteTransaction(id)
            .enqueueCallback("Lỗi không xác định khi xóa giao dịch", callback)
    }

    fun getMonthlyReport(userId: Int, year: Int, month: Int, callback: ApiCallback<ReportDTO?>) {
        RetrofitClient.getApiService().getMonthlyReport(userId, year, month)
            .enqueueCallback("Lỗi tải báo cáo tháng", callback)
    }

    fun getDailyReport(userId: Int, date: String, callback: ApiCallback<ReportDTO?>) {
        RetrofitClient.getApiService().getDailyReport(userId, date)
            .enqueueCallback("Lỗi tải báo cáo ngày", callback)
    }

    fun getCategoryReport(
        userId: Int, startDate: String, endDate: String,
        callback: ApiCallback<ReportDTO?>
    ) {
        RetrofitClient.getApiService().getCategoryReport(userId, startDate, endDate)
            .enqueueCallback("Lỗi tải báo cáo danh mục", callback)
    }

    fun submitScanFeedback(request: ScanFeedbackRequest, callback: ApiCallback<String?>) {
        RetrofitClient.getApiService().submitScanFeedback(request)
            .enqueueCallback("Lỗi gửi phản hồi hóa đơn", callback)
    }

    fun submitProductFeedback(request: ProductFeedbackRequest, callback: ApiCallback<String?>) {
        RetrofitClient.getApiService().submitProductFeedback(request)
            .enqueueCallback("Lỗi gửi phản hồi sản phẩm", callback)
    }
}
