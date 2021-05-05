package com.flow.android.kotlin.lockscreen.memo.listener

import com.flow.android.kotlin.lockscreen.memo.entity.Memo

interface OnMemoChangedListener {
    fun onMemoDeleted(memo: Memo)
    fun onMemoInserted(memo: Memo)
    fun onMemoUpdated(memo: Memo)
}