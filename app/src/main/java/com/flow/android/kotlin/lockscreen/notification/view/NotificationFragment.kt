package com.flow.android.kotlin.lockscreen.notification.view

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentNotificationBinding
import com.flow.android.kotlin.lockscreen.notification.adapter.NotificationAdapter
import com.flow.android.kotlin.lockscreen.notification.broadcastreceiver.NotificationBroadcastReceiver
import com.flow.android.kotlin.lockscreen.notification.service.NotificationListener
import com.flow.android.kotlin.lockscreen.util.LinearLayoutManagerWrapper
import io.reactivex.disposables.Disposable
import timber.log.Timber

class NotificationFragment: BaseMainFragment<FragmentNotificationBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentNotificationBinding {
        return FragmentNotificationBinding.inflate(LayoutInflater.from(requireContext()), container, false)
    }

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(requireContext()) }

    private var disposable: Disposable? = null

    private val notificationAdapter by lazy { NotificationAdapter(requireContext().applicationContext) }
    private val nl = NotificationBroadcastReceiver()
    private val notificationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            when(intent.action) {
                NotificationListener.Action.NOTIFICATION_POSTED -> {
                    val activeNotifications = intent.getParcelableArrayListExtra<Notification>(
                            NotificationListener.Extra.ACTIVE_NOTIFICATIONS
                    ) ?: return

                    //notifications.value = activeNotifications
                }
                NotificationListener.Action.NOTIFICATION_REMOVED -> {
                    val notification = intent.getParcelableExtra<Notification>(NotificationListener.Extra.NOTIFICATION_REMOVED)

                    //val value = notifications.value?.toMutableList() ?: return

                    //value.remove(notification)
                    //notifications.value = value
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireContext().registerReceiver(nl, IntentFilter().apply {
            addAction(NotificationListener.Action.NOTIFICATION_POSTED)
            addAction(NotificationListener.Action.NOTIFICATION_REMOVED)
        })

        localBroadcastManager.registerReceiver(notificationBroadcastReceiver, IntentFilter().apply {
            addAction(NotificationListener.Action.NOTIFICATION_POSTED)
            addAction(NotificationListener.Action.NOTIFICATION_REMOVED)
        })

        initializeViews()
        registerObservers()

        return viewBinding.root
    }

    private fun initializeViews() {
        viewBinding.recyclerView.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManagerWrapper(requireContext())
        }
    }

    private fun registerObservers() {
        viewModel.notifications.observe(viewLifecycleOwner, {
            if (it.isNullOrEmpty())
                return@observe

            notificationAdapter.submitList(it)
        })
    }

    override fun onDestroyView() {
        try {
            requireContext().unregisterReceiver(nl)
            localBroadcastManager.unregisterReceiver(notificationBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        } finally {
            disposable?.dispose()
        }

        super.onDestroyView()
    }
}