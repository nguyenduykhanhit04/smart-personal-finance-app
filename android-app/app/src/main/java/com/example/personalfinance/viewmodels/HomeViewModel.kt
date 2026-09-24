package com.example.personalfinance.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalfinance.models.ReportDTO
import com.example.personalfinance.models.Transaction
import com.example.personalfinance.repositories.ApiCallback
import com.example.personalfinance.repositories.TransactionRepository
import com.example.personalfinance.utils.DateUtils
import java.util.Calendar

class HomeViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _monthlyReport = MutableLiveData<ReportDTO>()
    val monthlyReport: LiveData<ReportDTO> = _monthlyReport

    private val _dailyReport = MutableLiveData<ReportDTO>()
    val dailyReport: LiveData<ReportDTO> = _dailyReport

    private val _monthlyTransactions = MutableLiveData<List<Transaction>>()
    val monthlyTransactions: LiveData<List<Transaction>> = _monthlyTransactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchDailyReport(userId: Int, date: String) {
        _isLoading.value = true
        repository.getDailyReport(userId, date, object : ApiCallback<ReportDTO?> {
            override fun onSuccess(result: ReportDTO?) {
                result?.let { _dailyReport.postValue(it) }
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải báo cáo ngày: $errorMessage")
                _isLoading.postValue(false)
            }
        })
    }

    fun fetchDashboardData(userId: Int, month: Int, year: Int) {
        _isLoading.value = true

        // Fetch monthly report
        repository.getMonthlyReport(userId, year, month, object : ApiCallback<ReportDTO?> {
            override fun onSuccess(result: ReportDTO?) {
                result?.let { _monthlyReport.postValue(it) }
                _isLoading.postValue(false)
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải báo cáo: $errorMessage")
                _isLoading.postValue(false)
            }
        })

        // Calculate custom month date range dynamically based on selected month and year
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startDate = DateUtils.formatApiDate(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = DateUtils.formatApiDate(cal.time)

        repository.getTransactions(userId, startDate, endDate, object : ApiCallback<List<Transaction>?> {
            override fun onSuccess(result: List<Transaction>?) {
                _monthlyTransactions.postValue(result ?: emptyList())
            }
            override fun onError(errorMessage: String?) {
                _errorMessage.postValue("Lỗi tải giao dịch: $errorMessage")
            }
        })
    }
}
