package com.example.personalfinance.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.models.ScanFeedbackRequest;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.repositories.AccountRepository;
import com.example.personalfinance.repositories.TransactionRepository;

import java.util.List;

public class TransactionViewModel extends ViewModel {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final MutableLiveData<List<Account>> accounts = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Transaction> transactionCreated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TransactionViewModel() {
        this.transactionRepository = new TransactionRepository();
        this.accountRepository = new AccountRepository();
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    public LiveData<List<Account>> getAccounts() {
        return accounts;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Transaction> getTransactionCreated() {
        return transactionCreated;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadTransactions(int userId, String startDate, String endDate) {
        isLoading.setValue(true);
        transactionRepository.getTransactions(userId, startDate, endDate, new TransactionRepository.ApiCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> result) {
                transactions.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải lịch sử giao dịch: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void loadFormData(int userId) {
        isLoading.setValue(true);
        // Load accounts
        accountRepository.getAccounts(userId, new AccountRepository.ApiCallback<List<Account>>() {
            @Override
            public void onSuccess(List<Account> result) {
                accounts.postValue(result);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải ví: " + error);
            }
        });

        // Load categories
        accountRepository.getCategories(userId, new AccountRepository.ApiCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải danh mục: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void createTransaction(Transaction transaction, final int aiScanLogId, final int aiProductLogId, final int actualCategoryId) {
        isLoading.setValue(true);
        transactionRepository.createTransaction(transaction, new TransactionRepository.ApiCallback<Transaction>() {
            @Override
            public void onSuccess(Transaction result) {
                transactionCreated.postValue(result);
                isLoading.postValue(false);
                
                // Submit silent feedback if it is OCR based transaction
                if (aiScanLogId > 0 && result != null) {
                    ScanFeedbackRequest feedback = new ScanFeedbackRequest(aiScanLogId, result.getTransactionId(), actualCategoryId);
                    transactionRepository.submitScanFeedback(feedback, new TransactionRepository.ApiCallback<String>() {
                        @Override
                        public void onSuccess(String r) {
                            // Silent success
                        }

                        @Override
                        public void onError(String e) {
                            // Silent error
                        }
                    });
                }

                // Submit silent feedback if it is YOLO based transaction
                if (aiProductLogId > 0 && result != null) {
                    com.example.personalfinance.models.ProductFeedbackRequest feedback = 
                            new com.example.personalfinance.models.ProductFeedbackRequest(aiProductLogId, result.getTransactionId());
                    transactionRepository.submitProductFeedback(feedback, new TransactionRepository.ApiCallback<String>() {
                        @Override
                        public void onSuccess(String r) {
                            // Silent success
                        }

                        @Override
                        public void onError(String e) {
                            // Silent error
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi lưu giao dịch: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void updateTransaction(int id, Transaction transaction) {
        isLoading.setValue(true);
        transactionRepository.updateTransaction(id, transaction, new TransactionRepository.ApiCallback<Transaction>() {
            @Override
            public void onSuccess(Transaction result) {
                transactionCreated.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi cập nhật giao dịch: " + error);
                isLoading.postValue(false);
            }
        });
    }

}
