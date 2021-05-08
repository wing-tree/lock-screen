package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import androidx.annotation.MainThread
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import timber.log.Timber

class Repository(context: Context) {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val appDatabase = AppDatabase.getInstance(context)
    private val memoDao = appDatabase.memoDao()
    private val compositeDisposable = CompositeDisposable()

    fun deleteMemo(memo: Memo, @MainThread onDeleted: (memo: Memo) -> Unit) {
        compositeDisposable.add(memoDao.delete(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onDeleted(memo) }, { Timber.e(it) })
        )
    }

    fun insertMemo(memo: Memo, @MainThread onInserted: (memo: Memo) -> Unit) {
        compositeDisposable.add(memoDao.insert(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onInserted(memo) }, { Timber.e(it) })
        )
    }

    fun updateMemo(memo: Memo, @MainThread onUpdated: (memo: Memo) -> Unit) {
        compositeDisposable.add(memoDao.update(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onUpdated(memo) }, { Timber.e(it) })
        )
    }

    suspend fun updateMemos(list: List<Memo>) {
        memoDao.updateList(list)
    }

    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }

    fun getAllMemos() = memoDao.getAll()
}