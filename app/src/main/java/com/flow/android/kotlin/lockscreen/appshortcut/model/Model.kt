package com.flow.android.kotlin.lockscreen.appshortcut.model

import android.graphics.drawable.Drawable
import com.flow.android.kotlin.lockscreen.persistence.entity.AppShortcut

sealed class Model {
    data class AppShortcut (
            val icon: Drawable,
            val label: String,
            val packageName: String,
            var priority: Long,
            var showInNotification: Boolean = false
    ): Model() {
        fun toEntity() = AppShortcut(packageName, priority, showInNotification)
    }
}