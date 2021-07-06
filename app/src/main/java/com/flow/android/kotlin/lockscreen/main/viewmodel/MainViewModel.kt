package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.calendar.model.CalendarModel
import com.flow.android.kotlin.lockscreen.calendar.model.EventModel
import com.flow.android.kotlin.lockscreen.configuration.viewmodel.ConfigurationChange
import com.flow.android.kotlin.lockscreen.persistence.data.entity.Memo
import com.flow.android.kotlin.lockscreen.persistence.data.entity.Shortcut
import com.flow.android.kotlin.lockscreen.repository.MemoRepository
import com.flow.android.kotlin.lockscreen.repository.ShortcutRepository
import com.flow.android.kotlin.lockscreen.shortcut.model.*
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contentResolver = application.contentResolver
    private val packageManager = application.packageManager
    private val memoRepository = MemoRepository(application)
    private val shortcutRepository = ShortcutRepository(application)

    override fun onCleared() {
        memoRepository.clearCompositeDisposable()
        shortcutRepository.clearCompositeDisposable()
        super.onCleared()
    }

    private var _viewPagerRegionColor = Color.WHITE
    val viewPagerRegionColor: Int
        @ColorInt
        get() = _viewPagerRegionColor

    private val _calendars = MutableLiveData<List<CalendarModel>>()
    val calendars: LiveData<List<CalendarModel>>
        get() = _calendars

    val memos = memoRepository.getAll()

    val shortcuts: LiveData<List<ShortcutModel>>
        get() = Transformations.map(shortcutRepository.getAll()) { list ->
            list.mapNotNull { it.toModel() }
        }

    val shortcutValues: List<Shortcut>
        get() = shortcutRepository.getAllValue()

    private fun Shortcut.toModel(): ShortcutModel? {
        return try {
            val packageName = this.packageName
            val info = packageManager.getApplicationInfo(packageName, 0)
            val icon = packageManager.getApplicationIcon(info)
            val label = packageManager.getApplicationLabel(info).toString()

            ShortcutModel(icon, label, packageName, priority, showInNotification)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            deleteShortcut(this) {}
            null
        }
    }

    fun calendarDisplays() = calendars.value
    fun contentResolver(): ContentResolver = contentResolver

    private val _events = MutableLiveData<List<EventModel>>()

    private val _refreshEvents = SingleLiveEvent<Unit>()
    val refreshEvents: SingleLiveEvent<Unit>
        get() = _refreshEvents

    fun callRefreshEvents() {
        _refreshEvents.call()
    }

    private val _refreshMemos = SingleLiveEvent<ConfigurationChange>()
    val refreshMemos: SingleLiveEvent<ConfigurationChange>
        get() = _refreshMemos

    private fun callRefreshMemos(configurationChange: ConfigurationChange? = null) {
        configurationChange?.let {
            _refreshMemos.value = it
        } ?: run {
            _refreshMemos.call()
        }
    }

    fun refresh(configurationChange: ConfigurationChange) {
        if (configurationChange.calendarChanged)
            callRefreshEvents()

        if (configurationChange.fondSizeChanged)
            callRefreshMemos()
    }

    private val _memoChanged = MutableLiveData<MemoChanged>()
    val memoChanged: LiveData<MemoChanged>
        get() = _memoChanged

    private fun notifyMemoChanged(value: MemoChanged) {
        _memoChanged.value = value
    }

    fun submitEvents(events: List<EventModel>) {
        _events.postValue(events)
    }

    fun postCalendarDisplays() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _calendars.postValue(CalendarLoader.calendarDisplays(contentResolver))
            }
        }
    }

    fun postEvents() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _events.postValue(CalendarLoader.events(contentResolver, CalendarLoader.calendarDisplays(contentResolver), 0))
            }
        }
    }

    fun refreshCalendarDisplays() {
        val value = calendars.value ?: return
        _calendars.value = value
    }

    fun addShortcut(item: Shortcut, onInserted: (ShortcutModel) -> Unit) {
        shortcutRepository.insert(item) {
            item.toModel()?.let { model -> onInserted(model) }
        }
    }

    fun deleteShortcut(item: Shortcut, onDeleted: (Shortcut) -> Unit) {
        shortcutRepository.delete(item) {
            onDeleted(item)
        }
    }

    fun updateShortcuts(shortcuts: List<ShortcutModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            shortcutRepository.updateAll(shortcuts.map { it.toEntity() })
        }
    }

    fun deleteMemo(memo: Memo) {
        memoRepository.delete(memo) {
            notifyMemoChanged(MemoChanged(it, MemoChangedState.Deleted))
        }
    }

    fun insertMemo(memo: Memo) {
        memoRepository.insert(memo) {
            notifyMemoChanged(MemoChanged(it, MemoChangedState.Inserted))
        }
    }

    fun updateMemo(memo: Memo, @MainThread onComplete: (() -> Unit)? = null) {
        memoRepository.update(memo) {
            onComplete?.invoke() ?: run {
                notifyMemoChanged(MemoChanged(it, MemoChangedState.Updated))
            }
        }
    }

    fun updateMemos(list: List<Memo>) {
        viewModelScope.launch(Dispatchers.IO) {
            memoRepository.updateAll(list)
        }
    }
}

@Parcelize
data class MemoChanged(
        val memo: Memo,
        val state: Int
) : Parcelable

object MemoChangedState {
    const val Deleted = 0
    const val Inserted = 1
    const val Updated = 2
}