package com.example.personalfinance.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.repositories.AccountRepository;

import java.util.List;

public class AccountViewModel extends ViewModel {

    private final AccountRepository repository;

    private final MutableLiveData<List<Account>> accounts = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Account> accountCreated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AccountViewModel() {
        this.repository = new AccountRepository();
    }

    public LiveData<List<Account>> getAccounts() {
        return accounts;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Account> getAccountCreated() {
        return accountCreated;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadAccounts(int userId) {
        isLoading.setValue(true);
        repository.getAccounts(userId, new AccountRepository.ApiCallback<List<Account>>() {
            @Override
            public void onSuccess(List<Account> result) {
                accounts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải ví: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void createAccount(Account account) {
        isLoading.setValue(true);
        repository.createAccount(account, new AccountRepository.ApiCallback<Account>() {
            @Override
            public void onSuccess(Account result) {
                accountCreated.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tạo ví: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void loadCategories(int userId) {
        isLoading.setValue(true);
        repository.getCategories(userId, new AccountRepository.ApiCallback<List<Category>>() {
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
}
