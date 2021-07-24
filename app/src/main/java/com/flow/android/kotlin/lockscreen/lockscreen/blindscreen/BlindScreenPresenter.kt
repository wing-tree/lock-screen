package com.flow.android.kotlin.lockscreen.lockscreen.blindscreen

import android.annotation.SuppressLint
import android.app.Service
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowManager
import com.flow.android.kotlin.lockscreen.R
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.databinding.BlindScreenBinding
import com.flow.android.kotlin.lockscreen.util.hideRipple
import com.flow.android.kotlin.lockscreen.util.scale
import com.flow.android.kotlin.lockscreen.util.showRipple
import com.flow.android.kotlin.lockscreen.util.toPx
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class BlindScreenPresenter {
    private val applicationContext = MainApplication.instance.applicationContext

    private var _viewBinding: BlindScreenBinding? = BlindScreenBinding.inflate(LayoutInflater.from(applicationContext))
    private val viewBinding: BlindScreenBinding
        get() = _viewBinding!!

    private val resources = applicationContext.resources
    private val windowManager = applicationContext.getSystemService(Service.WINDOW_SERVICE) as WindowManager

    private val duration = 300L

    var isBlindScreenVisible = false

    private object Unlock {
        var x = 0F
        var y = 0F
        const val endRange = 600F
        var outOfEndRange = false
    }

    private fun restoreVisibility() {
        viewBinding.frameLayoutRipple.hideRipple()
        viewBinding.linearLayout.scale(1F, duration)
        viewBinding.imageViewUnlock.scale(1F, duration)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (isBlindScreenVisible)
            return

        _viewBinding ?: run {
            _viewBinding = BlindScreenBinding.inflate(LayoutInflater.from(applicationContext))
        }

        restoreVisibility()

        viewBinding.constraintLayout.setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Unlock.x = event.x
                    Unlock.y = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    viewBinding.frameLayoutRipple.showRipple()

                    val distance = sqrt((Unlock.x - event.x).pow(2) + (Unlock.y - event.y).pow(2))
                    var scale = abs(Unlock.endRange - distance * 0.45F) / Unlock.endRange

                    when {
                        scale >= 1F -> scale = 1F
                        scale < 0.75F -> scale = 0.75F
                    }

                    val alpha: Float = (scale - 0.75F) * 4

                    viewBinding.linearLayout.alpha = alpha
                    viewBinding.linearLayout.scaleX = scale
                    viewBinding.linearLayout.scaleY = scale
                    viewBinding.imageViewUnlock.alpha = alpha
                    viewBinding.imageViewUnlock.scaleX = scale
                    viewBinding.imageViewUnlock.scaleY = scale

                    Unlock.outOfEndRange = distance * 1.25F > Unlock.endRange * 0.75F
                }
                MotionEvent.ACTION_UP -> {
                    if (Unlock.outOfEndRange)
                        hide()
                    else
                        restoreVisibility()
                }
            }

            true
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val layoutParams = WindowManager.LayoutParams(
                type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        layoutParams.height = windowHeight()
        layoutParams.windowAnimations = R.style.WindowAnimation

        isBlindScreenVisible = true
        windowManager.addView(viewBinding.root, layoutParams)
    }

    fun hide() {
        if (isBlindScreenVisible) {
            _viewBinding?.let {
                windowManager.removeViewImmediate(viewBinding.root)
                _viewBinding = null
                isBlindScreenVisible = false
            }
        }
    }

    private fun windowHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            windowMetrics.bounds.height() + insets.bottom + insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val navigationBarHeight = navigationBarHeight()
            val statusBarHeight = statusBarHeight()

            displayMetrics.heightPixels + navigationBarHeight.times(2) + statusBarHeight.times(2)
        }
    }

    private fun navigationBarHeight(): Int {
        val identifier = resources
            ?.getIdentifier("navigation_bar_height", "dimen", "android")
            ?: return 48.toPx

        if (identifier > 0)
            return resources.getDimensionPixelSize(identifier)

        return 48.toPx
    }

    private fun statusBarHeight(): Int {
        val identifier = resources.getIdentifier("status_bar_height", "dimen", "android")

        if (identifier > 0)
            return resources.getDimensionPixelSize(identifier)

        return 25.toPx
    }
}