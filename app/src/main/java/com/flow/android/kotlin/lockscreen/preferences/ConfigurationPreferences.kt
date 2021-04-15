package com.flow.android.kotlin.lockscreen.preferences

import android.content.Context

object ConfigurationPreferences {

    @Suppress("SpellCheckingInspection")
    private object Name {
        const val DisplayAfterUnlocking = "com.flow.android.kotlin.lockscreen.preferences.name.display_after_unlocking"
        const val SelectedTabIndex = "com.flow.android.kotlin.lockscreen.preferences.name.selected_tab_index"
        const val ShowOnLockScreen = "com.flow.android.kotlin.lockscreen.preferences.name.show_on_lock_screen"
        const val UncheckedCalendarIds = "com.flow.android.kotlin.lockscreen.preferences.name.unchecked_calendar_ids"
    }

    @Suppress("SpellCheckingInspection")
    private object Key {
        const val DisplayAfterUnlocking = "com.flow.android.kotlin.lockscreen.preferences.key.display_after_unlocking"
        const val SelectedTabIndex = "com.flow.android.kotlin.lockscreen.preferences.key.selected_tab_index"
        const val ShowOnLockScreen = "com.flow.android.kotlin.lockscreen.preferences.key.show_on_lock_screen"
        const val UncheckedCalendarIds = "com.flow.android.kotlin.lockscreen.preferences.key.unchecked_calendar_ids"
    }

    fun getShowOnLockScreen(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(Name.ShowOnLockScreen, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key.ShowOnLockScreen, true)
    }

    fun putShowOnLockScreen(context: Context, showOnLockScreen: Boolean) {
        val sharedPreferences = context.getSharedPreferences(Name.ShowOnLockScreen, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(Key.ShowOnLockScreen, showOnLockScreen).apply()
    }

    fun getDisplayAfterUnlocking(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(Name.DisplayAfterUnlocking, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key.DisplayAfterUnlocking, false)
    }

    fun putDisplayAfterUnlocking(context: Context, displayAfterUnlocking: Boolean) {
        val sharedPreferences = context.getSharedPreferences(Name.DisplayAfterUnlocking, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(Key.DisplayAfterUnlocking, displayAfterUnlocking).apply()
    }

    fun getUncheckedCalendarIds(context: Context): Set<String> {
        val sharedPreferences = context.getSharedPreferences(Name.UncheckedCalendarIds, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(Key.UncheckedCalendarIds, setOf()) ?: setOf()
    }

    private fun putUncheckedCalendarIds(context: Context, uncheckedCalendarIds: Set<String>) {
        val sharedPreferences = context.getSharedPreferences(Name.UncheckedCalendarIds, Context.MODE_PRIVATE)
        sharedPreferences.edit().putStringSet(Key.UncheckedCalendarIds, uncheckedCalendarIds).apply()
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
        context.getSharedPreferences(Name.SelectedTabIndex, Context.MODE_PRIVATE).apply {
            edit().putInt(Key.SelectedTabIndex, index).apply()
        }
    }

    fun getSelectedTabIndex(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Name.SelectedTabIndex, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Key.SelectedTabIndex, 0)
    }
}