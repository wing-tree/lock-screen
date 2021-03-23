package com.flow.android.kotlin.lockscreen.shared_preferences

import android.content.Context

class SharedPreferencesHelper {

    private object Name {
        const val ShowWhenLocked = "show_when_locked"
    }

    private object Key {
        const val ShowWhenLocked = "show_when_locked"
    }

    fun getShowWhenLocked(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(Name.ShowWhenLocked, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key.ShowWhenLocked, false)
    }

    fun putShowWhenLocked(context: Context, showWhenLocked: Boolean) {
        val sharedPreferences = context.getSharedPreferences(Name.ShowWhenLocked, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(Key.ShowWhenLocked, showWhenLocked).apply()
    }
}