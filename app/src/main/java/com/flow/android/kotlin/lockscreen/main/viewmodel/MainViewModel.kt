package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.os.Parcelable
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.calendar.CalendarDisplay
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.favoriteapp.entity.App
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import com.flow.android.kotlin.lockscreen.preferences.PackageNamePreferences
import com.flow.android.kotlin.lockscreen.repository.Repository
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contentResolver = application.contentResolver
    private val repository = Repository(application)
    private val packageManager = application.packageManager

    private val _calendarDisplays = MutableLiveData<List<CalendarDisplay>>()
    val calendarDisplays: LiveData<List<CalendarDisplay>>
        get() = _calendarDisplays

    private val _favoriteApps = MutableLiveData<List<App>>()
    val favoriteApps: LiveData<List<App>>
        get() = _favoriteApps

    val memos = repository.getAllMemos()

    fun calendarDisplays() = calendarDisplays.value
    fun contentResolver() = contentResolver

    private val _events = MutableLiveData<List<Event>>()

    private val _floatingActionButtonVisibility = MutableLiveData<Int>()
    val floatingActionButtonVisibility: LiveData<Int>
        get() = _floatingActionButtonVisibility

    private val _wallpaper = MutableLiveData<Bitmap>()
    val wallpaper: LiveData<Bitmap>
        get() = _wallpaper

    fun setWallpaper(value: Bitmap) {
        _wallpaper.value = value
    }

    fun setFloatingActionButtonVisibility(visibility: Int) {
        _floatingActionButtonVisibility.value = visibility
    }

    private val _memoChanged = MutableLiveData<MemoChanged>()
    val memoChanged: LiveData<MemoChanged>
        get() = _memoChanged

    private fun notifyMemoChanged(value: MemoChanged) {
        _memoChanged.value = value
    }

    init {
        postFavoriteApps(application)
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

    private fun postFavoriteApps(application: Application) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _favoriteApps.postValue(PackageNamePreferences.getPackageNames(application).map {
                    val info = packageManager.getApplicationInfo(it, 0)
                    val icon = packageManager.getApplicationIcon(info)
                    val label = packageManager.getApplicationLabel(info).toString()

                    App(icon, label, it)
                })
            }
        }
    }

    fun refreshCalendarDisplays() {
        val value = calendarDisplays.value ?: return
        _calendarDisplays.value = value
    }

    fun addFavoriteApp(app: App, onComplete: (app: App) -> Unit) {
        _favoriteApps.value?.let {
            if (it.contains(app).not()) {
                _favoriteApps.value = it.toMutableList().apply {
                    PackageNamePreferences.addPackageName(getApplication(), app.packageName)
                    add(app)
                    onComplete.invoke(app)
                }
            }
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