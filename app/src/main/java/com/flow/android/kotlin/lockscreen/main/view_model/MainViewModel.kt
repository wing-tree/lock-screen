package com.flow.android.kotlin.lockscreen.main.view_model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flow.android.kotlin.lockscreen.calendar.CalendarDisplay
import com.flow.android.kotlin.lockscreen.calendar.CalendarHelper
import com.flow.android.kotlin.lockscreen.calendar.Event
import com.flow.android.kotlin.lockscreen.favoriteapp.adapter.App
import com.flow.android.kotlin.lockscreen.preferences.PackageNamePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel(private val application: Application): ViewModel() {
    private val contentResolver = application.contentResolver
    private val packageManager = application.packageManager

    private val _calendarDisplays = MutableLiveData<List<CalendarDisplay>>()
    val calendarDisplays: LiveData<List<CalendarDisplay>>
        get() = _calendarDisplays

    private val _favoriteApps = MutableLiveData<List<App>>()
    val favoriteApps: LiveData<List<App>>
        get() = _favoriteApps

    fun calendarDisplays() = calendarDisplays.value
    fun contentResolver() = contentResolver

    private val _events = MutableLiveData<List<Event>>()

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
                    PackageNamePreferences.addPackageName(application, app.packageName)
                    add(app)
                    onComplete.invoke(app)
                }
            }
        }
    }
}