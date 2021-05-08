package com.flow.android.kotlin.lockscreen.preferences

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt

object ColorPreferences {
    @Suppress("SpellCheckingInspection")
    private object Name {
        const val Color = "com.flow.android.kotlin.lockscreen.preferences.ColorPreferences.Name.Color"
    }

    @Suppress("SpellCheckingInspection")
    private object Key {
        private const val Prefix = "com.flow.android.kotlin.lockscreen.preferences.ColorPreferences.Key"
        const val DateTimeTextColor = "$Prefix.DateTimeTextColor"
        const val FloatingActionButtonTint = "$Prefix.FloatingActionButtonTint"
        const val IconTint = "$Prefix.IconTint"
        const val TabText = "$Prefix.TabText"
        const val OnViewPagerColor = "$Prefix.OnViewPagerColor"
    }

    @ColorInt
    fun getDateTimeTextColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Key.DateTimeTextColor, Color.WHITE)
    }

    fun putDateTimeTextColor(context: Context, @ColorInt textColor: Int) {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        sharedPreferences.edit().putInt(Key.DateTimeTextColor, textColor).apply()
    }

    @ColorInt
    fun getIconTint(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Key.IconTint, Color.WHITE)
    }

    fun putIconTint(context: Context, @ColorInt textColor: Int) {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        sharedPreferences.edit().putInt(Key.IconTint, textColor).apply()
    }

    @ColorInt
    fun getTabTextColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Key.TabText, Color.WHITE)
    }

    fun putTabTextColor(context: Context, @ColorInt textColor: Int) {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        sharedPreferences.edit().putInt(Key.TabText, textColor).apply()
    }

    @ColorInt
    fun getOnViewPagerColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Key.OnViewPagerColor, Color.WHITE)
    }

    fun putOnViewPagerColor(context: Context, @ColorInt textColor: Int) {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        sharedPreferences.edit().putInt(Key.OnViewPagerColor, textColor).apply()
    }

    @ColorInt
    fun getFloatingActionButtonTint(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Key.FloatingActionButtonTint, Color.WHITE)
    }

    fun putFloatingActionButtonTint(context: Context, @ColorInt textColor: Int) {
        val sharedPreferences = context.getSharedPreferences(Name.Color, Context.MODE_PRIVATE)

        sharedPreferences.edit().putInt(Key.FloatingActionButtonTint, textColor).apply()
    }
}