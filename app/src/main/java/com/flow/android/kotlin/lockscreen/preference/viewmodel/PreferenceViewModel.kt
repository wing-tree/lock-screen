package com.flow.android.kotlin.lockscreen.preference.viewmodel

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import kotlinx.android.parcel.Parcelize

class PreferenceViewModel(application: Application): AndroidViewModel(application) {
    val configurationChanged = ConfigurationChange()

    var calendarChanged: Boolean
        get() = configurationChanged.calendarChanged
        set(value) { configurationChanged.calendarChanged = value }

    var fondSizeChanged: Boolean
        get() = configurationChanged.fondSizeChanged
        set(value) { configurationChanged.fondSizeChanged = value }
}

@Parcelize
data class ConfigurationChange(
        var calendarChanged: Boolean = false,
        var fondSizeChanged: Boolean = false
) : Parcelable