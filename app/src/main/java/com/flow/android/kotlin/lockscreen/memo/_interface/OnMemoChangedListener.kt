package com.flow.android.kotlin.lockscreen.memo._interface

import com.flow.android.kotlin.lockscreen.persistence.data.entity.Memo

interface OnMemoChangedListener {
    fun onMemoDeleted(memo: Memo)
    fun onMemoInserted(memo: Memo)
    fun onMemoUpdated(memo: Memo)
}