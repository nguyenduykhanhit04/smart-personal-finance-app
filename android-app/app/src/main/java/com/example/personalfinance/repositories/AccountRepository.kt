package com.example.personalfinance.repositories

import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.api.ApiClient
import com.example.personalfinance.api.enqueueCallback
import com.example.personalfinance.models.domain.Account
import com.example.personalfinance.models.domain.Category

class AccountRepository {

    fun getAccounts(userId: Int, callback: ApiCallback<List<Account>?>) {
        ApiClient.getApiService().getAccounts(userId)
            .enqueueCallback("Lỗi không xác định khi tải danh sách ví", callback)
    }

    fun createAccount(account: Account, callback: ApiCallback<Account?>) {
        ApiClient.getApiService().createAccount(account)
            .enqueueCallback("Lỗi không xác định khi thêm ví", callback)
    }

    fun updateAccount(id: Int, account: Account, callback: ApiCallback<Account?>) {
        ApiClient.getApiService().updateAccount(id, account)
            .enqueueCallback("Lỗi không xác định khi cập nhật ví", callback)
    }

    fun getCategories(userId: Int, callback: ApiCallback<List<Category>?>) {
        ApiClient.getApiService().getCategories(userId)
            .enqueueCallback("Lỗi tải danh mục", callback)
    }
}
