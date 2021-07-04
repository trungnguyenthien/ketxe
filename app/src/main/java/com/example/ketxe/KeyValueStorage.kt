package com.example.ketxe

import android.content.Context
import android.content.SharedPreferences

private val prefName = "KetXe"
class KeyValueStorage(val context: Context) {
    private val reader = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    private val editor = reader.edit()

    private val isBackgroundServiceRunningKey = "isBackgroundServiceRunning"
    fun setIsBackgroundServiceRunning(isRunning: Boolean) {
        editor.putBoolean(isBackgroundServiceRunningKey, isRunning)
        editor.apply()
    }

    val isBackgroundServiceRunning get() = reader.getBoolean(isBackgroundServiceRunningKey, false)
}