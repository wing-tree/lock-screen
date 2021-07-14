package com.flow.android.kotlin.lockscreen.main.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.preference.viewmodel.ConfigurationChange
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _viewPagerRegionColor = Color.WHITE
    val viewPagerRegionColor: Int
        @ColorInt
        get() = _viewPagerRegionColor

    private val _refreshEvent = SingleLiveEvent<Refresh>()
    val refresh: LiveData<Refresh>
        get() = _refreshEvent

    fun callRefresh(refresh: Refresh) {
        _refreshEvent.value = refresh
    }

    fun refresh(configurationChange: ConfigurationChange) {
        if (configurationChange.calendarChanged)
            callRefresh(Refresh.Calendar)

        if (configurationChange.fondSizeChanged)
            callRefresh(Refresh.Memo)
    }
}

enum class Refresh {
    Calendar, Memo, Shortcut
}