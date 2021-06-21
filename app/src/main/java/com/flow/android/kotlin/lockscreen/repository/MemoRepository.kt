package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import androidx.annotation.MainThread
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class MemoRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val memoDao = appDatabase.memoDao()
    private val compositeDisposable = CompositeDisposable()

    fun delete(memo: Memo, @MainThread onDeleted: (memo: Memo) -> Unit) {
        compositeDisposable.add(memoDao.delete(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onDeleted(memo) }, { Timber.e(it) })
        )
    }

    fun insert(memo: Memo, @MainThread onInserted: (memo: Memo) -> Unit) {
        compositeDisposable.add(memoDao.insert(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onInserted(memo) }, { Timber.e(it) })
        )
    }

    fun update(memo: Memo, @MainThread onUpdated: (memo: Memo) -> Unit) {
        compositeDisposable.add(memoDao.update(memo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onUpdated(memo) }, { Timber.e(it) })
        )
    }

    suspend fun updateAll(list: List<Memo>) {
        memoDao.updateAll(list)
    }

    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }

    fun getAll() = memoDao.getAll()

    fun getTodayMemos(): List<Memo> {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()

        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)

        end.set(Calendar.HOUR_OF_DAY, 0)
        end.set(Calendar.MINUTE, 0)
        end.set(Calendar.SECOND, 0)
        end.add(Calendar.DATE, 1)

        return memoDao.getAll(start.timeInMillis, end.timeInMillis)
    }
}