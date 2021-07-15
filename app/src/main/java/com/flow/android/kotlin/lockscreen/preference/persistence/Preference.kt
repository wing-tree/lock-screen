package com.flow.android.kotlin.lockscreen.preference.persistence

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object Preference {
    private const val Prefix = "com.flow.android.kotlin.lockscreen.preference.persistence.Preference.Prefix"

    private object Name {
        const val Preference = "$Prefix.Name.Preference"
    }

    private object Key {
        const val FirstRun = "$Prefix.Key.FirstRun"
        const val SelectedTabIndex = "$Prefix.Key.SelectedTabIndex"
    }

    fun putSelectedTabIndex(context: Context, index: Int) {
        context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE).apply {
            edit().putInt(Key.SelectedTabIndex, index).apply()
        }
    }

    fun getSelectedTabIndex(context: Context): Int {
        return context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                .getInt(Key.SelectedTabIndex, 1)
    }

    fun putFirstRun(context: Context, firstRun: Boolean) {
        context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE).apply {
            edit().putBoolean(Key.FirstRun, firstRun).apply()
        }
    }

    fun getFirstRun(context: Context): Boolean {
        return context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                .getBoolean(Key.FirstRun, true)
    }

    object Calendar {
        private object Name {
            const val Preference = "$Prefix.Calendar.Name.Preference"
        }

        private object Key {
            const val UncheckedCalendarIDs = "$Prefix.Key.UncheckedCalendarIDs"
        }

        fun getUncheckedCalendarIds(context: Context): Set<String> {
            return context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                    .getStringSet(Key.UncheckedCalendarIDs, setOf()) ?: setOf()
        }

        private fun putUncheckedCalendarIds(context: Context, value: Set<String>) {
            context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                    .edit().putStringSet(Key.UncheckedCalendarIDs, value).apply()
        }

        fun addUncheckedCalendarId(context: Context, value: String) {
            with(getUncheckedCalendarIds(context).toMutableSet().apply { add(value) }) {
                putUncheckedCalendarIds(context, this)
            }
        }

        fun removeUncheckedCalendarId(context: Context, value: String) {
            with(getUncheckedCalendarIds(context).toMutableSet().apply { remove(value) }) {
                putUncheckedCalendarIds(context, this)
            }
        }
    }

    object Display {
        private object Name {
            const val Preference = "$Prefix.Display.Name.Preference"
        }

        private object Key {
            const val DarkMode = "$Prefix.Display.Key.DarkMode"
            const val FontSize = "$Prefix.Display.Key.FontSize"
        }

        fun putIsDarkMode(context: Context, value: Boolean) {
            context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE).apply {
                edit().putBoolean(Key.DarkMode, value).apply()
            }
        }

        fun getIsDarkMode(context: Context): Boolean {
            val defValue = when(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                AppCompatDelegate.MODE_NIGHT_YES, Configuration.UI_MODE_NIGHT_YES -> true
                AppCompatDelegate.MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_NO -> false
                AppCompatDelegate.MODE_NIGHT_UNSPECIFIED, Configuration.UI_MODE_NIGHT_UNDEFINED -> true
                else -> true
            }

            return context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                    .getBoolean(Key.DarkMode, defValue)
        }

        fun getDarkMode(context: Context): Int {
            return if (getIsDarkMode(context))
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        }

        fun putFontSize(context: Context, value: Float) {
            context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE).apply {
                edit().putFloat(Key.FontSize, value).apply()
            }
        }

        fun getFontSize(context: Context): Float {
            return context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                    .getFloat(Key.FontSize, 16F)
        }
    }

    object LockScreen {
        private object Name {
            const val Preference = "$Prefix.LockScreen.Name.Preference"
        }

        private object Key {
            const val ShowAfterUnlocking = "$Prefix.LockScreen.Key.ShowAfterUnlocking"
            const val ShowOnLockScreen = "$Prefix.LockScreen.Key.ShowOnLockScreen"
        }

        fun getShowOnLockScreen(context: Context): Boolean {
            return context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                    .getBoolean(Key.ShowOnLockScreen, true)
        }

        fun putShowOnLockScreen(context: Context, value: Boolean) {
            context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
                    .edit().putBoolean(Key.ShowOnLockScreen, value).apply()
        }

        fun getShowAfterUnlocking(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(Key.ShowAfterUnlocking, false)
        }

        fun putShowAfterUnlocking(context: Context, value: Boolean) {
            val sharedPreferences = context.getSharedPreferences(Name.Preference, Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean(Key.ShowAfterUnlocking, value).apply()
        }
    }
}