package com.flow.android.kotlin.lockscreen.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

fun View.collapse(targetHeight: Int, duration: Number) {
    val initialHeight: Int = this.measuredHeight
    val animation: Animation = object : Animation() {
        override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation?
        ) {
            if (interpolatedTime == 1F) {
                this@collapse.layoutParams.height = targetHeight
            } else {
                this@collapse.layoutParams.height =
                        if ((initialHeight - (initialHeight * interpolatedTime).toInt()) > targetHeight)
                            initialHeight - (initialHeight * interpolatedTime).toInt()
                        else
                            targetHeight
            }

            this@collapse.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // 1dp/ms collapse rate
    // animation.duration = (initialHeight / this.context.resources.displayMetrics.density).toLong()

    animation.duration = duration.toLong()
    this.fadeOut(animation.duration)
    this.startAnimation(animation)
}

fun View.expand(duration: Number) {
    val matchParentMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(
            (this.parent as View).width,
            View.MeasureSpec.EXACTLY
    )

    val wrapContentMeasureSpec: Int =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    this.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight: Int = this.measuredHeight
    val originHeight = this.height

    // Older versions of Android (prior to API 21) cancel the animation of 0 height views.
    this.layoutParams.height = originHeight
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
                        if ((targetHeight * interpolatedTime).toInt() < originHeight)
                            originHeight
                        else
                            (targetHeight * interpolatedTime).toInt()
            this@expand.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // 1dp/ms expansion rate
    //animation.duration = (targetHeight / this.context.resources.displayMetrics.density).toLong()
    animation.duration = duration.toLong()
    this.fadeIn(animation.duration)
    this.startAnimation(animation)
}

fun View.fadeIn(duration: Number) {
        this.apply {
                alpha = 0F
                visibility = View.VISIBLE

                animate()
                        .alpha(1F)
                        .setDuration(duration.toLong())
                        .setListener(null)
        }
}

fun View.fadeOut(duration: Number, onAnimationEnd: (() -> Unit)? = null) {
    this.apply {
        alpha = 1F
        visibility = View.VISIBLE

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

fun View.scale(scale: Float, duration: Number = 200) {
    this.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(duration.toLong())
            .start()
}