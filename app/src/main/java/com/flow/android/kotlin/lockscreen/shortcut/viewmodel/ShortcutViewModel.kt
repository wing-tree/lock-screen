package com.flow.android.kotlin.lockscreen.shortcut.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
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

    val shortcutValues: List<Shortcut>
        get() = repository.getAllValue()

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

    fun insert(item: Shortcut, onInserted: (Model.Shortcut) -> Unit) {
        repository.insert(item) {
            item.toModel()?.let { model -> onInserted(model) }
        }
    }

    fun delete(item: Shortcut, onDeleted: ((Shortcut) -> Unit)? = null) {
        repository.delete(item) {
            onDeleted?.invoke(item)
        }
    }

    fun updateAll(list: List<Model.Shortcut>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAll(list.map { it.toEntity() })
        }
    }
}