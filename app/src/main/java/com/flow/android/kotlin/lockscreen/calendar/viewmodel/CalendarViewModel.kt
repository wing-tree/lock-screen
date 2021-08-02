package com.flow.android.kotlin.lockscreen.calendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.calendar.CalendarLoader
import com.flow.android.kotlin.lockscreen.calendar.model.Model
import com.flow.android.kotlin.lockscreen.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val contentResolver = getApplication<MainApplication>().contentResolver

    private val _calendars = MutableLiveData<List<Model.Calendar>>()
    val calendars: LiveData<List<Model.Calendar>>
        get() = _calendars

    fun postValue() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _calendars.postValue(CalendarLoader.calendars(contentResolver))
            }
        }
    }
}