package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.eventbus.EventBus
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference.isChanged
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent

class MainViewModel(application: Application) : AndroidViewModel(application) {
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
            EventBus.getInstance().publish(Refresh.Note)

        if (isChanged(preferenceChanged.fondSize, preferenceChanged.uncheckedCalendarIds))
            EventBus.getInstance().publish(Refresh.Calendar)
    }
}

enum class Refresh {
    Note, Calendar
}