package com.flow.android.kotlin.lockscreen.shortcut.datamodel

import android.graphics.drawable.Drawable
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut

data class ShortcutDataModel (
    val icon: Drawable,
    val label: String,
    val packageName: String,
    var priority: Long,
    var showInNotification: Boolean = false
) {
    fun toEntity() = Shortcut(packageName, priority, showInNotification)
}