package com.flow.android.kotlin.lockscreen.color

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.view.ViewTreeObserver.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.flow.android.kotlin.lockscreen.R
import kotlin.math.pow

class ColorHelper private constructor(private val application: Application) {
    private val dark: Int by lazy { ContextCompat.getColor(application, R.color.dark) }
    private val light: Int by lazy { ContextCompat.getColor(application, R.color.light) }

    private var viewPagerRegionColor = light

    fun viewPagerRegionColor() = viewPagerRegionColor
    fun setViewPagerRegionColor(viewPager2: ViewPager2, bitmap: Bitmap) {
        viewPager2.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val bottom = viewPager2.bottom
                val left = viewPager2.left
                val right = viewPager2.right
                val top = viewPager2.top

                Palette.Builder(bitmap).setRegion(left, top, right, bottom).generate { palette ->
                    val dominantColor = palette?.getDominantColor(dark) ?: light

                    viewPagerRegionColor = colorDependingOnBackground(dominantColor)
                }

                viewPager2.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun setTextColor(textView: TextView, bitmap: Bitmap) {
        textView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val bottom = textView.bottom
                val left = textView.left
                val right = textView.right
                val top = textView.top

                // todo; error point
                Palette.Builder(bitmap).setRegion(left, top, right, bottom).generate { palette ->
                    val dominantColor = palette?.getDominantColor(dark) ?: light
                    val textColor = colorDependingOnBackground(dominantColor)
                    val shadowColor =
                            if (textColor == dark)
                                light
                            else
                                dark

                    textView.setShadowLayer(4F, 4F, 4F, shadowColor)
                    textView.setTextColor(textColor)
                }

                textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    @ColorInt
    fun colorDependingOnBackground(@ColorInt colorInt: Int): Int {
        var red = Color.red(colorInt) / 255.0
        var green = Color.green(colorInt) / 255.0
        var blue = Color.blue(colorInt) / 255.0

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

    companion object {
        @Volatile
        private var INSTANCE: ColorHelper? = null

        fun getInstance(application: Application): ColorHelper {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ColorHelper(application)
                    INSTANCE = instance
                }

                return instance
            }
        }

        @ColorInt
        fun colorDependingOnBackground(@ColorInt colorInt: Int, @ColorInt dark: Int, @ColorInt light: Int): Int {
            var red = Color.red(colorInt) / 255.0
            var green = Color.green(colorInt) / 255.0
            var blue = Color.blue(colorInt) / 255.0

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
}