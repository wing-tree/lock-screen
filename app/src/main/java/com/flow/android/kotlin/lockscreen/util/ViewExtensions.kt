package com.flow.android.kotlin.lockscreen.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.drawable.RippleDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.animation.*
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat


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

fun FrameLayout.showRipple(@ColorInt colorPressed: Int? = null) {
    if (foreground is RippleDrawable) {
        val rippleDrawable = foreground

        rippleDrawable.state = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_enabled
        )
    }
}

fun View.collapse(duration: Long) {
    val measuredHeight: Int = this.measuredHeight

    visibility = View.VISIBLE

    val animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1F) {
                layoutParams.height = 0
                visibility = View.GONE
            } else {
                layoutParams.height = if ((measuredHeight - (measuredHeight * interpolatedTime).toInt()) > 0)
                    measuredHeight - (measuredHeight * interpolatedTime).toInt()
                else
                    0
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean = true
    }

    animation.duration = duration
    startAnimation(animation)
}

fun View.expand(duration: Long, onAnimationEnd: (() -> Unit)? = null) {
    val parent = this.parent
    val widthMeasureSpec = if (parent is View)
        MeasureSpec.makeMeasureSpec(parent.width, MeasureSpec.EXACTLY)
    else
        return

    val heightMeasureSpec = MeasureSpec.makeMeasureSpec(1, MeasureSpec.UNSPECIFIED)

    measure(widthMeasureSpec, heightMeasureSpec)

    val from = this.height
    val to = this.measuredHeight

    layoutParams.height = from

    show()

    val valueAnimator = ValueAnimator.ofInt(from, to)

    valueAnimator.addUpdateListener {
        layoutParams.height = it.animatedValue as Int
        alpha = it.animatedValue as Int / to.toFloat()
        requestLayout()
    }

    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator?) {
            onAnimationEnd?.invoke()
        }

        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    })

    valueAnimator.interpolator = DecelerateInterpolator()
    valueAnimator.duration = duration
    valueAnimator.start()
}

fun View.fadeIn(
        duration: Number,
        onAnimationEnd: (() -> Unit)? = null,
        alphaFrom: Float = 0F,
) {
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

fun View.fadeOut(duration: Number, invisible: Boolean = false, onAnimationEnd: (() -> Unit)? = null) {
    this.apply {
        alpha = 1F

        animate()
            .alpha(0F)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    this@fadeOut.visibility = if (invisible)
                        View.INVISIBLE
                    else
                        View.GONE

                    onAnimationEnd?.invoke()
                    super.onAnimationEnd(animation)
                }
            })
    }
}

fun View.showRipple() {
    if (background is RippleDrawable) {
        val rippleDrawable = background

        rippleDrawable.state = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_enabled
        )
    }
}

fun View.hideRipple() {
    if (background is RippleDrawable) {
        background.state = intArrayOf()
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

fun View.scale(scale: Float, duration: Number = 150, alpha: Float = 1F) {
    show()

    this.animate()
        .scaleX(scale)
        .scaleY(scale)
        .alpha(alpha)
        .setDuration(duration.toLong())
        .start()
}

fun View.hide(invisible: Boolean = false) {
    visibility = if (invisible)
        View.INVISIBLE
    else
        View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.expand(duration: Long, to: Int, onAnimationEnd: (() -> Unit)? = null) {
    val from = this.height

    show()

    val valueAnimator = ValueAnimator.ofInt(from, to)

    valueAnimator.addUpdateListener {
        layoutParams.height = it.animatedValue as Int
        alpha = it.animatedValue as Int / to.toFloat()
        requestLayout()
    }

    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator?) {
            onAnimationEnd?.invoke()
        }

        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    })

    valueAnimator.interpolator = DecelerateInterpolator()
    valueAnimator.duration = duration
    valueAnimator.start()
}

fun View.collapse(duration: Long, to: Int, hide: Boolean = true, onAnimationEnd: (() -> Unit)? = null) {
    val from = height
    val valueAnimator = ValueAnimator.ofInt(from, to)

    valueAnimator.interpolator = DecelerateInterpolator()
    valueAnimator.addUpdateListener { animation ->
        layoutParams.height = animation.animatedValue as Int
        alpha = animation.animatedValue as Int / from.toFloat()
        requestLayout()
    }

    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator?) {
            if (hide)
                hide()

            onAnimationEnd?.invoke()
        }

        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    })

    valueAnimator.interpolator = DecelerateInterpolator()
    valueAnimator.duration = duration
    valueAnimator.start()
}

fun View.measuredHeight(view: View): Int {
    val widthMeasureSpec = MeasureSpec.makeMeasureSpec(view.width, MeasureSpec.EXACTLY)
    val heightMeasureSpec = MeasureSpec.makeMeasureSpec(1, MeasureSpec.UNSPECIFIED)

    measure(widthMeasureSpec, heightMeasureSpec)

    return measuredHeight
}

fun View.setBackgroundTint(@ColorRes id: Int) {
    backgroundTintList = ContextCompat.getColorStateList(this.context, id)
}

fun CheckBox.setButtonTint(@ColorRes id: Int) {
    buttonTintList = ContextCompat.getColorStateList(this.context, id)
}