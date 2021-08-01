package com.flow.android.kotlin.lockscreen.appshortcut.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.persistence.entity.AppShortcut
import com.flow.android.kotlin.lockscreen.repository.ShortcutRepository
import com.flow.android.kotlin.lockscreen.appshortcut.model.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class AppShortcutViewModel(application: Application): AndroidViewModel(application) {
    private val packageManager = getApplication<MainApplication>().packageManager
    private val repository = ShortcutRepository(getApplication<MainApplication>())

    private fun disposeComposeDisposable() {
        repository.disposeComposeDisposable()
    }

    override fun onCleared() {
        disposeComposeDisposable()
        super.onCleared()
    }

    suspend fun getAll() = repository.getAll().mapNotNull { it.toModel() }

    private fun AppShortcut.toModel(): Model.AppShortcut? {
        return try {
            val packageName = this.packageName
            val info = packageManager.getApplicationInfo(packageName, 0)
            val icon = packageManager.getApplicationIcon(info)
            val label = packageManager.getApplicationLabel(info).toString()

            Model.AppShortcut(icon, label, packageName, priority, showInNotification)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            delete(this)
            null
        }
    }

    fun insert(item: AppShortcut, onComplete: (Model.AppShortcut) -> Unit) {
        repository.insert(item) {
            item.toModel()?.let { model ->
                onComplete(model)
            }
        }
    }

    fun delete(item: AppShortcut, onComplete: ((Model.AppShortcut) -> Unit)? = null) {
        repository.delete(item) {
            it.toModel()?.let { model ->
                onComplete?.invoke(model)
            }
        }
    }

    fun updateAll(list: List<Model.AppShortcut>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAll(list.map { it.toEntity() })
        }
    }
}