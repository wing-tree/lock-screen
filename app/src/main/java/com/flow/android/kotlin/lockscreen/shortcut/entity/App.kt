package com.flow.android.kotlin.lockscreen.shortcut.entity

import android.graphics.drawable.Drawable
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut

data class App(
    val icon: Drawable,
    val label: String,
    val packageName: String,
)