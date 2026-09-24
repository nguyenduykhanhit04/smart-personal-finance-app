package com.example.personalfinance.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String DEFAULT_BASE_URL = "https://unwinsome-vapoury-eustolia.ngrok-free.dev";
    private static final String HTTP_SCHEME = "http://";
    private static final String HTTPS_SCHEME = "https://";
    private static final String DEFAULT_PORT = "8080";
    private static String BASE_URL = DEFAULT_BASE_URL;
    private static Retrofit retrofit = null;

    public static synchronized void updateBaseUrl(String newIp) {
        if (newIp == null || newIp.trim().isEmpty()) return;
        BASE_URL = buildBaseUrl(newIp);
        retrofit = null; // Force recreation of Retrofit instance with new URL
    }

    private static String buildBaseUrl(String serverAddress) {
        String cleanAddress = serverAddress.trim();
        String scheme = HTTP_SCHEME;

        if (cleanAddress.startsWith(HTTPS_SCHEME)) {
            scheme = HTTPS_SCHEME;
            cleanAddress = cleanAddress.substring(HTTPS_SCHEME.length());
        } else if (cleanAddress.startsWith(HTTP_SCHEME)) {
            cleanAddress = cleanAddress.substring(HTTP_SCHEME.length());
        }

        int slashIndex = cleanAddress.indexOf('/');
        if (slashIndex >= 0) {
            cleanAddress = cleanAddress.substring(0, slashIndex);
        }

        if (HTTP_SCHEME.equals(scheme) && !cleanAddress.contains(":")) {
            cleanAddress = cleanAddress + ":" + DEFAULT_PORT;
        }

        return scheme + cleanAddress + "/";
    }

    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new TokenInterceptor())
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
