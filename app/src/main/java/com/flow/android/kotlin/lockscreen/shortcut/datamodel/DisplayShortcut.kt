package com.flow.android.kotlin.lockscreen.shortcut.datamodel

import android.graphics.drawable.Drawable
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut

data class DisplayShortcut (
    val icon: Drawable,
    val label: String,
    val packageName: String,
    val shortcut: Shortcut? = null
)