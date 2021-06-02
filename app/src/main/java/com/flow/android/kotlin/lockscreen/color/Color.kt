package com.flow.android.kotlin.lockscreen.color

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.preferences.ColorPreferences
import com.flow.android.kotlin.lockscreen.util.toPx
import timber.log.Timber
import kotlin.math.pow

object Color {
    fun colorDependingOnWallpaper(context: Context, wallpaper: Bitmap, screenWidth: Int): ColorDependingOnBackground {
        @ColorInt
        val dark: Int = ContextCompat.getColor(context, R.color.dark)
        @ColorInt
        val light: Int = ContextCompat.getColor(context, R.color.light)

        val dateTimeBottom = 168.toPx
        val dateTimeLeft = 24.toPx
        val dateTimeRight = screenWidth / 2
        val dateTimeTop = 64.toPx

        val iconBottom = 72.toPx
        val iconLeft = screenWidth / 2
        val iconRight = screenWidth - 24.toPx
        val iconTop = 48.toPx

        val tabLayoutBottom = 224.toPx
        val tabLayoutLeft = 24.toPx
        val tabLayoutRight = screenWidth - 24.toPx
        val tabLayoutTop = 168.toPx

        val viewPagerBottom = 324.toPx
        val viewPagerLeft = 24.toPx
        val viewPagerRight = screenWidth - 24.toPx
        val viewPagerTop = 224.toPx

        val floatingActionButtonBottom = 380.toPx
        val floatingActionButtonLeft = screenWidth / 2 - 28.toPx
        val floatingActionButtonRight = screenWidth / 2 + 28.toPx
        val floatingActionButtonTop = 324.toPx

        var dateTimeTextColor = light
        var iconTint = light
        var tabTextColor = light
        var onViewPagerColor = light
        var floatingActionButtonTint = light

        try {
            val builder = Palette.Builder(wallpaper)

            builder.setRegion(
                    dateTimeLeft,
                    dateTimeTop,
                    dateTimeRight,
                    dateTimeBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                dateTimeTextColor = colorDependingOnWallpaper(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, dateTimeTextColor)
            }

            builder.setRegion(
                    iconLeft,
                    iconTop,
                    iconRight,
                    iconBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                iconTint = colorDependingOnWallpaper(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, iconTint)
            }

            builder.setRegion(
                    tabLayoutLeft,
                    tabLayoutTop,
                    tabLayoutRight,
                    tabLayoutBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                tabTextColor = colorDependingOnWallpaper(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, tabTextColor)
            }

            builder.setRegion(
                    viewPagerLeft,
                    viewPagerTop,
                    viewPagerRight,
                    viewPagerBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                onViewPagerColor = colorDependingOnWallpaper(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, onViewPagerColor)
            }

            builder.setRegion(
                    floatingActionButtonLeft,
                    floatingActionButtonTop,
                    floatingActionButtonRight,
                    floatingActionButtonBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                floatingActionButtonTint = colorDependingOnWallpaper(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, floatingActionButtonTint)
            }
        } catch(e: IllegalArgumentException) {
            Timber.e(e)
            dateTimeTextColor = ColorPreferences.getDateTimeTextColor(context)
            iconTint = ColorPreferences.getIconTint(context)
            tabTextColor = ColorPreferences.getTabTextColor(context)
            onViewPagerColor = ColorPreferences.getOnViewPagerColor(context)
            floatingActionButtonTint = ColorPreferences.getFloatingActionButtonTint(context)
        } finally {
            return ColorDependingOnBackground(
                    dateTimeTextColor = dateTimeTextColor,
                    iconTint = iconTint,
                    tabTextColor = tabTextColor,
                    onViewPagerColor = onViewPagerColor,
                    floatingActionButtonTint = floatingActionButtonTint
            )
        }
    }

    @ColorInt
    fun colorDependingOnWallpaper(
        @ColorInt colorInt: Int,
        @ColorInt dark: Int,
        @ColorInt light: Int,
        useSimpleFormula: Boolean = true
    ): Int {
        var red = Color.red(colorInt) / 255.0
        var green = Color.green(colorInt) / 255.0
        var blue = Color.blue(colorInt) / 255.0

        if (useSimpleFormula) {
            return if (red * 0.299 + green * 0.587 + blue * 0.114 > 186)
                dark
            else
                light
        }

        if (red <= 0.03928)
            red /= 12.92
        else
            red = ((red + 0.055) / 1.055).pow(2.4)

        if (green <= 0.03928)
            green /= 12.92
        else
            green = ((green + 0.055) / 1.055).pow(2.4)

        if (blue <= 0.03928)
            blue /= 12.92
        else
            blue = ((red + 0.055) / 1.055).pow(2.4)

        val y = 0.2126 * red + 0.7152 * green + 0.0722 * blue

        return if (y > 0.179)
            dark
        else
            light
    }
}

data class ColorDependingOnBackground(
        @ColorInt
        val dateTimeTextColor: Int,
        @ColorInt
        var iconTint: Int,
        @ColorInt
        var tabTextColor: Int,
        @ColorInt
        var onViewPagerColor: Int,
        @ColorInt
        var floatingActionButtonTint: Int
)