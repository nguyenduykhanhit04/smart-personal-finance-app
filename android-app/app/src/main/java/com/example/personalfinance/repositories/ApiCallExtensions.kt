package com.example.personalfinance.repositories

import com.example.personalfinance.models.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Generic callback interface dùng cho tất cả Repository calls.
 */
interface ApiCallback<T> {
    fun onSuccess(result: T)
    fun onError(errorMessage: String?)
}

/**
 * Extension function: enqueue một Retrofit Call và tự động parse kết quả.
 * Loại bỏ hoàn toàn boilerplate trong mọi Repository.
 *
 * @param defaultError Thông báo lỗi mặc định nếu server không trả về message.
 * @param callback Callback nhận kết quả thành công hoặc lỗi.
 */
fun <T> Call<ApiResponse<T>>.enqueueCallback(
    defaultError: String = "Lỗi không xác định",
    callback: ApiCallback<T?>
) {
    enqueue(object : Callback<ApiResponse<T>> {
        override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                callback.onSuccess(body.data)
            } else {
                callback.onError(body?.message ?: defaultError)
            }
        }

        override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) {
            callback.onError(t.message)
        }
    })
}
