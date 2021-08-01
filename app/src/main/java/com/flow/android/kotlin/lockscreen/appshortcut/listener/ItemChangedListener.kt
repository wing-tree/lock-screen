package com.flow.android.kotlin.lockscreen.appshortcut.listener

import com.flow.android.kotlin.lockscreen.persistence.entity.AppShortcut
import com.flow.android.kotlin.lockscreen.appshortcut.model.Model

interface ItemChangedListener {
    fun onInsert(item: AppShortcut, onComplete: (Model.AppShortcut) -> Unit)
}