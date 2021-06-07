package com.flow.android.kotlin.lockscreen.color

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader.TileMode
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.palette.graphics.Palette
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Suppress("unused")
object ColorCalculator {
    fun TextView.setColorGradient(@ColorInt start: Int, @ColorInt end: Int) {
        val linearGradient = LinearGradient(0F, 0F, 0F, paint.textSize, intArrayOf(start, end), floatArrayOf(0F, 1F), TileMode.CLAMP)
        this.paint.shader = linearGradient
    }

    @ColorInt
    fun Bitmap.dominantColor(): Int {
        return try {
            Palette.Builder(this).generate().getDominantColor(Color.BLACK)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)

            Color.BLACK
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
    fun Int.darken(value: Float): Int {
        return colorToHSL(this).run {
            this[2] -= value
            this[2] = max(0f, min(this[2], 1F))

            hslToColor(this)
        }
    }

    @ColorInt
    fun Int.lighten(value: Float): Int {
        return colorToHSL(this).run {
            this[2] += value
            this[2] = max(0f, min(this[2], 1F))

            hslToColor(this)
        }
    }
}