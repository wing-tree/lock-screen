package com.flow.android.kotlin.lockscreen.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.RippleDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout

fun FrameLayout.forceRippleAnimation() {
    if (foreground is RippleDrawable) {
        val handler = Handler(Looper.getMainLooper())
        val rippleDrawable = foreground

        rippleDrawable.state = intArrayOf(
            android.R.attr.state_pressed,
            android.R.attr.state_enabled
        )
        handler.postDelayed({ rippleDrawable.state = intArrayOf() }, 200)
    }
}

fun FrameLayout.hideRipple() {
    if (foreground is RippleDrawable) {
        foreground.state = intArrayOf()
    }
}

fun FrameLayout.showRipple() {
    if (foreground is RippleDrawable) {
        val rippleDrawable = foreground

        rippleDrawable.state = intArrayOf(
            android.R.attr.state_pressed,
            android.R.attr.state_enabled
        )
    }
}

fun View.collapse(to: Int, duration: Number) {
    val measuredHeight: Int = this.measuredHeight
    val animation: Animation = object : Animation() {
        override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation?
        ) {
            if (interpolatedTime == 1F) {
                this@collapse.layoutParams.height = to
            } else {
                this@collapse.layoutParams.height =
                        if ((measuredHeight - (measuredHeight * interpolatedTime).toInt()) > to)
                            measuredHeight - (measuredHeight * interpolatedTime).toInt()
                        else
                            to
            }

            this@collapse.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    animation.duration = duration.toLong()
    this.fadeOut(animation.duration)
    this.startAnimation(animation)
}

fun View.expand(duration: Number) {
    val widthMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(
            (this.parent as View).width,
            View.MeasureSpec.EXACTLY
    )

    val heightMeasureSpec: Int =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

    this.measure(widthMeasureSpec, heightMeasureSpec)

    val to: Int = this.measuredHeight
    val height = this.height

    this.layoutParams.height = height
    this.visibility = View.VISIBLE

    val animation: Animation = object : Animation() {
        override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation?
        ) {
            if (interpolatedTime == 1F)
                this@expand.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            else
                this@expand.layoutParams.height =
                        if ((to * interpolatedTime).toInt() < height)
                            height
                        else
                            (to * interpolatedTime).toInt()
            this@expand.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    animation.duration = duration.toLong()
    this.fadeIn(animation.duration)
    this.startAnimation(animation)
}

fun View.fadeIn(duration: Number, onAnimationEnd: (() -> Unit)? = null, alphaFrom: Float = 0F) {
    this.apply {
        alpha = alphaFrom
        visibility = View.VISIBLE

        animate()
            .alpha(1F)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onAnimationEnd?.invoke()
                    super.onAnimationEnd(animation)
                }
            })
    }
}

fun View.fadeOut(duration: Number, onAnimationEnd: (() -> Unit)? = null) {
    this.apply {
        alpha = 1F

        animate()
            .alpha(0F)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    this@fadeOut.visibility = View.GONE
                    onAnimationEnd?.invoke()
                    super.onAnimationEnd(animation)
                }
            })
    }
}

fun View.rotate(
        degrees: Float, duration: Number,
        animationListenerAdapter: AnimatorListenerAdapter? = null
) {
    this.animate().rotation(degrees)
            .setDuration(duration.toLong())
            .setListener(animationListenerAdapter)
            .start()
}

fun View.scale(scale: Float, duration: Number = 200, alpha: Float = 1F) {
    this.animate()
        .scaleX(scale)
        .scaleY(scale)
        .alpha(alpha)
        .setDuration(duration.toLong())
        .start()
}

fun View.translateToBottom(duration: Number, onAnimationEnd: (() -> Unit)? = null) {
    this.apply {
        alpha = 0F

        animate()
            .alpha(1F)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onAnimationEnd?.invoke()
                    super.onAnimationEnd(animation)
                }
            })
            .translationY(0F)
    }
}

fun View.translateToTop(duration: Number, onAnimationEnd: (() -> Unit)? = null) {
    this.apply {
        alpha = 1F

        animate()
            .alpha(0F)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onAnimationEnd?.invoke()
                    super.onAnimationEnd(animation)
                }
            })
            .translationY(-height.toFloat())
    }
}

fun View.hide(invisible: Boolean = false) {
    visibility =
        if (invisible)
            View.INVISIBLE
        else
            View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}