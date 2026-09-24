package com.example.personalfinance.utils

import android.content.Context
import com.example.personalfinance.models.User
import com.google.gson.Gson

class SharedPrefManager private constructor(context: Context) {

    private val sharedPreferences = context.applicationContext
        .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        sharedPreferences.edit().putString(KEY_USER, Gson().toJson(user)).apply()
    }

    fun getUser(): User? {
        val json = sharedPreferences.getString(KEY_USER, null) ?: return null
        return Gson().fromJson(json, User::class.java)
    }

    fun saveServerIp(ip: String) {
        sharedPreferences.edit().putString(KEY_SERVER_IP, ip).apply()
    }

    fun getServerIp(): String {
        val savedIp = sharedPreferences.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP) ?: DEFAULT_SERVER_IP
        // Migrate legacy IPs to current default
        if (savedIp in listOf(LEGACY_DEFAULT_SERVER_IP, LAN_SERVER_IP, ADB_REVERSE_SERVER_IP)) {
            saveServerIp(DEFAULT_SERVER_IP)
            return DEFAULT_SERVER_IP
        }
        return savedIp
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val SHARED_PREF_NAME = "personal_finance_prefs"
        private const val KEY_USER = "key_user"
        private const val KEY_SERVER_IP = "server_ip"
        private const val LEGACY_DEFAULT_SERVER_IP = "192.168.30.103"
        private const val EMULATOR_HOST_SERVER_IP = "https://unwinsome-vapoury-eustolia.ngrok-free.dev/"
        private const val LAN_SERVER_IP = "192.168.1.63"
        private const val ADB_REVERSE_SERVER_IP = "127.0.0.1"
        private const val DEFAULT_SERVER_IP = EMULATOR_HOST_SERVER_IP

        @Volatile
        private var instance: SharedPrefManager? = null

        fun getInstance(context: Context): SharedPrefManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPrefManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
