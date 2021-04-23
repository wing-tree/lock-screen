package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import kotlinx.coroutines.*

class LocalRepository(context: Context) {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val appDatabase = AppDatabase.getInstance(context)
    private val memoDao = appDatabase.memoDao()

    fun deleteMemo(memo: Memo) {
        coroutineScope.launch {
            memoDao.delete(memo)
        }
    }

    fun insertMemo(memo: Memo) {
        coroutineScope.launch {
            memoDao.insert(memo)
        }
    }

    fun updateMemo(memo: Memo) {
        coroutineScope.launch {
            memoDao.update(memo)
        }
    }

    fun getAllMemos() = memoDao.getAll()
}