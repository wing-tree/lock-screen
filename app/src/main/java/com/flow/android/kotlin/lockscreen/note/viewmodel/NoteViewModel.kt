package com.flow.android.kotlin.lockscreen.note.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flow.android.kotlin.lockscreen.persistence.entity.Note
import com.flow.android.kotlin.lockscreen.repository.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MemoRepository(application)

    override fun onCleared() {
        repository.clearCompositeDisposable()
        super.onCleared()
    }

    suspend fun getAll() = repository.getAll()

    fun delete(note: Note, onComplete: (() -> Unit)?) {
        repository.delete(note) { onComplete?.invoke() }
    }

    fun insert(note: Note, onComplete: (() -> Unit)?) {
        repository.insert(note) { onComplete?.invoke() }
    }

    fun update(note: Note, onComplete: (() -> Unit)? = null) {
        repository.update(note) { onComplete?.invoke() }
    }

    fun updateAll(list: List<Note>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAll(list)
        }
    }
}