package com.example.personalfinance.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val DEFAULT_BASE_URL = "https://unwinsome-vapoury-eustolia.ngrok-free.dev"
    private const val HTTP_SCHEME = "http://"
    private const val HTTPS_SCHEME = "https://"
    private const val DEFAULT_PORT = "8080"

    @Volatile
    private var baseUrl: String = DEFAULT_BASE_URL

    @Volatile
    private var retrofit: Retrofit? = null

    @Synchronized
    fun updateBaseUrl(newIp: String?) {
        if (newIp.isNullOrBlank()) return
        baseUrl = buildBaseUrl(newIp)
        retrofit = null // Force recreation with new URL
    }

    private fun buildBaseUrl(serverAddress: String): String {
        var clean = serverAddress.trim()
        var scheme = HTTP_SCHEME

        when {
            clean.startsWith(HTTPS_SCHEME) -> {
                scheme = HTTPS_SCHEME
                clean = clean.removePrefix(HTTPS_SCHEME)
            }
            clean.startsWith(HTTP_SCHEME) -> {
                clean = clean.removePrefix(HTTP_SCHEME)
            }
        }

        // Strip path if present
        val slashIndex = clean.indexOf('/')
        if (slashIndex >= 0) clean = clean.substring(0, slashIndex)

        // Append default port only for plain HTTP
        if (scheme == HTTP_SCHEME && !clean.contains(":")) {
            clean = "$clean:$DEFAULT_PORT"
        }

        return "$scheme$clean/"
    }

    @Synchronized
    fun getClient(): Retrofit {
        return retrofit ?: run {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor())
                .addInterceptor(logging)
                .build()

            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofit = it }
        }
    }

    fun getApiService(): ApiService = getClient().create(ApiService::class.java)
}
