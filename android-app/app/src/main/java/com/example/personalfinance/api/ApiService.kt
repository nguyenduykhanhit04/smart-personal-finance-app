package com.example.personalfinance.api

import com.example.personalfinance.models.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // AUTH
    @POST("api/auth/firebase-login")
    fun firebaseLogin(@Body loginRequest: LoginRequest): Call<ApiResponse<User>>

    @PUT("api/users/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: User): Call<ApiResponse<User>>

    // ACCOUNTS
    @GET("api/accounts")
    fun getAccounts(@Query("userId") userId: Int): Call<ApiResponse<List<Account>>>

    @POST("api/accounts")
    fun createAccount(@Body account: Account): Call<ApiResponse<Account>>

    @PUT("api/accounts/{id}")
    fun updateAccount(@Path("id") id: Int, @Body account: Account): Call<ApiResponse<Account>>

    // CATEGORIES
    @GET("api/categories")
    fun getCategories(@Query("userId") userId: Int): Call<ApiResponse<List<Category>>>

    // TRANSACTIONS
    @GET("api/transactions")
    fun getTransactions(
        @Query("userId") userId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Call<ApiResponse<List<Transaction>>>

    @POST("api/transactions")
    fun createTransaction(@Body transaction: Transaction): Call<ApiResponse<Transaction>>

    @PUT("api/transactions/{id}")
    fun updateTransaction(@Path("id") id: Int, @Body transaction: Transaction): Call<ApiResponse<Transaction>>

    @DELETE("api/transactions/{id}")
    fun deleteTransaction(@Path("id") id: Int): Call<ApiResponse<Void>>

    // BUDGETS
    @GET("api/budgets")
    fun getBudgets(@Query("userId") userId: Int): Call<ApiResponse<List<Budget>>>

    @POST("api/budgets")
    fun createBudget(@Body budget: Budget): Call<ApiResponse<Budget>>

    @PUT("api/budgets/{id}")
    fun updateBudget(@Path("id") id: Int, @Body budget: Budget): Call<ApiResponse<Budget>>

    // REPORTS
    @GET("api/reports/daily")
    fun getDailyReport(
        @Query("userId") userId: Int,
        @Query("date") date: String
    ): Call<ApiResponse<ReportDTO>>

    @GET("api/reports/monthly")
    fun getMonthlyReport(
        @Query("userId") userId: Int,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Call<ApiResponse<ReportDTO>>

    @GET("api/reports/by-category")
    fun getCategoryReport(
        @Query("userId") userId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Call<ApiResponse<ReportDTO>>

    // AI SCAN (OCR)
    @POST("api/ai-scan/classify")
    fun classifyBill(@Body request: OcrRequest): Call<ApiResponse<AiScanResult>>

    @POST("api/ai-scan/feedback")
    fun submitScanFeedback(@Body request: ScanFeedbackRequest): Call<ApiResponse<String>>

    // AI PRODUCT (YOLO)
    @POST("api/ai-product/classify")
    fun classifyProduct(@Body request: ProductClassificationRequest): Call<ApiResponse<AiProductResult>>

    @POST("api/ai-product/feedback")
    fun submitProductFeedback(@Body request: ProductFeedbackRequest): Call<ApiResponse<String>>

    // RECURRING TRANSACTIONS
    @GET("api/recurring-transactions")
    fun getRecurringTransactions(@Query("userId") userId: Int): Call<ApiResponse<List<RecurringTransaction>>>

    @POST("api/recurring-transactions")
    fun createRecurringTransaction(@Body dto: RecurringTransaction): Call<ApiResponse<RecurringTransaction>>

    @PUT("api/recurring-transactions/{id}")
    fun updateRecurringTransaction(@Path("id") id: Int, @Body dto: RecurringTransaction): Call<ApiResponse<RecurringTransaction>>

    @DELETE("api/recurring-transactions/{id}")
    fun deleteRecurringTransaction(@Path("id") id: Int): Call<ApiResponse<Void>>

    // TRANSACTION IMAGES
    @Multipart
    @POST("api/transaction-images/upload")
    fun uploadTransactionImage(
        @Query("transactionId") transactionId: Int,
        @Part file: MultipartBody.Part
    ): Call<ApiResponse<Void>>

    // USER AVATAR
    @Multipart
    @POST("api/users/{id}/avatar")
    fun uploadAvatar(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Call<ApiResponse<User>>
}
