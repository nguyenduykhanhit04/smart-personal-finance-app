package com.example.personalfinance.repositories

import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.api.enqueueCallback
import com.example.personalfinance.models.Account
import com.example.personalfinance.models.Category

class AccountRepository {

    fun getAccounts(userId: Int, callback: ApiCallback<List<Account>?>) {
        RetrofitClient.getApiService().getAccounts(userId)
            .enqueueCallback("Lỗi không xác định khi tải danh sách ví", callback)
    }

    fun createAccount(account: Account, callback: ApiCallback<Account?>) {
        RetrofitClient.getApiService().createAccount(account)
            .enqueueCallback("Lỗi không xác định khi thêm ví", callback)
    }

    fun updateAccount(id: Int, account: Account, callback: ApiCallback<Account?>) {
        RetrofitClient.getApiService().updateAccount(id, account)
            .enqueueCallback("Lỗi không xác định khi cập nhật ví", callback)
    }

    fun getCategories(userId: Int, callback: ApiCallback<List<Category>?>) {
        RetrofitClient.getApiService().getCategories(userId)
            .enqueueCallback("Lỗi tải danh mục", callback)
    }
}
