package com.example.makmurjayakosmetik

import android.content.Context
import android.content.SharedPreferences

class AccountSharedPreferences(context: Context) {
    val USERNAME = "USERNAME"
    val LOGIN_INFO_FILE = "login_account"
    private var sharePref: SharedPreferences

    init {
        sharePref = context.getSharedPreferences(LOGIN_INFO_FILE, Context.MODE_PRIVATE)
    }

    var username: String?
        get() = sharePref.getString(USERNAME, "Kosong")
        set(value) {
            sharePref.edit().putString(USERNAME, value).apply()
        }

    fun clearValues() {
        sharePref.edit().clear().apply()
    }
}