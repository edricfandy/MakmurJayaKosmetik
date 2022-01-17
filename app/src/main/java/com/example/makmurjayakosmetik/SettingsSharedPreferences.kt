package com.example.makmurjayakosmetik

import android.content.Context
import android.content.SharedPreferences

class SettingsSharedPreferences(context: Context) {
    val USE_IMAGE = "USE_IMAGE"
    val SETTINGS_FILE = "settings"
    private val sharedPref: SharedPreferences

    init {
        sharedPref = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
    }

    var use_image: Boolean
        get() = sharedPref.getBoolean(USE_IMAGE, false)
        set(value) {
            sharedPref.edit().putBoolean(USE_IMAGE, value).apply()
        }

    fun clearValues() {
        sharedPref.edit().clear().apply()
    }
}