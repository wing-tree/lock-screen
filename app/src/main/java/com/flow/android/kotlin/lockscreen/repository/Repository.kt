package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import androidx.annotation.MainThread
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import timber.log.Timber

class Repository(context: Context) {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val appDatabase = AppDatabase.getInstance(context)
    private val memoDao = appDatabase.memoDao()

    fun deleteMemo(memo: Memo, @MainThread onDeleted: (memo: Memo) -> Unit) {
        memoDao.delete(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onDeleted(memo) }, { Timber.e(it) })
    }

    fun insertMemo(memo: Memo, @MainThread onInserted: (memo: Memo) -> Unit) {
        memoDao.insert(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onInserted(memo) }, { Timber.e(it) })
    }

    fun updateMemo(memo: Memo, @MainThread onUpdated: (memo: Memo) -> Unit) {
        memoDao.update(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onUpdated(memo) }, { Timber.e(it) })
    }

    fun getAllMemos() = memoDao.getAll()
}