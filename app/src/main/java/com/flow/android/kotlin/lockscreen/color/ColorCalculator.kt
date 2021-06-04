package com.flow.android.kotlin.lockscreen.color

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader.TileMode
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.preferences.ColorPreferences
import com.flow.android.kotlin.lockscreen.util.toPx
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ColorCalculator {
    fun TextView.setColorGradient(@ColorInt start: Int, @ColorInt end: Int) {
        val linearGradient = LinearGradient(0F, 0F, 0F, paint.textSize, intArrayOf(start, end), floatArrayOf(0f, 1f), TileMode.CLAMP)
        this.paint.shader = linearGradient
    }

    fun colorDependingOnWallpaper(context: Context, wallpaper: Bitmap, screenWidth: Int): ColorDependingOnBackground {
        @ColorInt
        val dark = ContextCompat.getColor(context, R.color.dark)
        @ColorInt
        val light = ContextCompat.getColor(context, R.color.light)

        val dateTimeBottom = 168.toPx
        val dateTimeLeft = 24.toPx
        val dateTimeRight = screenWidth / 2
        val dateTimeTop = 64.toPx
        @ColorInt
        var dateTimeDominantColor = ContextCompat.getColor(context, R.color.light)

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

                dateTimeDominantColor = dominantColor
                dateTimeTextColor = onBackgroundColor(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, dateTimeTextColor)
            }

            builder.setRegion(
                    iconLeft,
                    iconTop,
                    iconRight,
                    iconBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                iconTint = onBackgroundColor(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, iconTint)
            }

            builder.setRegion(
                    tabLayoutLeft,
                    tabLayoutTop,
                    tabLayoutRight,
                    tabLayoutBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                tabTextColor = onBackgroundColor(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, tabTextColor)
            }

            builder.setRegion(
                    viewPagerLeft,
                    viewPagerTop,
                    viewPagerRight,
                    viewPagerBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                onViewPagerColor = onBackgroundColor(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, onViewPagerColor)
            }

            builder.setRegion(
                    floatingActionButtonLeft,
                    floatingActionButtonTop,
                    floatingActionButtonRight,
                    floatingActionButtonBottom
            ).generate().also {
                val dominantColor = it.getDominantColor(dark)

                floatingActionButtonTint = onBackgroundColor(dominantColor, dark, light)
                ColorPreferences.putIconTint(context, floatingActionButtonTint)
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            dateTimeTextColor = ColorPreferences.getDateTimeTextColor(context)
            iconTint = ColorPreferences.getIconTint(context)
            tabTextColor = ColorPreferences.getTabTextColor(context)
            onViewPagerColor = ColorPreferences.getOnViewPagerColor(context)
            floatingActionButtonTint = ColorPreferences.getFloatingActionButtonTint(context)
        } finally {
            return ColorDependingOnBackground(
                    dateTimeDominantColor = dateTimeDominantColor,
                    dateTimeTextColor = dateTimeTextColor,
                    iconTint = iconTint,
                    tabTextColor = tabTextColor,
                    onViewPagerColor = onViewPagerColor,
                    floatingActionButtonTint = floatingActionButtonTint
            )
        }
    }

    @ColorInt
    fun onBackgroundColor(
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

    @Size(3)
    private fun colorToHSL(
            @ColorInt color: Int,
            @Size(3) hsl: FloatArray = FloatArray(3)
    ): FloatArray {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f

        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        hsl[2] = (max + min) / 2

        if (max == min) {
            hsl[1] = 0f
            hsl[0] = hsl[1]
        } else {
            val d = max - min
            hsl[1] = if (hsl[2] > 0.5f) d / (2f - max - min) else d / (max + min)

            when (max) {
                r -> hsl[0] = (g - b) / d + (if (g < b) 6 else 0)
                g -> hsl[0] = (b - r) / d + 2
                b -> hsl[0] = (r - g) / d + 4
            }

            hsl[0] /= 6f
        }

        return hsl
    }

    @ColorInt
    private fun hslToColor(@Size(3) hsl: FloatArray): Int {
        val r: Float
        val g: Float
        val b: Float

        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]

        if (s == 0f) {
            b = l
            g = b
            r = g
        } else {
            val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
            val p = 2 * l - q
            r = hue2rgb(p, q, h + 1f / 3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1f / 3)
        }

        return Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    private fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var valueT = t
        if (valueT < 0) valueT += 1f
        if (valueT > 1) valueT -= 1f
        if (valueT < 1f / 6) return p + (q - p) * 6f * valueT
        if (valueT < 1f / 2) return q
        return if (valueT < 2f / 3) p + (q - p) * (2f / 3 - valueT) * 6f else p
    }

    @ColorInt
    fun lightenColor(
            @ColorInt color: Int,
            value: Float
    ): Int {
        val hsl = colorToHSL(color)
        hsl[2] += value
        hsl[2] = max(0f, min(hsl[2], 1f))
        return hslToColor(hsl)
    }

    @ColorInt
    fun darkenColor(
            @ColorInt color: Int,
            value: Float
    ): Int {
        val hsl = colorToHSL(color)
        hsl[2] -= value
        hsl[2] = max(0f, min(hsl[2], 1f))
        return hslToColor(hsl)
    }
}

data class ColorDependingOnBackground(
        @ColorInt
        val dateTimeDominantColor: Int,
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