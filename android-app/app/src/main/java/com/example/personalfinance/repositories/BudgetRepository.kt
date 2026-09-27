package com.example.personalfinance.repositories

import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.api.ApiClient
import com.example.personalfinance.api.enqueueCallback
import com.example.personalfinance.models.domain.Budget

class BudgetRepository {

    fun getBudgets(userId: Int, callback: ApiCallback<List<Budget>?>) {
        ApiClient.getApiService().getBudgets(userId)
            .enqueueCallback("Lỗi không xác định khi tải ngân sách", callback)
    }

    fun createBudget(budget: Budget, callback: ApiCallback<Budget?>) {
        ApiClient.getApiService().createBudget(budget)
            .enqueueCallback("Lỗi không xác định khi thêm ngân sách", callback)
    }

    fun updateBudget(id: Int, budget: Budget, callback: ApiCallback<Budget?>) {
        ApiClient.getApiService().updateBudget(id, budget)
            .enqueueCallback("Lỗi không xác định khi sửa ngân sách", callback)
    }
}
