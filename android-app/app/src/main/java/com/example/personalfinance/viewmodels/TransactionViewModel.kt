package com.example.personalfinance.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalfinance.models.domain.Account
import com.example.personalfinance.models.domain.Category
import com.example.personalfinance.models.domain.Transaction
import com.example.personalfinance.models.dto.ScanFeedbackRequest
import com.example.personalfinance.models.dto.ProductFeedbackRequest
import com.example.personalfinance.api.ApiCallback
import com.example.personalfinance.repositories.AccountRepository
import com.example.personalfinance.repositories.TransactionRepository

class TransactionViewModel : ViewModel() {

    private val transactionRepository = TransactionRepository()
    private val accountRepository = AccountRepository()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _transactionCreated = MutableLiveData<Transaction>()
    val transactionCreated: LiveData<Transaction> = _transactionCreated

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadTransactions(userId: Int, startDate: String, endDate: String) {
        _isLoading.value = true
        transactionRepository.getTransactions(userId, startDate, endDate, object : ApiCallback<List<Transaction>?> {
            override fun onSuccess(result: List<Transaction>?) {
                _transactions.postValue(result ?: emptyList())
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải lịch sử giao dịch: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }

    fun loadFormData(userId: Int) {
        _isLoading.value = true
        // Load accounts
        accountRepository.getAccounts(userId, object : ApiCallback<List<Account>?> {
            override fun onSuccess(result: List<Account>?) {
                _accounts.postValue(result ?: emptyList())
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải ví: $errorMessage")
            }
        })
        // Load categories
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

    fun createTransaction(
        transaction: Transaction,
        aiScanLogId: Int,
        aiProductLogId: Int,
        actualCategoryId: Int
    ) {
        _isLoading.value = true
        transactionRepository.createTransaction(transaction, object : ApiCallback<Transaction?> {
            override fun onSuccess(result: Transaction?) {
                result?.let { _transactionCreated.postValue(it) }
                _isLoading.postValue(false)

                // Submit silent feedback if OCR-based transaction
                if (aiScanLogId > 0 && result != null) {
                    val feedback = ScanFeedbackRequest(aiScanLogId, result.transactionId, actualCategoryId)
                    transactionRepository.submitScanFeedback(feedback, object : ApiCallback<String?> {
                        override fun onSuccess(result: String?) {} // Silent success
                        override fun onError(errorMessage: String?) {} // Silent error
                    })
                }

                // Submit silent feedback if YOLO-based transaction
                if (aiProductLogId > 0 && result != null) {
                    val feedback = ProductFeedbackRequest(aiProductLogId, result.transactionId)
                    transactionRepository.submitProductFeedback(feedback, object : ApiCallback<String?> {
                        override fun onSuccess(result: String?) {} // Silent success
                        override fun onError(errorMessage: String?) {} // Silent error
                    })
                }
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi lưu giao dịch: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }

    fun updateTransaction(id: Int, transaction: Transaction) {
        _isLoading.value = true
        transactionRepository.updateTransaction(id, transaction, object : ApiCallback<Transaction?> {
            override fun onSuccess(result: Transaction?) {
                result?.let { _transactionCreated.postValue(it) }
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi cập nhật giao dịch: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }
}
