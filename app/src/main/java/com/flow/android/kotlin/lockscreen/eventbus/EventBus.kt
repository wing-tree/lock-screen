package com.flow.android.kotlin.lockscreen.eventbus

import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class EventBus {
    private val publishSubject = PublishSubject.create<Any>()

    fun publish(value: Any) {
        publishSubject.onNext(value)
    }

    fun subscribe(onNext: (Any) -> Unit, onError: (Throwable) -> Unit): Disposable =
        publishSubject.subscribe({
            onNext(it)
        }, {
            onError(it)
        })

    companion object {
        private var instance: EventBus? = null

        fun getInstance(): EventBus {
            return instance ?: EventBus().apply {
                instance = this
            }
        }
    }
}