package com.flow.android.kotlin.lockscreen.notification.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.lockscreen.base.BaseMainFragment
import com.flow.android.kotlin.lockscreen.databinding.FragmentNotificationBinding
import com.flow.android.kotlin.lockscreen.notification.adapter.NotificationAdapter
import com.flow.android.kotlin.lockscreen.notification.broadcastreceiver.NotificationBroadcastReceiver
import com.flow.android.kotlin.lockscreen.notification.service.NotificationListener
import com.flow.android.kotlin.lockscreen.notification.viewmodel.NotificationViewModel
import io.reactivex.disposables.Disposable
import timber.log.Timber

class NotificationFragment: BaseMainFragment<FragmentNotificationBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentNotificationBinding {
        return FragmentNotificationBinding.inflate(LayoutInflater.from(requireContext()), container, false)
    }

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(requireContext()) }
    private val viewModel by viewModels<NotificationViewModel>()

    private var disposable: Disposable? = null

    private val notificationAdapter by lazy { NotificationAdapter(requireActivity()) }
    private val notificationBroadcastReceiver = NotificationBroadcastReceiver()
    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            when(intent.action) {
                NotificationListener.Action.NOTIFICATION_POSTED -> {
                    val sbn = intent.getParcelableExtra<StatusBarNotification>(
                            NotificationListener.Extra.NOTIFICATION_POSTED
                    ) ?: return

                    viewModel.addNotification(sbn)
                }
                NotificationListener.Action.NOTIFICATION_REMOVED -> {
                    val sbn = intent.getParcelableExtra<StatusBarNotification>(
                            NotificationListener.Extra.NOTIFICATION_REMOVED
                    ) ?: return

                    viewModel.removeNotification(sbn)
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

        requireContext().registerReceiver(notificationBroadcastReceiver, IntentFilter().apply {
            addAction(NotificationListener.Action.NOTIFICATION_POSTED)
            addAction(NotificationListener.Action.NOTIFICATION_REMOVED)
        })

        localBroadcastManager.registerReceiver(localBroadcastReceiver, IntentFilter().apply {
            addAction(NotificationListener.Action.NOTIFICATION_POSTED)
            addAction(NotificationListener.Action.NOTIFICATION_REMOVED)
        })

        initializeView()
        registerObservers()

        return viewBinding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.bindNotificationListener()
    }

    override fun onStop() {
        viewModel.unbindNotificationListener()
        super.onStop()
    }

    private fun initializeView() {
        viewBinding.recyclerView.apply {
            adapter = notificationAdapter.apply { setHasStableIds(true) }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
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
            requireContext().unregisterReceiver(notificationBroadcastReceiver)
            localBroadcastManager.unregisterReceiver(localBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        } finally {
            disposable?.dispose()
        }

        super.onDestroyView()
    }
}