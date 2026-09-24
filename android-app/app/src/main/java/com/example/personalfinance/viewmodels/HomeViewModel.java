package com.example.personalfinance.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.personalfinance.models.ReportDTO;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.repositories.TransactionRepository;
import com.example.personalfinance.utils.DateUtils;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final TransactionRepository repository;

    private final MutableLiveData<ReportDTO> monthlyReport = new MutableLiveData<>();
    private final MutableLiveData<ReportDTO> dailyReport = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> monthlyTransactions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel() {
        this.repository = new TransactionRepository();
    }

    public LiveData<ReportDTO> getMonthlyReport() {
        return monthlyReport;
    }

    public LiveData<ReportDTO> getDailyReport() {
        return dailyReport;
    }

    public LiveData<List<Transaction>> getMonthlyTransactions() {
        return monthlyTransactions;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchDailyReport(int userId, String date) {
        isLoading.setValue(true);
        repository.getDailyReport(userId, date, new TransactionRepository.ApiCallback<ReportDTO>() {
            @Override
            public void onSuccess(ReportDTO result) {
                dailyReport.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải báo cáo ngày: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void fetchDashboardData(int userId, int month, int year) {
        isLoading.setValue(true);

        repository.getMonthlyReport(userId, year, month, new TransactionRepository.ApiCallback<ReportDTO>() {
            @Override
            public void onSuccess(ReportDTO result) {
                monthlyReport.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải báo cáo: " + error);
                isLoading.postValue(false);
            }
        });

        // Calculate custom month dates dynamically based on selected month and year
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, year);
        cal.set(java.util.Calendar.MONTH, month - 1);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        
        String startDate = DateUtils.formatApiDate(cal.getTime());
        
        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        String endDate = DateUtils.formatApiDate(cal.getTime());

        repository.getTransactions(userId, startDate, endDate, new TransactionRepository.ApiCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> result) {
                monthlyTransactions.postValue(result);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải giao dịch: " + error);
            }
        });
    }
}
