package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import androidx.annotation.MainThread
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.persistence.data.entity.Shortcut
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ShortcutRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val dao = appDatabase.shortcutDao()
    private val compositeDisposable = CompositeDisposable()

    fun delete(shortcut: Shortcut, @MainThread onDeleted: (shortcut: Shortcut) -> Unit) {
        compositeDisposable.add(dao.delete(shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDeleted(shortcut) }, { Timber.e(it) })
        )
    }

    fun insert(shortcut: Shortcut, @MainThread onInserted: (shortcut: Shortcut) -> Unit) {
        compositeDisposable.add(dao.insert(shortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onInserted(shortcut) }, { Timber.e(it) })
        )
    }

    suspend fun updateAll(shortcuts: List<Shortcut>) {
        dao.updateAll(shortcuts)
    }

    fun getAll() = dao.getAll()

    fun getAllValue() = dao.getAllValue()

    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }
}