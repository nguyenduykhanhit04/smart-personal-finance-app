package com.example.personalfinance.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.personalfinance.models.User;
import com.google.gson.Gson;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "personal_finance_prefs";
    private static final String KEY_USER = "key_user";
    private static final String KEY_SERVER_IP = "server_ip";
    private static final String LEGACY_DEFAULT_SERVER_IP = "192.168.30.103";
    private static final String EMULATOR_HOST_SERVER_IP = "https://unwinsome-vapoury-eustolia.ngrok-free.dev/";
    private static final String LAN_SERVER_IP = "192.168.1.63";
    private static final String ADB_REVERSE_SERVER_IP = "127.0.0.1";
    private static final String DEFAULT_SERVER_IP = EMULATOR_HOST_SERVER_IP;
    
    private static SharedPrefManager mInstance;
    private final SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(mCtx.getApplicationContext());
        }
        return mInstance;
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    public User getUser() {
        String json = sharedPreferences.getString(KEY_USER, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, User.class);
        }
        return null;
    }

    public void saveServerIp(String ip) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SERVER_IP, ip);
        editor.apply();
    }

    public String getServerIp() {
        String savedIp = sharedPreferences.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP);
        if (LEGACY_DEFAULT_SERVER_IP.equals(savedIp)
                || LAN_SERVER_IP.equals(savedIp)
                || ADB_REVERSE_SERVER_IP.equals(savedIp)) {
            saveServerIp(DEFAULT_SERVER_IP);
            return DEFAULT_SERVER_IP;
        }
        return savedIp;
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
