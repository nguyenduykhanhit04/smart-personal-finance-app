package com.example.personalfinance.repositories

import com.example.personalfinance.api.RetrofitClient
import com.example.personalfinance.models.ApiResponse
import com.example.personalfinance.models.LoginRequest
import com.example.personalfinance.models.User
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository {

    fun firebaseLogin(loginRequest: LoginRequest, callback: ApiCallback<User?>) {
        RetrofitClient.getApiService().firebaseLogin(loginRequest)
            .enqueue(object : Callback<ApiResponse<User>> {
                override fun onResponse(call: Call<ApiResponse<User>>, response: Response<ApiResponse<User>>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.success) {
                        callback.onSuccess(body.data)
                    } else {
                        // Try to parse error body for a more descriptive message
                        val errorMsg = try {
                            response.errorBody()?.string()?.let { errJson ->
                                Gson().fromJson(errJson, ApiResponse::class.java)?.message
                            } ?: body?.message
                        } catch (e: Exception) {
                            body?.message
                        } ?: "Lỗi không xác định khi đăng nhập"
                        callback.onError(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                    callback.onError(t.message)
                }
            })
    }
}
