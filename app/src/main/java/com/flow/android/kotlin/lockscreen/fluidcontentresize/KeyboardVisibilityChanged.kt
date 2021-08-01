package com.flow.android.kotlin.lockscreen.fluidcontentresize

data class KeyboardVisibilityChanged(
    val visible: Boolean,
    val contentViewHeight: Int,
    val previousContentViewHeight: Int
)