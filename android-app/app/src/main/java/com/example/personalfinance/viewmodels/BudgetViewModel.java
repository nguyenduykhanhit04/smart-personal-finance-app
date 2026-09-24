package com.example.personalfinance.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.repositories.AccountRepository;
import com.example.personalfinance.repositories.BudgetRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BudgetViewModel extends ViewModel {

    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository; // to fetch categories for AddBudget

    private final MutableLiveData<List<Budget>> budgets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Integer> budgetsCreated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BudgetViewModel() {
        this.budgetRepository = new BudgetRepository();
        this.accountRepository = new AccountRepository();
    }

    public LiveData<List<Budget>> getBudgets() {
        return budgets;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Integer> getBudgetsCreated() {
        return budgetsCreated;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadBudgets(int userId) {
        isLoading.setValue(true);
        budgetRepository.getBudgets(userId, new BudgetRepository.ApiCallback<List<Budget>>() {
            @Override
            public void onSuccess(List<Budget> result) {
                budgets.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải ngân sách: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void loadCategories(int userId) {
        isLoading.setValue(true);
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

    public void saveBudgets(List<Budget> budgetsToCreate, List<Budget> budgetsToUpdate) {
        List<Budget> creates = budgetsToCreate != null ? budgetsToCreate : java.util.Collections.emptyList();
        List<Budget> validUpdates = new java.util.ArrayList<>();
        if (budgetsToUpdate != null) {
            for (Budget budget : budgetsToUpdate) {
                if (budget.getBudgetId() != null) {
                    validUpdates.add(budget);
                }
            }
        }

        int totalRequestCount = creates.size() + validUpdates.size();
        if (totalRequestCount == 0) {
            errorMessage.setValue("Vui lòng nhập ngân sách cho ít nhất một danh mục");
            return;
        }

        isLoading.setValue(true);
        AtomicInteger remainingRequests = new AtomicInteger(totalRequestCount);
        AtomicBoolean hasError = new AtomicBoolean(false);

        BudgetRepository.ApiCallback<Budget> callback = new BudgetRepository.ApiCallback<Budget>() {
            @Override
            public void onSuccess(Budget result) {
                finishRequest();
            }

            @Override
            public void onError(String error) {
                if (hasError.compareAndSet(false, true)) {
                    errorMessage.postValue("Lỗi lưu ngân sách: " + error);
                }
                finishRequest();
            }

            private void finishRequest() {
                if (remainingRequests.decrementAndGet() == 0) {
                    if (!hasError.get()) {
                        budgetsCreated.postValue(totalRequestCount);
                    }
                    isLoading.postValue(false);
                }
            }
        };

        for (Budget budget : creates) {
            budgetRepository.createBudget(budget, callback);
        }

        for (Budget budget : validUpdates) {
            budgetRepository.updateBudget(budget.getBudgetId(), budget, callback);
        }
    }

}
