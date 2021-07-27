package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.base.DataChanged
import com.flow.android.kotlin.lockscreen.base.DataChangedState
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.calendar.model.Model
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference.isChanged
import com.flow.android.kotlin.lockscreen.repository.MemoRepository
import com.flow.android.kotlin.lockscreen.repository.ShortcutRepository
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    init {
        Timber.d("init :$this")
    }

    var isInitialized = false

    val calendarViewModel = CalendarViewModel()
    val memoViewModel = MemoViewModel()
    val shortcutViewModel = ShortcutViewModel()

    val contentResolver: ContentResolver = application.contentResolver

    private var _viewPagerRegionColor = Color.WHITE
    val viewPagerRegionColor: Int
        @ColorInt
        get() = _viewPagerRegionColor

    private val _showRequestCalendarPermissionSnackbar = SingleLiveEvent<Unit>()
    val showRequestCalendarPermissionSnackbar: LiveData<Unit>
        get() = _showRequestCalendarPermissionSnackbar

    fun callShowRequestCalendarPermissionSnackbar() {
        _showRequestCalendarPermissionSnackbar.call()
    }

    fun refresh(preferenceChanged: Preference.PreferenceChanged) {
        if (isChanged(preferenceChanged.fondSize, preferenceChanged.timeFormat))
            memoViewModel.callRefresh()

        if (isChanged(preferenceChanged.fondSize, preferenceChanged.uncheckedCalendarIds))
            calendarViewModel.callRefresh()
    }

    override fun onCleared() {
        memoViewModel.clearCompositeDisposable()
        shortcutViewModel.clearComposeDisposable()
        super.onCleared()
    }

    inner class CalendarViewModel {
        private val contentResolver = getApplication<MainApplication>().contentResolver

        private val _calendars = MutableLiveData<List<Model.Calendar>>()
        val calendars: LiveData<List<Model.Calendar>>
            get() = _calendars

        private val _disableCalendarControlViews = SingleLiveEvent<Unit>()
        val disableCalendarControlViews: LiveData<Unit>
            get() = _disableCalendarControlViews

        private val _refresh = SingleLiveEvent<Unit>()
        val refresh: LiveData<Unit>
            get() = _refresh

        fun callDisableCalendarControlViews() {
            _disableCalendarControlViews.call()
        }

        fun callRefresh() {
            _refresh.call()
        }

        fun postValue() {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    _calendars.postValue(CalendarLoader.calendars(contentResolver))
                }
            }
        }
    }
    
    inner class MemoViewModel {
        private val repository = MemoRepository(getApplication<MainApplication>())
        val publishSubject = PublishSubject.create<DataChanged<Memo>>()

        fun clearCompositeDisposable() {
            repository.clearCompositeDisposable()
        }

        suspend fun getAll() = repository.getAll()

        private val _refresh = SingleLiveEvent<Unit>()
        val refresh: LiveData<Unit>
            get() = _refresh

        fun callRefresh() {
            _refresh.call()
        }

        fun delete(memo: Memo) {
            repository.delete(memo) { publishSubject.onNext(DataChanged(memo, DataChangedState.Deleted)) }
        }

        fun insert(memo: Memo) {
            repository.insert(memo) { publishSubject.onNext(DataChanged(memo, DataChangedState.Inserted)) }
        }

        fun update(memo: Memo, onComplete: (() -> Unit)? = null) {
            repository.update(memo) {
                onComplete?.invoke()
                publishSubject.onNext(DataChanged(memo, DataChangedState.Updated))
            }
        }

        fun updateAll(list: List<Memo>) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateAll(list)
            }
        }
    }
    
    inner class ShortcutViewModel {
        private val packageManager = getApplication<MainApplication>().packageManager
        private val repository = ShortcutRepository(getApplication<MainApplication>())

        private val _dataChanged = MutableLiveData<DataChanged<com.flow.android.kotlin.lockscreen.shortcut.model.Model.Shortcut>>()
        val dataChanged: LiveData<DataChanged<com.flow.android.kotlin.lockscreen.shortcut.model.Model.Shortcut>>
            get() = _dataChanged

        fun clearComposeDisposable() {
            repository.clearCompositeDisposable()
        }

        suspend fun getAll() = repository.getAll().mapNotNull { it.toModel() }

        private fun Shortcut.toModel(): com.flow.android.kotlin.lockscreen.shortcut.model.Model.Shortcut? {
            return try {
                val packageName = this.packageName
                val info = packageManager.getApplicationInfo(packageName, 0)
                val icon = packageManager.getApplicationIcon(info)
                val label = packageManager.getApplicationLabel(info).toString()

                com.flow.android.kotlin.lockscreen.shortcut.model.Model.Shortcut(icon, label, packageName, priority, showInNotification)
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.e(e)
                delete(this)
                null
            }
        }

        fun insert(item: Shortcut, onComplete: (com.flow.android.kotlin.lockscreen.shortcut.model.Model.Shortcut) -> Unit) {
            repository.insert(item) {
                item.toModel()?.let { model ->
                    onComplete(model)
                    _dataChanged.value = DataChanged(model, DataChangedState.Inserted)
                }
            }
        }

        fun delete(item: Shortcut) {
            repository.delete(item) {
                it.toModel()?.let { model ->
                    _dataChanged.value = DataChanged(model, DataChangedState.Deleted)
                }
            }
        }

        fun updateAll(list: List<com.flow.android.kotlin.lockscreen.shortcut.model.Model.Shortcut>) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateAll(list.map { it.toEntity() })
            }
        }
    }
}