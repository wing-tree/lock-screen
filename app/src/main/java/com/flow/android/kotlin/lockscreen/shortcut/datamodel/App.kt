package com.flow.android.kotlin.lockscreen.shortcut.datamodel

import android.graphics.drawable.Drawable

data class App(
    val icon: Drawable,
    val label: String,
    val packageName: String,
)