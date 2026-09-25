package com.example.personalfinance.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalfinance.models.domain.Account
import com.example.personalfinance.models.domain.Category
import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.repositories.AccountRepository

class AccountViewModel : ViewModel() {

    private val repository = AccountRepository()

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _accountCreated = MutableLiveData<Account>()
    val accountCreated: LiveData<Account> = _accountCreated

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadAccounts(userId: Int) {
        _isLoading.value = true
        repository.getAccounts(userId, object : ApiCallback<List<Account>?> {
            override fun onSuccess(result: List<Account>?) {
                _accounts.postValue(result ?: emptyList())
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải ví: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }

    fun createAccount(account: Account) {
        _isLoading.value = true
        repository.createAccount(account, object : ApiCallback<Account?> {
            override fun onSuccess(result: Account?) {
                result?.let { _accountCreated.postValue(it) }
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tạo ví: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }

    fun loadCategories(userId: Int) {
        _isLoading.value = true
        repository.getCategories(userId, object : ApiCallback<List<Category>?> {
            override fun onSuccess(result: List<Category>?) {
                _categories.postValue(result ?: emptyList())
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải danh mục: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }
}
