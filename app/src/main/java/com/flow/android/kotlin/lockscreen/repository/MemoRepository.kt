package com.flow.android.kotlin.lockscreen.repository

import android.content.Context
import androidx.annotation.MainThread
import com.flow.android.kotlin.lockscreen.persistence.database.AppDatabase
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class MemoRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val compositeDisposable = CompositeDisposable()
    private val dao = appDatabase.noteDao()

    fun delete(note: Note, @MainThread onComplete: (() -> Unit)? = null) {
        compositeDisposable.add(dao.delete(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onComplete?.invoke() }, { Timber.e(it) })
        )
    }

    fun insert(note: Note, @MainThread onComplete: (() -> Unit)? = null) {
        compositeDisposable.add(dao.insert(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onComplete?.invoke() }, { Timber.e(it) })
        )
    }

    fun update(note: Note, @MainThread onComplete: (() -> Unit)? = null) {
        compositeDisposable.add(dao.update(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onComplete?.invoke() }, { Timber.e(it) })
        )
    }

    fun updateAll(list: List<Note>, @MainThread onComplete: (() -> Unit)? = null) {
        compositeDisposable.add(dao.updateAll(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onComplete?.invoke() }, { Timber.e(it) })
        )
    }

    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }

    suspend fun getAll() = dao.getAll()

    fun getTodayMemos(): List<Note> {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()

        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)

        end.set(Calendar.HOUR_OF_DAY, 0)
        end.set(Calendar.MINUTE, 0)
        end.set(Calendar.SECOND, 0)
        end.add(Calendar.DATE, 1)

        return dao.getAll(start.timeInMillis, end.timeInMillis)
    }
}