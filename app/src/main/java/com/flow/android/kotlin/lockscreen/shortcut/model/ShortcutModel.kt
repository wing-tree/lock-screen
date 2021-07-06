package com.flow.android.kotlin.lockscreen.shortcut.model

import android.graphics.drawable.Drawable
import com.flow.android.kotlin.lockscreen.persistence.data.entity.Shortcut

data class ShortcutModel (
    val icon: Drawable,
    val label: String,
    val packageName: String,
    var priority: Long,
    var showInNotification: Boolean = false
) {
    fun toEntity() = Shortcut(packageName, priority, showInNotification)
}