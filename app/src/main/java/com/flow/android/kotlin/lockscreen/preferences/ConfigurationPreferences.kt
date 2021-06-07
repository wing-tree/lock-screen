package com.flow.android.kotlin.lockscreen.preferences

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object ConfigurationPreferences {

    @Suppress("SpellCheckingInspection")
    private object Name {
        const val Configuration = "com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences.Name.Configuration"
    }

    @Suppress("SpellCheckingInspection")
    private object Key {
        const val DarkMode = "com.flow.android.kotlin.lockscreen.preferences.Key.DarkMode"
        const val DisplayAfterUnlocking = "com.flow.android.kotlin.lockscreen.preferences.key.display_after_unlocking"
        const val FirstRun = "com.flow.android.kotlin.lockscreen.preferences.key.first_run"
        const val FontSize = "com.flow.android.kotlin.lockscreen.preferences.Key.FontSize"
        const val SelectedTabIndex = "com.flow.android.kotlin.lockscreen.preferences.key.selected_tab_index"
        const val ShowOnLockScreen = "com.flow.android.kotlin.lockscreen.preferences.key.show_on_lock_screen"
        const val UncheckedCalendarIDs = "com.flow.android.kotlin.lockscreen.preferences.key.unchecked_calendar_ids"
    }

    fun getShowOnLockScreen(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key.ShowOnLockScreen, true)
    }

    fun putShowOnLockScreen(context: Context, showOnLockScreen: Boolean) {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(Key.ShowOnLockScreen, showOnLockScreen).apply()
    }

    fun getDisplayAfterUnlocking(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key.DisplayAfterUnlocking, false)
    }

    fun putDisplayAfterUnlocking(context: Context, displayAfterUnlocking: Boolean) {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(Key.DisplayAfterUnlocking, displayAfterUnlocking).apply()
    }

    fun getUncheckedCalendarIds(context: Context): Set<String> {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(Key.UncheckedCalendarIDs, setOf()) ?: setOf()
    }

    private fun putUncheckedCalendarIds(context: Context, uncheckedCalendarIDs: Set<String>) {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        sharedPreferences.edit().putStringSet(Key.UncheckedCalendarIDs, uncheckedCalendarIDs).apply()
    }

    fun addUncheckedCalendarId(context: Context, id: String) {
        val uncheckedCalendarIds = getUncheckedCalendarIds(context).toMutableSet().apply { add(id) }
        putUncheckedCalendarIds(context, uncheckedCalendarIds)
    }

    fun removeUncheckedCalendarId(context: Context, id: String) {
        val uncheckedCalendarIds = getUncheckedCalendarIds(context).toMutableSet().apply { remove(id) }
        putUncheckedCalendarIds(context, uncheckedCalendarIds)
    }

    fun putSelectedTabIndex(context: Context, index: Int) {
        context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE).apply {
            edit().putInt(Key.SelectedTabIndex, index).apply()
        }
    }

    fun getSelectedTabIndex(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Key.SelectedTabIndex, 1)
    }

    fun putFirstRun(context: Context, firstRun: Boolean) {
        context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE).apply {
            edit().putBoolean(Key.FirstRun, firstRun).apply()
        }
    }

    fun getFirstRun(context: Context): Boolean {
        return context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE).getBoolean(Key.FirstRun, true)
    }

    fun putDarkMode(context: Context, darkMode: Boolean) {
        context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE).apply {
            edit().putBoolean(Key.DarkMode, darkMode).apply()
        }
    }

    fun getDarkMode(context: Context): Boolean {
        val defValue = when(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            AppCompatDelegate.MODE_NIGHT_YES, Configuration.UI_MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED, Configuration.UI_MODE_NIGHT_UNDEFINED -> true
            else -> true
        }

        return context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
                .getBoolean(Key.DarkMode, defValue)
    }

    fun putFontSize(context: Context, fontSize: Float) {
        context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE).apply {
            edit().putFloat(Key.FontSize, fontSize).apply()
        }
    }

    fun getFontSize(context: Context): Float {
        return context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
                .getFloat(Key.FontSize, 16F)
    }
}