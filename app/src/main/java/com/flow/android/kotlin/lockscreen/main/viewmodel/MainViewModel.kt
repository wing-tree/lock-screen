package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference.isChanged
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _viewPagerRegionColor = Color.WHITE
    val viewPagerRegionColor: Int
        @ColorInt
        get() = _viewPagerRegionColor

    private val _refreshEvent = SingleLiveEvent<Refresh>()
    val refresh: LiveData<Refresh>
        get() = _refreshEvent

    private fun callRefresh(refresh: Refresh) {
        _refreshEvent.value = refresh
    }

    fun refresh(preferenceChanged: Preference.PreferenceChanged) {
        if (isChanged(preferenceChanged.fondSize, preferenceChanged.timeFormat))
            callRefresh(Refresh.Memo)

        if (isChanged(preferenceChanged.fondSize, preferenceChanged.uncheckedCalendarIds))
            callRefresh(Refresh.Calendar)
    }
}

enum class Refresh {
    Calendar, Memo, Shortcut
}