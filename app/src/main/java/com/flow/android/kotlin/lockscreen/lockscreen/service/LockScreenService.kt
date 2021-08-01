package com.flow.android.kotlin.lockscreen.lockscreen.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flow.android.kotlin.lockscreen.lockscreen.blindscreen.BlindScreenPresenter
import com.flow.android.kotlin.lockscreen.main.notification.ManageOverlayPermissionNotificationBuilder
import com.flow.android.kotlin.lockscreen.main.view.MainActivity
import com.flow.android.kotlin.lockscreen.permission.PermissionChecker
import com.flow.android.kotlin.lockscreen.preference.persistence.Preference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LockScreenService : Service() {
    object Action {
        private const val Prefix = "com.flow.android.kotlin.lockscreen.lockscreen" +
                ".LockScreenService.Action"
        const val HomeKeyPressed = "$Prefix.HomePressed"
        const val MainActivityDestroyed = "$Prefix.MainActivityDestroyed"
        const val RecentAppsPressed = "$Prefix.RecentAppsPressed"
        const val StopSelf = "$Prefix.StopSelf"
    }

    private val blindScreenPresenter = BlindScreenPresenter()
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var disposable: Disposable? = null

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val showOnLockScreen = Preference.LockScreen.getShowOnLockScreen(context)
            val displayAfterUnlocking = Preference.LockScreen.getShowAfterUnlocking(context)

            when (intent.action) {
                Action.MainActivityDestroyed -> {
                    if (PermissionChecker.hasManageOverlayPermission().not()) {
                        ManageOverlayPermissionNotificationBuilder.create(context).build().run {
                            notificationManager.notify(ManageOverlayPermissionNotificationBuilder.ID, this)
                        }
                    }
                }
                Action.StopSelf -> {
                    stopSelf()
                    notificationManager.cancel(NotificationBuilder.ID)
                }
                Intent.ACTION_SCREEN_OFF -> {
                    if (showOnLockScreen.not() || displayAfterUnlocking)
                        return

                    Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(this)
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    if (showOnLockScreen.not() || displayAfterUnlocking.not())
                        return

                    Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(this)
                    }
                }
                else -> {
                    // pass
                }
            }
        }
    }

    private val bottomNavigationBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                Action.HomeKeyPressed, Action.RecentAppsPressed -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(context))
                            blindScreenPresenter.show()
                    } else {
                        blindScreenPresenter.show()
                    }
                }
                else -> {
                    // pass
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        localBroadcastManager.registerReceiver(bottomNavigationBroadcastReceiver, IntentFilter().apply {
            addAction(Action.HomeKeyPressed)
            addAction(Action.RecentAppsPressed)
        })

        registerReceiver(
                broadcastReceiver,
                IntentFilter().apply {
                    addAction(Action.MainActivityDestroyed)
                    addAction(Action.StopSelf)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_USER_PRESENT)
                }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        disposable = NotificationBuilder.single(this, notificationManager)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    startForeground(NotificationBuilder.ID, it.build())
                }) {
                    Timber.e(it)
                }

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            blindScreenPresenter.hide()
            localBroadcastManager.unregisterReceiver(bottomNavigationBroadcastReceiver)
            unregisterReceiver(broadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        } finally {
            disposable?.dispose()
        }

        blindScreenPresenter.isBlindScreenVisible = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}