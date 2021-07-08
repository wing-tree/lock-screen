package com.flow.android.kotlin.lockscreen.base

data class DataChanged<T>(
        val data: T,
        val state: DataChangedState
)

enum class DataChangedState {
    Deleted, Inserted, Updated
}