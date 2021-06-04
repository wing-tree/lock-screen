package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import androidx.annotation.MainThread
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class Repository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val memoDao = appDatabase.memoDao()
    private val shortcutDao = appDatabase.shortcutDao()
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
        memoDao.updateAll(list)
    }

    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }

    fun getAllMemos() = memoDao.getAll()

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

    fun deleteShortcut(shortcut: Shortcut, @MainThread onDeleted: (shortcut: Shortcut) -> Unit) {
        compositeDisposable.add(shortcutDao.delete(shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDeleted(shortcut) }, { Timber.e(it) })
        )
    }

    fun insertShortcut(shortcut: Shortcut, @MainThread onInserted: (shortcut: Shortcut) -> Unit) {
        compositeDisposable.add(shortcutDao.insert(shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onInserted(shortcut) }, { Timber.e(it) })
        )
    }

    suspend fun updateShortcuts(shortcuts: List<Shortcut>) {
        shortcutDao.updateAll(shortcuts)
    }

    fun getAllShortcuts() = shortcutDao.getAll()
}