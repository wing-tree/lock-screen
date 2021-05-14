package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.calendar.CalendarDisplay
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.color.ColorDependingOnBackground
import com.flow.android.kotlin.lockscreen.shortcut.entity.App
import com.flow.android.kotlin.lockscreen.memo.entity.Memo
import com.flow.android.kotlin.lockscreen.preferences.PackageNamePreferences
import com.flow.android.kotlin.lockscreen.repository.Repository
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contentResolver = application.contentResolver
    private val repository = Repository(application)
    private val packageManager = application.packageManager

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

    private val _favoriteApps = MutableLiveData<List<App>>()
    val favoriteApps: LiveData<List<App>>
        get() = _favoriteApps

    val memos = repository.getAllMemos()

    fun calendarDisplays() = calendarDisplays.value
    fun contentResolver(): ContentResolver = contentResolver

    private val _events = MutableLiveData<List<Event>>()

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
                val apps = arrayListOf<App>()

                for (packageName in PackageNamePreferences.getPackageNames(application)) {
                    try {
                        val info = packageManager.getApplicationInfo(packageName, 0)
                        val icon = packageManager.getApplicationIcon(info)
                        val label = packageManager.getApplicationLabel(info).toString()

                        apps.add(App(icon, label, packageName))
                    } catch (e: PackageManager.NameNotFoundException) {
                        Timber.e(e)
                        PackageNamePreferences.removePackageName(application, packageName)
                    }
                }

                _favoriteApps.postValue(apps)
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