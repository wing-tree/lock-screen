package com.flow.android.kotlin.lockscreen.notification.viewmodel

import android.app.Application
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.service.notification.StatusBarNotification
import androidx.lifecycle.*
import com.flow.android.kotlin.lockscreen.application.ApplicationUtil
import com.flow.android.kotlin.lockscreen.application.MainApplication
import com.flow.android.kotlin.lockscreen.notification.model.NotificationModel
import com.flow.android.kotlin.lockscreen.notification.service.NotificationListener
import com.flow.android.kotlin.lockscreen.util.BLANK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>>
        get() = _notifications

    private val packageManager = application.packageManager

    private val notificationListenerConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is NotificationListener.NotificationListenerBinder) {
                viewModelScope.launch(Dispatchers.IO) {
                    val notificationListener = service.getNotificationListener()
                    val activeNotifications = notificationListener.activeNotifications?.mapNotNull {
                        it?.let { sbn ->
                            val notification = sbn.notification
                            val isGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                sbn.isGroup
                            else
                                notification.group != null || notification.sortKey != null

                            NotificationModel(
                                    group = notification.group ?: BLANK,
                                    groupKey = sbn.groupKey ?: BLANK,
                                    id = System.currentTimeMillis(),
                                    isGroup = isGroup,
                                    label = ApplicationUtil.getApplicationLabel(packageManager, sbn.packageName),
                                    notification = notification,
                                    packageName = sbn.packageName,
                                    postTime = sbn.postTime,
                                    template = notification.extras.getCharSequence(Notification.EXTRA_TEMPLATE).toString(),
                                    children = arrayListOf()
                            )
                        }
                    } ?: return@launch

                    val notifications = mutableListOf<NotificationModel>()

                    notifications.addAll(activeNotifications.filter { it.isGroup.not() })

                    activeNotifications.filter { it.isGroup }.groupBy { it.groupKey }.forEach {
                        val list = it.value

                        if (list.isNotEmpty()) {
                            for (i in 1 until list.size)
                                list[0].children.add(list[i])

                            notifications.add(list[0])
                        }
                    }

                    withContext(Dispatchers.Main) {
                        setNotifications(notifications)
                    }
                }
            } else {
                // call event for error. todo.
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // todo show error.
        }
    }

    fun setNotifications(notifications: List<NotificationModel>) {
        with(notifications.filter {
            val template = it.notification.extras.getCharSequence(Notification.EXTRA_TEMPLATE) ?: BLANK
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                if (template.endsWith(Notification.DecoratedCustomViewStyle::class.java.name) ||
                        template.endsWith(Notification.DecoratedMediaCustomViewStyle::class.java.name))
                    return@filter false
            }

            if (it.notification.category == Notification.CATEGORY_SYSTEM)
                return@filter false

            true
        }) {
            _notifications.value = this
        }
    }

    fun addNotification(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val isGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            sbn.isGroup
        else
            notification.group != null || notification.sortKey != null

        val model = NotificationModel(
                group = notification.group ?: BLANK,
                groupKey = sbn.groupKey ?: BLANK,
                id = System.currentTimeMillis(),
                isGroup = isGroup,
                label = ApplicationUtil.getApplicationLabel(getApplication<MainApplication>().packageManager, sbn.packageName),
                notification = notification,
                packageName = sbn.packageName,
                postTime = sbn.postTime,
                template = notification.extras.getCharSequence(Notification.EXTRA_TEMPLATE).toString(),
                children = arrayListOf()
        )

        val value = notifications.value ?: listOf()
        val notifications = value.toMutableList()

        if (model.isGroup) {
            if (model.group.isBlank() || model.groupKey.isBlank()) {
                val group = notifications.find {
                    it.group == model.group && it.groupKey == model.groupKey
                }

                val index = notifications.indexOf(group)

                group?.let {
                    it.children.add(model)

                    notifications[index] = it
                    _notifications.value = notifications
                } ?: run {
                    notifications.add(model)
                    _notifications.value = notifications
                }
            } else {
                notifications.add(model)
                _notifications.value = notifications
            }
        } else {
            notifications.add(model)
            _notifications.value = notifications
        }
    }

    fun removeNotification(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val isGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            sbn.isGroup
        else
            notification.group != null || notification.sortKey != null

        val model = NotificationModel(
                group = notification.group ?: BLANK,
                groupKey = sbn.groupKey ?: BLANK,
                id = System.currentTimeMillis(),
                isGroup = isGroup,
                label = ApplicationUtil.getApplicationLabel(packageManager, sbn.packageName),
                notification = notification,
                packageName = sbn.packageName,
                postTime = sbn.postTime,
                template = notification.extras.getCharSequence(Notification.EXTRA_TEMPLATE).toString(),
                children = arrayListOf()
        )

        val value = notifications.value ?: listOf()
        val notifications = value.toMutableList()

        if (model.isGroup) {
            if (model.group.isBlank() || model.groupKey.isBlank()) {
                val group = notifications.find {
                    it.group == model.group && it.groupKey == model.groupKey
                }

                val index = notifications.indexOf(group)

                group?.let {
                    it.children.remove(model)

                    notifications[index] = it
                    _notifications.value = notifications
                } ?: run {
                    notifications.remove(model)
                    _notifications.value = notifications
                }
            } else {
                notifications.remove(model)
                _notifications.value = notifications
            }
        } else {
            notifications.remove(model)
            _notifications.value = notifications
        }
    }

    fun bindNotificationListener() {
        getApplication<MainApplication>().bindService(Intent(getApplication(), NotificationListener::class.java), notificationListenerConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindNotificationListener() {
        getApplication<MainApplication>().unbindService(notificationListenerConnection)
    }
}