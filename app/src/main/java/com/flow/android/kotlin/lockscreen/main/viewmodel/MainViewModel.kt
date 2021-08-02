package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val contentResolver: ContentResolver = application.contentResolver

    private var _postCalendars = SingleLiveEvent<Unit>()
    val postCalendars: LiveData<Unit>
        get() = _postCalendars

    private var _refresh = MutableLiveData<Preference.PreferenceChanged>()
    val refresh: LiveData<Preference.PreferenceChanged>
        get() = _refresh

    private val _showRequestCalendarPermissionSnackbar = SingleLiveEvent<Unit>()
    val showRequestCalendarPermissionSnackbar: LiveData<Unit>
        get() = _showRequestCalendarPermissionSnackbar

    private var _viewPagerRegionColor = Color.WHITE
    val viewPagerRegionColor: Int
        @ColorInt
        get() = _viewPagerRegionColor

    fun callPostCalendars() {
        _postCalendars.call()
    }

    fun callShowRequestCalendarPermissionSnackbar() {
        _showRequestCalendarPermissionSnackbar.call()
    }

    fun refresh(preferenceChanged: Preference.PreferenceChanged) {
        _refresh.value = preferenceChanged
    }
}