package com.flow.android.kotlin.lockscreen.preferences

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object Preference {

    @Suppress("SpellCheckingInspection")
    private object Name {
        const val Configuration = "com.flow.android.kotlin.lockscreen.preferences.ConfigurationPreferences.Name.Configuration"
    }

    @Suppress("SpellCheckingInspection")
    private object Key {
        private const val Prefix = "com.flow.android.kotlin.lockscreen.preferences.Key"
        const val DarkMode = "$Prefix.DarkMode"
        const val DisplayAfterUnlocking = "$Prefix.display_after_unlocking"
        const val FirstRun = "$Prefix.first_run"
        const val FontSize = "$Prefix.FontSize"
        const val SelectedTabIndex = "$Prefix.selected_tab_index"
        const val ShowOnLockScreen = "$Prefix.show_on_lock_screen"
        const val UncheckedCalendarIDs = "$Prefix.unchecked_calendar_ids"
    }

    fun getShowOnLockScreen(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key.ShowOnLockScreen, true)
    }

    fun putShowOnLockScreen(context: Context, showOnLockScreen: Boolean) {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(Key.ShowOnLockScreen, showOnLockScreen).apply()
    }

    fun getShowAfterUnlocking(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key.DisplayAfterUnlocking, false)
    }

    fun putShowAfterUnlocking(context: Context, displayAfterUnlocking: Boolean) {
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

    fun putIsNightMode(context: Context, value: Boolean) {
        context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE).apply {
            edit().putBoolean(Key.DarkMode, value).apply()
        }
    }

    fun getIsNightMode(context: Context): Boolean {
        val defValue = when(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            AppCompatDelegate.MODE_NIGHT_YES, Configuration.UI_MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED, Configuration.UI_MODE_NIGHT_UNDEFINED -> true
            else -> true
        }

        return context.getSharedPreferences(Name.Configuration, Context.MODE_PRIVATE)
                .getBoolean(Key.DarkMode, defValue)
    }

    fun getNightMode(context: Context): Int {
        return if (getIsNightMode(context))
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO
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