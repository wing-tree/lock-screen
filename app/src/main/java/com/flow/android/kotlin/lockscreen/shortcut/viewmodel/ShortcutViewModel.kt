package com.flow.android.kotlin.lockscreen.shortcut.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.base.DataChanged
import com.flow.android.kotlin.lockscreen.base.DataChangedState
import com.flow.android.kotlin.lockscreen.persistence.entity.Shortcut
import com.flow.android.kotlin.lockscreen.repository.ShortcutRepository
import com.flow.android.kotlin.lockscreen.shortcut.model.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ShortcutViewModel(application: Application): AndroidViewModel(application) {
    private val packageManager = application.packageManager
    private val repository = ShortcutRepository(application)

    suspend fun getAll() = repository.getAll().mapNotNull { it.toModel() }

    private val _dataChanged = MutableLiveData<DataChanged<Model.Shortcut>>()
    val dataChanged: LiveData<DataChanged<Model.Shortcut>>
        get() = _dataChanged

    private fun Shortcut.toModel(): Model.Shortcut? {
        return try {
            val packageName = this.packageName
            val info = packageManager.getApplicationInfo(packageName, 0)
            val icon = packageManager.getApplicationIcon(info)
            val label = packageManager.getApplicationLabel(info).toString()

            Model.Shortcut(icon, label, packageName, priority, showInNotification)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            delete(this)
            null
        }
    }

    fun insert(item: Shortcut, onComplete: (Model.Shortcut) -> Unit) {
        repository.insert(item) {
            item.toModel()?.let {
                model -> onComplete(model)
                _dataChanged.value = DataChanged(model, DataChangedState.Inserted)
            }
        }
    }

    fun delete(item: Shortcut) {
        repository.delete(item) {
            it.toModel()?.let { model ->
                _dataChanged.value = DataChanged(model, DataChangedState.Deleted)
            }
        }
    }

    fun updateAll(list: List<Model.Shortcut>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAll(list.map { it.toEntity() })
        }
    }
}