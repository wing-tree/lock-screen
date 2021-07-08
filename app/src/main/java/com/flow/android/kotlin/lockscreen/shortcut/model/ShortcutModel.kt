package com.flow.android.kotlin.lockscreen.shortcut.model

import android.graphics.drawable.Drawable
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut

sealed class Model {
    data class Shortcut (
            val icon: Drawable,
            val label: String,
            val packageName: String,
            var priority: Long,
            var showInNotification: Boolean = false
    ): Model() {
        fun toEntity() = Shortcut(packageName, priority, showInNotification)
    }
}