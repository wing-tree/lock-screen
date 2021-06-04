package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.calendar.CalendarDisplay
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.color.ColorDependingOnBackground
import com.flow.android.kotlin.lockscreen.persistence.entity.Memo
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut
import com.flow.android.kotlin.lockscreen.repository.Repository
import com.flow.android.kotlin.lockscreen.shortcut.datamodel.*
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contentResolver = application.contentResolver
    private val repository = Repository(application)

    private var _viewPagerRegionColor = Color.WHITE
    val viewPagerRegionColor: Int
        @ColorInt
        get() = _viewPagerRegionColor

    override fun onCleared() {
        repository.clearCompositeDisposable()
        super.onCleared()
    }

    private val _calendarDisplays = MutableLiveData<List<CalendarDisplay>>()
    val calendarDisplays: LiveData<List<CalendarDisplay>>
        get() = _calendarDisplays

    val memos = repository.getAllMemos()
    val shortcuts = repository.getAllShortcuts()

    fun shortcuts() = shortcuts.value

    fun calendarDisplays() = calendarDisplays.value
    fun contentResolver(): ContentResolver = contentResolver

    private val _events = MutableLiveData<List<Event>>()

    private val _refreshEvents = SingleLiveEvent<Unit>()
    val refreshEvents: SingleLiveEvent<Unit>
        get() = _refreshEvents

    fun callRefreshEvents() {
        _refreshEvents.call()
    }

    private val _memoChanged = MutableLiveData<MemoChanged>()
    val memoChanged: LiveData<MemoChanged>
        get() = _memoChanged

    private fun notifyMemoChanged(value: MemoChanged) {
        _memoChanged.value = value
    }

    private val _colorDependingOnBackground = MutableLiveData<ColorDependingOnBackground>()
    val colorDependingOnBackground: LiveData<ColorDependingOnBackground>
        get() = _colorDependingOnBackground

    fun setColorDependingOnBackground(value: ColorDependingOnBackground) {
        _colorDependingOnBackground.value = value
    }

    fun submitEvents(events: List<Event>) {
        _events.postValue(events)
    }

    fun postCalendarDisplays() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _calendarDisplays.postValue(CalendarHelper.calendarDisplays(contentResolver))
            }
        }
    }

    fun postEvents() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _events.postValue(CalendarHelper.events(contentResolver, CalendarHelper.calendarDisplays(contentResolver), 0))
            }
        }
    }

    fun refreshCalendarDisplays() {
        val value = calendarDisplays.value ?: return
        _calendarDisplays.value = value
    }

    fun addShortcut(item: ShortcutDataModel, onInserted: (ShortcutDataModel) -> Unit) {
        repository.insertShortcut(item.toEntity()) {
            onInserted(item)
        }
    }

    fun deleteShortcut(item: Shortcut, onDeleted: (Shortcut) -> Unit) {
        repository.deleteShortcut(item) {
            onDeleted(item)
        }
    }

    fun updateShortcuts(shortcuts: List<ShortcutDataModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShortcuts(shortcuts.map { it.toEntity() })
        }
    }

    fun deleteMemo(memo: Memo) {
        repository.deleteMemo(memo) {
            notifyMemoChanged(MemoChanged(it, MemoChangedState.Deleted))
        }
    }

    fun insertMemo(memo: Memo) {
        repository.insertMemo(memo) {
            notifyMemoChanged(MemoChanged(it, MemoChangedState.Inserted))
        }
    }

    fun updateMemo(memo: Memo) {
        repository.updateMemo(memo) {
            notifyMemoChanged(MemoChanged(it, MemoChangedState.Updated))
        }
    }

    fun updateMemos(list: List<Memo>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMemos(list)
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