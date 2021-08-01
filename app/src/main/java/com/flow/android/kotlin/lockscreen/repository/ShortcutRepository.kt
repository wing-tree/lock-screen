package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import androidx.annotation.MainThread
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.persistence.entity.AppShortcut
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ShortcutRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val compositeDisposable = CompositeDisposable()
    private val dao = appDatabase.appShortcutDao()

    fun delete(appShortcut: AppShortcut, @MainThread onDeleted: (appShortcut: AppShortcut) -> Unit) {
        compositeDisposable.add(dao.delete(appShortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDeleted(appShortcut) }, { Timber.e(it) })
        )
    }

    fun insert(appShortcut: AppShortcut, @MainThread onInserted: (appShortcut: AppShortcut) -> Unit) {
        compositeDisposable.add(dao.insert(appShortcut)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onInserted(appShortcut) }, { Timber.e(it) })
        )
    }

    suspend fun updateAll(appShortcuts: List<AppShortcut>) {
        dao.updateAll(appShortcuts)
    }

    suspend fun getAll() = dao.getAll()

    fun disposeComposeDisposable() {
        compositeDisposable.dispose()
    }
}