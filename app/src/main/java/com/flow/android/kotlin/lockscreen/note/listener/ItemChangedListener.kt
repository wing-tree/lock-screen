package com.flow.android.kotlin.lockscreen.note.listener

import com.flow.android.kotlin.lockscreen.persistence.entity.Note

interface ItemChangedListener {
    fun onDelete(note: Note)
    fun onInsert(note: Note)
    fun onUpdate(note: Note, onComplete: (() -> Unit)? = null)
}