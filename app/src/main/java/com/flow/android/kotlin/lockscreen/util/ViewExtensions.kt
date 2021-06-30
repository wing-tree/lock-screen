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
import android.widget.FrameLayout
import timber.log.Timber


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
    show()
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
    this.startAnimation(animation)
}

fun View.collapse(duration: Long) {
    val measuredHeight: Int = this.measuredHeight

    visibility = View.VISIBLE

    Timber.d("view id: ${this.id}")

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

fun View.expand(duration: Long) {
    measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    val measuredHeight = this.measuredHeight
    val height = this.height

    this.layoutParams.height = height
    this.visibility = View.VISIBLE

    val animation: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            if (interpolatedTime == 1F)
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            else
                layoutParams.height =
                        if ((measuredHeight * interpolatedTime).toInt() < height)
                            height
                        else
                            (measuredHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean = true
    }

    animation.duration = duration
    animation.interpolator = DecelerateInterpolator()
    startAnimation(animation)
}

fun View.expand(expand: Int, duration: Number) {
    val heightMeasureSpec: Int =
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

    this.measure(ViewGroup.LayoutParams.MATCH_PARENT, heightMeasureSpec)

    val to: Int = this.measuredHeight + expand
    val height = this.height

    this.layoutParams.height = height
    this.visibility = View.VISIBLE

    val animation: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            if (interpolatedTime == 1F)
                this@expand.layoutParams.height = to
            else {
                this@expand.layoutParams.height =
                    if ((to * interpolatedTime).toInt() < height)
                        height
                    else
                        (to * interpolatedTime).toInt()
            }
            this@expand.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    animation.duration = duration.toLong()
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

// test
fun expand(v: View, duration: Long, targetHeight: Int) {
    val prevHeight = v.height
    v.visibility = View.VISIBLE
    val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
    valueAnimator.addUpdateListener { animation ->
        v.layoutParams.height = animation.animatedValue as Int
        v.alpha = animation.animatedValue as Int / targetHeight.toFloat()
        v.requestLayout()
    }
    Timber.d("exex43:::22 ${v.id}, target: $targetHeight,, prev: $prevHeight")
    valueAnimator.interpolator = AccelerateDecelerateInterpolator()
    valueAnimator.duration = duration
    valueAnimator.start()
}

fun collapse(v: View, duration: Int, targetHeight: Int) {
    val prevHeight = v.height
    val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
    valueAnimator.interpolator = AccelerateDecelerateInterpolator()
    valueAnimator.addUpdateListener { animation ->
        v.layoutParams.height = animation.animatedValue as Int
        v.alpha = animation.animatedValue as Int / prevHeight.toFloat()
        v.requestLayout()
    }
    Timber.d("collapse2302211::: ${v.id}")
    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            v.hide()
        }

        override fun onAnimationCancel(animation: Animator?) {

        }

        override fun onAnimationRepeat(animation: Animator?) {
        }

    })
    valueAnimator.duration = duration.toLong()
    valueAnimator.start()
}

fun View.getM(vv: View): Int {
    val widthSpec = MeasureSpec.makeMeasureSpec(vv.width, MeasureSpec.EXACTLY)
    val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    measure(widthSpec, heightSpec)
    return getMeasuredHeight()
}