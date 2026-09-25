package com.example.personalfinance.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalfinance.models.domain.Budget
import com.example.personalfinance.models.domain.Category
import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.repositories.AccountRepository
import com.example.personalfinance.repositories.BudgetRepository
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class BudgetViewModel : ViewModel() {

    private val budgetRepository = BudgetRepository()
    private val accountRepository = AccountRepository() // to fetch categories for AddBudget

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _budgetsCreated = MutableLiveData<Int>()
    val budgetsCreated: LiveData<Int> = _budgetsCreated

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadBudgets(userId: Int) {
        _isLoading.value = true
        budgetRepository.getBudgets(userId, object : ApiCallback<List<Budget>?> {
            override fun onSuccess(result: List<Budget>?) {
                _budgets.postValue(result ?: emptyList())
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải ngân sách: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }

    fun loadCategories(userId: Int) {
        _isLoading.value = true
        accountRepository.getCategories(userId, object : ApiCallback<List<Category>?> {
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

    fun saveBudgets(budgetsToCreate: List<Budget>?, budgetsToUpdate: List<Budget>?) {
        val creates = budgetsToCreate ?: emptyList()
        val validUpdates = budgetsToUpdate?.filter { it.budgetId != null } ?: emptyList()

        val totalRequestCount = creates.size + validUpdates.size
        if (totalRequestCount == 0) {
            _errorMessage.value = "Vui lòng nhập ngân sách cho ít nhất một danh mục"
            return
        }

        _isLoading.value = true
        val remainingRequests = AtomicInteger(totalRequestCount)
        val hasError = AtomicBoolean(false)

        val callback = object : ApiCallback<Budget?> {
            override fun onSuccess(result: Budget?) = finishRequest()
            override fun onError(errorMessage: String?) {
                if (hasError.compareAndSet(false, true)) {
                    _errorMessage.postValue("Lỗi lưu ngân sách: $errorMessage")
                }
                finishRequest()
            }
            private fun finishRequest() {
                if (remainingRequests.decrementAndGet() == 0) {
                    if (!hasError.get()) _budgetsCreated.postValue(totalRequestCount)
                    _isLoading.postValue(false)
                }
            }
        }

        creates.forEach { budgetRepository.createBudget(it, callback) }
        validUpdates.forEach { budgetRepository.updateBudget(it.budgetId!!, it, callback) }
    }
}
